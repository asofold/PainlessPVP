package me.asofold.bpl.painlesspvp.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.asofold.bpl.painlesspvp.inventory.filter.LocationFilter;
import me.asofold.bpl.painlesspvp.permissions.Permissions;
import me.asofold.bpl.painlesspvp.plshared.Blocks;
import me.asofold.bpl.painlesspvp.plshared.items.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class KeepInventory implements Listener{
	
	private static class GraceEntry{
		public long time;
		public double x, y, z;
		public String world;
		public GraceEntry(final long time, final Location loc){
			set(time, loc);
		}
		
		public void set(long time, Location loc) {
			this.time = time;
			this.x = loc.getX();
			this.y = loc.getY();
			this.z = loc.getZ();
			this.world = loc.getWorld().getName();
		}
		
		public boolean expires(final long time, final long durExpire, final Location loc, final double distExpire){
			if (time < this.time){
				// Inconsistencies.
				return true;
			}
			if (time > this.time + durExpire){
				return true;
			}
			if (loc != null){
				if (Math.abs(x - loc.getX()) > distExpire || Math.abs(y - loc.getY()) > distExpire || Math.abs(z - loc.getZ()) > distExpire || !loc.getWorld().getName().equals(world)){
					return true;
				}
			}
			return false;
		}
		
	}
	
	/** Plain prefix for inconsistency warning message, set to null to disable */
	private String logCommandPrefix = null;
	private boolean logToconsole = true;
	
	protected final Plugin plugin;
	
	public long durDamageGrace = 30000;
	public double distDamageGrace = 50.0;
	private final Map<String, GraceEntry> damageGrace = new LinkedHashMap<String, GraceEntry>(50);
	
	private final List<LocationFilter> locationFilters = new ArrayList<LocationFilter>();

	public KeepInventory(final Plugin plugin){
		this.plugin = plugin;
	}
	
	/**
	 * 
	 */
	private final Location useLoc = new Location(null, 0, 0, 0);
	
	/**
	 * Remove all expired by time.
	 */
	public void checkGraceMap(){
		final long time = System.currentTimeMillis();
		final Iterator<Entry<String, GraceEntry>> it = damageGrace.entrySet().iterator();
		while (it.hasNext()){
			final Entry<String, GraceEntry> mapEntry = it.next();
			final GraceEntry graceEntry = mapEntry.getValue();
			if (graceEntry.expires(time, durDamageGrace, null, distDamageGrace)){
				it.remove();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent event){
		final Player player = event.getPlayer();
		final String playerName = player.getName();
		final GraceEntry entry = damageGrace.get(playerName);
		if (entry == null) return;
		final Location to = event.getTo();
		if (to == null || entry.expires(System.currentTimeMillis(), durDamageGrace, player.getLocation(useLoc), distDamageGrace)){
			damageGrace.remove(playerName);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(final EntityDamageEvent event){
		final Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		final Player player = (Player) entity;
		final String playerName = player.getName();
		final GraceEntry entry = damageGrace.get(playerName);
		final Location loc = player.getLocation(useLoc);
		// TODO: Can this be optimized not to check every time ?
		if (wouldKeepInventory(player, loc)){
			final long time = System.currentTimeMillis();
			if (entry == null){
				if (damageGrace.size() > 100){
					// TODO: Make this relative to server size?
					checkGraceMap();
				}
				damageGrace.put(playerName, new GraceEntry(time, loc));
			}
			else{
				entry.set(time, loc);
			}
		}
		else if (entry != null){
			if (entry.expires(System.currentTimeMillis(), durDamageGrace, loc, distDamageGrace)){
				damageGrace.remove(playerName);
			}
		}
		useLoc.setWorld(null);
	}
	
	/**
	 * Handle keep-inventory.
	 * @param eEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(final EntityDeathEvent eEvent){
		if (!(eEvent instanceof PlayerDeathEvent)) return;
		final PlayerDeathEvent event = (PlayerDeathEvent) eEvent;
		final Player player = event.getEntity();
		
		final Collection<ItemStack> drops = event.getDrops();
		if (drops.isEmpty()){
			return;
		}
		
		final Location loc = player.getLocation(useLoc);
		
		final boolean keepInventory = checkGrace(player, loc) || wouldKeepInventory(player, loc);
		
		// Finally process it.
		if (keepInventory){
			// Keep exp.
			event.setKeepLevel(true);
			event.setDroppedExp(0);
			// Get contents.
			final PlayerInventory inv = player.getInventory();
			final ItemStack[] contents = inv.getContents();
			final ItemStack[] armorContents = inv.getArmorContents();
			// TODO: Check consistency with drops?
			// Clear drops.
			drops.clear();
			// Schedule task to reset inventory.
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					restoreInventory(player, loc, contents, armorContents);
				}
			});
			
			// TODO: Consider preventing pickup until restored.
			
		}
		useLoc.setWorld(null);
	}
	
	/**
	 * Check if player should keep inventory due to grace, removes grace entry (called on death).
	 * @param player
	 * @param loc
	 * @return If to keep inventory on death.
	 */
	private boolean checkGrace(final Player player, final Location loc) {
		final GraceEntry entry = damageGrace.remove(player.getName());
		return entry != null && !entry.expires(System.currentTimeMillis(), durDamageGrace, loc, distDamageGrace);
	}

	public boolean wouldKeepInventory(final Player player, final Location loc) {
		// General permission.
		if (player.hasPermission(Permissions.KEEP_INVENTORY)){
			return true;
		}
		
		else{
			// World permission.
			final World world = loc.getWorld();
			final String lcwn = world.getName().trim().toLowerCase();
			if (player.hasPermission(Permissions.KEEP_INVENTORY + ".w." + lcwn)){
				return true;
			}
			else{
				// Query available LocationFilter instances.
				for (int i = 0; i < locationFilters.size(); i++){
					if (locationFilters.get(i).wouldKeepInventory(player, loc)){
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void restoreInventory(final Player player, final Location loc, final ItemStack[] contents, final ItemStack[] armorContents)
	{
		boolean problem = !player.isOnline();
		try{
			final PlayerInventory inv = player.getInventory();
			// TODO: Consistency checks (drop already contained stacks)?
			try{
				dropAllItems(loc, inv);
			} catch (Throwable t){
				// Ignore.
				// TODO: Maybe log this too.
			}
			inv.setContents(contents);
			inv.setArmorContents(armorContents);
		} catch (Throwable t){
			problem = true;
		}
		
		if (problem && (logToconsole || logCommandPrefix != null)){
			final StringBuilder b = new StringBuilder(512);
			b.append("[DEATH]["+player.getName()+"]  At "+Blocks.blockString(loc) + ": " + "Might have failed to set inventory contents [DROPS] ");
			ItemUtil.addItemDescr(Arrays.asList(armorContents), b);
			ItemUtil.addItemDescr(Arrays.asList(contents), b);
			final String msg = b.toString();
			if (logToconsole){
				Bukkit.getLogger().warning(msg);
			}
			if (logCommandPrefix != null){
				final Server server = Bukkit.getServer();
				server.dispatchCommand(server.getConsoleSender(), logCommandPrefix + msg);
			}
		}
	}

	/**
	 * Just drop naturally at the location, no ItemDrop events.
	 * @param loc
	 * @param inv
	 */
	private void dropAllItems(final Location loc, final PlayerInventory inv) {
		dropAllItems(loc, inv.getContents());
		dropAllItems(loc, inv.getArmorContents());
		inv.clear();
		
	}

	private void dropAllItems(final Location loc, final ItemStack[] contents) {
		if (contents == null) return;
		final World world = loc.getWorld();
		for (int i = 0; i < contents.length; i++){
			final ItemStack stack = contents[i];
			if (stack != null && stack.getTypeId() != Material.AIR.getId()){
				world.dropItemNaturally(loc, stack);
			}
		}
	}
	
	public void setLogCommandPrefix(String prefix){
		this.logCommandPrefix = prefix;
	}
	
	public void setLogToConsole(boolean log){
		this.logToconsole = log;
	}
	
	/**
	 * All filters are removed in PainlessPVP.onDisable.
	 * @param filter
	 */
	public void addLocationFilter(LocationFilter filter){
		if (!locationFilters.contains(filter)) locationFilters.add(filter);
	}
	
	public void clearLocationFilters(){
		locationFilters.clear();
	}
}
