package io.github.nikmang.stevecoin.crypto;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * Individual Block in a blockchain.
 */
public class Block {

    public static final Block GENESIS_BLOCK = new Block(0, "", "Hello World");

    private int index;
    private String hash, prevHash; //TODO: delete prevHash if unused
    private long timestamp;
    private String data;

    /**
     * Constructor for a block. Timestamp is the moment of constructor call.
     *
     * @param index    The index of the block.
     * @param prevHash Previous block's hash. Done in SHA256.
     * @param data     The data incorporated within the block.
     */
    public Block(int index, String prevHash, String data) {
        assert prevHash != null && data != null;

        this.prevHash = prevHash;
        this.index = index;
        this.timestamp = System.currentTimeMillis();
        this.data = data;

        this.hash = genHash();
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

    public String getData() {
        return data;
    }

    //Creates the hash for the block
    private String genHash() {
        return Hashing.sha256().hashString(index + prevHash + timestamp + data, StandardCharsets.UTF_8).toString();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Block))
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
