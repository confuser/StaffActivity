package me.confuser.staffactivity.storage;


import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.staffactivity.StaffActivity;

import java.sql.SQLException;
import java.util.List;

public class PlayerStorage extends BaseDaoImpl<PlayerData, byte[]> {

  private StaffActivity plugin = StaffActivity.getPlugin();

  public PlayerStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerData>) StaffActivity.getPlugin().getConfiguration().getDatabaseConfig()
                                                                     .getTable("players"));

    if (!isTableExists()) TableUtils.createTable(connection, tableConfig);
  }

  public PlayerData getByName(String name) throws SQLException {
    List<PlayerData> players = queryForEq("name", name);

    if (players.size() == 0) return null;

    return players.get(0);
  }
}
