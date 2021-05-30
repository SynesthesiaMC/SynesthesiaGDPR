package services.synesthesia.synesthesia_gdpr;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.mysql.jdbc.Statement;

import services.synesthesia.synesthesia_gdpr.listeners.InventoryListener;
import services.synesthesia.synesthesia_gdpr.listeners.PlayerJoinListener;
import services.synesthesia.synesthesia_gdpr.managers.GDPRManager;
import services.synesthesia.synesthesia_gdpr.managers.MysqlManager;

public class Main extends JavaPlugin {

	private MysqlManager mysqlmanager;
	private GDPRManager gdprmanager;
	private File dataFile;
	private FileConfiguration data;
	private boolean mysql;

	@Override
	public void onEnable() {

		final Date date = new Date();
		final long startTime = date.getTime();

		this.getLogger().info("------------[SYNESTHESIAGDPR]-------------");

		this.saveDefaultConfig();

		try {
			if (this.getConfig().getBoolean("Mysql.Enabled")) {
				this.mysqlmanager = new MysqlManager(this);
				this.mysqlmanager.openConnection();
				this.mysqlmanager.createTable();
				this.getLogger().info(" - Successfully Connected to Mysql Database");
				this.mysql = true;
				Bukkit.getScheduler().runTask(this, () -> {
					loadPlayerData();
				});
			} else {
				this.createData();
				this.mysql = false;
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			this.getLogger().info(" - Mysql Connection Failed, Disabling...");
			this.getLogger().info("------------[SYNESTHESIAGDPR]-------------");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.gdprmanager = new GDPRManager(this);

		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
		this.getLogger().info(" - Loaded PlayerJoinEvent");

		getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
		this.getLogger().info(" - Loaded InventoryEvents");

		this.getLogger().info("");
		this.getLogger().info("SynesthesiaGdpr v1.0 has successfully loaded!");
		final Date date2 = new Date();
		final long endTime = date2.getTime();
		this.getLogger().info("Plugin successfully loaded in " + (endTime - startTime) + "ms.");

		this.getLogger().info("------------[SYNESTHESIAGDPR]-------------");

	}

	@Override
	public void onDisable() {

		this.getLogger().info("------------[SYNESTHESIAGDPR]-------------");
		this.getLogger().info("Unloading Plugin...");
		this.getLogger().info(" - Writing Data on Database...");
		this.writeToDb();
		this.getLogger().info("");
		this.getLogger().info(" - Unloaded All Hooks");
		this.getLogger().info(" - Unloaded All Events");
		this.getLogger().info(" - Unloaded All Managers");
		this.getLogger().info("");
		this.getLogger().info("SynesthesiaGdpr v1.0 plugin disabled!");
		this.getLogger().info("------------[SYNESTHESIAGDPR]-------------");

	}

	private void createData() {
		dataFile = new File(getDataFolder(), "data.yml");
		boolean no_load = false;
		if (!dataFile.exists()) {
			dataFile.getParentFile().mkdirs();
			saveResource("data.yml", false);
			no_load = true;
		}

		data = new YamlConfiguration();
		try {
			data.load(dataFile);
			if (!no_load) {
				Bukkit.getScheduler().runTask(this, () -> {
					loadPlayerData();
				});
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void writeToDb() {

		for (Iterator<Entry<UUID, Boolean>> iterator = this.gdprmanager.getData().entrySet().iterator(); iterator
				.hasNext();) {

			Entry<UUID, Boolean> player = iterator.next();

			if (this.mysql) {

				ResultSet result = this.mysqlmanager.selectionExtract(player.getKey());
				if (result == null) {
					this.getLogger().info("inserito");
					this.mysqlmanager.selectionInsert(player.getKey(), player.getValue());
				} else {
					try {
						while (result.next()) {
							this.getLogger().info("modificato");
							this.mysqlmanager.selectionUpdate(player.getKey(), player.getValue());
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				
				String path = "Playerdata." + player.getKey().toString();
				String section = "Playerdata." + player.getKey().toString();

				if (this.data.getConfigurationSection(section) == null) {
					this.data.createSection(section);
					this.data.set(path + ".Accepted", player.getValue());
				} else {
					this.data.set(path + ".Accepted", player.getValue());
				}
				this.SaveDataConfig();
				
			}

		}

	}

	public void loadPlayerData() {

		if (this.mysql) {

			Statement stmt;
			ResultSet result = null;
			try {

				stmt = (Statement) this.mysqlmanager.getConnection().createStatement();
				result = stmt.executeQuery(
						"SELECT * FROM " + this.getConfig().getString("Mysql.TableName").toUpperCase() + ";");

				while (result.next()) {
					this.getLogger().info("letto");
					UUID player = UUID.fromString(result.getString("UUID"));
					if (player != null) {
						boolean choice = result.getBoolean("ACCEPTED");
						this.gdprmanager.addPlayer(player);
						this.gdprmanager.setDecision(player, choice);
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			
			for (String key : this.data.getConfigurationSection("Playerdata").getKeys(false)) {
				ConfigurationSection keySection = this.data.getConfigurationSection("Playerdata")
						.getConfigurationSection(key);
				
				boolean accepted = keySection.getBoolean("Accepted");
				this.gdprmanager.addPlayer(UUID.fromString(key));
				this.gdprmanager.setDecision(UUID.fromString(key), accepted);
				
			}
			
		}

	}
	
	public void SaveDataConfig() {

		try {
			data.save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public GDPRManager getGdprManager() {
		return this.gdprmanager;
	}

	public MysqlManager getMysql() {
		return this.mysqlmanager;
	}

}
