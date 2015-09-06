package me.confuser.staffactivity.data;

import com.google.common.collect.HashMultiset;
import lombok.Getter;
import lombok.Setter;

public class PlayerActivitySession {

  // Unix time stamp of when they joined
  @Getter
  private final long joined = System.currentTimeMillis() / 1000L;
  @Getter
  @Setter
  private long left;
  // Time in seconds they've been inactive for
  @Getter
  private long afkTime = 0;
  @Getter
  private long afkTimeStarted;
  @Getter
  private boolean isAfk = false;
  @Getter
  private HashMultiset<String> stats = HashMultiset.create();

  public void incrementStat(String stat) {
    stats.add(stat);
  }

  public int getStatCount(String stat) {
    return stats.count(stat);
  }

  public void setAfk(boolean setAfk) {
    if (setAfk && afkTimeStarted == 0) {
      afkTimeStarted = System.currentTimeMillis() / 1000L;
      isAfk = true;
    } else if (!setAfk && afkTimeStarted != 0) {
      afkTime += (System.currentTimeMillis() / 1000L) - afkTimeStarted;
      afkTimeStarted = 0;
      isAfk = false;
    }
  }
}
