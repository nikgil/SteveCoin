package io.github.nikmang.stevecoin.crypto;

import java.util.Deque;
import java.util.LinkedList;

/**
 * The blockchain that contains all blocks.
 * Contains utility methods for block creation, validation.
 */
public class Blockchain {

    private Deque<Block> chain;

    /**
     * Constructor for blockchain.
     * Initializes chain with a single genesis block created.
     */
    public Blockchain() {
        this.chain = new LinkedList<>();
        this.chain.addFirst(Block.GENESIS_BLOCK);
    }

    /**
     * Returns an unmodifiable instance of the current chain.
     *
     * @return An instance of the chain that cannot be modified.
     */
    public Deque<Block> getChain() {
        //Since Collections.unmodifiableDeque does not exist.
        return new LinkedList<>(chain);
    }

    /**
     * Attempts to add a block to the chain.
     *
     * @param b Block to be added.
     * @return <b>true</b> if block was valid and added.
     */
    public boolean addBlockToChain(Block b) {
        if (isValidBlock(b, chain.getLast())) {
            chain.push(b);
            return true;
        }

        return false;
    }

    /**
     * Validate a block before addition.
     * O(1) run time.
     *
     * @param block    The block to be validated.
     * @param oldBlock The newest block in the chain that is established.
     * @return <b>true</b> if block is valid.
     */
    private static boolean isValidBlock(Block block, Block oldBlock) {
        if (block.getIndex() != oldBlock.getIndex() + 1) {
            System.err.println("Block index is invalid\n" + block.getData());
            return false;
        }
        if (!block.getPrevHash().equals(oldBlock.getHash())) {
            System.err.println("Block hashes are not equal\n" + block.getData());
            return false;
        }

        //The default tutorial checks for correct hash but since I do it inside the constructor, it is not required.
        return true;
    }

    /**
     * Validates a full blockchain.
     * Runtime is O(n) length of chain.
     *
     * @param chain Blockchain to be validated.
     * @return <b>true</b> if chain has valid blocks, and is validly created.
     */
    private static boolean isValidChain(Deque<Block> chain) {
        if (!chain.getFirst().equals(Block.GENESIS_BLOCK))
            return false;

        Block[] tempArr = chain.toArray(new Block[chain.size()]);

        for (int i = 1; i < tempArr.length; i++) {
            if (!isValidBlock(tempArr[i], tempArr[i - 1]))
                return false;
        }

        return true;
    }

    /**
     * Atempts to replace the current blockchain with the newer one.
     * New chain validation is done within the method.
     * Runtime O(1) with O(n) validation method.
     *
     * @param chain New chain to replace old one.
     * @return <b>true</b> if chain was replaced. <b>false</b> if chain is either invalid or length was less or equal to current one.
     */
    public boolean replaceChain(Deque<Block> chain) {
        //TODO: currently possible to replace chain with invalid one as long as only the first block is equal to last. May need to change.
        if (!isValidChain(chain))
            return false;

        if (chain.size() <= this.chain.size()) {
            return false;
        }

        this.chain = new LinkedList<>(chain); //This is so it cannot be manipulated after with an existing reference
        return true;
    }
}
