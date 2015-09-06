package me.confuser.staffactivity.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

@DatabaseTable(tableName = "sa_activities", daoClass = PlayerActivityStorage.class)
public class PlayerActivityData {

  @DatabaseField(generatedId = true, columnDefinition = "INT(255) UNSIGNED NOT NULL AUTO_INCREMENT")
  @Getter
  private int id;

  @DatabaseField(index = true, foreign = true, foreignAutoRefresh = true, columnDefinition = "INT (255) UNSIGNED NOT NULL")
  private PlayerSessionData session;

  @DatabaseField(index = true, columnDefinition = "VARCHAR(20) NOT NULL")
  @Getter
  private String stat;

  @DatabaseField(index = true, columnDefinition = "INT(255) UNSIGNED NOT NULL")
  @Getter
  private int value;

  PlayerActivityData() {
  }

  public PlayerActivityData(PlayerSessionData session, String stat, int value) {
    this.session = session;
    this.stat = stat;
    this.value = value;
  }
}
