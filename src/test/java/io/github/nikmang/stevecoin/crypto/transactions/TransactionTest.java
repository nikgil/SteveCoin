package io.github.nikmang.stevecoin.crypto.transactions;

import io.github.nikmang.stevecoin.crypto.Block;
import io.github.nikmang.stevecoin.crypto.Blockchain;
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

    private static Blockchain blockchain;

    private static Wallet coinbase;
    private static Wallet w1;
    private static Wallet w2;

    @BeforeClass
    public static void setup() {
        Security.addProvider(new BouncyCastleProvider());

        blockchain = new Blockchain();

        w1 = new Wallet();
        w2 = new Wallet();
        coinbase = new Wallet();
    }

    @Test
    public void testTransaction() {
        Transaction t = new Transaction(w1.getPublicKey(), w2.getPublicKey(), 5.0, Collections.emptyList());
        t.getSignature(w1.getPrivateKey());

        Assert.assertTrue(t.verifySignature());

        Wallet invalid = new Wallet();
        t.getSignature(invalid.getPrivateKey());

        Assert.assertFalse(t.verifySignature());
    }

    @Test
    public void testSend() {
        Transaction genesis = new Transaction(coinbase.getPublicKey(), w1.getPublicKey(), 100, Collections.emptyList());
        genesis.getSignature(coinbase.getPrivateKey());
        genesis.setGenesis();

        TxOut txOut = new TxOut(genesis.getReceiver(), genesis.getValue(), genesis.getID());

        genesis.setOutput(txOut);
        blockchain.getUnspentTransactions().put(genesis.getID(), txOut);
        Block.GENESIS_BLOCK.getData().add(genesis);

        Assert.assertEquals(100, w1.getBalance(blockchain.getUnspentTransactions()), .001); //It should be exactly 100

        //simple successful transaction
        Block block1 = new Block(blockchain.getChain().size(), System.currentTimeMillis(), blockchain.getChain().getLast().getHash());
        boolean success = block1.addTransaction(blockchain.getUnspentTransactions(), w1.sendFunds(blockchain.getUnspentTransactions(), w2.getPublicKey(), 40.5));

        Assert.assertTrue(success);

        block1.mineBlock(blockchain.getDifficulty());
        boolean added = blockchain.addBlockToChain(block1);

        Assert.assertTrue(added);
        Assert.assertEquals(59.5, w1.getBalance(blockchain.getUnspentTransactions()), .001);
        Assert.assertEquals(40.5, w2.getBalance(blockchain.getUnspentTransactions()), .001);

        //simple failed transaction
        Block block2 = new Block(blockchain.getChain().size(), System.currentTimeMillis(), blockchain.getChain().getLast().getHash());
        boolean fail = block1.addTransaction(blockchain.getUnspentTransactions(), w1.sendFunds(blockchain.getUnspentTransactions(), w2.getPublicKey(), 1040.5));

        Assert.assertFalse(fail);

        block2.mineBlock(blockchain.getDifficulty());
        boolean added2 = blockchain.addBlockToChain(block1);

        Assert.assertTrue(added2);
        Assert.assertEquals(59.5, w1.getBalance(blockchain.getUnspentTransactions()), .001);
        Assert.assertEquals(40.5, w2.getBalance(blockchain.getUnspentTransactions()), .001);

        //succeeding transaction. Going the other way B -> A
        Block block3 = new Block(blockchain.getChain().size(), System.currentTimeMillis(), blockchain.getChain().getLast().getHash());
        boolean success2 = block1.addTransaction(blockchain.getUnspentTransactions(), w2.sendFunds(blockchain.getUnspentTransactions(), w1.getPublicKey(), 10.5));

        Assert.assertTrue(success2);

        block3.mineBlock(blockchain.getDifficulty());
        boolean added3 = blockchain.addBlockToChain(block1);

        Assert.assertTrue(added3);
        Assert.assertEquals(70, w1.getBalance(blockchain.getUnspentTransactions()), .001);
        Assert.assertEquals(30, w2.getBalance(blockchain.getUnspentTransactions()), .001);
    }
}
