package me.asofold.bpl.painlesspvp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import me.asofold.bpl.painlesspvp.factory.PluginGetterFactory;
import me.asofold.bpl.painlesspvp.inventory.KeepInventory;
import me.asofold.bpl.painlesspvp.inventory.filter.worldguard.WorldGuardFilter;
import me.asofold.bpl.painlesspvp.inventory.hooks.mcmmo.KeepInventoryListener;
import me.asofold.bpl.painlesspvp.inventory.hooks.mcmmo.WGListener;
import me.asofold.bpl.painlesspvp.plshared.plugins.PluginGetter;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.mcMMO;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Quick detach from server core: Keep inventory+exp based on permissions, also region-dependent permissions for WorldGuard (direct, members, owners).
 * @author mc_dev
 *
 */
public class PainlessPVP extends JavaPlugin {
	
	private final KeepInventory keepInventory = new KeepInventory(this);
	
	private final Map<Class<? extends Plugin>, PluginGetter<? extends Plugin>> pluginGetters = new HashMap<Class<? extends Plugin>, PluginGetter<? extends Plugin>>();
	
	public KeepInventory getKeepInventory(){
		return keepInventory;
	}

	@Override
	public void onDisable() {
		// Cleanup.
		keepInventory.clearLocationFilters();
		pluginGetters.clear();
		// Done.
		getLogger().info(getDescription().getFullName() + " is disabled.");
	}

	@Override
	public void onEnable() {
		final Logger logger = getLogger();
		final PluginManager pm = getServer().getPluginManager();
		
		// Add core listeners.
		pm.registerEvents(keepInventory, this);
		
		// Set up plugin getters.
		new PluginGetterFactory().updateAvailableFactories(pluginGetters, this);
		
		// Add dependency stuff.
		try{
			addFeatures_WorldGuard(pm, logger);
		} catch (Throwable t){t.printStackTrace();}
		try{
			addFeatures_mcMMO(pm, logger);
		} catch (Throwable t){t.printStackTrace();}
		
		// Done.
		logger.info(getDescription().getFullName() + " is enabled.");
	}

	private void addFeatures_mcMMO(PluginManager pm, Logger logger) {
		PluginGetter<mcMMO> getter_mcMMO = getPluginGetter(mcMMO.class);
		if (getter_mcMMO == null) return;
		try{
			pm.registerEvents(new KeepInventoryListener(keepInventory), this);
			logger.info("Added abuse prevention for mcMMO + keeping inventory.");
		} catch (Throwable t){}
	}

	private void addFeatures_WorldGuard(PluginManager pm, Logger logger) {
		PluginGetter<WorldGuardPlugin> getter_WorldGuard = getPluginGetter(WorldGuardPlugin.class);
		
		// Location filter.
		keepInventory.addLocationFilter(new WorldGuardFilter(getter_WorldGuard));
		logger.info("Added location filter for WorldGuard.");
		
		// Dependencies with more plugins.
		try{
			addFeatures_WorldGuard_mcMMO(pm, logger);
		} catch(Throwable t){}
	}
	
	private void addFeatures_WorldGuard_mcMMO(PluginManager pm, Logger logger) {
		PluginGetter<mcMMO> getter_mcMMO = getPluginGetter(mcMMO.class);
		if (getter_mcMMO == null) return;
		PluginGetter<WorldGuardPlugin> getter_WorldGuard = getPluginGetter(WorldGuardPlugin.class);
		pm.registerEvents(new WGListener(getter_WorldGuard), this);
		logger.info("Added abuse prevention for mcMMO + WorldGuard.");
	}
	
	public boolean hasPluginGetter(String pluginName){
		for (final PluginGetter<? extends Plugin> getter : pluginGetters.values()){
			if (getter.pluginName.equals(pluginName)) return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Plugin> PluginGetter<T> getPluginGetter(Class<T> clazz){
		return (PluginGetter<T>) pluginGetters.get(clazz);
	}
	
}
