package me.asofold.bpl.painlesspvp.plshared.items;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.asofold.bpl.painlesspvp.plshared.Utils;

import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

/**
 * Copy and paste from plshared.
 * @author mc_dev
 *
 */
public class ItemUtil {
	
	/**
	 * Color name to color.
	 */
	private static final Map <String, Color> colors = new HashMap<String, Color>();
	
	static{
		for (final Field field : Color.class.getDeclaredFields()){
			if (field.getType() == Color.class){
				try {
					final Color color = (Color) field.get(Color.class);
					colors.put(field.getName(), color);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Return color with closest 3d distance.
	 * @param color
	 * @return
	 */
	public static String getClosestColorName(Color color){
		double minDist = Double.MAX_VALUE;
		String name = null;
		for (Entry<String, Color> entry : colors.entrySet()){
			Color other = entry.getValue();
			double dR = color.getRed() - other.getRed();
			double dB = color.getBlue() - other.getBlue();
			double dG = color.getGreen() - other.getGreen();
			double d = Math.sqrt(dR * dR + dB * dB + dG * dG);
			if (d < minDist){
				minDist = d;
				name = entry.getKey();
			}
		}
		return name;
	}
	
	/**
	 * Return color name only if it is a default color.
	 * @param color
	 * @return
	 */
	public static String getColorName(Color color){
		for (Entry<String, Color> entry : colors.entrySet()){
			Color other = entry.getValue();
			double dR = color.getRed() - other.getRed();
			if (dR != 0) continue;
			double dB = color.getBlue() - other.getBlue();
			if (dB != 0) continue;
			double dG = color.getGreen() - other.getGreen();
			if (dG != 0) continue; 
			return entry.getKey();
		}
		return null;
	}

	/**
	 * Meant to include all details relevant for selling and similar, including meta.
	 * @param stack
	 * @return
	 */
	public static String getFullItemDescription(final ItemStack stack){
		return getFullItemDescription(stack, true);
	}
	
	/**
	 * Meant to include all details relevant for selling and similar, including meta.
	 * @param stack
	 * @param amount If to add the item amount.
	 * @return
	 */
	public static String getFullItemDescription(final ItemStack stack, final boolean amount){
		final StringBuilder b = new StringBuilder(120);
		final ItemSpec spec = new ItemSpec(stack);
		b.append(spec.shortestName());
		if (amount) b.append(" x " + stack.getAmount());
		final ItemMeta meta = stack.getItemMeta();
		if (meta != null){
			if (meta.hasDisplayName()){
				b.append(" + Item name: ");
				b.append(meta.getDisplayName());
			}
			if (meta.hasLore()){
				b.append( " + Lore: ");
				b.append(Utils.join(meta.getLore(), " "));
			}
			if (meta.hasEnchants()){
				// TODO: This is redundant ?
				b.append(" + Enchantments: ");
				ItemUtil.addEnchantments(b, meta.getEnchants());
			}
			if (meta instanceof EnchantmentStorageMeta){
				EnchantmentStorageMeta esMeta = (EnchantmentStorageMeta) meta;
				if (esMeta.hasStoredEnchants()){
					b.append(" + Stored enchantements: ");
					ItemUtil.addEnchantments(b, esMeta.getStoredEnchants());
				}
			}
			if (meta instanceof SkullMeta){
				SkullMeta skMeta = (SkullMeta) meta;
				if (skMeta.hasOwner()){
					b.append(" + Owner: " + skMeta.getOwner()); 
				}
				else{
					// Attempt to guess skull name.
					switch(stack.getDurability() & 0x7){
					case 0:
						b.append(" + Skeleton skull");
						break;
					case 1:
						b.append(" + Wither Skeleton skull");
						break;
					case 2:
						b.append(" + Zombie skull");
						break;
					case 3:
						b.append(" + Human skull");
						break;
					case 4:
						b.append(" + Creeper skull");
						break;
					default:
						b.append(" + Unknown skull");
					}
				}
			}
			if (meta instanceof LeatherArmorMeta){
				LeatherArmorMeta lMeta = (LeatherArmorMeta) meta;
				Color color = lMeta.getColor();
				String name = getColorName(color);
				if (name == null){
					b.append( " + Color: closest to " + getClosestColorName(color));
				}
				else{
					b.append(" + Color: " + name);
				}
			}
			if (meta instanceof BookMeta){
				BookMeta bMeta = (BookMeta) meta;
				if (bMeta.hasAuthor()) b.append(" + Author: " + bMeta.getAuthor());
				if (bMeta.hasTitle()) b.append(" + Title: " + bMeta.getTitle());
				if (bMeta.hasPages()) b.append(" + Pages: " + bMeta.getPageCount());
			}
			if (meta instanceof PotionMeta){
				PotionMeta pMeta = (PotionMeta) meta;
				if (pMeta.hasCustomEffects()){
					b.append(" + Effects: ");
					addPotionEffects(b, pMeta.getCustomEffects());
				}
			}
		}	
		return b.toString();
	}

	public static void addPotionEffects(StringBuilder b, Collection<PotionEffect> customEffects)
	{
		List<String> es = new ArrayList<String>(customEffects.size());
		for (final PotionEffect effect : customEffects){
			es.add(effect.getType().getName() + "(" + effect.getAmplifier() + "x" + " " + Utils.millisToShortDHMS(1000L * effect.getDuration()) + ")");
		}
		Collections.sort(es);
		b.append(Utils.join(es, " "));
	}

	public static void addEnchantments(StringBuilder b, Map<Enchantment, Integer> enchantments)
	{
		List<String> es = new ArrayList<String>(enchantments.size());
		for (final Entry<Enchantment, Integer> entry : enchantments.entrySet()){
			es.add(entry.getKey().getName() + "@" + entry.getValue());
		}
		Collections.sort(es);
		b.append(Utils.join(es, " "));
	}

	/**
		 * Add verbalized and sorted item descriptions.
		 * @param items
		 * @param builder
		 */
	public static void addItemDescr(final Collection<ItemStack> items, final StringBuilder builder){
		if (items.isEmpty()) return;
		List<String> keys = new ArrayList<String>(items.size()); // will rather be shorter.
		Map<String, Integer> dropList = new HashMap<String, Integer>();
		for (final ItemStack stack : items) {
			if (stack == null) continue;
			if (stack.getTypeId() == 0) continue;
//			int d;
//			if (stack.getType().isBlock()) d = stack.getData().getData();
//			else d = stack.getDurability();
			final String key = getFullItemDescription(stack, false);
	//			if ( d == 0) key = ""+stack.getTypeId();
	//			else key = stack.getTypeId()+":"+d;
//				key = new ItemSpec(stack.getTypeId(), d).shortestName().toLowerCase();
//				Map<Enchantment, Integer> enchantments = stack.getEnchantments();
//				if ( enchantments != null) {
//					if ( !enchantments.isEmpty()){
//						List<String> es = new ArrayList<String>(enchantments.size());
//						for ( Enchantment e : enchantments.keySet()){
//							es.add(e.getName()+"@"+enchantments.get(e));
//						}
//						Collections.sort(es);
//						key+="(";
//						for (String s : es){
//							key +=s+",";
//						}
//						key+=")";
//					}
//				}
			final Integer n = dropList.get(key);
			if (n != null) dropList.put(key, n + stack.getAmount());
			else {
				dropList.put(key, stack.getAmount());
				keys.add(key);
			}
		}
		Collections.sort(keys);
		for (final String key : keys) {
			builder.append(key + " x" + dropList.get(key) + ", ");
		}
	}
}
