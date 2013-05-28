package me.asofold.bpl.painlesspvp.inventory.hooks.mcmmo;

import me.asofold.bpl.painlesspvp.plshared.plugins.PluginGetter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.nossr50.events.skills.unarmed.McMMOPlayerDisarmEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

/**
 * Prevent disarmament on no-pvp regions.
 * @author mc_dev
 *
 */
public class WGListener implements Listener{
	
	private final PluginGetter<WorldGuardPlugin> wgGetter;

	public WGListener(PluginGetter<WorldGuardPlugin> wgGetter) {
		this.wgGetter = wgGetter;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public final void onDisarm(final McMMOPlayerDisarmEvent event){
		final Player disarmed = event.getPlayer();
		final Location loc = disarmed.getLocation();
		if (!wgGetter.getPlugin().getRegionManager(loc.getWorld()).getApplicableRegions(loc).allows(DefaultFlag.PVP)){
			event.setCancelled(true);
		}
	}
	
}
