package me.asofold.bpl.painlesspvp.permissions;

/**
 * Note that there are sub permissions like KEEP_INVENTORY + "w.(WORLDNAME)" or ... + "r.(REGIONNAME) or ... + "w.(WORLDNAME).r.(REGIONNAME)".
 * @author mc_dev
 *
 */
public class Permissions {
	private static final String ROOT = "painlesspvp";
	
	private static final String KEEP = ROOT + ".keep";
	
	public static final String KEEP_INVENTORY = KEEP + ".inventory";
 
	/**
	 * Keep inventory on any defined region.
	 */
	public static final String KEEP_INVENTORY_REGION = KEEP_INVENTORY + ".region";
	
	
	public static final String KEEP_INVENTORY_REGION_MEMBER = KEEP_INVENTORY_REGION + ".member";
	public static final String KEEP_INVENTORY_REGION_OWNER = KEEP_INVENTORY_REGION + ".owner";
}
