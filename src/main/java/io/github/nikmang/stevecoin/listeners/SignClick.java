package io.github.nikmang.stevecoin.listeners;

import io.github.nikmang.stevecoin.utils.ItemHasher;
import io.github.nikmang.stevecoin.utils.MessageManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * When a person right clicks a sign to enable/disable a miner.
 */
public class SignClick implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        //Connected to chest means sign
        if (e.getClickedBlock().getType() != Material.WALL_SIGN)
            return;

        Sign wallSign = (Sign) e.getClickedBlock().getState();

        if (!wallSign.getLine(0).contains("[Coin Miner]"))
            return;

        if (wallSign.getLine(0).contains(ChatColor.DARK_GREEN + "")) {
            wallSign.setLine(0, ChatColor.DARK_RED + "[Coin Miner]");
            wallSign.setLine(1, "");
            wallSign.setLine(2, "");
            wallSign.setLine(3, "");
        } else {
            //It will be Dark Red
            Block attached = e.getClickedBlock().getRelative(((org.bukkit.material.Sign)wallSign.getData()).getAttachedFace());

            if(attached.getType() != Material.CHEST && attached.getType() != Material.TRAPPED_CHEST) {
                MessageManager.getManager(e.getPlayer()).sendMessage(MessageManager.MessageType.BAD, "Sign must be attached to a chest to work");
                return;
            } else {
                wallSign.setLine(0, ChatColor.DARK_GREEN + "[Coin Miner]");
                int hashes = ItemHasher.getHashRate(((Chest)attached.getState()).getBlockInventory());
                wallSign.setLine(1, hashes + " per/sec");
                wallSign.setLine(3, e.getPlayer().getName());

                //TODO: add chest to list for hash creation
            }
        }

        wallSign.update();
    }
}
