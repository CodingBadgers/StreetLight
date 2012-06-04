package com.thijsdev.etc;

import java.util.Arrays;

import org.bukkit.Material;

import com.thijsdev.StreetLights.StreetLights;

public class Configuration {
	private StreetLights plugin;

	public Configuration(StreetLights plugin) {
		this.plugin = plugin;
	}

	public void loadConfig() {
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();

		plugin.config_on_time = plugin.getConfig().getInt("Time_on");
		plugin.config_off_time = plugin.getConfig().getInt("Time_off");
		plugin.config_use_rain = plugin.getConfig().getBoolean("On_when_rain");

		for (String string : plugin.getConfig().getStringList("materials")) {
			plugin.onstate_mats.add(Material.getMaterial(string.split(",")[0]));
		}
		for (String string : plugin.getConfig().getStringList("materials")) {
			plugin.offstate_mats.add(Material.getMaterial(string.split(",")[1]));
		}
		for (String string : plugin.getConfig().getStringList("lights")) {
			plugin.pendingBlocks.add(string.split(",", 6));
		}
	}

	public void save() {
		String[] listOfStrings = new String[plugin.pendingBlocks.size()];
		Integer count = 0;
		for (String[] array : plugin.pendingBlocks) {
			listOfStrings[count] = array[0] + "," + array[1] + "," + array[2] + "," + array[3] + "," + array[4] + "," + array[5];
			count++;
		}
		plugin.getConfig().set("lights", Arrays.asList(listOfStrings));
		plugin.saveConfig();
	}

}
