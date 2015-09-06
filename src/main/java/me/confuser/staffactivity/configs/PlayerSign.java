package me.confuser.staffactivity.configs;

import lombok.Getter;
import me.confuser.bukkitutil.Message;
import me.confuser.staffactivity.storage.PlayerData;
import me.confuser.staffactivity.utils.LocationBuilder;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerSign {

  @Getter
  private LocationBuilder locationBuilder;

  public PlayerSign(LocationBuilder location) {
    this.locationBuilder = location;
  }

  public synchronized void setOnline(PlayerData player) throws Exception {
    Sign sign = getSign();

    if (!sign.getChunk().isLoaded()) {
      sign.getChunk().load();
    }

    for (int i = 1; i < 5; i++) {
      sign.setLine(i - 1, Message.get("sign.online." + i).set("player", player.getName()).toString());
    }

    sign.update();
  }

  public synchronized void setOffline(PlayerData player) throws Exception {
    Sign sign = getSign();
    String lastSeen = new SimpleDateFormat(Message.getString("sign.offline.seenFormat"))
            .format(new Date(System.currentTimeMillis()));

    if (!sign.getChunk().isLoaded()) {
      sign.getChunk().load();
    }

    for (int i = 1; i < 5; i++) {
      sign.setLine(i - 1, Message.get("sign.offline." + i).set("player", player.getName()).set("seen", lastSeen)
                                 .toString());
    }

    sign.update();
  }

  public synchronized Sign getSign() throws Exception {
    Block block = locationBuilder.toLocation().getBlock();

    if (!(block.getState() instanceof Sign)) throw new Exception("Expected sign");

    return (Sign) block.getState();
  }
}
