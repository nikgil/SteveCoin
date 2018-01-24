package io.github.nikmang.stevecoin.utils;

import com.google.common.hash.Hashing;
import io.github.nikmang.stevecoin.crypto.transactions.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class that contains some convenience methods to se in the crypto package.<br>
 * <br>
 * This binary table is for plugin use only, to speed up block creation.<br>
 * The values are skewed to favour block creation so actual binary results will differ.<br>
 * Taken from tutorial source listed in README.md.
 */
public enum CryptoUtils {
    INSTANCE;

    private Map<Character, String> lookupTable;

    CryptoUtils() {
        lookupTable = new HashMap<>();

        //0-9
        lookupTable.put('0', "0000");
        lookupTable.put('1', "0001");
        lookupTable.put('2', "0010");
        lookupTable.put('3', "0011");
        lookupTable.put('4', "0100");
        lookupTable.put('5', "0101");
        lookupTable.put('6', "0110");
        lookupTable.put('7', "0111");
        lookupTable.put('8', "1000");
        lookupTable.put('9', "1001");

        //a-f
        lookupTable.put('a', "1010");
        lookupTable.put('b', "1011");
        lookupTable.put('c', "1100");
        lookupTable.put('d', "1101");
        lookupTable.put('e', "1110");
        lookupTable.put('f', "1111");
    }

    /**
     * Creates a binary string based on the values of the lookup table.
     *
     * @param input String to be turned to a binary string.
     * @return Binary string representation of current string.
     */
    public String getBinaryString(String input) {
        StringBuilder sb = new StringBuilder();

        for (char c : input.toCharArray()) {
            String s = lookupTable.get(c);

            if (s == null)
                System.err.println(c + " requires a binary translation");
            else
                sb.append(s);
        }

        return sb.toString();
    }

    /**
     * Sign a string using ECDSA.
     *
     * @param pKey   {@link PrivateKey} of signer.
     * @param toSign The string to be signed.
     * @return <b>null</b> if exception occurs. Signed bytes of the string.
     * @throws InvalidKeyException If private key provided was invalid.
     */
    public byte[] signECDSA(PrivateKey pKey, String toSign) throws InvalidKeyException {
        Signature dsa;
        byte[] out = null;

        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(pKey);

            byte[] arr = toSign.getBytes();
            dsa.update(arr);

            out = dsa.sign();
        } catch (NoSuchAlgorithmException e) {
            //This and NoSuchProvider should not occur as it is shaded in
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Attempts to verify data using ECDSA.
     *
     * @param pKey {@link PublicKey} of a user.
     * @param data The data to be verified.
     * @param signature The signature that was used to sign the data.
     * @return <b>true</b> if successfully verified. <b>false</b> if verification is wrong or exception is thrown.
     * @throws InvalidKeyException If public key provided was invalid.
     */
    public boolean verifyECDSA(PublicKey pKey, String data, byte[] signature) throws InvalidKeyException {
        try {
            Signature verify = Signature.getInstance("ECDSA", "BC");

            verify.initVerify(pKey);
            verify.update(data.getBytes());

            return verify.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            //This and NoSuchProvider should not occur as it is shaded in
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets the merkle root for a list of transactions.
     *
     * @param transactions List of transactions from which to generate the root.
     * @return The String representation of the merkle root.
     */
    public String getMerkleRoot(List<Transaction> transactions) {
        int count = transactions.size();

        //TODO: read into merkle roots
        List<String> lastLayer = new ArrayList<>();

        for(Transaction t : transactions) {
            lastLayer.add(t.getID());
        }

        List<String> treeLayer = new ArrayList<>();

        while(count > 1) {
            treeLayer = new ArrayList<>();
            for(int i=1; i<lastLayer.size(); i++) {
                treeLayer.add(Hashing.sha256().hashString(lastLayer.get(i-1) + lastLayer.get(i), StandardCharsets.UTF_8).toString());
            }

            count = treeLayer.size();
            lastLayer = treeLayer;
        }

        String root = treeLayer.size() == 1 ? treeLayer.get(0) : "";

        return root;
    }
}
