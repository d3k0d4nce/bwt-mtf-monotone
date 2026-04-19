package ru.kishko;

import java.io.IOException;

public class ArithmeticDecoder {

    private static final int TOP_VALUE = 0xFFFF;

    private static final int FIRST_QTR = (TOP_VALUE / 4) + 1;

    private static final int HALF = 2 * FIRST_QTR;

    private BitInputStream bitIn;

    private long low;

    private long range;

    private long value;

    public ArithmeticDecoder(BitInputStream in) throws IOException {
        this.bitIn = in;
        this.low = 0;
        this.range = TOP_VALUE;
        this.value = 0;
        for (int i = 0; i < 16; i++) {
            int b = bitIn.readBit();
            if (b == -1) throw new IOException("Unexpected end of stream");
            value = (value << 1) | b;
        }
    }

    public int decode(FrequencyTable freq) throws IOException {
        long total = freq.getTotal();
        long scaledValue = (value - low) / (range / total);
        int symbol = 0;
        int cum = 0;
        for (int i = 0; i < FrequencyTable.SYMBOL_COUNT; i++) {
            int f = freq.getCumulative(i + 1) - freq.getCumulative(i);
            if (scaledValue < cum + f) {
                symbol = i;
                break;
            }
            cum += f;
        }
        long cumLow = freq.getCumulative(symbol);
        long freqSym = freq.getCumulative(symbol + 1) - cumLow;
        long newRange = range / total;
        low += cumLow * newRange;
        range = freqSym * newRange;
        if (range == 0) throw new RuntimeException("Range zero");
        while (range <= FIRST_QTR) {
            if (low >= HALF) {
                low -= HALF;
                value -= HALF;
            } else if (low >= FIRST_QTR) {
                low -= FIRST_QTR;
                value -= FIRST_QTR;
            }
            low <<= 1;
            range <<= 1;
            int bit = bitIn.readBit();
            if (bit == -1) throw new IOException("Unexpected end");
            value = (value << 1) | bit;
            low &= TOP_VALUE;
            value &= TOP_VALUE;
        }
        freq.update(symbol);
        return symbol;
    }

    public void close() throws IOException {
        bitIn.close();
    }
}