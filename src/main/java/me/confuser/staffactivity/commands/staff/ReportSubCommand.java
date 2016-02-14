package me.confuser.staffactivity.commands.staff;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.data.ActivityReport;
import me.confuser.staffactivity.storage.PlayerData;
import me.confuser.staffactivity.utils.DateUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ReportSubCommand extends SubCommand<StaffActivity> {

  private int defaultPageNumber = 1;
  private Parser dateParser = new Parser();
  private ConcurrentHashMap<String, ActivityReport> cache = new ConcurrentHashMap();

  public ReportSubCommand() {
    super("report");
  }

  @Override
  public boolean onCommand(final CommandSender sender, String[] args) {
    if (args.length == 0) return false;

    final int pageNumber;

    if (args.length == 1) {
      if (NumberUtils.isNumber(args[0])) {
        pageNumber = Integer.parseInt(args[0]);
      } else {
        pageNumber = defaultPageNumber;
      }

      ActivityReport report = cache.get(sender.getName());

      if (report == null) {
        Message.get("report.error.noReport").sendTo(sender);
      } else {
        report.display(sender, pageNumber);
      }

      return true;
    }

    final String playerName = args[0];
    final long from;
    final long to;

    if (args.length == 2) {
      try {
        from = DateUtils.parseDateDiff(args[1], false);
      } catch (Exception e) {
        Message.get("report.error.invalidDate").sendTo(sender);
        return true;
      }

      to = System.currentTimeMillis() / 1000L;

    } else if (args.length == 3) {
      List<DateGroup> fromDates = dateParser.parse(args[1]);

      if (fromDates.size() == 0) {
        Message.get("report.error.invalidDate").sendTo(sender);
        return true;
      }

      from = fromDates.get(0).getDates().get(0).getTime();

      List<DateGroup> toDates = dateParser.parse(args[1]);

      if (toDates.size() == 0) {
        Message.get("report.error.invalidDate").sendTo(sender);
        return true;
      }

      to = toDates.get(0).getDates().get(0).getTime();
    } else {
      return false;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerData player;

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

        ActivityReport activityReport = plugin.getPlayerSessionStorage().getReport(player, from, to);
        if (activityReport == null) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          return;
        }

        cache.put(sender.getName(), activityReport);
        activityReport.display(sender, 1);
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return " <name> <from> [to]";
  }

  @Override
  public String getPermission() {
    return "command.report";
  }
}
