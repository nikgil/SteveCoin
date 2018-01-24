package io.github.nikmang.stevecoin.crypto.transactions;

import com.google.common.hash.Hashing;
import io.github.nikmang.stevecoin.utils.CryptoUtils;
import org.apache.logging.log4j.LogManager;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * An individual transaction in a block.
 */
public class Transaction {
    //TODO: add transactions into signing process

    private static int sequence; //amount of generated transactions
    private static float minimumTransaction;

    private String id;
    private PublicKey sender;
    private PublicKey receiver;
    private double value;
    private byte[] signature; //TODO: maybe hashed string?

    private List<TxIn> inputs;
    private List<TxOut> outputs;

    static {
        sequence = 0;
        minimumTransaction = 0.0000001f;
    }

    public Transaction(PublicKey sender, PublicKey receiver, double value, List<TxIn> inputs) {
        this.sender = sender;
        this.receiver = receiver;
        this.value = value;
        this.inputs = inputs;

        this.outputs = new ArrayList<>();
        this.signature = new byte[0];
        this.id = "";
    }

    /**
     * Processes a transaction to see if it is valid.
     *
     * @param uTxOuts Current list of unspent transactions.
     * @return <b>true</b> if transaction goes through successfully and unspent transactions are updated.
     */
    public boolean processTransaction(Map<String, TxOut> uTxOuts) {
        if (!verifySignature()) {
            LogManager.getRootLogger().debug("Transaction signature could not be verified");
            return false;
        }

        //Gather transaction inputs
        for (TxIn txIn : inputs) {
            txIn.setuTxOut(uTxOuts.get(txIn.gettOutID()));
        }

        double in = getInputValue();

        if (in < minimumTransaction) {
            LogManager.getRootLogger().debug("Transaction input too small");
            return false;
        }

        double leftOver = getInputValue() - value; //Get left over change
        this.id = genHash();

        outputs.add(new TxOut(this.receiver, value, this.id)); //This is an output of new balance to receiver
        outputs.add(new TxOut(this.sender, leftOver, this.id)); //Statement of remainder to sender

        //Add outputs to unspent list
        for (TxOut txOut : this.outputs) {
            uTxOuts.put(txOut.getID(), txOut);
        }

        for (TxIn txIn : this.inputs) {
            if (txIn.getTxOut() == null)
                continue;

            uTxOuts.remove(txIn.getTxOut().getID());
        }

        return true;
    }

    /**
     * Generates a signature and sets it as the transaction's signature.
     *
     * @param privateKey Private Key of the wallet to generate signature.
     */
    public void getSignature(PrivateKey privateKey) {
        String data = Base64.getEncoder().encodeToString(sender.getEncoded()) + Base64.getEncoder().encodeToString(receiver.getEncoded()) + value;

        try {
            signature = CryptoUtils.INSTANCE.signECDSA(privateKey, data);
        } catch (InvalidKeyException e) {
            LogManager.getRootLogger().warn("Private key supplied was invalid.");
            e.printStackTrace();
        }
    }

    /**
     * Verifies the transaction signature.
     *
     * @return <b>true</b> if signature if verified. <b>false</b> if unverified or exception occurs.
     */
    public boolean verifySignature() {
        String data = Base64.getEncoder().encodeToString(sender.getEncoded()) + Base64.getEncoder().encodeToString(receiver.getEncoded()) + value;

        try {
            return CryptoUtils.INSTANCE.verifyECDSA(sender, data, signature);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Generates a new hash for set block.
     *
     * @return A generated hash based on keys and value of coins.
     */
    private String genHash() {
        sequence++;

        return Hashing.sha256().hashString(Base64.getEncoder().encodeToString(sender.getEncoded()) +
                Base64.getEncoder().encodeToString(receiver.getEncoded()) + value + sequence, StandardCharsets.UTF_8).toString();
    }

    private double getInputValue() {
        return this.inputs.stream().filter(txIn -> txIn.getTxOut() != null).mapToDouble(txIn -> txIn.getTxOut().getValue()).sum();
    }

    // Getters //
    public String getID() {
        return id;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public double getValue() {
        return value;
    }

    //Genesis block stuff
    /**
     * Sets the block to a genesis block.
     */
    public void setGenesis() {
        this.id = "0";
    }

    /**
     * Manually add a new output transaction.
     *
     * @param txOut Output transaction.
     */
    public void setOutput(TxOut txOut) {
        this.outputs.add(txOut);
    }
}
