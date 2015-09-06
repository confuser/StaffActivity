package me.confuser.staffactivity.configs;

import lombok.Getter;
import me.confuser.bukkitutil.configs.Config;
import me.confuser.staffactivity.StaffActivity;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultConfig extends Config<StaffActivity> {
  @Getter
  private DatabaseConfig databaseConfig;
  private ConcurrentHashMap<String, InactiveConfig> inactivity;

  @Getter
  private List<String> demoteCommands = new ArrayList<>();
  @Getter
  private List<String> resignCommands = new ArrayList<>();

  @Getter
  private boolean removeActivityOnDemote = true;
  @Getter
  private  boolean removeActivityOnResign = true;

  @Getter
  private int keepActivityDays = 60;

  public DefaultConfig() {
    super("config.yml");
  }

  @Override
  public void afterLoad() {
    databaseConfig = new DatabaseConfig(conf.getConfigurationSection("database"));
    inactivity = new ConcurrentHashMap<>();

    ConfigurationSection inactivityConf = conf.getConfigurationSection("inactivity");

    if (inactivityConf != null) {
      for (String groupName : inactivityConf.getKeys(false)) {
        inactivity.put(groupName, new InactiveConfig(groupName, inactivityConf.getConfigurationSection(groupName)));
      }
    }

    demoteCommands = conf.getStringList("demoteCommands");
    resignCommands = conf.getStringList("resignCommands");

    removeActivityOnDemote = conf.getBoolean("removeActivity.onDemote", true);
    removeActivityOnResign = conf.getBoolean("removeActivity.onResign", true);

    keepActivityDays = conf.getInt("keepActivity", 60);
  }

  @Override
  public void onSave() {
  }

  public InactiveConfig getInactiveGroupConfig(String name) {
    return inactivity.get(name);
  }

  public Collection<InactiveConfig> getInactiveGroupConfigs() {
    return inactivity.values();
  }

}
