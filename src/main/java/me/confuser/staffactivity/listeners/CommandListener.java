package me.confuser.staffactivity.listeners;

import me.confuser.bukkitutil.listeners.Listeners;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.data.StaffPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener extends Listeners<StaffActivity> {


  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCommand(PlayerCommandPreprocessEvent event) {
    StaffPlayer staff = plugin.getStaffManager().getPlayer(event.getPlayer());

    if (staff == null) return;

    // Ignore afk commands
    if (event.getMessage().toLowerCase().split(" ")[0].replace("/", "").equals("afk")) return;

    staff.getSession().incrementStat("commands");
  }
}
