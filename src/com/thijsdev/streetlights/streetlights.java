package com.thijsdev.streetlights;

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
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class streetlights extends JavaPlugin implements Listener {
	public static File directory;

	private Logger log;
	private static ArrayList<String[]> pendingBlocks = new ArrayList<String[]>();
	private static ArrayList<Material> onstate_mats = new ArrayList<Material>();
	private static ArrayList<Material> offstate_mats = new ArrayList<Material>();
	private Map<World, Boolean> World_Light_Status = new HashMap<World, Boolean>();

	private boolean edit = false;

	private int config_on_time, config_off_time;
	private boolean config_use_rain;

	public void onEnable() {
		directory = getDataFolder();
		if (!directory.exists())
			directory.mkdir();

		for (World world : Bukkit.getWorlds()) {
			if (world.getTime() > config_on_time) {
				World_Light_Status.put(world, true);
			}
			if (world.getTime() > config_off_time
					&& world.getTime() < config_on_time) {
				World_Light_Status.put(world, false);
			}
		}

		loadConfig();
		log = this.getLogger();
		log.info("Streetlights is now enabled.");
		getServer().getPluginManager().registerEvents(this, this);
		startTimeCheck();
	}

	private void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		config_on_time = getConfig().getInt("Time_on");
		config_off_time = getConfig().getInt("Time_off");
		config_use_rain = getConfig().getBoolean("On_when_rain");

		for (String string : getConfig().getStringList("materials")) {
			onstate_mats.add(Material.getMaterial(string.split(",")[0]));
		}
		for (String string : getConfig().getStringList("materials")) {
			offstate_mats.add(Material.getMaterial(string.split(",")[1]));
		}
		for (String string : getConfig().getStringList("lights")) {
			pendingBlocks.add(string.split(",", 6));
		}
	}

	public void onDisable() {
		saveConfig();
		log.info("Streetlights is now disabled.");
	}

	public Material getmatchingmaterial(Integer blockset, boolean onoff) {
		if (onoff == true) {
			return offstate_mats.get(blockset);
		} else {
			return onstate_mats.get(blockset);
		}
	}

	public Integer getmaterialset(Material mat) {
		for (int i = 0; i < offstate_mats.size(); i++) {
			if (offstate_mats.get(i) == mat) {
				return i;
			}
		}
		for (int i = 0; i < onstate_mats.size(); i++) {
			if (onstate_mats.get(i) == mat) {
				return i;
			}
		}
		return -1;
	}

	public void togglelights(boolean aanuit, int wereld) {
		if (pendingBlocks != null) {
			if (pendingBlocks.size() >= 0) {
				for (int i = 0; i < pendingBlocks.size(); i++) {
					String[] element = pendingBlocks.get(i);
					if (Integer.parseInt(element[4]) == wereld) {
						Block blokje = Bukkit.getWorld(
								getServer().getWorlds()
										.get(Integer.parseInt(element[4]))
										.getName()).getBlockAt(
								Integer.parseInt(element[1]),
								Integer.parseInt(element[2]),
								Integer.parseInt(element[3]));
						if (World_Light_Status.get(blokje.getWorld()) == true
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

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if (config_use_rain == true) {
			int worldid = 0;
			for (int a = 0; a < getServer().getWorlds().size(); a++) {
				if (event.getWorld() == getServer().getWorlds().get(a)) {
					worldid = a;
				}
			}
			if (event.toWeatherState() == true) {
				togglelights(true, worldid);
			} else {
				togglelights(false, worldid);
			}
		}
	}

	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {
		if (pendingBlocks != null) {
			Block block = event.getBlock();
			if (block.getType() == Material.REDSTONE_LAMP_ON) {
				int worldid = 0;
				for (int a = 0; a < getServer().getWorlds().size(); a++) {
					if (block.getWorld() == getServer().getWorlds().get(a)) {
						worldid = a;
					}
				}
				for (int b = 0; b < pendingBlocks.size(); b++) {
					if (Integer.parseInt(pendingBlocks.get(b)[1]) == block
							.getX()
							&& Integer.parseInt(pendingBlocks.get(b)[2]) == block
									.getY()
							&& Integer.parseInt(pendingBlocks.get(b)[3]) == block
									.getZ()
							&& Integer.parseInt(pendingBlocks.get(b)[4]) == worldid) {
						event.setNewCurrent(100);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		for (int b = 0; b < pendingBlocks.size(); b++) {
			int worldid = 0;
			for (int a = 0; a < getServer().getWorlds().size(); a++) {
				if (block.getWorld() == getServer().getWorlds().get(a)) {
					worldid = a;
				}
			}
			if (Integer.parseInt(pendingBlocks.get(b)[1]) == block.getX()
					&& Integer.parseInt(pendingBlocks.get(b)[2]) == block
							.getY()
					&& Integer.parseInt(pendingBlocks.get(b)[3]) == block
							.getZ()
					&& Integer.parseInt(pendingBlocks.get(b)[4]) == worldid) {
				if(pendingBlocks.get(b)[0] == event.getPlayer().getName() || event.getPlayer().isOp()) {
					pendingBlocks.remove(b);
					event.getPlayer().sendMessage(
						ChatColor.RED + "StreetLight Removed");
				}else{
					event.getPlayer().sendMessage(
							ChatColor.RED + "This is not your streetlight!");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (edit == true) {
			Block block = event.getClickedBlock();
			Player player = event.getPlayer();
			if (onstate_mats.contains(block.getType())
					|| offstate_mats.contains(block.getType())) {
				int worldid = 0;
				for (int a = 0; a < getServer().getWorlds().size(); a++) {
					if (block.getWorld() == getServer().getWorlds().get(a)) {
						worldid = a;
					}
				}
				String[] coords = { player.getName(),
						Integer.toString(block.getX()),
						Integer.toString(block.getY()),
						Integer.toString(block.getZ()),
						Integer.toString(worldid),
						Integer.toString(getmaterialset(block.getType())) };

				// Turn it on or off
				if (World_Light_Status.get(block.getWorld()) == true) {
					block.setType(getmatchingmaterial(
							getmaterialset(block.getType()), false));
				} else {
					block.setType(getmatchingmaterial(
							getmaterialset(block.getType()), true));
				}
				if (pendingBlocks != null) {
					if (!pendingBlocks.contains(coords)) {
						pendingBlocks.add(coords);
						player.sendMessage("Lamp added.");
					}
				}
			} else {
				player.sendMessage(block.getType().toString());
			}
		}
	}

	public void startTimeCheck() {
		getServer().getScheduler().scheduleAsyncRepeatingTask(this,
				new Runnable() {
					public void run() {
						// getServer().broadcastMessage(Boolean.toString(switched_sl_state));
						// World world =
						// Bukkit.getWorlds().get(getConfig().getInt("World"));
						for (World world : Bukkit.getWorlds()) {
							int worldid = 0;
							for (int a = 0; a < getServer().getWorlds().size(); a++) {
								if (world == getServer().getWorlds().get(a)) {
									worldid = a;
								}
							}

							for (World wereld : Bukkit.getWorlds()) {
								if (!World_Light_Status.containsKey(wereld)) {
									if (wereld.getTime() > config_on_time) {
										World_Light_Status.put(wereld, true);
									}
									if (wereld.getTime() > config_off_time
											&& wereld.getTime() < config_on_time) {
										World_Light_Status.put(wereld, false);
									}
								}
							}

							if (world.getTime() > config_on_time
									&& World_Light_Status.get(world) == false) {
								World_Light_Status.put(world, true);
								togglelights(false, worldid);
							}
							if (world.getTime() > config_off_time
									&& world.getTime() < config_on_time
									&& World_Light_Status.get(world) == true) {
								World_Light_Status.put(world, false);
								togglelights(false, worldid);
							}
						}
					}
				}, 0L, 60L);
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("sl")) {
			if (player == null) {
				sender.sendMessage("This command can only be run by a player");
			} else {
				if (args.length > 0) {
					if (args[0].equals("info")) {
						if (player.hasPermission("streetlights.info")) {
							player.sendMessage("You have the permission");
						}
						return true;
					}
					if (args[0].equals("reload")) {
						if (player.hasPermission("streetlights.reload")) {
							this.reloadConfig();

							onstate_mats.clear();
							offstate_mats.clear();
							pendingBlocks.clear();
							
							for (String string : getConfig().getStringList(
									"materials")) {
								onstate_mats.add(Material.getMaterial(string
										.split(",")[0]));
							}
							for (String string : getConfig().getStringList(
									"materials")) {
								offstate_mats.add(Material.getMaterial(string
										.split(",")[1]));
							}
							for (String string : getConfig().getStringList(
									"lights")) {
								pendingBlocks.add(string.split(",", 6));
							}
							
							config_on_time = getConfig().getInt("Time_on");
							config_off_time = getConfig().getInt("Time_off");
							config_use_rain = getConfig().getBoolean("On_when_rain");

							player.sendMessage(ChatColor.RED
									+ "Config Reloaded!");
						}
						return true;
					}
					if (args[0].equals("save")) {
						if (player.hasPermission("streetlights.save")) {
							String[] listOfStrings = new String[pendingBlocks
									.size()];
							Integer count = 0;
							for (String[] array : pendingBlocks) {
								listOfStrings[count] = array[0] + ","
										+ array[1] + "," + array[2] + ","
										+ array[3] + "," + array[4] + ","
										+ array[5];
								count++;
							}
							this.getConfig().set("lights",
									Arrays.asList(listOfStrings));
							saveConfig();
							player.sendMessage("Saved!");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equals("list")) {
						if (player.hasPermission("streetlights.list")) {
							for (int i = 0; i < pendingBlocks.size(); i++) {
								String[] element = pendingBlocks.get(i);
								player.sendMessage("Loc: " + element[1] + ","
										+ element[2] + "," + element[3]);
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equals("clear")) {
						if (player.hasPermission("streetlights.clear")) {
							pendingBlocks.clear();
							player.sendMessage("List is now cleared!");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equals("on")) {
						if (player.hasPermission("streetlights.update")) {
							int worldid = 0;
							for (int a = 0; a < getServer().getWorlds().size(); a++) {
								if (player.getWorld() == getServer()
										.getWorlds().get(a)) {
									worldid = a;
								}
							}
							togglelights(true, worldid);
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equals("off")) {
						if (player.hasPermission("streetlights.update")) {
							int worldid = 0;
							for (int a = 0; a < getServer().getWorlds().size(); a++) {
								if (player.getWorld() == getServer()
										.getWorlds().get(a)) {
									worldid = a;
								}
							}
							togglelights(false, worldid);
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have the permission to do this.");
						}
						return true;
					}
					if (args[0].equals("edit")) {
						if (player.hasPermission("streetlights.edit")) {
							if(edit == false) {
								edit = true;
								player.sendMessage("You can now add lights to the list!");
							}else{
								edit = false;
								player.sendMessage("You're done editing now!");
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "You don't have the permission to do this.");
						}
						return true;
					}
				}
			}
		}
		return false;
	}

}