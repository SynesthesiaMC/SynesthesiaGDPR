package services.synesthesia.synesthesia_gdpr.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import services.synesthesia.synesthesia_gdpr.Main;
import services.synesthesia.synesthesia_gdpr.Utils;
import services.synesthesia.synesthesia_gdpr.managers.GDPRManager;

public class GdprInventory implements InventoryHolder {

	private Main plugin;
	private FileConfiguration config;
	private GDPRManager manager;
	private Inventory gui;
	private Player player;

	public GdprInventory(Main plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		this.config = this.plugin.getConfig();
		this.manager = this.plugin.getGdprManager();
		int size = this.config.getInt("Inventory.Size");
		String title = Utils.chat(this.config.getString("Inventory.Title"));
		this.gui = Bukkit.createInventory(this, size, title);
		this.createGui();
	}

	@SuppressWarnings("deprecation")
	public void createGui() {

		ItemStack item;
		ItemMeta meta;
		int slot;
		List<String> lore = new ArrayList<String>();

		for (String key : this.config.getConfigurationSection("Inventory.Items").getKeys(false)) {

			ConfigurationSection keySection = plugin.getConfig().getConfigurationSection("Inventory.Items")
					.getConfigurationSection(key);

			slot = keySection.getInt("Slot");
			item = new ItemStack(Material.valueOf(keySection.getString("Item")));
			meta = item.getItemMeta();
			meta.setDisplayName(Utils.chat(keySection.getString("Name")));
			int damage = keySection.getInt("Data");
			item.setDurability((short) damage);

			int action = keySection.getInt("Action");

			if (action == 1 || action == 2) {
				if (this.manager.hasAccepted(player.getUniqueId()) == false) {
					meta.addEnchant(Enchantment.LUCK, 1, false);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				}
			}

			lore.clear();
			for (String lore_line : keySection.getStringList("Lore")) {
				lore.add(Utils.chat(lore_line));
			}

			meta.setLore(lore);
			item.setItemMeta(meta);
			this.gui.setItem(slot, item);

		}

	}

	@Override
	public Inventory getInventory() {
		return this.gui;
	}

}
