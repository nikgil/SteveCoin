package io.github.nikmang.stevecoin.crypto;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Deque;

/**
 * Test for blockchain methods
 */
public class BlockchainTest {

    private static Blockchain chain;

    @BeforeClass
    public static void createChain() {
        chain = new Blockchain();
    }

    @Test
    public void addValidBlock() {
        Block b = new Block(chain.getChain().size(), chain.getChain().getLast().getHash(), "Valid Block");
        Assert.assertTrue(chain.addBlockToChain(b));
    }

    @Test
    public void addInvalidBlocks() {
        //Index is off
        Block test = new Block(chain.getChain().size()+1, chain.getChain().getLast().getHash(), "Bad Index");
        Assert.assertFalse(chain.addBlockToChain(test));

        //Invalid hash
        test = new Block(chain.getChain().size(), chain.getChain().getLast().getHash() + "abc", "Bad Hash");
        Assert.assertFalse(chain.addBlockToChain(test));
    }

    @Test
    public void failedForceAdd() {
        Deque<Block> list = chain.getChain();

        list.add(new Block(chain.getChain().size(), chain.getChain().getLast().getHash() + "abc", "Bad Hash 2"));

        Assert.assertFalse(list.getLast().equals(chain.getChain().getLast()));
    }

    @Test
    public void replaceSuccessfulChain() {
        Deque<Block> list = chain.getChain();
        list.add(new Block(chain.getChain().size(), chain.getChain().getLast().getHash(), "Valid Block 2"));

        Assert.assertTrue(chain.replaceChain(list));

        list.add(new Block(chain.getChain().size()+1, chain.getChain().getLast().getHash(), "Bad Index 2"));
        Assert.assertFalse(chain.replaceChain(list));

        Assert.assertEquals(list.size()-1, chain.getChain().size()); //chain should not have the last block
    }
}
