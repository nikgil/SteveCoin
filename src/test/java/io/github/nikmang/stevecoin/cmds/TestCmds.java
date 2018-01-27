package io.github.nikmang.stevecoin.cmds;

import io.github.nikmang.stevecoin.SteveCoin;
import io.github.nikmang.stevecoin.crypto.Block;
import io.github.nikmang.stevecoin.crypto.Blockchain;
import io.github.nikmang.stevecoin.crypto.Wallet;
import io.github.nikmang.stevecoin.crypto.transactions.Transaction;
import io.github.nikmang.stevecoin.crypto.transactions.TxOut;
import net.md_5.bungee.api.ChatColor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.security.Security;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tests commands: {@link Balance} {@link Send}
 */
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
public class TestCmds {

    private static Player pl1;
    private static Player pl2;
    private static Wallet coinbase;
    private static SteveCoin main;
    private static Map<UUID, Wallet> testWallet;

    @BeforeClass
    public static void setup() {
        Security.addProvider(new BouncyCastleProvider());
        testWallet = new HashMap<>();
        coinbase = new Wallet();

        main = Mockito.mock(SteveCoin.class);
        main.blockchain = new Blockchain();
        main.wallets = new HashMap<>();

        pl1 = Mockito.mock(Player.class);
        Mockito.when(pl1.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(pl1.getName()).thenReturn("Test_1");

        pl2 = Mockito.mock(Player.class);
        Mockito.when(pl2.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(pl2.getName()).thenReturn("Test_2");

        //going to manually add as testing for event is done elsewhere
        main.wallets.put(pl1.getUniqueId(), new Wallet());
        main.wallets.put(pl2.getUniqueId(), new Wallet());

        Server server = Mockito.mock(Server.class);
        Field field = null;
        try {
            field = Bukkit.class.getDeclaredField("server");
            field.setAccessible(true);
            field.set(null, server);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Mockito.when(Bukkit.getPlayer(Mockito.anyString())).thenReturn(null);
        Mockito.when(Bukkit.getPlayer("Test_1")).thenReturn(pl1);
        Mockito.when(Bukkit.getPlayer("Test_2")).thenReturn(pl2);
    }

    @Test
    public void testBalance() {
        ConsoleCommandSender ccs = Mockito.mock(ConsoleCommandSender.class);
        Balance balanceCmd = new Balance(main);

        balanceCmd.onCommand(ccs, new String[]{});

        Mockito.verify(ccs).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.RED + "must be a player to check own balance");
        balanceCmd.onCommand(pl1, new String[]{});

        double balance = main.wallets.get(pl1.getUniqueId()).getBalance(main.blockchain.getUnspentTransactions());
        Mockito.verify(pl1, Mockito.atLeast(1)).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.YELLOW + "Balance: " + balance);
    }

    @Test
    public void sendMoney() {
        Wallet w1 = main.wallets.get(pl1.getUniqueId());
        Wallet w2 = main.wallets.get(pl2.getUniqueId());

        //GENESIS block
        Transaction genesis = new Transaction(coinbase.getPublicKey(), w1.getPublicKey(), 100, Collections.emptyList());
        genesis.getSignature(coinbase.getPrivateKey());
        genesis.setGenesis();

        TxOut txOut = new TxOut(genesis.getReceiver(), genesis.getValue(), genesis.getID());

        genesis.setOutput(txOut);
        main.blockchain.getUnspentTransactions().put(txOut.getID(), txOut);
        Block.GENESIS_BLOCK.getData().add(genesis);
        //End GENESIS

        Send sendCmd = new Send(main);

        //Test invalid user
        sendCmd.onCommand(pl1, new String[]{"Test_3", "5.0"});
        Mockito.verify(pl1).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.RED + "Player could not be found");

        //Test faulty double
        sendCmd.onCommand(pl1, new String[]{"Test_2", "something"});
        Mockito.verify(pl1).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.RED + "Please type in a valid number with optional decimals (X.XX)");

        //Test too little funds
        sendCmd.onCommand(pl1, new String[]{"Test_2", "100.01"});
        Mockito.verify(pl1).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.RED + "You have insufficient funds");

        //Successful send to test: messages, balances, transaction, block
        sendCmd.onCommand(pl1, new String[]{"Test_2", "56.04"});

        Mockito.verify(pl1).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.GREEN + "You have sent " + "56.04" + " SteveCoins to " + "Test_2");
        Mockito.verify(pl2).sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "SteveCoin" + ChatColor.DARK_PURPLE + "] " + ChatColor.GREEN + "You have received " + "56.04" + " SteveCoins from " + "Test_1");

        Assert.assertEquals(56.04, w2.getBalance(main.blockchain.getUnspentTransactions()), 0.01);
        Assert.assertEquals(43.96, w1.getBalance(main.blockchain.getUnspentTransactions()), 0.01);

        Assert.assertEquals(2, main.blockchain.getChain().getLast().getData().size()); //Genesis plus one send
    }
}
