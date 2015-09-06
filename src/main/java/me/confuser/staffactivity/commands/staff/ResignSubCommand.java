package me.confuser.staffactivity.commands.staff;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.PlayerSubCommand;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.storage.PlayerData;
import me.confuser.staffactivity.utils.UUIDUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ResignSubCommand extends PlayerSubCommand<StaffActivity> {

  public ResignSubCommand() {
    super("resign");
  }

  @Override
  public boolean onPlayerCommand(final Player sender, String[] args) {
    if (args.length != 0) return false;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player;
        try {
          player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(sender.getUniqueId()));
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (player == null) {
          Message.get("sender.error.notFound").set("player", sender.getName()).sendTo(sender);
          return;
        }

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            plugin.getStaffManager()
                  .demote(player, plugin.getConfiguration().getResignCommands(), plugin.getConfiguration()
                                                                                       .isRemoveActivityOnResign());
            Message.get("resign.success").sendTo(sender);
          }
        });
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<player>";
  }

  @Override
  public String getPermission() {
    return "command.resign";
  }
}
