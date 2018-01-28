package io.github.nikmang.stevecoin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Sign;

/**
 * Listens to make sure that sign is valid for miners.
 */
public class SignCreation implements Listener {

    @EventHandler
    public void onCreate(SignChangeEvent e) {
        if(!e.getLine(0).contains("[Coin Miner]"))
            return;

        Sign sign = (Sign) e.getBlock().getState().getData();
        Block attached = e.getBlock().getRelative(sign.getAttachedFace());

        if(attached.getType() != Material.CHEST && attached.getType() != Material.TRAPPED_CHEST)
            return;

        e.setLine(0, ChatColor.DARK_RED + "[Coin Miner]");
    }
}
