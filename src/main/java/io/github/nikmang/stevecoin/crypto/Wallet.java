package io.github.nikmang.stevecoin.crypto;

import io.github.nikmang.stevecoin.crypto.transactions.Transaction;
import io.github.nikmang.stevecoin.crypto.transactions.TxIn;
import io.github.nikmang.stevecoin.crypto.transactions.TxOut;
import org.apache.logging.log4j.LogManager;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single user's wallet which contains coins.<br>
 * Very basically, a wallet's coin count is actually just a bunch of unspent transactions.
 */
public class Wallet {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private Map<String, TxOut> uTxOuts; //owned by this wallet only

    /**
     * Constructor for a wallet.<br>
     * This should be used when a player already has made a wallet before and hence possesses both private and public keys.
     *
     * @param privateKey The private key for a user.
     * @param publicKey  The public key for a user.
     */
    public Wallet(PrivateKey privateKey, PublicKey publicKey) {
        assert publicKey != null && privateKey != null;

        this.publicKey = publicKey;
        this.privateKey = privateKey;

        this.uTxOuts = new HashMap<>();
    }

    /**
     * Constructor for a new wallet.<br>
     * This shoould be used if the private and public keys do not exist for a user.
     */
    public Wallet() {
        KeyPair pair = genKeyPair();

        if (pair == null) {
            LogManager.getRootLogger().error("KeyPair generation failed. This wallet is invalid");
            return;
        }

        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    /**
     * Generates a keypair for a new wallet.
     *
     * @return A new {@link KeyPair} for the wallet. <b>null</b> if an exception occured.
     */
    private KeyPair genKeyPair() {
        //This section taken from https://medium.com/programmers-blockchain/creating-your-first-blockchain-with-java-part-2-transactions-2cdac335e0ce

        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            gen.initialize(ecSpec, random);

            return gen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            //This shouldn't happen since I am supplying the algorithm and provider
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Sends funds from this wallet to a different, designated one.
     *
     * @param uTxOuts   List of all current unspent outgoing transactions.
     * @param recipient Recipient of the coins.
     * @param value     Amount of coins to be sent.
     * @return Transaction which is responsible for this coin movement. <b>null</b> if there are insufficient coins.
     */
    public Transaction sendFunds(Map<String, TxOut> uTxOuts, PublicKey recipient, double value) {
        if (getBalance(uTxOuts) < value) {
            LogManager.getRootLogger().debug("Insufficient funds: " + value + " wanted. " + getBalance(uTxOuts) + " in wallet.");
            return null;
        }

        List<TxIn> inputs = new ArrayList<>();

        float total = 0;

        for (TxOut out : this.uTxOuts.values()) {
            total += out.getValue();
            inputs.add(new TxIn(out.getID()));

            //Add enough transactions to cover the cost
            if (total > value)
                break;
        }

        Transaction t = new Transaction(publicKey, recipient, value, inputs);
        t.getSignature(privateKey);

        for (TxIn txIn : inputs)
            this.uTxOuts.remove(txIn.gettOutID());

        return t;
    }

    /**
     * Get the current balance of a wallet.
     *
     * @param uTxOuts List of all current unspent outgoing transactions.
     * @return amount of money a person has.
     */
    public double getBalance(Map<String, TxOut> uTxOuts) {
        double total = 0;

        for (Map.Entry<String, TxOut> item : uTxOuts.entrySet()) {
            if (this.getPublicKey().equals(item.getValue().getRecipient())) {
                total += item.getValue().getValue();
                this.uTxOuts.put(item.getKey(), item.getValue());
            }
        }

        return total;
    }

    // Getters //
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
