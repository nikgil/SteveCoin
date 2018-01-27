package io.github.nikmang.stevecoin.cmds;

import io.github.nikmang.stevecoin.SteveCoin;
import io.github.nikmang.stevecoin.crypto.Wallet;
import io.github.nikmang.stevecoin.utils.MessageManager;
import io.github.nikmang.stevecoin.utils.cmds.CommandExec;
import io.github.nikmang.stevecoin.utils.cmds.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Gets the player's balance
 */
@CommandInfo(description = "Shows the balance of a player. No arguments means shows your balance", aliases = {"bal", "balance"}, usage = "[player]")
public class Balance extends CommandExec {

    private SteveCoin main;

    public Balance(SteveCoin main) {
        this.main = main;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessageManager.getManager(sender).sendMessage(MessageManager.MessageType.BAD, "must be a player to check own balance");
                return;
            }

            Player p = (Player) sender;

            //Should not occur but failsafe of sorts (can be removed later)
            Wallet w = main.wallets.get(p.getUniqueId());

            if (w == null) {
                MessageManager.getManager(p).sendMessage(MessageManager.MessageType.BAD, "Balance: 0. You do not have a wallet yet");
                return;
            }

            MessageManager.getManager(p).sendMessage(MessageManager.MessageType.NEUTRAL, "Balance: " + w.getBalance(main.blockchain.getUnspentTransactions()));

            return;
        }

        Player pl = Bukkit.getPlayer(args[0]);

        if (pl == null) {
            MessageManager.getManager(sender).sendMessage(MessageManager.MessageType.BAD, "Player not found");
            return;
        }

        Wallet w = main.wallets.get(pl.getUniqueId());

        if (w == null) {
            MessageManager.getManager(sender).sendMessage(MessageManager.MessageType.BAD, "Balance: 0. You do not have a wallet yet");
            return;
        }

        MessageManager.getManager(sender).sendMessage(MessageManager.MessageType.NEUTRAL, pl.getName() + "\'s Balance: " + w.getBalance(main.blockchain.getUnspentTransactions()));
    }
}
