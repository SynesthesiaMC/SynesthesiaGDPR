package services.synesthesia.synesthesia_gdpr.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import services.synesthesia.synesthesia_gdpr.Main;
import services.synesthesia.synesthesia_gdpr.Utils;
import services.synesthesia.synesthesia_gdpr.inventories.GdprInventory;
import services.synesthesia.synesthesia_gdpr.managers.GDPRManager;

public class InventoryListener implements Listener {

	private Main plugin;
	private FileConfiguration config;
	private GDPRManager manager;

	public InventoryListener(Main plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getConfig();
		this.manager = this.plugin.getGdprManager();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onClick(InventoryClickEvent event) {

		if (event.getClickedInventory() == null) {
			return;
		}

		if (!(event.getClickedInventory().getHolder() instanceof GdprInventory)) {
			return;
		}

		event.setCancelled(true);

		ItemStack is = event.getCurrentItem();

		if (is == null) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		Material mat;
		Material clickmat = event.getCurrentItem().getType();
		int action = 0;
		for (String key : this.config.getConfigurationSection("Inventory.Items").getKeys(false)) {
			ConfigurationSection keySection = plugin.getConfig().getConfigurationSection("Inventory.Items")
					.getConfigurationSection(key);

			mat = Material.valueOf((keySection.getString("Item")));
			if (clickmat == mat) {
				action = Integer.parseInt(keySection.getString("Action"));
				break;
			}

		}

		if (action == 0) {
			return;
		}

		switch (action) {

		case 1:
			this.manager.setDecision(player.getUniqueId(), false);
			player.kickPlayer(Utils.chat(this.config.getString("Messages.Kick").replace("%prefix%",
					Utils.chat(this.config.getString("prefix")))));
			break;
		case 2:
			this.manager.setDecision(player.getUniqueId(), true);
			player.sendMessage(Utils.chat(this.config.getString("Messages.Accepted").replace("%prefix%",
					Utils.chat(this.config.getString("prefix")))));
			player.closeInventory();
			break;
		case 3:
			player.kickPlayer(Utils.chat(this.config.getString("Messages.Exit").replace("%prefix%",
					Utils.chat(this.config.getString("prefix")))));
			break;
			
		}

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onClose(InventoryCloseEvent event)throws Exception {
		
		Player player = (Player) event.getPlayer();
		
		if(!player.isOnline()) {
			return;
		}
		
		if (!this.manager.hasAccepted(player.getUniqueId())) {

			Bukkit.getScheduler().runTaskLater((Plugin) plugin, () -> {
				GdprInventory gui = new GdprInventory(plugin, player);
				player.openInventory(gui.getInventory());
			}, 5L);
			
		}

	}

}
