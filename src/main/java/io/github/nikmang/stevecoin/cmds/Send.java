package io.github.nikmang.stevecoin.cmds;

import io.github.nikmang.stevecoin.SteveCoin;
import io.github.nikmang.stevecoin.crypto.Wallet;
import io.github.nikmang.stevecoin.crypto.transactions.Transaction;
import io.github.nikmang.stevecoin.utils.MessageManager;
import io.github.nikmang.stevecoin.utils.cmds.CommandExec;
import io.github.nikmang.stevecoin.utils.cmds.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;

/**
 * Send coins from player to player.
 */
@CommandInfo(description = "Send coins from one player to another", aliases = {"send", "pay"}, mandatoryArgs = 2, usage = "<player> <amount>", nonPlayer = false)
public class Send extends CommandExec {

    private SteveCoin main;

    public Send(SteveCoin main) {
        this.main = main;
    }


    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player pl = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if(target == null) {
            MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.BAD, "Player could not be found");
            return;
        }

        Wallet plWallet = main.wallets.get(pl.getUniqueId());

        if(plWallet == null) {
            MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.BAD, "You do not possess a wallet");
            return;
        }

        Wallet targetWallet = main.wallets.get(target.getUniqueId());

        if(targetWallet == null) {
            MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.BAD, "Target does not possess a wallet");
            return;
        }

        double amount = 0.0D;

        try {
            amount = Double.parseDouble(args[1]);
        } catch(NumberFormatException e) {
            MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.BAD, "Please type in a valid number with optional decimals (X.XX)");
            return;
        }

        Transaction sent = plWallet.sendFunds(main.blockchain.getUnspentTransactions(), targetWallet.getPublicKey(), amount);

        if(sent == null) {
            MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.BAD, "You have insufficient funds");
            return;
        }

        boolean b = main.blockchain.getChain().getLast().addTransaction(main.blockchain.getUnspentTransactions(), sent);

        //Technically it can't not be a fake signature so the only test needed is less than minimum
        if(!b) {
            MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.BAD, "Amount was too small to be sent");
            return;
        }

        MessageManager.getManager(pl).sendMessage(MessageManager.MessageType.GOOD, "You have sent " + amount + " SteveCoins to " + target.getName());
        MessageManager.getManager(target).sendMessage(MessageManager.MessageType.GOOD, "You have received " + amount + " SteveCoins from " + pl.getName());

    }
}
