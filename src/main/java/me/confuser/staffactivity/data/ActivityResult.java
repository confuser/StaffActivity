package me.confuser.staffactivity.data;

import lombok.Getter;

import java.util.HashMap;

public class ActivityResult {
  @Getter
  private long joined;
  @Getter
  private long left;
  @Getter
  private long totalTime;
  @Getter
  private HashMap<String, Long> stats;

  public ActivityResult(long joined, long left, long totalTime, HashMap<String, Long> stats) {
    this.joined = joined;
    this.left = left;
    this.totalTime = totalTime;
    this.stats = stats;
  }
}
