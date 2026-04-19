package ru.kishko;

import java.io.IOException;

public class ArithmeticEncoder {

    private static final int TOP_VALUE = 0xFFFF;

    private static final int FIRST_QTR = (TOP_VALUE / 4) + 1;

    private static final int HALF = 2 * FIRST_QTR;

    private BitOutputStream bitOut;

    private long low;

    private long range;

    private int bitsToFollow;

    public ArithmeticEncoder(BitOutputStream out) {
        this.bitOut = out;
        this.low = 0;
        this.range = TOP_VALUE;
        this.bitsToFollow = 0;
    }

    public void encode(int symbol, FrequencyTable freq) throws IOException {
        long total = freq.getTotal();
        long cum = freq.getCumulative(symbol);
        long freqSym = freq.getCumulative(symbol + 1) - cum;

        long newRange = range / total;
        low += cum * newRange;
        range = freqSym * newRange;
        if (range == 0) {
            throw new RuntimeException("Range became zero");
        }
        while (range <= FIRST_QTR) {
            if (low < HALF) {
                writeBit(0);
                while (bitsToFollow > 0) {
                    writeBit(1);
                    bitsToFollow--;
                }
            } else if (low >= HALF) {
                writeBit(1);
                while (bitsToFollow > 0) {
                    writeBit(0);
                    bitsToFollow--;
                }
                low -= HALF;
            } else {
                bitsToFollow++;
                low -= FIRST_QTR;
            }
            low <<= 1;
            range <<= 1;
            low &= TOP_VALUE;
        }
        freq.update(symbol);
    }

    public void finish() throws IOException {
        bitsToFollow++;
        if (low < HALF) {
            writeBit(0);
            while (bitsToFollow > 0) {
                writeBit(1);
                bitsToFollow--;
            }
        } else {
            writeBit(1);
            while (bitsToFollow > 0) {
                writeBit(0);
                bitsToFollow--;
            }
        }
        bitOut.flush();
    }

    private void writeBit(int bit) throws IOException {
        bitOut.writeBit(bit);
    }

    public void close() throws IOException {
        bitOut.close();
    }
}