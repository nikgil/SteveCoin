package io.github.nikmang.stevecoin.listeners;

import io.github.nikmang.stevecoin.SteveCoin;
import io.github.nikmang.stevecoin.crypto.Wallet;
import io.github.nikmang.stevecoin.utils.MessageManager;
import org.apache.logging.log4j.LogManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener for player joining.<br>
 * This will automatically give them a wallet.
 */
public class OnJoin implements Listener {

    private SteveCoin main;

    public OnJoin(SteveCoin main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!main.wallets.containsKey(e.getPlayer().getUniqueId())) {
            main.wallets.put(e.getPlayer().getUniqueId(), new Wallet());
            MessageManager.getManager(e.getPlayer()).sendMessage(MessageManager.MessageType.GOOD, "New coin wallet created");
            LogManager.getRootLogger().debug("Created wallet for " + e.getPlayer().getName());
        }
    }
}
