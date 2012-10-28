package com.thijsdev.StreetLights;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.thijsdev.etc.Configuration;
import com.thijsdev.etc.Functions;


public class StreetLights extends JavaPlugin implements Listener {
	public static File directory;

	private Logger log;
	public ArrayList<String[]> pendingBlocks = new ArrayList<String[]>();
	public ArrayList<Material> onstate_mats = new ArrayList<Material>();
	public ArrayList<Material> offstate_mats = new ArrayList<Material>();
	public Map<World, Boolean> World_Light_Status = new HashMap<World, Boolean>();
	public Map<Player, String> status = new HashMap<Player, String>();
	
	public Functions func;
	public Configuration conf;

	public int config_on_time, config_off_time;
	public boolean config_use_rain;

	public void onEnable() {
		// Register Events
		getServer().getPluginManager().registerEvents(this, this);
		(new StreetLightsBlockListner(this)).registerEvents();
		(new StreetLightsPlayerListner(this)).registerEvents();
		(new StreetLightsWeatherListner(this)).registerEvents();
		func = new Functions(this);
		conf = new Configuration(this);
		
		//Other stuff
		directory = getDataFolder();
		if (!directory.exists())
			directory.mkdir();

		conf.loadConfig();

		for (World world : Bukkit.getWorlds()) {
			if (world.getTime() > config_on_time) {
				World_Light_Status.put(world, true);
			}
			if (world.getTime() > config_off_time && world.getTime() < config_on_time) {
				World_Light_Status.put(world, false);
			}
		}

		log = this.getLogger();
		log.info("Streetlights is now enabled.");
		startTimeCheck();
	}

	public void onDisable() {
		saveConfig();
		log.info("Streetlights is now disabled.");
	}

	public void startTimeCheck() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					public void run() {
						for (World world : Bukkit.getWorlds()) {
							for (World wereld : Bukkit.getWorlds()) {
								if (!World_Light_Status.containsKey(wereld)) {
									if (wereld.getTime() > config_on_time) {
										World_Light_Status.put(wereld, true);
									}
									if (wereld.getTime() > config_off_time && wereld.getTime() < config_on_time) {
										World_Light_Status.put(wereld, false);
									}
								}
							}

							if (world.getTime() > config_on_time && World_Light_Status.get(world) == false) {
								World_Light_Status.put(world, true);
								func.togglelights(false, world.getName());
							}
							if (world.getTime() > config_off_time && world.getTime() < config_on_time && World_Light_Status.get(world) == true) {
								World_Light_Status.put(world, false);
								func.togglelights(false, world.getName());
							}
						}
					}
				}, 0L, 60L);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("sl")) {
			if (player == null) {
				sender.sendMessage("This command can only be run by a player");
			} else {
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("info")) {
						if (player.hasPermission("streetlights.info")) {
							if(status.get(player) == null) {
								status.put(player, "");
							}
							if (!status.get(player).equalsIgnoreCase("info")) {
								status.put(player, "info");
								player.sendMessage("Please click a streetlight to get info about it.");
							} else {
								status.put(player, "");
								player.sendMessage("You're out info mode now");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("reload")) {
						if (player.hasPermission("streetlights.reload")) {
							this.reloadConfig();

							onstate_mats.clear();
							offstate_mats.clear();
							pendingBlocks.clear();

							for (String string : getConfig().getStringList("materials")) {
								onstate_mats.add(Material.getMaterial(string.split(",")[0]));
							}
							for (String string : getConfig().getStringList("materials")) {
								offstate_mats.add(Material.getMaterial(string.split(",")[1]));
							}
							for (String string : getConfig().getStringList("lights")) {
								pendingBlocks.add(string.split(",", 6));
							}

							config_on_time = getConfig().getInt("Time_on");
							config_off_time = getConfig().getInt("Time_off");
							config_use_rain = getConfig().getBoolean("On_when_rain");

							player.sendMessage(ChatColor.RED + "Config Reloaded!");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("save")) {
						if (player.hasPermission("streetlights.save")) {
							String[] listOfStrings = new String[pendingBlocks.size()];
							Integer count = 0;
							for (String[] array : pendingBlocks) {
								listOfStrings[count] = array[0] + "," + array[1] + "," + array[2] + "," + array[3] + "," + array[4] + "," + array[5];
								count++;
							}
							this.getConfig().set("lights", Arrays.asList(listOfStrings));
							saveConfig();
							player.sendMessage("Saved!");
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("list")) {
						if (player.hasPermission("streetlights.list")) {
							for (int i = 0; i < pendingBlocks.size(); i++) {
								String[] element = pendingBlocks.get(i);
								player.sendMessage("Loc: " + element[1] + "," + element[2] + "," + element[3]);
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("clear")) {
						if (player.hasPermission("streetlights.clear")) {
							pendingBlocks.clear();
							player.sendMessage("List is now cleared!");
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}/*
					if (args[0].equalsIgnoreCase("on")) {
						if (player.hasPermission("streetlights.update")) {
							func.togglelights(true, player.getWorld().getName());
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("off")) {
						if (player.hasPermission("streetlights.update")) {
							func.togglelights(false, player.getWorld().getName());
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}*/
					if (args[0].equalsIgnoreCase("create")) {
						if (player.hasPermission("streetlights.create")) {
							if(status.get(player) == null) {
								status.put(player, "");
							}
							if (!status.get(player).equalsIgnoreCase("create")) {
								status.put(player, "create");
								player.sendMessage("You can now add lights to the list!");
							} else {
								status.put(player, "");
								player.sendMessage("You're done creating streetlights now!");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("remove")) {
						if (player.hasPermission("streetlights.remove")) {
							if(status.get(player) == null) {
								status.put(player, "");
							}
							if (!status.get(player).equalsIgnoreCase("remove")) {
								status.put(player, "remove");
								player.sendMessage("You can now remove streetlights");
							} else {
								status.put(player, "");
								player.sendMessage("You're done removing streetlights now!");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permission to do this.");
						}
						return true;
					}
				}
			}
		}
		return false;
	}

}