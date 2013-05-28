package me.asofold.bpl.painlesspvp.inventory.filter.worldguard;

import me.asofold.bpl.painlesspvp.inventory.filter.LocationFilter;
import me.asofold.bpl.painlesspvp.permissions.Permissions;
import me.asofold.bpl.painlesspvp.plshared.plugins.PluginGetter;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardFilter implements LocationFilter {
	
	private final PluginGetter<WorldGuardPlugin> wgGetter;

	public WorldGuardFilter(PluginGetter<WorldGuardPlugin> wgGetter){
		this.wgGetter = wgGetter;
	}

	@Override
	public boolean wouldKeepInventory(final Player player, final Location loc) {
		final World world = loc.getWorld();
		final ApplicableRegionSet set = wgGetter.getPlugin().getRegionManager(world).getApplicableRegions(loc);
		if (set.size() > 0 && player.hasPermission(Permissions.KEEP_INVENTORY_REGION)){
			return true;
		}
		final String lcwn = world.getName().toLowerCase();
		final String playerName = player.getName();
		for (final ProtectedRegion region : set){
			final String rid = region.getId().trim().toLowerCase();
			if (player.hasPermission(Permissions.KEEP_INVENTORY + ".w." + lcwn + ".r." + rid) || player.hasPermission(Permissions.KEEP_INVENTORY + ".r." + rid)){
				return true;
			}
			else if (region.isOwner(playerName) && player.hasPermission(Permissions.KEEP_INVENTORY_REGION_OWNER)){
				return true;
			}
			else if (region.isMember(playerName) && player.hasPermission(Permissions.KEEP_INVENTORY_REGION_MEMBER)){
				return true;
			}
		}
		return false;
	}
	
}
