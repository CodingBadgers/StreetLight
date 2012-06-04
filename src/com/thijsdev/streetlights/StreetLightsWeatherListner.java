package com.thijsdev.StreetLights;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class StreetLightsWeatherListner implements Listener {
	private StreetLights plugin;

	public StreetLightsWeatherListner(StreetLights plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if (plugin.config_use_rain == true) {
			if (event.toWeatherState() == true) {
				plugin.func.togglelights(true, event.getWorld().getName());
			} else {
				plugin.func.togglelights(false, event.getWorld().getName());
			}
		}
	}

}
