package me.confuser.staffactivity.listeners;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.storage.PlayerData;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.sql.SQLException;

public class SignListener extends Listeners<StaffActivity> {

  @EventHandler(ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    if (!event.getPlayer().hasPermission("staffactivity.sign.create")) return;

    BlockState state = event.getBlock().getState();

    if (!(state instanceof Sign)) return;

    if (!event.getLine(0).equalsIgnoreCase("[Staff]")) return;
    if (event.getLine(1) == null) return;

    String name = event.getLine(1);
    Sign sign = (Sign) state;
    PlayerData player;

    try {
      player = plugin.getPlayerStorage().getByName(name);
    } catch (SQLException e) {
      event.getPlayer().sendMessage(Message.get("sender.error.exception").toString());
      e.printStackTrace();
      return;
    }

    if (player == null) {
      Message.get("sender.error.notFound").set("player", name).sendTo(event.getPlayer());
      return;
    }

    event.setLine(1, player.getName());
    sign.update(true);

    plugin.getSignManager().addSign(player, event.getBlock());
    plugin.getSignManager().updateSigns(player, plugin.getStaffManager().isOnline(player.getUUID()));

    Message.get("sign.created").sendTo(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    if (!(event.getBlock().getState() instanceof Sign)) return;

    plugin.getSignManager().removeSign(event.getBlock());
  }
}

