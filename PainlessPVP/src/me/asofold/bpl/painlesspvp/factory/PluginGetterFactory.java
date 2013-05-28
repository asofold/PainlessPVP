package me.asofold.bpl.painlesspvp.factory;

import java.util.Map;

import me.asofold.bpl.painlesspvp.plshared.plugins.PluginGetter;

import org.bukkit.plugin.Plugin;

import com.gmail.nossr50.mcMMO;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Set up PluginGetter instances for dependencies and register as listeners with given plugin.
 * @author mc_dev
 *
 */
public class PluginGetterFactory {
	public void updateAvailableFactories(Map<Class<? extends Plugin>, PluginGetter<? extends Plugin>> pluginGetters, final Plugin plugin){
		try{
			pluginGetters.put(WorldGuardPlugin.class, new PluginGetter<WorldGuardPlugin>("WorldGuard").registerEvents(plugin));
		} catch (Throwable t){}
		try{
			pluginGetters.put(mcMMO.class, new PluginGetter<WorldGuardPlugin>("mcMMO").registerEvents(plugin));
		} catch (Throwable t){}
	}
}
