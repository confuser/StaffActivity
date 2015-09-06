package me.confuser.staffactivity.configs;

import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.configs.Config;
import me.confuser.staffactivity.StaffActivity;

public class MessagesConfig extends Config<StaffActivity> {

	public MessagesConfig() {
		super("messages.yml");
	}

	public void afterLoad() {
		Message.load(conf);
	}

	public void onSave() {

	}

}
