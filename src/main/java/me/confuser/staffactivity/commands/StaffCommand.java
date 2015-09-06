package me.confuser.staffactivity.commands;

import me.confuser.bukkitutil.commands.MultiCommandHandler;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.commands.staff.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class StaffCommand extends MultiCommandHandler<StaffActivity> {

  private ListSubCommand listSubCommand;

  public StaffCommand() {
    super("staff");

    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerSubCommand(new DemoteSubCommand());

    listSubCommand = new ListSubCommand();
    registerSubCommand(listSubCommand);

    registerSubCommand(new InactiveSubCommand());
    registerSubCommand(new ReportSubCommand());
    registerSubCommand(new ReloadSubCommand());
    registerSubCommand(new ResignSubCommand());
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (args.length == 0) {
      return listSubCommand.onCommand(sender, args);
    }

    return super.onCommand(sender, cmd, commandLabel, args);
  }
}
