package me.confuser.staffactivity.commands.staff;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.storage.PlayerData;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class DemoteSubCommand extends SubCommand<StaffActivity> {

  public DemoteSubCommand() {
    super("demote");
  }

  @Override
  public boolean onCommand(final CommandSender sender, String[] args) {
    if (args.length != 1) return false;

    final String playerName = args[0];

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player;
        try {
          player = plugin.getPlayerStorage().getByName(playerName);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (player == null) {
          Message.get("sender.error.notFound").set("player", playerName).sendTo(sender);
          return;
        }

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            plugin.getStaffManager()
                  .demote(player, plugin.getConfiguration().getDemoteCommands(), plugin.getConfiguration()
                                                                                       .isRemoveActivityOnDemote());
            Message.get("demote.success").set("player", player.getName()).sendTo(sender);
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
    return "command.demote";
  }
}
