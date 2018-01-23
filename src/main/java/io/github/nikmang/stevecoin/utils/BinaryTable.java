package io.github.nikmang.stevecoin.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This table is for plugin use only, to speed up block creation.<br>
 * The values are skewed to favour block creation so actual binary results will differ.<br>
 * Taken from tutorial source listed in README.md.
 */
public enum BinaryTable {
    INSTANCE;

    private Map<Character, String> lookupTable;

    BinaryTable() {
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
     *
     * @return Binary string representation of current string.
     */
    public String getBinaryString(String input) {
        StringBuilder sb = new StringBuilder();

        for(char c : input.toCharArray()) {
            String s = lookupTable.get(c);

            if(s == null)
                System.err.println(c + " requires a binary translation");
            else
                sb.append(s);
        }

        return sb.toString();
    }
}
