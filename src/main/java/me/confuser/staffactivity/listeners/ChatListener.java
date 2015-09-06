package me.confuser.staffactivity.listeners;

import me.confuser.bukkitutil.listeners.Listeners;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.data.StaffPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener extends Listeners<StaffActivity> {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChat(AsyncPlayerChatEvent event) {
    StaffPlayer staff = plugin.getStaffManager().getPlayer(event.getPlayer());

    if (staff == null) return;

    staff.getSession().incrementStat("chat");
  }
}
