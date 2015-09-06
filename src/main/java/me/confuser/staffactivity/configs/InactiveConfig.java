package me.confuser.staffactivity.configs;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

public class InactiveConfig {

  @Getter
  private String name;

  @Getter
  private boolean enabled = false;

  @Getter
  private boolean autoDemote = false;

  @Getter
  private NotifyType type;

  @Getter
  private boolean notifySelf = true;
  @Getter
  private boolean notifyOwners = true;

  // Checks
  @Getter
  private long minimumChat = 0;
  @Getter
  private long minimumCommands = 0;
  @Getter
  private long minimumSeconds = 0;


  public InactiveConfig(String name, ConfigurationSection conf) {
    this.name = name;

    enabled = conf.getBoolean("enabled", false);

    autoDemote = conf.getBoolean("autoDemote", false);

    type = NotifyType.valueOf(conf.getString("type").toUpperCase());

    notifySelf = conf.getBoolean("notify.self");
    notifyOwners = conf.getBoolean("notify.owners");

    minimumChat = conf.getLong("checks.chat");
    minimumCommands = conf.getLong("checks.commands");
    minimumSeconds = conf.getLong("checks.time");
  }

  public long getTimeDiff() {
    long timeDiff = 0;

    switch (type) {
      case DAILY:
        timeDiff = 86400;
        break;
      case WEEKLY:
        timeDiff = 604800;
        break;
      case MONTHLY:
        timeDiff = (long) 2.62974e6;
        break;
    }

    return timeDiff;
  }

  public long getJoinCheck() {
    long joinCheck = 0;
    long currentTime = System.currentTimeMillis() / 1000L;

    switch (type) {
      case DAILY:
        joinCheck = currentTime - 86400;
        break;
      case WEEKLY:
        joinCheck = currentTime - 604800;
        break;
      case MONTHLY:
        joinCheck = (long) (currentTime - 2.62974e6);
        break;
    }

    return joinCheck;
  }
}
