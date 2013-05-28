package me.asofold.bpl.painlesspvp.inventory.hooks.mcmmo;

import me.asofold.bpl.painlesspvp.inventory.KeepInventory;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.nossr50.events.skills.unarmed.McMMOPlayerDisarmEvent;

/**
 * Prevent disarmament if the disarmed player would keep inventory on death.
 * @author mc_dev
 *
 */
public class KeepInventoryListener implements Listener{
	
	private final KeepInventory keepInventory;

	public KeepInventoryListener(KeepInventory keepInventory){
		this.keepInventory = keepInventory;
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public final void onDisarm(final McMMOPlayerDisarmEvent event){
		final Player disarmed = event.getPlayer();
		final Location loc = disarmed.getLocation();
		if (keepInventory.wouldKeepInventory(disarmed, loc)){
			event.setCancelled(true);
		}
	}
}
