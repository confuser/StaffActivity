package me.confuser.staffactivity.managers;

import com.google.common.collect.HashMultiset;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.data.PlayerActivitySession;
import me.confuser.staffactivity.data.StaffPlayer;
import me.confuser.staffactivity.storage.PlayerActivityData;
import me.confuser.staffactivity.storage.PlayerData;
import me.confuser.staffactivity.storage.PlayerSessionData;
import me.confuser.staffactivity.utils.UUIDUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaffManager {

  private StaffActivity plugin = StaffActivity.getPlugin();
  private ConcurrentHashMap<UUID, StaffPlayer> players = new ConcurrentHashMap<>();

  public StaffManager() {
    // Add online players to allow for plugin reloads
    for (Player player : plugin.getServer().getOnlinePlayers()) {
      if (!player.hasPermission("staffactivity.log")) continue;

      final String group = plugin.getChat().getPrimaryGroup(player);

      if (group == null) continue;

      PlayerData playerData;
      try {
        playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.getUniqueId()));

        if (playerData == null) {
          playerData = new PlayerData(player, group);
          plugin.getPlayerStorage().create(playerData);
        }

        StaffPlayer staffPlayer = new StaffPlayer(playerData, new PlayerActivitySession());

        addPlayer(playerData.getUUID(), staffPlayer);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public StaffPlayer getPlayer(UUID uuid) {
    return players.get(uuid);
  }

  public StaffPlayer getPlayer(Player player) {
    return getPlayer(player.getUniqueId());
  }

  public void addPlayer(UUID uuid, StaffPlayer session) {
    players.put(uuid, session);
  }

  public StaffPlayer removePlayer(UUID uuid) {
    return players.remove(uuid);
  }

  public StaffPlayer removePlayer(Player player) {
    return removePlayer(player.getUniqueId());
  }

  public boolean isOnline(UUID uuid) {
    return players.get(uuid) != null;
  }

  public void save(StaffPlayer player) throws SQLException {
    PlayerSessionData session = new PlayerSessionData(player.getData(), player.getSession());
    plugin.getPlayerSessionStorage().create(session);

    if (player.getSession().getStats().size() == 0) return;

    HashMultiset<String> stats = player.getSession().getStats();

    for (String statName : stats.elementSet()) {
      plugin.getPlayerActivityStorage().create(new PlayerActivityData(session, statName, stats.count(statName)));
    }

  }

  public Collection<StaffPlayer> getPlayers() {
    return players.values();
  }

  public void demote(final PlayerData player, List<String> commands, boolean removeActivity) {
    removePlayer(player.getUUID());

    if (removeActivity) {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          try {
            ArrayList<Integer> sessionIds = plugin.getPlayerSessionStorage().delete(player);
            if (sessionIds.size() != 0) {
              plugin.getPlayerActivityStorage().deleteIds(sessionIds);
            }

            plugin.getPlayerStorage().delete(player);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      });
    }

    for (String command : commands) {
      String commandParsed = command.replace("[player]", player.getName())
                                    .replace("[uuid]", player.getUUID().toString());
      // TODO execute bukkit command event
      plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandParsed);
    }

  }
}
