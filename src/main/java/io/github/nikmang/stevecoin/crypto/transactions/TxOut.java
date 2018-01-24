package io.github.nikmang.stevecoin.crypto.transactions;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Created by Nik on 1/23/2018.
 */
public class TxOut {

    private String id;
    private PublicKey recipient;
    private double value;
    private String transactionID;

    /**
     * Constructor for TxOut.
     *
     * @param recipient     The public key of the person receiving this output. The new owner of the coin(s).
     * @param value         The amount of coins being transferred.
     * @param transactionID Which transaction contains this output.
     */
    public TxOut(PublicKey recipient, double value, String transactionID) {
        this.recipient = recipient;
        this.value = value;
        this.transactionID = transactionID;

        this.id = Hashing.sha256().hashString(Base64.getEncoder().encodeToString(recipient.getEncoded()) + value + transactionID, StandardCharsets.UTF_8).toString();
    }

    // Getters //

    public String getID() {
        return id;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public double getValue() {
        return value;
    }

    public String getTransactionID() {
        return transactionID;
    }
}
