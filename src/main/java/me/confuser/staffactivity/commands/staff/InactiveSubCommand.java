package me.confuser.staffactivity.commands.staff;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.configs.InactiveConfig;
import me.confuser.staffactivity.data.InactiveReport;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;

import java.util.concurrent.ConcurrentHashMap;

public class InactiveSubCommand extends SubCommand<StaffActivity> {

  private ConcurrentHashMap<String, InactiveReport> cache = new ConcurrentHashMap();

  public InactiveSubCommand() {
    super("inactive");
  }

  @Override
  public boolean onCommand(final CommandSender sender, String[] args) {
    if (args.length != 1) return false;

    if (NumberUtils.isNumber(args[0])) {
      int pageNumber = Integer.parseInt(args[0]);

      InactiveReport report = cache.get(sender.getName());

      if (report == null) {
        Message.get("inactive.error.noReport").sendTo(sender);
      } else {
        report.display(sender, pageNumber);
      }

      return true;
    }

    String groupName = args[0];

    final InactiveConfig groupConfig = plugin.getConfiguration().getInactiveGroupConfig(groupName);

    if (groupConfig == null || !groupConfig.isEnabled()) {
      Message.get("inactive.error.invalidGroup").set("group", groupName).sendTo(sender);
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        InactiveReport report = plugin.getPlayerSessionStorage().getInactivityReport(groupConfig);

        if (report == null) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          return;
        }

        cache.put(sender.getName(), report);
        report.display(sender, 1);
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<group | page>";
  }

  @Override
  public String getPermission() {
    return null;
  }
}
