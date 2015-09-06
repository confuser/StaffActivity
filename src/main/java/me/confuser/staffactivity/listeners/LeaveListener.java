package me.confuser.staffactivity.listeners;

import me.confuser.bukkitutil.listeners.Listeners;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.data.StaffPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class LeaveListener extends Listeners<StaffActivity> {

  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    if (!event.getPlayer().hasPermission("staffactivity.log")) return;

    final StaffPlayer staffPlayer = plugin.getStaffManager().removePlayer(event.getPlayer());

    if (staffPlayer == null) return;

    plugin.getSignManager().setOffline(staffPlayer.getData());

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        try {
          staffPlayer.getSession().setLeft(System.currentTimeMillis() / 1000L);
          plugin.getStaffManager().save(staffPlayer);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
