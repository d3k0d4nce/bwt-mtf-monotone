package ru.kishko;

public class MTF {

    private static final int ALPHABET_SIZE = 256;
    private final int[] stack = new int[ALPHABET_SIZE];

    public MTF() {
        for (int i = 0; i < ALPHABET_SIZE; i++) stack[i] = i;
    }

    public int encode(int symbol) {
        if (symbol < 0 || symbol >= ALPHABET_SIZE)
            throw new IllegalArgumentException("Invalid MTF symbol: " + symbol);

        int index = 0;
        while (stack[index] != symbol) index++;

        int found = stack[index];
        for (int i = index; i > 0; i--) stack[i] = stack[i - 1];
        stack[0] = found;

        return index;
    }

    public int decode(int index) {
        if (index < 0 || index >= ALPHABET_SIZE)
            throw new IllegalArgumentException("Invalid MTF index: " + index);

        int symbol = stack[index];
        for (int i = index; i > 0; i--) stack[i] = stack[i - 1];
        stack[0] = symbol;

        return symbol;
    }
}