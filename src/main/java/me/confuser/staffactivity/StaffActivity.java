package me.confuser.staffactivity;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.confuser.bukkitutil.BukkitPlugin;
import me.confuser.staffactivity.commands.StaffCommand;
import me.confuser.staffactivity.configs.DatabaseConfig;
import me.confuser.staffactivity.configs.DefaultConfig;
import me.confuser.staffactivity.configs.MessagesConfig;
import me.confuser.staffactivity.configs.SignsConfig;
import me.confuser.staffactivity.data.StaffPlayer;
import me.confuser.staffactivity.listeners.*;
import me.confuser.staffactivity.listeners.afk.EssentialsListener;
import me.confuser.staffactivity.managers.SignManager;
import me.confuser.staffactivity.managers.StaffManager;
import me.confuser.staffactivity.storage.PlayerActivityStorage;
import me.confuser.staffactivity.storage.PlayerSessionStorage;
import me.confuser.staffactivity.storage.PlayerStorage;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.sql.SQLException;

public class StaffActivity extends BukkitPlugin {

  @Getter
  private static StaffActivity plugin;
  @Getter
  private static Chat chat = null;
  @Getter
  private DefaultConfig configuration;
  @Getter
  private SignsConfig signsConfig;
  private ConnectionSource localConn;
  @Getter
  private PlayerStorage playerStorage;
  @Getter
  private PlayerSessionStorage playerSessionStorage;
  @Getter
  private PlayerActivityStorage playerActivityStorage;
  @Getter
  private StaffManager staffManager;
  @Getter
  private SignManager signManager;

  @Override
  public void onEnable() {
    plugin = this;

    setupConfigs();

    disableDatabaseLogging();

    try {
      if (!setupConnections()) {
        return;
      }

      setupStorage();
    } catch (SQLException e) {
      getLogger().warning("An error occurred attempting to make a database connection, please see stack trace below");
      plugin.getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    setupChat();
    staffManager = new StaffManager();
    signManager = new SignManager();

    setupListeners();
    setupCommands();
    setupRunnables();

    try {
      MetricsLite metrics = new MetricsLite(this);
      metrics.start();
    } catch (IOException e) {
      // Failed to submit the stats :-(
    }
  }

  @Override
  public void onDisable() {
    if (localConn != null) {
      getLogger().info("Saving activity");
      for (StaffPlayer player : staffManager.getPlayers()) {
        try {
          player.getSession().setLeft(System.currentTimeMillis() / 1000L);
          staffManager.save(player);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      localConn.closeQuietly();
    }
  }

  @Override
  public String getPluginFriendlyName() {
    return "StaffActivity";
  }

  @Override
  public String getPermissionBase() {
    return "staffactivity";
  }

  @Override
  public void setupConfigs() {
    configuration = new DefaultConfig();
    configuration.load();

    signsConfig = new SignsConfig();
    signsConfig.load();

    new MessagesConfig().load();
  }

  @Override
  public void setupCommands() {
    new StaffCommand();
  }

  @Override
  public void setupListeners() {
    new JoinListener().register();
    new LeaveListener().register();
    new ChatListener().register();
    new CommandListener().register();
    new SignListener().register();

    if (getServer().getPluginManager().getPlugin("Essentials") != null) {
      new EssentialsListener().register();
    }
  }

  @Override
  public void setupRunnables() {

  }

  public boolean setupConnections() throws SQLException {
    DatabaseConfig dbConfig = configuration.getDatabaseConfig();

    if (!dbConfig.isEnabled()) {
      getLogger().warning("Local Database is not enabled, disabling plugin");
      plugin.getPluginLoader().disablePlugin(this);
      return false;
    }

    HikariDataSource ds = new HikariDataSource();

    if (!dbConfig.getUser().isEmpty()) {
      ds.setUsername(dbConfig.getUser());
    }
    if (!dbConfig.getPassword().isEmpty()) {
      ds.setPassword(dbConfig.getPassword());
    }

    ds.setJdbcUrl(dbConfig.getJDBCUrl());
    ds.setMaximumPoolSize(dbConfig.getMaxConnections());
    ds.setLeakDetectionThreshold(2000);
    ds.addDataSourceProperty("prepStmtCacheSize", "250");
    ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    ds.addDataSourceProperty("cachePrepStmts", "true");

    localConn = new DataSourceConnectionSource(ds, dbConfig.getJDBCUrl());

    return true;
  }

  public void setupStorage() throws SQLException {
    playerStorage = new PlayerStorage(localConn);
    playerSessionStorage = new PlayerSessionStorage(localConn);
    playerActivityStorage = new PlayerActivityStorage(localConn);

    if (!playerSessionStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerSessionStorage.getTableConfig());
    }

    if (!playerActivityStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerActivityStorage.getTableConfig());
    }
  }

  private void disableDatabaseLogging() {
    System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
  }

  private boolean setupChat() {
    RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager()
                                                              .getRegistration(net.milkbowl.vault.chat.Chat.class);
    if (chatProvider != null) {
      chat = chatProvider.getProvider();
    }

    return (chat != null);
  }
}
