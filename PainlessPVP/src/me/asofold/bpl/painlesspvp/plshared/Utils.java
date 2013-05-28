package me.asofold.bpl.painlesspvp.plshared;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


/**
 * Miscellaneous methods for static access.
 * This will likely get refactored to other parts of shared and marked as deprecated.
 * @author mc_dev
 *
 */
public class Utils {
	
	public static final long msSec = 1000;
	public static final long msMin = 60*msSec;
	public static final long msHour = 60*msMin;
	public static final long msDay = 24*msHour;
	public static final long msMonth = 31*msDay;
	public static final long msYear = 365*msDay;
	
	public static DecimalFormat zfill2 = new DecimalFormat();
	
	static{
		zfill2.setMinimumIntegerDigits(2);
	}
	
	public static long parseSimpleTime(String s) {
		return parseSimpleTime(s, 0L);
	}
	
	/**
	 * Parse time from multiple entries.
	 * @param args
	 * @param startIndex
	 * @return
	 */
	public static long parseSimpleTime(String[] args, int startIndex){
		long time = 0;
		for (int i = startIndex; i < args.length; i++){
			time += parseSimpleTime(args[i], 0);
		}
		return time;
	}

	public static long parseSimpleTime(String s, long presetBase) {
		try{
			return presetBase * Long.parseLong(s);
		} catch(NumberFormatException e){
			
		}
		s = s.toLowerCase();
		long base = 0;
		if ( s.endsWith("y")) base = msYear;
		else if ( s.endsWith("d")) base = msDay;
		else if (s.endsWith("h")) base = msHour;
		else if (s.endsWith("m")) base = msMin;
		else if ( s.endsWith("s")) base = msSec;
		else throw new NumberFormatException("Need time specification ending with y, d, h, m or s!");
		long num = Long.parseLong(s.substring(0,s.length()-1));
		return num*base;
	}

	/**
	 * converts time (in milliseconds) to human-readable format
	 *  "<dd:>hh:mm:ss"
	 */
	public static String millisToShortDHMS(long duration) {
	  String res = "";
	  long days  = TimeUnit.MILLISECONDS.toDays(duration);
	  long hours = TimeUnit.MILLISECONDS.toHours(duration)
	                 - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
	  long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
	                   - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
	  long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
	                 - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
	  if (days == 0) {
	    res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	  }
	  else {
	    res = String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
	  }
	  return res;
	}

	public static com.sk89q.worldguard.bukkit.WorldGuardPlugin getWorldGuard(){
		Plugin plg = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if (plg == null) return null;
		try{
			return (com.sk89q.worldguard.bukkit.WorldGuardPlugin) plg;
		} catch (ClassCastException e){
			return null;
		}
	}

	public static void addFakeLightBlocks(Player player, boolean minimal, Map<Material, Material> fakeLightReplacements){
		// add torches at player position:
		Location loc = player.getLocation().clone();
		Location loc2 = loc.clone();
		loc2.setY(loc.getY()+1.0);
		Location [] pLocs = new Location[]{loc,loc2};
		Collection<Location> locs = new LinkedList<Location>();
		// add further blocks around player.
		for ( Location cloc : pLocs){
			Block block = cloc.getBlock();
			if (block.getLightLevel()<10){
				double x = cloc.getX();
				double y = cloc.getY();
				double z = cloc.getZ();
				double[] increments = new double[]{-1,0, 1};
				for (double dy : increments){
					for ( double dx : increments){
						for ( double dz : increments){
							if ( Math.abs(dx)+Math.abs(dy)+Math.abs(dz) <= 1){
								loc2 = cloc.clone();
								loc2.setX(x+dx);
								loc2.setY(y+dy);
								loc2.setZ(z+dz);
								locs.add(loc2);
							}
						}
					}
				}
			}
		}
		for ( Location l : locs ){
			Block block = l.getBlock();
			if ( block.getLightLevel()<10){
				Material replacement = fakeLightReplacements.get(block.getType());
				if ( replacement != null){
					player.sendBlockChange(l, replacement, (byte) 0);
					if ( minimal ) return;
				}
			} else if (minimal) return;
		}
		for ( Location lc : pLocs){
			Block block = lc.getBlock();
			if ( block.getLightLevel()<10 ){
				player.sendBlockChange(lc, Material.TORCH, (byte) 0);
			} else if (minimal) return;
		}
	}

	public static void logSevere(String action, Throwable t){
		Logger logger = Bukkit.getServer().getLogger();
		logger.severe("Failed ("+action+"): "+t.getMessage());
		t.printStackTrace();
	}
	
