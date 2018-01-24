package io.github.nikmang.stevecoin.crypto.transactions;

/**
 * Sent to receiver as incoming transaction.<br>
 * This makes reference to an old output from the sender.
 */
public class TxIn {

    private String tOutID;
    private TxOut uTxOut;

    public TxIn(String tOutID) {
        this.tOutID = tOutID;
    }

    public void setuTxOut(TxOut txOut) {
        this.uTxOut = txOut;
    }

    // Getters //

    public String gettOutID() {
        return tOutID;
    }

    public TxOut getTxOut() {
        return uTxOut;
    }
}
