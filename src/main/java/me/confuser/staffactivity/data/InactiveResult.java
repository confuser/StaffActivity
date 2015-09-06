package me.confuser.staffactivity.data;

import lombok.Getter;

import java.util.HashMap;

public class InactiveResult {

  @Getter
  private String playerName;
  @Getter
  private long totalTime;
  @Getter
  private HashMap<String, Long> stats;

  public InactiveResult(String playerName, long totalTime, HashMap<String, Long> stats) {
    this.playerName = playerName;
    this.totalTime = totalTime;
    this.stats = stats;
  }
}
