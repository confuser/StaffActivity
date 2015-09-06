package me.confuser.staffactivity.managers;

import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.configs.PlayerSign;
import me.confuser.staffactivity.configs.SignsConfig;
import me.confuser.staffactivity.storage.PlayerData;
import me.confuser.staffactivity.utils.LocationBuilder;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Iterator;

public class SignManager {

  private StaffActivity plugin = StaffActivity.getPlugin();
  private SignsConfig config = plugin.getSignsConfig();


  public void addSign(PlayerData player, Block block) {
    LocationBuilder locationBuilder = new LocationBuilder(block.getLocation());
    PlayerSign sign = new PlayerSign(locationBuilder);

    config.addSign(player.getUUID(), sign);
    config.save();
  }

  public void updateSigns(PlayerData player, boolean isOnline) {
    if (isOnline) {
      setOnline(player);
    } else {
      setOffline(player);
    }
  }

  public void setOnline(PlayerData player) {
    ArrayList<PlayerSign> signs = config.getPlayerSigns(player.getUUID());

    if (signs == null || signs.size() == 0) return;
    Iterator<PlayerSign> iterator = signs.iterator();

    while (iterator.hasNext()) {
      PlayerSign sign = iterator.next();

      try {
        sign.setOnline(player);
      } catch (Exception e) {
        if (!e.getMessage().equals("Expected sign")) e.printStackTrace();

        iterator.remove();
        config.save();
      }
    }

  }

  public void setOffline(PlayerData player) {
    ArrayList<PlayerSign> signs = config.getPlayerSigns(player.getUUID());

    if (signs == null || signs.size() == 0) return;
    Iterator<PlayerSign> iterator = signs.iterator();

    while (iterator.hasNext()) {
      PlayerSign sign = iterator.next();

      try {
        sign.setOffline(player);
      } catch (Exception e) {
        if (!e.getMessage().equals("Expected sign")) e.printStackTrace();

        iterator.remove();
        config.save();
      }
    }
  }

  public void removeSign(Block block) {
    Iterator<ArrayList<PlayerSign>> iterator = config.getSigns().iterator();
    boolean shouldSave = false;

    while (iterator.hasNext()) {
      Iterator<PlayerSign> itr = iterator.next().iterator();

      while (itr.hasNext()) {
        PlayerSign sign = itr.next();

        if (sign.getLocationBuilder().equalsLocation(block.getLocation())) {
          itr.remove();
          shouldSave = true;
        }
      }
    }

    if (shouldSave) config.save();
  }
}
