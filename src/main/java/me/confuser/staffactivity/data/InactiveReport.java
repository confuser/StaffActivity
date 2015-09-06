package me.confuser.staffactivity.data;

import lombok.Getter;
import me.confuser.bukkitutil.Message;
import me.confuser.staffactivity.configs.InactiveConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.List;

public class InactiveReport extends PaginatedResult<InactiveResult> {

  @Getter
  private InactiveConfig group;
  private List<String> noData;

  public InactiveReport(InactiveConfig group, List<? extends InactiveResult> results, List<String> noData) {
    super(Message.get("reports.group.header"), Message.get("reports.group.subhead"), results);

    this.group = group;
    this.noData = noData;
  }

  @Override
  public String format(InactiveResult entry) {
    Message message = Message.get("reports.group.row")
                             .set("activity", getDuration("activity", entry.getTotalTime()))
                             .set("player", entry.getPlayerName());

    for (String stat : entry.getStats().keySet()) {
      message.set(stat, entry.getStats().get(stat));
    }

    return message.toString();
  }

  @Override
  public String formatSubhead() {
    if (noData.size() == 0) return null;

    return Message.get("reports.group.subhead").set("players", StringUtils.join(noData, ", ")).toString();
  }

  @Override
  protected Message formatHeader(Message message) {
    return message.set("group", group.getName()).set("type", WordUtils.capitalize(group.getType().toString().toLowerCase()));
  }

  @Override
  public String noResults() {
    return Message.getString("reports.group.noResults");
  }

  private String getDuration(String key, long amount) {
    return DurationFormatUtils.formatDuration(amount * 1000L, Message.getString("reports.group.durations." + key));
  }

}
