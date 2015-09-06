package me.confuser.staffactivity.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import me.confuser.staffactivity.storage.mysql.ByteArray;
import me.confuser.staffactivity.utils.UUIDUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

;

@DatabaseTable(tableName = "bliv_trails", daoClass = PlayerStorage.class)
public class PlayerData {

  @DatabaseField(id = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private byte[] id;

  @DatabaseField(index = true, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  @Setter
  private String name;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long startedLogging = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, width = 64, columnDefinition = "VARCHAR(64) NOT NULL")
  @Getter
  @Setter
  private String groupName;

  private UUID uuid = null;

  PlayerData() {
  }

  public PlayerData(Player player, String groupName) {
    id = UUIDUtils.toBytes(player.getUniqueId());
    uuid = player.getUniqueId();
    name = player.getName();
    this.groupName = groupName;
  }

  public UUID getUUID() {
    if (uuid == null) {
      uuid = UUIDUtils.fromBytes(id);
    }
    return uuid;
  }
}
