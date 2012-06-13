package com.thijsdev.StreetLights;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class StreetLightsPlayerListner implements Listener {
	private StreetLights plugin;

	public StreetLightsPlayerListner(StreetLights plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (plugin.status.get(event.getPlayer()) == null) {
				plugin.status.put(event.getPlayer(), "");
			}
			if (plugin.status.get(event.getPlayer()).equalsIgnoreCase("create")) {
				Block block = event.getClickedBlock();
				Player player = event.getPlayer();
				if (plugin.onstate_mats.contains(block.getType()) || plugin.offstate_mats.contains(block.getType())) {
					String[] coords = { player.getName(), Integer.toString(block.getX()), Integer.toString(block.getY()), Integer.toString(block.getZ()), block.getWorld().getName(), Integer.toString(plugin.func.getmaterialset(block.getType())) };

					// Check if it doesn't already exist
					Boolean exists = false;
					for (int b = 0; b < plugin.pendingBlocks.size(); b++) {
						if (plugin.func.ComparePendingBlockLoc(b, block, event.getPlayer().getWorld().getName())) {
							exists = true;
						}
					}

					if (!exists) {
						// Turn it on or off
						if (plugin.World_Light_Status.get(block.getWorld()) == true) {
							block.setType(plugin.func.getmatchingmaterial(plugin.func.getmaterialset(block.getType()), false));
						} else {
							block.setType(plugin.func.getmatchingmaterial(plugin.func.getmaterialset(block.getType()), true));
						}
						if (plugin.pendingBlocks != null) {
							plugin.pendingBlocks.add(coords);
							plugin.conf.save();
							player.sendMessage("Lamp added.");
						}
					} else {
						player.sendMessage(ChatColor.RED + "This streetlight is already registered.");
					}
				}
			}
			if (plugin.status.get(event.getPlayer()).equalsIgnoreCase("remove")) {
				Block block = event.getClickedBlock();
				Player player = event.getPlayer();
				for (int b = 0; b < plugin.pendingBlocks.size(); b++) {
					if (plugin.func.ComparePendingBlockLoc(b, block, event.getPlayer().getWorld().getName())) {
						if (plugin.pendingBlocks.get(b)[0].equalsIgnoreCase(event.getPlayer().getName()) || event.getPlayer().isOp() || event.getPlayer().hasPermission("streetlights.admin")) {
							plugin.pendingBlocks.remove(b);
							plugin.conf.save();
							player.sendMessage(ChatColor.RED + "Streetlight Removed.");
						}else{
							player.sendMessage(ChatColor.RED + "This StreetLight belongs to " + plugin.pendingBlocks.get(b)[0] + ".");
						}
					}
				}
			}
			if (plugin.status.get(event.getPlayer()).equalsIgnoreCase("info")) {
				Block block = event.getClickedBlock();
				Player player = event.getPlayer();
				Integer issl = -1;
				for (int b = 0; b < plugin.pendingBlocks.size(); b++) {
					if (plugin.func.ComparePendingBlockLoc(b, block, event.getPlayer().getWorld().getName())) {
						issl = b;
					}
				}
				if (issl != -1) {
					player.sendMessage(ChatColor.DARK_GREEN + "This streetlight belongs to: " + ChatColor.GREEN + plugin.pendingBlocks.get(issl)[0]);
					player.sendMessage(ChatColor.DARK_GREEN + "This streetlight is using: " + ChatColor.GREEN + plugin.onstate_mats.get(Integer.parseInt(plugin.pendingBlocks.get(issl)[5])) + ChatColor.DARK_GREEN + " during the day");
					player.sendMessage(ChatColor.DARK_GREEN + "This streetlight is using: " + ChatColor.GREEN + plugin.offstate_mats.get(Integer.parseInt(plugin.pendingBlocks.get(issl)[5])) + ChatColor.DARK_GREEN + " during the night");
				} else {
					player.sendMessage(ChatColor.RED + "This block is not a registered streetlight.");
				}
			}
		}
	}
}
