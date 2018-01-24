package io.github.nikmang.stevecoin.crypto;

import com.google.common.hash.Hashing;
import io.github.nikmang.stevecoin.crypto.transactions.Transaction;
import io.github.nikmang.stevecoin.crypto.transactions.TxOut;
import io.github.nikmang.stevecoin.utils.CryptoUtils;
import org.apache.logging.log4j.LogManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Individual Block in a blockchain.
 */
public class Block {

    public static final Block GENESIS_BLOCK = new Block(0, System.currentTimeMillis(), "");

    private int index;
    private String hash, prevHash;
    private String merkleRoot;

    private List<Transaction> data;

    private long timestamp;

    private int difficulty;

    /**
     * Constructor for a block. Timestamp is the moment of constructor call.
     *
     * @param index     The index of the block.
     * @param timestamp The timestamp of creation of the block.
     * @param prevHash  Previous block's hash. Done in SHA256.
     */
    public Block(int index, long timestamp, String prevHash) {
        assert prevHash != null;

        this.prevHash = prevHash;
        this.index = index;
        this.timestamp = timestamp;

        this.difficulty = -1;
        this.data = new ArrayList<>();
        this.merkleRoot = "";
        this.hash = genHash(0);
    }

    /**
     * Attempts to find a hash for this block
     *
     * @param difficulty The difficulty with which this block is to be mined
     * @return <b>true</b> when the block is mined.
     */
    public boolean mineBlock(int difficulty) {
        this.merkleRoot = CryptoUtils.INSTANCE.getMerkleRoot(data);
        this.difficulty = difficulty;

        StringBuilder prefix = new StringBuilder(); //Proof-of-work
        for (int i = 0; i < difficulty; i++)
            prefix.append("0");

        int nonce = 0;

        //TODO: possibly make this x times to prevent server overload
        while (true) {
            if (CryptoUtils.INSTANCE.getBinaryString(this.hash).startsWith(prefix.toString())) {
                return true;
            }

            this.hash = genHash(nonce);
            nonce++;
        }
    }

    /**
     * Adds a transaction to the block.
     *
     * @param unspent Listof all current unspent transactions.
     * @param t       The transaction to be added.
     * @return <b>true</b> if transaction was successfully added.
     */
    public boolean addTransaction(Map<String, TxOut> unspent, Transaction t) {
        assert t != null;

        // 0 = genesis transaction
        if (!t.getID().equals("0")) {
            if (!t.processTransaction(unspent)) {
                LogManager.getRootLogger().debug("Invalid funds for transaction");
                return false;
            }
        }

        data.add(t);
        return true;
    }

    /**
     * Generate SHA256 hash string for the block.
     *
     * @param nonce The single access digit for this block.
     * @return SHA256 hashed string.
     */
    private String genHash(long nonce) {
        return Hashing.sha256().hashString(index + prevHash + timestamp + merkleRoot + difficulty + nonce, StandardCharsets.UTF_8).toString();
    }

    public void setMerkleRoot(String root) {
        this.merkleRoot = root;
    }

    // Getters //

    public int getIndex() {
        return index;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getHash() {
        return hash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getData() {
        return data;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Block))
            return false;

        Block b = (Block) o;

        //Technically I can check individually but this uses all values and has no clashing issues (that I know of)
        return this.hash.equals(b.hash);
    }

    @Override
    public int hashCode() {
        return hash.hashCode() * 31;
    }
}
