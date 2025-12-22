// Copyright (c) 2025 Bruno
package me.thebrunorm.skywars.menus;

import com.cryptomorin.xseries.XMaterial;
import me.thebrunorm.skywars.Skywars;
import me.thebrunorm.skywars.managers.ArenaManager;
import me.thebrunorm.skywars.singletons.InventoryUtils;
import me.thebrunorm.skywars.singletons.MessageUtils;
import me.thebrunorm.skywars.singletons.SkywarsUtils;
import me.thebrunorm.skywars.structures.Arena;
import me.thebrunorm.skywars.structures.SkywarsMap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ConfigMenu implements Listener {

	public final static HashMap<Player, Location> playerLocations = new HashMap<>();
	public final static HashMap<Player, Arena> currentArenas = new HashMap<>();
	final static String teamSizeName = Messager.getMessage("CONFIG_MENU_TEAM_SIZE");
	final static String positionName = Messager.getMessage("CONFIG_MENU_POSITION");
	final static String spawnName = Messager.getMessage("CONFIG_MENU_SPAWN_SETUP");
	final static String worldFolderName = Messager.getMessage("CONFIG_MENU_WORLD");
	final static String statusName = Messager.getMessage("CONFIG_MENU_STATUS");
	final static String calculateSpawnsName = Messager.getMessage("CONFIG_MENU_CALCULATE_SPAWNS");
	final static String regenerateCasesName = Messager.getMessage("CONFIG_MENU_REGENERATE_CASES");
	final static String reloadWorld = Messager.getMessage("CONFIG_MENU_WORLD_RELOAD");
	final static String saveWorld = Messager.getMessage("CONFIG_MENU_WORLD_SAVE");
	final static String clearName = Messager.getMessage("CONFIG_MENU_CLEAR");
	final static String teleportName = Messager.getMessage("CONFIG_MENU_TELEPORT");
	final static String chestsName = Messager.getMessage("CONFIG_MENU_FILL_CHESTS");
	final File worldsFolder = new File(Skywars.worldsPath);

	static void OpenWorldsMenu(Player player) {
		final File folder = new File(Skywars.get().getDataFolder() + "/worlds");
		final Inventory inventory = Bukkit.createInventory(null, 9 * 6, Messager.color("&aWorld folders"));

		int index = 10;
		for (final File worldFolder : Objects.requireNonNull(folder.listFiles())) {
			final List<String> lore = new ArrayList<>();

			boolean alreadyUsing = false;
			for (final SkywarsMap map : Skywars.get().getMapManager().getMaps()) {
				final String worldName = map.getWorldName();
				if (worldName != null && worldName.equals(worldFolder.getName())) {
					if (map == currentArenas.get(player).getMap()) {
						lore.add(Messager.color("&6Current world folder", map.getName()));
					} else {
						lore.add(Messager.color("&cWarning! %s already uses this world folder", map.getName()));
					}
					alreadyUsing = true;
					break;
				}
			}

			if (!alreadyUsing)
				lore.add(Messager.color("&eClick to select this file"));

			final ItemStack item = new ItemStack(Objects.requireNonNull(XMaterial.PAPER.parseItem()));
			final ItemMeta meta = item.getItemMeta();

			meta.setDisplayName(Messager.color("&a%s", worldFolder.getName()));
			meta.setLore(lore);
			item.setItemMeta(meta);
			inventory.setItem(index, item);
			index++;
		}

		player.openInventory(inventory);
		PlayerInventoryManager.setMenu(player, MenuType.MAP_SCHEMATIC);
	}

	static void addItemToInventory(Inventory inv, Material mat, int slot, String name, String... loreLines) {
		final ItemStack item = new ItemStack(mat);
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(MessageUtils.color(name));
		final List<String> lore = new ArrayList<>();
		for (final String line : loreLines)
			lore.add(MessageUtils.color("&e" + line));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(slot, item);
	}

	public static void OpenConfigurationMenu(Player player, SkywarsMap map) {
		currentArenas.put(player, ArenaManager.getArenaByMap(map, true));
		final Inventory inventory = Bukkit.createInventory(null, 9 * 3, MessageUtils.color("&a&l" + map.getName()));
		player.openInventory(inventory);
		PlayerInventoryManager.setMenu(player, MenuType.MAP_CONFIGURATION);
		UpdateInventory(player);
	}

	static void UpdateInventory(Player player) {
		final InventoryView openInv = player.getOpenInventory();
		if (openInv == null)
			return;
		final Inventory inventory = openInv.getTopInventory();
		if (inventory == null)
			return;
		final Arena currentArena = currentArenas.get(player);
		if (currentArena == null)
			return;
		final SkywarsMap currentMap = currentArena.getMap();

		InventoryUtils.addItem(inventory, XMaterial.SADDLE.parseMaterial(), 11,
			Messager.color(teamSizeName, currentMap.getTeamSize()),
			Messager.getMessage("CONFIG_MENU_LEFT_CLICK_ADD"),
			Messager.getMessage("CONFIG_MENU_RIGHT_CLICK_REMOVE"));

		String currentWorldFile = currentMap.getWorldName();
		if (currentWorldFile == null)
			currentWorldFile = "none";

		InventoryUtils.addItem(inventory, XMaterial.PAPER.parseMaterial(), 14,
				Messager.color(worldFolderName, currentWorldFile));

		InventoryUtils.addItem(inventory, XMaterial.GLASS.parseMaterial(), 15,
				Messager.color(statusName, "&6&lYES"));

		final List<String> spawnLore = new ArrayList<>();
		spawnLore.add(Messager.color("&eWhen you enter &bSpawn Setup Mode&e,"));
		spawnLore.add(Messager.color("&eyou can click blocks on the arena"));
		spawnLore.add(Messager.color("&eto set spawns easily."));
		if (!currentMap.getSpawns().isEmpty()) {
			spawnLore.add(Messager.color(""));
			spawnLore.add(Messager.color("&cThis will delete all current spawns."));
		}

		InventoryUtils.addItem(inventory, XMaterial.BLAZE_ROD.parseMaterial(), 16, spawnName,
				spawnLore.toArray(new String[0]));

		InventoryUtils.addItem(inventory, XMaterial.GLASS.parseMaterial(), 18, regenerateCasesName);

		InventoryUtils.addItem(inventory, XMaterial.BEACON.parseMaterial(), 19, calculateSpawnsName,
				"&cThis will override current spawns.");

		InventoryUtils.addItem(inventory, XMaterial.WOODEN_AXE.parseMaterial(), 20, pasteSchematicName,
				"&cThis will regenerate the map.");

		InventoryUtils.addItem(inventory, XMaterial.BARRIER.parseMaterial(), 21, clearName);

		InventoryUtils.addItem(inventory, XMaterial.COMPASS.parseMaterial(), 22, teleportName);

		InventoryUtils.addItem(inventory, XMaterial.CHEST.parseMaterial(), 23, chestsName);

	}

	public static void OpenConfigurationMenu(Player player, SkywarsMap map) {
		currentArenas.put(player, ArenaManager.getArenaByMap(map, true));
		final Inventory inventory = Bukkit.createInventory(null, 9 * 3, Messager.color("&a&l" + map.getName()));
		player.openInventory(inventory);
		PlayerInventoryManager.setMenu(player, MenuType.MAP_CONFIGURATION);
		UpdateInventory(player);
	}

	static String locationName(Location location) {
		if (location == null)
			return Messager.color(positionName, "none");
		final String positionString = String.format("%s, %s, %s", (double) location.getBlockX(),
			(double) location.getBlockY(), (double) location.getBlockZ());
		return Messager.color(positionName, positionString);
	}

	@EventHandler
	void onClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		final MenuType currentMenu = PlayerInventoryManager.getCurrentMenu(player);
		if (currentMenu != MenuType.MAP_CONFIGURATION && currentMenu != MenuType.MAP_CONFIG_WORLD_SELECTION)
			return;
		event.setCancelled(true);
		final ItemStack clicked = event.getCurrentItem();
		if (clicked == null || clicked.getItemMeta() == null)
			return;
		final String name = clicked.getItemMeta().getDisplayName();

		Arena currentArena = currentArenas.get(player);
		if (currentArena == null)
			return;

		final SkywarsMap currentMap = currentArena.getMap();

		if (name.equals(MessageUtils.color(teamSizeName, currentMap.getTeamSize()))) {
			int n = currentMap.getTeamSize() + (event.getClick() == ClickType.LEFT ? 1:-1);
			n = Math.max(n, 0);
			currentMap.setTeamSize(n);
			currentMap.saveParametersInConfig();
			currentMap.saveConfig();
		}

		if (name.equals(MessageUtils.color(calculateSpawnsName))) {
			if (!currentMap.calculateSpawns())
				player.sendMessage(MessageUtils.color("&cNo beacons found in the arena. Nothing changed."));
			else player.sendMessage(MessageUtils.color(
					"&aSuccessfully &bcalculated &aand &bsaved &6%s spawns&a.",
					currentMap.getSpawns().size()));
			return;
		}

		if (name.equals(MessageUtils.color(regenerateCasesName))) {
			currentArena.resetCases();
			if (currentMap.getSpawns().size() <= 0)
				player.sendMessage(Messager.getMessage("WARNING_NO_SPAWNS_TO_CREATE_CASES"));
			player.sendMessage(Messager.getMessage("REGENERATED_CASES_FOR_SPAWNS", currentMap.getSpawns().size()));
			return;
		}

		if (name.equals(MessageUtils.color(saveWorld))) {
			Path worldsPath = Skywars.get().getDataFolder().toPath().resolve("worlds").toAbsolutePath().normalize();
			Path newFolder = worldsPath.resolve(currentMap.getName()).normalize();
			if (newFolder.toFile().exists()) {
				String backupWorldName = currentMap.getName() + "_old" + System.currentTimeMillis();
				Path oldWorldsPath = Skywars.get().getDataFolder().toPath().resolve("old_worlds").toAbsolutePath().normalize();
				if (!oldWorldsPath.toFile().exists())
					if (!oldWorldsPath.toFile().mkdirs())
						player.sendMessage("Could not create old_worlds directory: " + oldWorldsPath.toAbsolutePath());
				Path worldBackup = oldWorldsPath.resolve(backupWorldName).toAbsolutePath().normalize();
				try {
					Files.move(newFolder, worldBackup, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					player.sendMessage("Error moving world folder: " + e.getMessage());
					player.sendMessage("\nCould not move world\nFrom: "
							+ newFolder + "\nTo: " + worldBackup);
				}
			}

			World currentWorld = currentArena.getWorld();

			player.sendMessage("Teleporting any players inside the world outside of it...");
			for (final Player p : currentWorld.getPlayers())
				SkywarsUtils.teleportPlayerLobbyOrLastLocation(p, true);

			currentWorld.save();
			player.sendMessage("Saved the world for arena: " + currentMap.getName());

			/*
			if (!Bukkit.unloadWorld(currentWorld, true)) {
				player.sendMessage("Could not unload world :(");
				return;
			}
			player.sendMessage("Unloaded the world for arena: " + currentMap.getName());
			*/

			File worldFolder = currentWorld.getWorldFolder();
			Path worldPath = Paths.get(worldFolder.getAbsolutePath()).normalize();

			if (!worldFolder.isDirectory()) {
				player.sendMessage("The world folder does not exist or is not a directory: " + worldPath);
				return;
			}
			Location loc = ConfigurationUtils.getLocationConfig(arenaWorld,
					currentArena.getMap().getConfig().getConfigurationSection("center"));
			if (loc == null)
				loc = currentArena.getWorld().getSpawnLocation();
			if (loc == null)
				player.sendMessage(Messager.getMessage("LOCATION_NOT_SET"));
			else {
				player.teleport(loc);
				player.sendMessage(Messager.getMessage("TELEPORTED_SUCCESSFULLY"));

			try {
				Files.copy(worldPath, newFolder, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				player.sendMessage("Error moving world folder: " + e.getMessage());
				player.sendMessage("\nCould not move world\nFrom: "
						+ worldPath + "\nTo: " + newFolder);
			}

			return;
		}

		if (name.equals(MessageUtils.color(teleportName))) {
			currentArena.goBackToCenter(player);
			player.setGameMode(GameMode.CREATIVE);
			return;
		}

		if (name.equals(Messager.color(chestsName))) {
			currentArena.fillChests();
			player.sendMessage(Messager.getMessage("CHESTS_FILLED_NAMED",
				currentArena.getActiveChests().size(), currentArena.getMap().getName()));
			return;
		}

		if (name.equals(Messager.color(clearName))) {
			ArenaManager.removeArena(currentArena);
			currentArenas.remove(player);
			currentArena = null;
			player.sendMessage(Messager.getMessage("CONFIG_MENU_CLEARED"));
			player.closeInventory();
		}

		String currentWorldName = currentMap.getWorldName();
		if (currentWorldName == null)
			currentWorldName = "none";

		if (name.equals(MessageUtils.color(worldFolderName, currentWorldName))) {
			if (this.worldsFolder.exists() && this.worldsFolder.listFiles().length <= 0) {
				player.closeInventory();
				player.sendMessage(Messager.getMessage("NO_WORLD_FOLDERS_AVAILABLE"));
				player.sendMessage(Messager.getMessage("NEED_TO_PUT_SCHEMATICS_IN_FOLDER"));
			} else
				OpenWorldsMenu(player);
			return;
		}

		final String worldFolderName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
		for (final File worldFolder : this.worldsFolder.listFiles()) {
			Skywars.get().sendDebugMessage("current file: " + worldFolder.getName());
			if (worldFolder.getName().equals(worldFolderName)) {
				currentMap.setWorldName(worldFolderName);
				currentMap.saveParametersInConfig();
				currentMap.saveConfig();
				player.sendMessage(MessageUtils.getMessage("SET_UP_WORLD_NAMED", currentMap.getWorldName()));
				break;
			}
		}

		UpdateInventory(player);
	}

	static void OpenWorldsMenu(Player player) {
		final File folder = new File(Skywars.get().getDataFolder() + "/worlds");
		final Inventory inventory = Bukkit.createInventory(null, 9 * 6, MessageUtils.color("&aWorld folders"));

		int index = 10;
		for (final File worldFolder : Objects.requireNonNull(folder.listFiles())) {
			final List<String> lore = new ArrayList<>();

			boolean alreadyUsing = false;
			for (final SkywarsMap map : Skywars.get().getMapManager().getMaps()) {
				final String worldName = map.getWorldName();
				if (worldName != null && worldName.equals(worldFolder.getName())) {
					if (map == currentArenas.get(player).getMap()) {
						lore.add(MessageUtils.color("&6Current world folder", map.getName()));
					} else {
						lore.add(MessageUtils.color("&cWarning! %s already uses this world folder", map.getName()));
					}
					alreadyUsing = true;
					break;
				}
			}

			if (!alreadyUsing)
				lore.add(MessageUtils.color("&eClick to select this file"));

			final ItemStack item = new ItemStack(Objects.requireNonNull(XMaterial.PAPER.parseItem()));
			final ItemMeta meta = item.getItemMeta();

			meta.setDisplayName(MessageUtils.color("&a%s", worldFolder.getName()));
			meta.setLore(lore);
			item.setItemMeta(meta);
			inventory.setItem(index, item);
			index++;
		}

		player.openInventory(inventory);
		PlayerInventoryManager.setMenu(player, MenuType.MAP_CONFIG_WORLD_SELECTION);
	}

}
