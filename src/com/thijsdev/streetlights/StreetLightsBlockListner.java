package com.thijsdev.StreetLights;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

public class StreetLightsBlockListner implements Listener {
	private StreetLights plugin;

	public StreetLightsBlockListner(StreetLights plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {
		if (plugin.pendingBlocks != null) {
			Block block = event.getBlock();
			if (block.getType() == Material.REDSTONE_LAMP_ON) {
				for (int b = 0; b < plugin.pendingBlocks.size(); b++) {
					if (plugin.func.ComparePendingBlockLoc(b, block, event.getBlock().getWorld().getName())) {
						event.setNewCurrent(100);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (plugin.status.get(event.getPlayer()) == null) {
			plugin.status.put(event.getPlayer(), "");
		}
		if (!plugin.status.get(event.getPlayer()).equalsIgnoreCase("create") && !plugin.status.get(event.getPlayer()).equalsIgnoreCase("info") && !plugin.status.get(event.getPlayer()).equalsIgnoreCase("remove")) {
			for (int b = 0; b < plugin.pendingBlocks.size(); b++) {
				if (plugin.func.ComparePendingBlockLoc(b, block, event.getPlayer().getWorld().getName())) {
					if (plugin.pendingBlocks.get(b)[0].equalsIgnoreCase(event.getPlayer().getName()) || event.getPlayer().isOp() || event.getPlayer().hasPermission("streetlights.admin")) {
						plugin.pendingBlocks.remove(b);
						event.getPlayer().sendMessage(ChatColor.RED + "StreetLight Removed");
						plugin.conf.save();
					} else {
						event.getPlayer().sendMessage(ChatColor.RED + "This StreetLight belongs to " + plugin.pendingBlocks.get(b)[0] + ".");
						event.setCancelled(true);
					}
				}
			}
		} else {
			event.setCancelled(true);
		}
	}
}
