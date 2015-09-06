package me.confuser.staffactivity.data;

import com.google.common.collect.HashMultiset;
import lombok.Getter;
import me.confuser.bukkitutil.Message;
import me.confuser.staffactivity.storage.PlayerData;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActivityReport extends PaginatedResult<ActivityResult> {

  @Getter
  private HashMultiset<String> overallStats;
  private PlayerData player;
  private long from;
  private long to;

  public ActivityReport(long from, long to, PlayerData player, HashMultiset<String> overallStats, List<? extends ActivityResult> results) {
    super(Message.get("reports.player.header"), Message.get("reports.player.subhead"), results);

    this.overallStats = overallStats;
    this.player = player;
    this.from = from;
    this.to = to;
  }

  @Override
  public String format(ActivityResult entry) {
    Message message = Message.get("reports.player.row")
                             .set("joined", getDateFormatted("joined", entry.getJoined()))
                             .set("left", getDateFormatted("left", entry.getLeft()))
                             .set("activity", getDuration("activity", entry.getTotalTime()));

    for (String stat : entry.getStats().keySet()) {
      message.set(stat, entry.getStats().get(stat));
    }

    return message.toString();
  }

  @Override
  public String formatSubhead() {
    for (String stat : overallStats.elementSet()) {
      if (stat.equals("overallActivity")) {
        subhead.set(stat, getDuration("overallActivity", overallStats.count(stat)));
      } else {
        subhead.set(stat, overallStats.count(stat));
      }
    }

    return subhead.toString();
  }

  @Override
  protected Message formatHeader(Message message) {
    return message.set("player", player.getName())
                  .set("from", getDateFormatted("from", from))
                  .set("to", getDateFormatted("to", to));
  }

  @Override
  public String noResults() {
    return Message.getString("reports.player.noResults");
  }

  private String getDuration(String key, long amount) {
    return DurationFormatUtils.formatDuration(amount * 1000L, Message.getString("reports.player.durations." + key));
  }

  private String getDateFormatted(String key, long timestamp) {
    return new SimpleDateFormat(Message.getString("reports.player.dates." + key)).format(new Date(timestamp * 1000L));
  }
}
