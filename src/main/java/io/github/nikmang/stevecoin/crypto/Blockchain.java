package io.github.nikmang.stevecoin.crypto;

import io.github.nikmang.stevecoin.crypto.transactions.TxOut;
import io.github.nikmang.stevecoin.utils.CryptoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The blockchain that contains all blocks.
 * Contains utility methods for block creation, validation.
 */
public class Blockchain {

    //TODO: make these config values. Increase minimum tolerance of diff_adjust and decrease block gen speed requirement
    private static final int BLOCK_GEN_INTERVAL = 10 * 1000; //After how many **seconds** should a block be found
    private static final int DIFF_ADJUST_INTERVAL = 10; //After how many blocks should difficulty be adjusted

    private static Logger logger = LogManager.getRootLogger();

    private Deque<Block> chain;
    private Map<String, TxOut> uTxO;

    /**
     * Constructor for blockchain.
     * Initializes chain with a single genesis block created.
     */
    public Blockchain() {
        this.uTxO = new HashMap<>();
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

    public Map<String, TxOut> getUnspentTransactions() {
        return uTxO;
    }

    /**
     * Attempts to add a block to the chain.
     *
     * @param b Block to be added.
     * @return <b>true</b> if block was valid and added.
     */
    public boolean addBlockToChain(Block b) {
        if (isValidBlock(b, chain.getLast())) {
            chain.addLast(b);
            return true;
        }

        return false;
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

        if (getTotalDifficulty(chain) <= getTotalDifficulty(this.chain)) {
            return false;
        }

        this.chain = new LinkedList<>(chain); //This is so it cannot be manipulated after with an existing reference
        return true;
    }

    /**
     * Get a new difficulty for mining blocks.
     * Difficulty is based on configurable adjustment interval multiplied by average time to find block.
     * <i>Example: if adjustment interval set to 10 and time to mine blocks set to 20, it should take 200 seconds to mine all.</i>
     *
     * @return New difficulty based on result of mining efficiency.
     */
    private int getNewDifficulty() {
        Block[] temp = chain.toArray(new Block[chain.size()]);
        Block lastChange = temp[temp.length - DIFF_ADJUST_INTERVAL];

        long timeExpected = DIFF_ADJUST_INTERVAL * BLOCK_GEN_INTERVAL; //How long it should take to generate x blocks
        long timeTaken = chain.getLast().getTimestamp() - lastChange.getTimestamp(); //The actual time it took

        logger.debug("Time taken for getting blocks: " + timeTaken);
        logger.debug("Time it should have taken: " + timeExpected);

        //TODO: make values configurable
        if (timeTaken > timeExpected * 2)
            return chain.getLast().getDifficulty() > 0 ? chain.getLast().getDifficulty() - 1 : 0;
        else if (timeTaken < timeExpected / 2)
            return chain.getLast().getDifficulty() + 1;

        return chain.getLast().getDifficulty();
    }

    /**
     * Gets the difficulty for the next block.
     * If time interval not exceeded will return the difficulty of the last block.
     *
     * @return Difficulty for new block.
     */
    public int getDifficulty() {
        Block last = chain.getLast();

        if (last.getIndex() > 0 && last.getIndex() % DIFF_ADJUST_INTERVAL == 0) {
            return getNewDifficulty();
        }

        return last.getDifficulty();
    }

    /**
     * Generates a new block based on current chain.
     * Does not automatically add it to the existing chain.
     */
    public Block generateNextBlock() {
        Block b = new Block(chain.size(), System.currentTimeMillis(), chain.getLast().getHash());

        return b;
    }

    /**
     * Get the total difficulty of generating all of the blocks.
     * Done through the formula 2^0 + 2^1 + ... + 2^(n-1) with n being size of chain.
     *
     * @return Total difficulty in getting all blocks.
     */
    private static double getTotalDifficulty(Deque<Block> chain) {
        return chain.stream().mapToInt(Block::getDifficulty).mapToDouble(diff -> Math.pow(2, diff)).sum();
    }

    /**
     * Check if hash has correct difficulty attached to it.<br>
     * This is a utility method for isValidBlock
     *
     * @param difficulty The difficulty of the block
     * @param hash       The hash that is used in a potential block
     * @return <b>true</b> if hash has applicable difficulty attached to it.
     */
    public static boolean hasMatchingDifficulty(int difficulty, String hash) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < difficulty; i++) {
            stringBuilder.append("0");
        }

        String binary = CryptoUtils.INSTANCE.getBinaryString(hash);

        return binary.startsWith(stringBuilder.toString());
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
            logger.debug("Block index is invalid " + block.getMerkleRoot() + " " + block.getIndex());
            return false;
        }
        if (!block.getPrevHash().equals(oldBlock.getHash())) {
            logger.debug("Block hashes are not equal " + block.getMerkleRoot());
            return false;
        }
        if (block.getTimestamp() < oldBlock.getTimestamp()) { //May change it to <= later to slow down process
            logger.debug("Block timestamp is invalid " + block.getMerkleRoot());
            return false;
        }

        //The default tutorial checks for correct hash but since I do it inside the constructor, it is not required.
        return hasMatchingDifficulty(block.getDifficulty(), block.getHash());
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
}
