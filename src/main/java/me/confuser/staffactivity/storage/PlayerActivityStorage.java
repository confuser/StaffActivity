package me.confuser.staffactivity.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.staffactivity.StaffActivity;

import java.sql.SQLException;

public class PlayerActivityStorage extends BaseDaoImpl<PlayerActivityData, Integer> {

  private StaffActivity plugin = StaffActivity.getPlugin();

  public PlayerActivityStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerActivityData>) StaffActivity.getPlugin().getConfiguration()
                                                                             .getDatabaseConfig()
                                                                             .getTable("activities"));
  }
}
