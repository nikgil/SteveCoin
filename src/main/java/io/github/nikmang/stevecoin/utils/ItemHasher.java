package io.github.nikmang.stevecoin.utils;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Is responsible for calculating computer ({@link org.bukkit.inventory.Inventory}) hash rates.
 */
public class ItemHasher {

    private static final int MAX_HASHRATE; //TODO: adjust this as necessary
    private static Map<Material, Integer> items;

    static {
        MAX_HASHRATE = 100;
        items = new HashMap<>();

        items.put(Material.IRON_INGOT, 1);
        items.put(Material.GOLD_INGOT, 2);
        items.put(Material.DIAMOND, 3);
        items.put(Material.EMERALD, 4);
    }

    /**
     * Get the hash rate of a miner inventory. If greater, is set to max hashrate (100).
     *
     * @param inv Inventory of miner.
     * @return Amount of hashes the miner can generate per second.
     */
    public static int getHashRate(Inventory inv) {
        int i = 0;

        for (ItemStack item : inv.getContents()) {
            LogManager.getRootLogger().debug(item == null);

            if(item != null) {
                Integer amount = items.get(item.getType());

                if (amount != null) {
                    i += (amount * item.getAmount());
                }
            }
        }

        return i <= MAX_HASHRATE ? i : MAX_HASHRATE;
    }
}
