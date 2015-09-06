package me.confuser.staffactivity.commands.staff;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import me.confuser.staffactivity.StaffActivity;
import me.confuser.staffactivity.storage.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListSubCommand extends SubCommand<StaffActivity> {

  public ListSubCommand() {
    super("list");
  }

  @Override
  public boolean onCommand(final CommandSender sender, String[] args) {
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        StringBuilder online = new StringBuilder();
        StringBuilder offline = new StringBuilder();

        CloseableIterator<PlayerData> iterator = plugin.getPlayerStorage().iterator();

        while (iterator.hasNext()) {
          PlayerData data = iterator.next();
          Player player = plugin.getServer().getPlayer(data.getUUID());

          if (player == null) {
            Message message = Message.get("list.offline")
                                     .set("player", data.getName())
                                     .set("group", data.getGroupName());
            offline.append(message.toString());
            offline.append(", ");
          } else {
            Message message = Message.get("list.online")
                                     .set("player", data.getName())
                                     .set("group", data.getGroupName())
                                     .set("displayName", player.getDisplayName());

            online.append(message.toString());
            online.append(", ");
          }
        }

        iterator.closeQuietly();

        Message message;

        if (offline.length() == 0) {
          message = Message.get("list.display.allOnline");
        } else if (online.length() == 0) {
          message = Message.get("list.display.allOffline");
        } else {
          message = Message.get("list.display.mixed");
        }

        removeDelimiter(offline);
        removeDelimiter(online);

        message.set("online", online.toString()).set("offline", offline.toString()).sendTo(sender);
      }
    });

    return true;
  }

  private void removeDelimiter(StringBuilder sb) {
    if (sb.length() >= 2) sb.setLength(sb.length() - 2);
  }

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public String getPermission() {
    return "command.list";
  }
}
