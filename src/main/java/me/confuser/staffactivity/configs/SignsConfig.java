package me.confuser.staffactivity.configs;

import me.confuser.bukkitutil.configs.Config;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.utils.LocationBuilder;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class SignsConfig extends Config<StaffActivity> {

  private HashMap<UUID, ArrayList<PlayerSign>> signs;

  public SignsConfig() {
    super("signs.yml");
  }

  @Override
  public void afterLoad() {
    signs = new HashMap<>();
    Set<String> keys = conf.getKeys(false);

    if (keys == null) return;

    for (String uuidStr : keys) {
      loadSigns(UUID.fromString(uuidStr), conf.getConfigurationSection(uuidStr));
    }
  }

  private void loadSigns(UUID uuid, ConfigurationSection configurationSection) {
    if (uuid == null) return;

    ConfigurationSection signsConf = configurationSection.getConfigurationSection("signs");

    if (signsConf == null) return;

    ArrayList<PlayerSign> playerSigns = new ArrayList<>();

    for (String key : signsConf.getKeys(false)) {
      ConfigurationSection signConf = signsConf.getConfigurationSection(key);
      double x = signConf.getDouble("x");
      double y = signConf.getDouble("y");
      double z = signConf.getDouble("z");
      float yaw = (float) signConf.getDouble("yaw");
      float pitch = (float) signConf.getDouble("pitch");
      String world = signConf.getString("world");

      LocationBuilder location = new LocationBuilder(x, y, z, yaw, pitch, world);

      playerSigns.add(new PlayerSign(location));
    }

    signs.put(uuid, playerSigns);

  }

  @Override
  public void onSave() {
    if (signs == null) return;

    for (UUID uuid : signs.keySet()) {
      ArrayList<PlayerSign> playerSigns = signs.get(uuid);

      if (playerSigns.size() == 0) continue;

      ConfigurationSection playerConf = conf.createSection(uuid.toString());

      int i = 0;

      for (PlayerSign playerSign : playerSigns) {
        ConfigurationSection playerSignConf = playerConf.createSection("signs." + i);

        playerSignConf.set("x", playerSign.getLocationBuilder().getX());
        playerSignConf.set("y", playerSign.getLocationBuilder().getY());
        playerSignConf.set("z", playerSign.getLocationBuilder().getZ());
        playerSignConf.set("yaw", playerSign.getLocationBuilder().getYaw());
        playerSignConf.set("pitch", playerSign.getLocationBuilder().getPitch());
        playerSignConf.set("world", playerSign.getLocationBuilder().getWorld());

        i++;
      }

    }
  }

  public void addSign(UUID uuid, PlayerSign sign) {
    if (signs.get(uuid) == null) {
      signs.put(uuid, new ArrayList<PlayerSign>(1));
    }

    signs.get(uuid).add(sign);
  }

  public ArrayList<PlayerSign> getPlayerSigns(UUID uuid) {
    return signs.get(uuid);
  }

  public Collection<ArrayList<PlayerSign>> getSigns() {
    return signs.values();
  }
}
