package me.confuser.staffactivity.listeners;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.configs.InactiveConfig;
import me.confuser.staffactivity.data.InactiveReport;
import me.confuser.staffactivity.data.InactiveResult;
import me.confuser.staffactivity.data.PlayerActivitySession;
import me.confuser.staffactivity.data.StaffPlayer;
import me.confuser.staffactivity.storage.PlayerData;
import me.confuser.staffactivity.utils.UUIDUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class JoinListener extends Listeners<StaffActivity> {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("staffactivity.log")) return;

    final String group = plugin.getChat().getPrimaryGroup(event.getPlayer());

    if (group == null) return;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerData playerData;
        try {
          playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(event.getPlayer().getUniqueId()));

          if (playerData == null) {
            playerData = new PlayerData(event.getPlayer(), group);
            plugin.getPlayerStorage().create(playerData);
          } else {

            boolean shouldUpdate = false;

            if (!playerData.getName().equals(event.getPlayer().getName())) {
              // Name changed
              shouldUpdate = true;
              playerData.setName(event.getPlayer().getName());
            }

            if (!playerData.getGroupName().equals(group)) {
              // Group changed
              shouldUpdate = true;
              playerData.setGroupName(group);
            }

            if (shouldUpdate) {
              plugin.getPlayerStorage().update(playerData);
            }
          }

          StaffPlayer player = new StaffPlayer(playerData, new PlayerActivitySession());

          plugin.getStaffManager().addPlayer(playerData.getUUID(), player);
          final PlayerData data = playerData;
          plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

            @Override
            public void run() {
              plugin.getSignManager().setOnline(data);
            }
          });

        } catch (SQLException e) {
          e.printStackTrace();
          return;
        }

        InactiveConfig inactiveConfig = plugin.getConfiguration().getInactiveGroupConfig(group);

        if (inactiveConfig == null || !inactiveConfig.isEnabled()) return;

        if (inactiveConfig.isNotifySelf()) {
          InactiveResult result;
          try {
            result = plugin.getPlayerSessionStorage().getInactivityResult(inactiveConfig, playerData);
          } catch (SQLException e) {
            e.printStackTrace();
            return;
          }

          if (result == null) return;

          if (inactiveConfig.isAutoDemote()) {
            Message.get("player.error.demoted").sendTo(event.getPlayer());

            final PlayerData finalPlayerData = playerData;
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

              @Override
              public void run() {
                plugin.getStaffManager()
                      .demote(finalPlayerData, plugin.getConfiguration().getDemoteCommands(), plugin.getConfiguration()
                                                                                                    .isRemoveActivityOnDemote());
              }
            });

          } else {
            Message.get("player.error.inactive").sendTo(event.getPlayer());
          }
        }

      }
    });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onOwnerJoin(final PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("staffactivity.notify.inactive")) return;

    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        for (InactiveConfig group : plugin.getConfiguration().getInactiveGroupConfigs()) {
          if (!event.getPlayer().hasPermission("staffactivity.notify.inactive." + group.getName().replace(" ", ""))) {
            continue;
          }
          InactiveReport report = null;
          try {
            report = plugin.getPlayerSessionStorage().getInactivityReport(group);
          } catch (SQLException e) {
            e.printStackTrace();
            continue;
          }

          if (!report.hasResults()) continue;

          report.display(event.getPlayer(), 1);
        }
      }
    }, 20L);
  }
}
