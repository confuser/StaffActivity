package me.confuser.staffactivity.storage;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.staffactivity.data.PlayerActivitySession;
import me.confuser.staffactivity.storage.mysql.ByteArray;

@DatabaseTable(tableName = "sa_sessions", daoClass = PlayerSessionStorage.class)
public class PlayerSessionData {

  @DatabaseField(generatedId = true, columnDefinition = "INT(255) UNSIGNED NOT NULL AUTO_INCREMENT")
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = false, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @DatabaseField(index = true, columnDefinition = "INT(10) UNSIGNED NOT NULL")
  @Getter
  private long joined;

  @DatabaseField(index = true, columnDefinition = "INT(10) UNSIGNED NOT NULL")
  @Getter
  private long left = System.currentTimeMillis() / 1000L;

  @DatabaseField(columnDefinition = "INT(10) UNSIGNED NOT NULL")
  @Getter
  private long totalTime;

  @ForeignCollectionField
  @Getter
  private ForeignCollection<PlayerActivityData> activity;

  PlayerSessionData() {
  }

  public PlayerSessionData(PlayerData player, PlayerActivitySession session) {
    this.player = player;

    joined = session.getJoined();
    left = session.getLeft();

    totalTime = left - joined - session.getAfkTime();
  }
}
