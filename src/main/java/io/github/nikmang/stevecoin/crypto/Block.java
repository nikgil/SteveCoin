package io.github.nikmang.stevecoin.crypto;

import com.google.common.hash.Hashing;
import io.github.nikmang.stevecoin.crypto.transactions.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Individual Block in a blockchain.
 */
public class Block {

    public static final Block GENESIS_BLOCK = new Block(0, System.currentTimeMillis(), "", 0, 0);

    private int index;
    private String hash, prevHash;
    private String merkleRoot;

    private List<Transaction> data;

    private long timestamp;

    private int difficulty;
    private long nonce;

    /**
     * Constructor for a block. Timestamp is the moment of constructor call.
     *
     * @param index      The index of the block.
     * @param timestamp  The timestamp of creation of the block.
     * @param prevHash   Previous block's hash. Done in SHA256.
     * @param difficulty The difficulty to mine set block.
     * @param nonce      non-reusable number that is used to prevent replay attacks.
     */
    public Block(int index, long timestamp, String prevHash, int difficulty, long nonce) {
        assert prevHash != null && data != null;

        this.prevHash = prevHash;
        this.index = index;
        this.timestamp = timestamp;

        this.difficulty = difficulty;
        this.nonce = nonce;

        this.hash = genHash();
        this.data = new ArrayList<>();
        this.merkleRoot = "";
    }

    //Creates the hash for the block
    private String genHash() {
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

    public long getNonce() {
        return nonce;
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
