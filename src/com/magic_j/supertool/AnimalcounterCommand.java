package com.magic_j.supertool;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.magic_j.pluginhelpers.RegionHelper;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AnimalcounterCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("animalcounter")) {
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Diese Befehle sind nur von Spielern verwendbar!");
			return false;				
		}
		
		Player currentPlayer = (Player)sender;
		int rgX1 = 0;			
		int rgX2 = 0;
		int rgZ1 = 0;
		int rgZ2 = 0;
		String rgName = "";
		if (currentPlayer.getWorld().getName().equals("minemap")) {
			int pX = currentPlayer.getLocation().getBlockX();
			int pZ = currentPlayer.getLocation().getBlockZ();
			rgX1 = pX-20;			
			rgX2 = pX+20;
			rgZ1 = pZ-20;
			rgZ2 = pZ+20;
			rgName = "minemap(40x40)";
		}
		else if (currentPlayer.getWorld().getName().equals("world")) {
						
			ProtectedRegion region = null;
			if (args.length > 0) {
				region = RegionHelper.getRegionByName(args[0], currentPlayer.getWorld());
				if (region == null) {
					sender.sendMessage(ChatColor.RED + "Diese Region wurde nicht gefunden!");
					return true;
				}
				rgName = args[0];
			}
			else {
				region = RegionHelper.getCurrentRegion(currentPlayer);				
				if (region == null) {
					sender.sendMessage(ChatColor.RED + "Du befindest dich in keiner Region!");
					return true;
				}
				rgName = region.getId();
			}
			List<BlockVector2D> borders = region.getPoints();
			
			rgX1 = borders.get(0).getBlockX();			
			rgX2 = borders.get(0).getBlockX();
			rgZ1 = borders.get(0).getBlockZ();
			rgZ2 = borders.get(0).getBlockZ();
			
			for (BlockVector2D blockPos : borders) {
				int x = blockPos.getBlockX();
				int z = blockPos.getBlockZ();
				rgX1 = Math.min(rgX1, x);
				rgX2 = Math.max(rgX2, x);
				rgZ1 = Math.min(rgZ1, z);
				rgZ2 = Math.max(rgZ2, z);
			}
			
					
		}
		else {
			sender.sendMessage(ChatColor.RED + "Dieser Befehl geht nur in der Hauptwelt und Minemap!");
			return true;			
		}
		
		int chunkX1 = (int) Math.floor(rgX1 / 16.0);
		int chunkZ1 = (int) Math.floor(rgZ1 / 16.0);
		int chunkX2 = (int) Math.floor(rgX2 / 16.0);
		int chunkZ2 = (int) Math.floor(rgZ2 / 16.0);
		
		int chunkCount = (chunkX2-chunkX1+1) * (chunkZ2-chunkZ1+1);
		
		if (chunkCount > 100) {
			sender.sendMessage(ChatColor.RED + "Diese Region umfasst zuviele  Chunks! (" + chunkCount + ")");
			return true;
		}
		
		//sender.sendMessage("Die Tiere in den Chunks "+chunkX1+":"+chunkZ1+" bis "+chunkX2+":"+chunkZ2+" werden gezählt...");
		World world = currentPlayer.getWorld();
		
		// Ermitteln der Chunk Entities
		List<Entity> entities = new ArrayList<Entity>();
		Entity[] chunkEntities;
		int unloadedChunks = 0;
		for (int cx = chunkX1; cx <= chunkX2; cx++) {
			for (int cz = chunkZ1; cz <= chunkZ2; cz++) {
				Chunk chunk = world.getChunkAt(cx, cz);
				if (!chunk.isLoaded()) {
					unloadedChunks++;
					continue;
				}
				chunkEntities = chunk.getEntities();				
				for (Entity entity : chunkEntities) {
					entities.add(entity);
				}
			}
		}
		int animalCounts[] = new int[EntityType.values().length];
		for (int i = 0; i < animalCounts.length; i++) {
			animalCounts[i] = 0;
		}
		int animalCount = 0;
		// Entities beschränken auf Regiongrenze und Type
		for (Entity entity : entities) {
			int xPos = entity.getLocation().getBlockX();
			int zPos = entity.getLocation().getBlockZ();
			
			// im geschnittenen Chunk aber nichtmehr in Region
			if (xPos < rgX1 || xPos > rgX2 || zPos < rgZ1 || zPos > rgZ2) continue;
			// unbekannte überspringen
			if (!isAnimal(entity.getType())) continue;
			
			int typeId = entity.getType().ordinal();				
			animalCounts[typeId]++;
			animalCount++;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Region " +ChatColor.RED+ rgName +ChatColor.YELLOW+ " ["+rgX1+":"+rgZ1+" / "+rgX2+":"+rgZ2+"]");
		sender.sendMessage(ChatColor.YELLOW + " in den Chunks ["+chunkX1+":"+chunkZ1+" / "+chunkX2+":"+chunkZ2+"]");
		if (unloadedChunks > 0) {
			sender.sendMessage(ChatColor.YELLOW + " davon sind " + ChatColor.GOLD + unloadedChunks + ChatColor.YELLOW + " Chunks nicht geladen!");
		}
		sender.sendMessage(ChatColor.YELLOW + " enthält "+ChatColor.WHITE+animalCount+ChatColor.YELLOW+" Tiere");
		for (int i = 0; i < animalCounts.length; i++) {
			if (animalCounts[i] > 0) {
				EntityType type = EntityType.values()[i];
				
				sender.sendMessage(ChatColor.GRAY + " - " + type.getName() + ": " + animalCounts[i]);
			}				
		}		
		return true;
	}

	private boolean isAnimal(EntityType type) {
		switch (type) {
			case BAT:
			case BLAZE:
			case CAVE_SPIDER:
			case CHICKEN:
			case COW:
			case CREEPER:
			case ENDERMAN:
			case ENDER_DRAGON:
			case GHAST:
			case GIANT:
			case IRON_GOLEM:
			case MUSHROOM_COW:
			case OCELOT:
			case PIG:
			case PIG_ZOMBIE:
			case SHEEP:
			case SILVERFISH:
			case SKELETON:
			case SLIME:
			case SNOWMAN:
			case SPIDER:
			case SQUID:
			case WEATHER:
			case WITHER:
			case WOLF:
			case ZOMBIE:
				return true;
			default:
				break;		
		}
		
		
		return false;
	}

}
