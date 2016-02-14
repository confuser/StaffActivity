package me.confuser.staffactivity.storage;

import com.google.common.collect.HashMultiset;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.configs.InactiveConfig;
import me.confuser.staffactivity.data.ActivityReport;
import me.confuser.staffactivity.data.ActivityResult;
import me.confuser.staffactivity.data.InactiveReport;
import me.confuser.staffactivity.data.InactiveResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PlayerSessionStorage extends BaseDaoImpl<PlayerSessionData, Integer> {

  private final String reportStatement = "SELECT s.joined, s.left, s.totalTime" +
          ", (SELECT SUM(value) FROM {activityTable} WHERE stat = 'chat' AND session_id = s.id) AS chat" +
          ", (SELECT SUM(value) FROM {activityTable} WHERE stat = 'commands' AND session_id = s.id) AS commands" +
          " FROM {sessionTable} s WHERE s.player_id = ? AND s.joined >= ? AND s.left <= ? ";
  private final String inactiveStatement = "SELECT p.name, SUM(s.totalTime) AS totalTime" +
          ", SUM(IF(sa.stat = 'chat', sa.value, 0)) AS chat" +
          ", SUM(IF(sa.stat = 'commands', sa.value, 0)) AS commands" +
          " FROM {playerTable} p" +
          " LEFT JOIN {sessionTable} s ON s.player_id = p.id" +
          " LEFT JOIN {activityTable} sa ON sa.session_id = s.id" +
          " WHERE p.groupName = ? AND (? - p.startedLogging) >= ? AND s.joined >= ? AND s.left <= ?" +
          " GROUP BY s.player_id" +
          " HAVING totalTime < ? OR chat < ? OR commands < ?";
  private final String inactivePlayerStatement = "SELECT p.name, SUM(s.totalTime) AS totalTime" +
          ", SUM(IF(sa.stat = 'chat', sa.value, 0)) AS chat" +
          ", SUM(IF(sa.stat = 'commands', sa.value, 0)) AS commands" +
          " FROM {playerTable} p" +
          " LEFT JOIN {sessionTable} s ON s.player_id = p.id" +
          " LEFT JOIN {activityTable} sa ON sa.session_id = s.id" +
          " WHERE p.id = ? AND (? - p.startedLogging) >= ? AND s.joined >= ? AND s.left <= ?" +
          " GROUP BY s.player_id" +
          " HAVING totalTime < ? OR chat < ? OR commands < ?";
  private final String noDataStatement = "SELECT p.name, MAX(s.joined) AS lastJoined" +
          " FROM {playerTable}" +
          " p LEFT JOIN {sessionTable} s ON p.id = s.player_id" +
          " WHERE p.groupName = ?" +
          " GROUP BY p.id HAVING lastJoined < ?";
  private StaffActivity plugin = StaffActivity.getPlugin();

  public PlayerSessionStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerSessionData>) StaffActivity.getPlugin().getConfiguration()
                                                                            .getDatabaseConfig()
                                                                            .getTable("sessions"));
  }

  public ActivityReport getReport(PlayerData player, long from, long to) {
    String activityTableName = plugin.getPlayerActivityStorage().getTableConfig().getTableName();
    String sessionTableName = getTableConfig().getTableName();
    String sql = reportStatement.replace("{activityTable}", activityTableName)
                                .replace("{sessionTable}", sessionTableName);

    DatabaseConnection connection = null;
    try {
      connection = connectionSource.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }

    int overallActivity = 0;
    HashMultiset<String> overallStats = HashMultiset.create();
    ArrayList<ActivityResult> activityResults = new ArrayList<>();

    try {
      CompiledStatement statement = connection.compileStatement(sql
              , StatementBuilder.StatementType.SELECT
              , null
              , DatabaseConnection.DEFAULT_RESULT_FLAGS);

      statement.setObject(0, player.getId(), SqlType.BYTE_ARRAY);
      statement.setObject(1, from, SqlType.LONG);
      statement.setObject(2, to, SqlType.LONG);

      DatabaseResults results = statement.runQuery(null);

      while (results.next()) {
        overallActivity += results.getLong(2);
        String[] columnNames = results.getColumnNames();
        HashMap<String, Long> stats = new HashMap<>(2);

        for (int i = 3; i < results.getColumnCount(); i++) {
          String stat = columnNames[i];
          long value = results.getLong(i);

          overallStats.add(stat, (int) value);
          stats.put(stat, value);
        }

        activityResults.add(new ActivityResult(results.getLong(0), results.getLong(1), results.getLong(2), stats));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      connection.closeQuietly();
    }

    overallStats.add("overallActivity", overallActivity);

    return new ActivityReport(from, to, player, overallStats, activityResults);

  }

  public InactiveReport getInactivityReport(InactiveConfig group) {
    String activityTableName = plugin.getPlayerActivityStorage().getTableConfig().getTableName();
    String sessionTableName = getTableConfig().getTableName();
    String playerTableName = plugin.getPlayerStorage().getTableConfig().getTableName();
    String sql = inactiveStatement.replace("{activityTable}", activityTableName)
                                  .replace("{sessionTable}", sessionTableName)
                                  .replace("{playerTable}", playerTableName);

    DatabaseConnection connection;
    try {
      connection = connectionSource.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }

    ArrayList<InactiveResult> inactiveResults = new ArrayList<>();

    try {
      CompiledStatement statement = connection
              .compileStatement(sql
                      , StatementBuilder.StatementType.SELECT
                      , null
                      , DatabaseConnection.DEFAULT_RESULT_FLAGS);

      long joinCheck = group.getJoinCheck();
      long currentTime = System.currentTimeMillis() / 1000L;
      long timeDiff = group.getTimeDiff();

      statement.setObject(0, group.getName(), SqlType.STRING);
      statement.setObject(1, currentTime, SqlType.LONG);
      statement.setObject(2, timeDiff, SqlType.LONG);
      statement.setObject(3, joinCheck, SqlType.LONG);
      statement.setObject(4, currentTime, SqlType.LONG);
      statement.setObject(5, group.getMinimumSeconds(), SqlType.LONG);
      statement.setObject(6, group.getMinimumCommands(), SqlType.LONG);
      statement.setObject(7, group.getMinimumChat(), SqlType.LONG);

      DatabaseResults results = statement.runQuery(null);

      while (results.next()) {
        String[] columnNames = results.getColumnNames();
        HashMap<String, Long> stats = new HashMap<>(2);

        for (int i = 1; i < results.getColumnCount(); i++) {
          String stat = columnNames[i];
          long value = results.getLong(i);

          stats.put(stat, value);
        }

        inactiveResults.add(new InactiveResult(results.getString(0), results.getLong(1), stats));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      connection.closeQuietly();
    }

    return new InactiveReport(group, inactiveResults, getNoDataList(group));
  }

  public InactiveResult getInactivityResult(InactiveConfig group, PlayerData player) {
    String activityTableName = plugin.getPlayerActivityStorage().getTableConfig().getTableName();
    String sessionTableName = getTableConfig().getTableName();
    String playerTableName = plugin.getPlayerStorage().getTableConfig().getTableName();
    String sql = inactivePlayerStatement.replace("{activityTable}", activityTableName)
                                        .replace("{sessionTable}", sessionTableName)
                                        .replace("{playerTable}", playerTableName);

    DatabaseConnection connection;
    try {
      connection = connectionSource.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }

    InactiveResult inactiveResult = null;

    try {
      CompiledStatement statement = connection
              .compileStatement(sql
                      , StatementBuilder.StatementType.SELECT
                      , null
                      , DatabaseConnection.DEFAULT_RESULT_FLAGS);

      long joinCheck = group.getJoinCheck();
      long currentTime = System.currentTimeMillis() / 1000L;
      long timeDiff = group.getTimeDiff();

      statement.setObject(0, player.getId(), SqlType.BYTE_ARRAY);
      statement.setObject(1, currentTime, SqlType.LONG);
      statement.setObject(2, timeDiff, SqlType.LONG);
      statement.setObject(3, joinCheck, SqlType.LONG);
      statement.setObject(4, currentTime, SqlType.LONG);
      statement.setObject(5, group.getMinimumSeconds(), SqlType.LONG);
      statement.setObject(6, group.getMinimumCommands(), SqlType.LONG);
      statement.setObject(7, group.getMinimumChat(), SqlType.LONG);

      DatabaseResults results = statement.runQuery(null);

      if (results.next()) {
        String[] columnNames = results.getColumnNames();
        HashMap<String, Long> stats = new HashMap<>(2);

        for (int i = 1; i < results.getColumnCount(); i++) {
          String stat = columnNames[i];
          long value = results.getLong(i);

          stats.put(stat, value);
        }

        inactiveResult = new InactiveResult(results.getString(0), results.getLong(1), stats);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      connection.closeQuietly();
    }

    return inactiveResult;
  }

  public List<String> getNoDataList(InactiveConfig group) {
    String playerTableName = plugin.getPlayerStorage().getTableConfig().getTableName();
    String sessionTableName = getTableConfig().getTableName();
    String sql = noDataStatement.replace("{sessionTable}", sessionTableName)
                                .replace("{playerTable}", playerTableName);

    DatabaseConnection connection;
    try {
      connection = connectionSource.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }

    ArrayList<String> playerNames = new ArrayList<>();

    try {
      CompiledStatement statement = connection
              .compileStatement(sql
                      , StatementBuilder.StatementType.SELECT
                      , null
                      , DatabaseConnection.DEFAULT_RESULT_FLAGS);

      long joinCheck = group.getJoinCheck();

      statement.setObject(0, group.getName(), SqlType.STRING);
      statement.setObject(1, joinCheck, SqlType.LONG);

      DatabaseResults results = statement.runQuery(null);

      while (results.next()) {
        playerNames.add(results.getString(0));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      connection.closeQuietly();
    }

    return playerNames;

  }

  public ArrayList<Integer> delete(PlayerData player) throws SQLException {
    Iterator<PlayerSessionData> iterator = queryForEq("player_id", player.getId()).iterator();
    ArrayList<Integer> ids = new ArrayList<>();

    while (iterator.hasNext()) {
      ids.add(iterator.next().getId());
    }

    deleteIds(ids);

    return ids;
  }
}
