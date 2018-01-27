package io.github.nikmang.stevecoin.listeners;

import io.github.nikmang.stevecoin.SteveCoin;
import io.github.nikmang.stevecoin.crypto.Wallet;
import org.apache.logging.log4j.LogManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Tests Listeners: {@link OnJoin}
 */
@PowerMockIgnore("javax.management.*") //avoids an annoying error with logger and power mock
@RunWith(PowerMockRunner.class)
public class TestListeners {

    private Player pl1;
    private SteveCoin main;
    private Map<UUID, Wallet> testWallet;

    @Before
    public void setup() {
        Security.addProvider(new BouncyCastleProvider());
        testWallet = new HashMap<>();

        main = Mockito.mock(SteveCoin.class);
        main.wallets = new HashMap<>();

        UUID uuid = UUID.randomUUID();

        pl1 = Mockito.mock(Player.class);
        Mockito.when(pl1.getUniqueId()).thenReturn(uuid);
        Mockito.when(pl1.getName()).thenReturn("Test #1");
    }

    @Test
    public void testJoin() {
        PlayerJoinEvent join = new PlayerJoinEvent(pl1, "Test Message");

        OnJoin oj = new OnJoin(main);

        Assert.assertEquals(0, main.wallets.size());

        oj.onJoin(join);

        Assert.assertEquals(1, main.wallets.size());
        Assert.assertTrue(main.wallets.containsKey(pl1.getUniqueId()));
    }
}
