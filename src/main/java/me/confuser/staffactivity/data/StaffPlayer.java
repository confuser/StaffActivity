package me.confuser.staffactivity.data;

import lombok.Getter;
import me.confuser.staffactivity.storage.PlayerData;

public class StaffPlayer {
  @Getter
  private PlayerData data;

  @Getter
  private PlayerActivitySession session;

  public StaffPlayer(PlayerData data, PlayerActivitySession session) {
    this.data = data;
    this.session = session;
  }
}
