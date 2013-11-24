package com.thijsdev.streetlights;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.thijsdev.streetlights.StreetLights;

public class Functions {
	private StreetLights plugin;

	public Functions(StreetLights plugin) {
		this.plugin = plugin;
	}

	public Material getmatchingmaterial(Integer blockset, boolean onoff) {
		if (onoff == true) {
			return plugin.offstate_mats.get(blockset);
		} else {
			return plugin.onstate_mats.get(blockset);
		}
	}

	public Integer getmaterialset(Material mat) {
		for (int i = 0; i < plugin.offstate_mats.size(); i++) {
			if (plugin.offstate_mats.get(i) == mat) {
				return i;
			}
		}
		for (int i = 0; i < plugin.onstate_mats.size(); i++) {
			if (plugin.onstate_mats.get(i) == mat) {
				return i;
			}
		}
		return -1;
	}

	public String LocationToString(Location locatie) {
		return locatie.getWorld().getName() + "," + locatie.getX() + ","
				+ locatie.getY() + "," + locatie.getZ();
	}
	
	public Boolean ComparePendingBlockLoc(Integer index, Block block, String world) {
		if (Integer.parseInt(plugin.pendingBlocks.get(index)[1]) == block.getX()
				&& Integer.parseInt(plugin.pendingBlocks.get(index)[2]) == block
						.getY()
				&& Integer.parseInt(plugin.pendingBlocks.get(index)[3]) == block
						.getZ()
				&& plugin.pendingBlocks.get(index)[4].equalsIgnoreCase(world)) {
			return true;
		}
		return false;
	}

	public void togglelights(boolean aanuit, String wereld) {
		if (plugin.pendingBlocks != null) {
			if (plugin.pendingBlocks.size() >= 0) {
				for (int i = 0; i < plugin.pendingBlocks.size(); i++) {
					String[] element = plugin.pendingBlocks.get(i);
					if (element[4].equalsIgnoreCase(wereld)) {
						Block blokje = Bukkit.getWorld(element[4]).getBlockAt(
								Integer.parseInt(element[1]),
								Integer.parseInt(element[2]),
								Integer.parseInt(element[3]));
						if (plugin.World_Light_Status.get(blokje.getWorld()) == true
								|| aanuit == true) {
							blokje.setType(getmatchingmaterial(
									Integer.parseInt(element[5]), false));
						} else {
							blokje.setType(getmatchingmaterial(
									Integer.parseInt(element[5]), true));
						}
					}
				}
			}
		}
	}
}
