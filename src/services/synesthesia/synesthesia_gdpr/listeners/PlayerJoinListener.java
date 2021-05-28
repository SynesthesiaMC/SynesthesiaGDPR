package services.synesthesia.synesthesia_gdpr.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import services.synesthesia.synesthesia_gdpr.Main;
import services.synesthesia.synesthesia_gdpr.inventories.GdprInventory;
import services.synesthesia.synesthesia_gdpr.managers.GDPRManager;

public class PlayerJoinListener implements Listener {

	private Main plugin;
	private GDPRManager manager;
	@SuppressWarnings("unused")
	private FileConfiguration config;

	public PlayerJoinListener(Main plugin) {
		this.plugin = plugin;
		this.manager = this.plugin.getGdprManager();
		this.config = this.plugin.getConfig();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		if (this.manager.hasAccepted(event.getPlayer().getUniqueId())) {
			return;
		}
		
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

			@Override
			public void run() {
				GdprInventory gui = new GdprInventory(plugin, event.getPlayer());
				event.getPlayer().openInventory(gui.getInventory());
			}
		}, 10L);

	}

}
