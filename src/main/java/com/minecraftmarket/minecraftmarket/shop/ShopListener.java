package com.minecraftmarket.minecraftmarket.shop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.minecraftmarket.minecraftmarket.util.Chat;

public class ShopListener implements Listener {

	Shop gui = Shop.getInstance();

	Chat chat = Chat.get();

	public String getMsg(String string) {
		return Chat.get().getLanguage().getString(string);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		try {
			if (event.getInventory().getTitle().contains("Category: ") || event.getInventory().getTitle().contains("Categories")) {
				event.setCancelled(true);
				if (event.getCurrentItem() == null) {
					event.setCancelled(true);
					return;
				}
				if (event.getCurrentItem().getItemMeta() == null) {
					event.setCancelled(true);
					return;
				}
			}
			if (event.getInventory().getTitle().contains("Category: ")) {
				Player player = (Player) event.getWhoClicked();
				String name = event.getCurrentItem().getItemMeta().getDisplayName();
				
				if (name.contains(Chat.get().translate(getMsg("shop.back-to-category")))) {
					event.getWhoClicked().closeInventory();
					gui.showCategories((Player) event.getWhoClicked());
					event.setCancelled(true);
					return;
				}
				
				String[] id = name.split(": ");
				int id1 = Integer.parseInt(id[1]);
				String url = ShopPackage.getById(id1).getUrl();
				player.sendMessage(chat.prefix + ChatColor.GOLD + getMsg("shop.item-url") + ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + ChatColor.UNDERLINE + url);
				event.getWhoClicked().closeInventory();
				event.setCancelled(true);
				return;
			}
			if (event.getInventory().getTitle().contains("Categories")) {
				int num = ShopCategory.getCategoryBySlot(event.getSlot()).getID();
				event.getWhoClicked().closeInventory();
				gui.showGui((Player) event.getWhoClicked(), num);
				event.setCancelled(true);
			}
			
		} catch (Exception e1) {
		}
	}

}