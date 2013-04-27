package com.magic_j.supertool;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.magic_j.pluginhelpers.RegionHelper;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SuperTool extends JavaPlugin implements Listener {

	@Override
    public void onEnable() {
		Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    if (wgPlugin != null || (wgPlugin instanceof WorldGuardPlugin)) {		        
	    	 RegionHelper.init((WorldGuardPlugin) wgPlugin);
	    }
	    getCommand("animalcounter").setExecutor(new AnimalcounterCommand());
	}
	
	@Override
    public void onDisable() {
		
	}
	
}
