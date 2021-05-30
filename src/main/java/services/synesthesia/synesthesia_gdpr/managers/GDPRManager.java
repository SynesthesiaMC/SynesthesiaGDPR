package services.synesthesia.synesthesia_gdpr.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import services.synesthesia.synesthesia_gdpr.Main;

public class GDPRManager {

	private Main plugin;
	private HashMap<UUID, Boolean> players;
	private MysqlManager mysql;
	private FileConfiguration config;

	public GDPRManager(Main plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getConfig();
		this.players = new HashMap<UUID, Boolean>();
		this.mysql = this.plugin.getMysql();
		this.startDataSave();
	}

	private void startDataSave() {

		final BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimerAsynchronously((Plugin) this.plugin, (Runnable) new Runnable() {
			@Override
			public void run() {

				plugin.getLogger().info("Players data save Started...");

				for (Iterator<Entry<UUID, Boolean>> iterator = players.entrySet().iterator(); iterator.hasNext();) {

					Entry<UUID, Boolean> player = iterator.next();

					if (config.getBoolean("Mysql.Enabled")) {

						ResultSet result = mysql
								.selectionExtract(Bukkit.getOfflinePlayer(player.getKey()).getUniqueId());
						if (result == null) {
							mysql.selectionInsert(player.getKey(), player.getValue());
						} else {
							try {
								while (result.next()) {
									mysql.selectionUpdate(player.getKey(), player.getValue());
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						
					} else {
						
						
						
					}
				}

				plugin.getLogger().info("Players data save Finished");

			}
		}, 0L, 72000L);
	}

	public void setDecision(UUID player, Boolean accepted) {
		if (this.players.containsKey(player)) {
			this.players.remove(player);
			this.players.put(player, accepted);
		} else {
			this.players.put(player, accepted);
		}
	}

	public void addPlayer(UUID player) {
		this.players.put(player, false);
	}

	public boolean hasAccepted(UUID player) {
		if (this.players.containsKey(player)) {
			return this.players.get(player);
		} else {
			return false;
		}

	}

	public HashMap<UUID, Boolean> getData() {
		return this.players;
	}

}
