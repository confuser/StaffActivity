package me.confuser.staffactivity.listeners.afk;

import me.confuser.bukkitutil.listeners.Listeners;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.data.StaffPlayer;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.event.EventHandler;

public class EssentialsListener extends Listeners<StaffActivity> {

  @EventHandler
  public void onAFK(AfkStatusChangeEvent event) {
    StaffPlayer staff = plugin.getStaffManager().getPlayer(event.getAffected().getBase());

    if (staff == null)
      return;

    staff.getSession().setAfk(event.getValue());
  }
}
