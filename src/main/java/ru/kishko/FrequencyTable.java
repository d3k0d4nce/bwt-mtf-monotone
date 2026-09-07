package ru.kishko;

public class FrequencyTable {

    public static final int SYMBOL_COUNT = 257;
    public static final int EOF_SYMBOL = 256;
    public static final int MAX_TOTAL = 16384;

    private final int[] frequencies;

    private int total;

    public FrequencyTable() {
        frequencies = new int[SYMBOL_COUNT];
        total = 0;
        for (int i = 0; i < SYMBOL_COUNT; i++) {
            frequencies[i] = 1;
            total++;
        }
    }

    public int getTotal() {
        return total;
    }

    public int getCumulative(int symbol) {
        int cum = 0;
        for (int i = 0; i < symbol; i++) {
            cum += frequencies[i];
        }
        return cum;
    }

    public void update(int symbol) {
        frequencies[symbol]++;
        total++;
        if (total > MAX_TOTAL) {
            scale();
        }
    }

    private void scale() {
        int newTotal = 0;
        for (int i = 0; i < SYMBOL_COUNT; i++) {
            frequencies[i] = (frequencies[i] + 1) >> 1;
            newTotal += frequencies[i];
        }
        total = newTotal;
    }
}