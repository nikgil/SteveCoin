package io.github.nikmang.stevecoin.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Deque;

/**
 * Test for blockchain methods
 */
public class BlockchainTest {

    private static Blockchain chain;
    private static Logger logger;

    @BeforeClass
    public static void createChain() {
        chain = new Blockchain();
        logger = LogManager.getRootLogger();
    }

    @Test
    public void addValidBlock() {
        Block b = chain.generateNextBlock("test block");

        Assert.assertTrue(chain.addBlockToChain(b));
    }

    @Test
    public void addInvalidBlocks() {
        Block valid = chain.generateNextBlock("Test Block"); //Using this for invalid block creation

        //Index is off
        Block test = new Block(valid.getIndex()+1, valid.getTimestamp(), valid.getPrevHash(), valid.getData(), valid.getDifficulty(), valid.getNonce());
        Assert.assertFalse(chain.addBlockToChain(test));

        //Invalid hash
        test = new Block(valid.getIndex(), valid.getTimestamp(), "1"+valid.getPrevHash(), valid.getData(), valid.getDifficulty(), valid.getNonce());
        Assert.assertFalse(chain.addBlockToChain(test));

        //Invalid timestamp
        test = new Block(valid.getIndex(), chain.getChain().getLast().getTimestamp()-1, valid.getPrevHash(), valid.getData(), valid.getDifficulty(), valid.getNonce());
        Assert.assertFalse(chain.addBlockToChain(test));
    }

    @Test
    public void failedForceAdd() {
        Deque<Block> list = chain.getChain();

        list.add(chain.getChain().getFirst());

        Assert.assertFalse(list.getLast().equals(chain.getChain().getLast()));
    }

    @Test
    public void testDifficulty() {
        while(chain.getChain().size() < 100) {
            chain.addBlockToChain(chain.generateNextBlock("Block #" + chain.getChain().size()));
        }

        logger.debug("Latest difficulty: " + chain.getChain().getLast().getDifficulty());
        Assert.assertEquals(chain.getDifficulty(), chain.getChain().getLast().getDifficulty());
    }

    @Test
    public void replaceSuccessfulChain() {
        Deque<Block> list = chain.getChain();
        list.add(chain.generateNextBlock("Test Block 2"));

        Assert.assertTrue(chain.replaceChain(list));

        list.add(list.getFirst()); //technically should fault on the timestamp or index
        Assert.assertFalse(chain.replaceChain(list));

        Assert.assertEquals(list.size()-1, chain.getChain().size()); //chain should not have the last block
    }
}