	/**
	 * Simply return a capitalized version of the string.
	 * @param input
	 * @return
	 */
	public final static String capitalize(String input){
		int l = input.length();
		if (l == 0) return input;
		else if ( l ==1 )return input.toUpperCase();
		else return input.substring(0 , 1).toUpperCase() + input.substring(1).toLowerCase();
	}

	public static String timeStr(long ts) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
		cal.setTimeInMillis(ts);
		return cal.get(Calendar.YEAR)+"-"+zfill2.format(cal.get(Calendar.MONTH)+1)+"-"+zfill2.format(cal.get(Calendar.DAY_OF_MONTH))+"-"+zfill2.format(cal.get(Calendar.HOUR_OF_DAY))+":"+zfill2.format(cal.get(Calendar.MINUTE))+":"+zfill2.format(cal.get(Calendar.SECOND));
	}

	/**
	 * Get a string (minimal) with days, hours, minutes for given minutes.
	 * @param minutes
	 * @return
	 */
	public static String minutesInDays(int minutes){
		String out = "";
		if ( minutes >= 1440){
			// add days
			int days = minutes / 1440;
			minutes -= days*1440;
			if ( days >1 )	out += days+" days";
			else out += "1 day"; 
		}
		if ( minutes >= 60 ){
			if ( !out.isEmpty()) out+=" ";
			// add hours
			int hours = minutes / 60;
			minutes -= hours*60;
			if ( hours >1 )	out += hours+" hours";
			else out += "1 hour"; 
		}
		if ( minutes > 0){
			if ( !out.isEmpty()) out+=" ";
			// add minutes
			if ( minutes >1 )	out += minutes+" minutes";
			else out += "1 minute"; 
		}
		return out;
	}
	
	
	
	// ######## *** DEPRECATION CANDIDATES BELOW *** ########
	
	/**
	 * 
	 * @param parts
	 * @param link can be null
	 * @return
	 */
	public static final String join(final Collection<String> parts, final String link){
		final StringBuilder builder = new StringBuilder();
		int i = 0;
		final int max = parts.size();
		for (final String part : parts){
			builder.append(part);
			i++;
			if ( i<max && link!=null ) builder.append(link);
		}
		return builder.toString();
	}
	
	   /**
     * 
     * @param parts
     * @param link can be null
     * @return
     */
    public static final String joinObjects(final Collection<?> parts, final String link){
        final StringBuilder builder = new StringBuilder();
        int i = 0;
        final int max = parts.size();
        for (final Object part : parts){
            if (part == null) builder.append("<null>");
            else builder.append(part.toString());
            i++;
            if ( i<max && link!=null ) builder.append(link);
        }
        return builder.toString();
    }
	
//	/**
//	 * Get a long value from a Configuration instance.
//	 * [taken from rbuy]
//	 * @param config
//	 * @param key
//	 * @param preset
//	 * @return
//	 */
//	public static Long getLong(CompatConfig config, String key, Long preset ){
//		String candidate = config.getString(key, null);
//		if ( candidate == null) return preset;
//		if ( !(candidate instanceof String) ) candidate = candidate.toString();
//		try{
//			return Long.parseLong(candidate);
//		} catch (NumberFormatException e){
//			return preset;
//		}
//	}
	
	/**
	 * 
	 * @param args
	 * @param sep What to separate lines with.
	 * @param join What to fill in between args that are not separated by sep.
	 * @return
	 */
	public static final String[] parseLines(final String[] args, final String sep, final String join){
		return join(Arrays.asList(args), join).split(sep);
	}
	
	public static final String[] trim(final String[] args){
		final String[] out = new String[args.length];
		for (int i = 0; i < args.length; i++){
			out[i] = args[i].trim();
		}
		return out;
	}

	/**
	 * Split args by given regex.
	 * @param args
	 * @param split
	 * @return
	 */
	public static final String[] split(final String[] args, final String split) {
		final String[][] split1 = new String[args.length][];
		int len = 0;
		for (int i = 0; i < args.length; i++){
			final String[] split2 = args[i].split(split);
			split1[i] = split2;
			len += split2.length;
		}
		String[] out = new String[len];
		int done = 0;
		for (int i = 0; i < args.length; i++){
			final String[] split2 = split1[i];
			for (int j = 0; j < split2.length; j++){
				out[done] = split2[j];
				done ++;
			}
		}
		return out;
	}
}
