package me.confuser.staffactivity.commands.staff;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.configs.MessagesConfig;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand extends SubCommand<StaffActivity> {

  public ReloadSubCommand() {
    super("reload");
  }

  @Override
  public boolean onCommand(CommandSender sender, String[] args) {
    plugin.getConfiguration().load();
    plugin.getSignsConfig().load();
    new MessagesConfig().load();

    Message.get("reload.success").sendTo(sender);

    return true;
  }

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public String getPermission() {
    return "command.reload";
  }
}
