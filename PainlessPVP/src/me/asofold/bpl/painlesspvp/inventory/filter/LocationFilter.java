package me.asofold.bpl.painlesspvp.inventory.filter;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 
 * @author mc_dev
 *
 */
public interface LocationFilter {
	
	/**
	 * TODO: Switch to an ENUM (exp, other).
	 * @param player
	 * @param loc
	 * @return True if the player is to keep inventory at this location.
	 */
	public boolean wouldKeepInventory(final Player player, final Location loc);
}
