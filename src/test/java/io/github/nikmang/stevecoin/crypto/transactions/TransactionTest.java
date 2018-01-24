package io.github.nikmang.stevecoin.crypto.transactions;

import io.github.nikmang.stevecoin.crypto.Wallet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.Security;
import java.util.Collections;

/**
 * Test transactions.
 */
public class TransactionTest {

    @BeforeClass
    public static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testTransaction() {
        Wallet w1 = new Wallet();
        Wallet w2 = new Wallet();

        Transaction t = new Transaction(w1.getPublicKey(), w2.getPublicKey(), 5.0, Collections.emptyList());
        t.getSignature(w1.getPrivateKey());

        Assert.assertTrue(t.verifySignature());

        Wallet invalid = new Wallet();
        t.getSignature(invalid.getPrivateKey());

        Assert.assertFalse(t.verifySignature());
    }
}
