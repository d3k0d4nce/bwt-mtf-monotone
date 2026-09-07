package ru.kishko;

import java.io.IOException;

public class ArithmeticEncoder {

    private static final long MAX_RANGE = 0xFFFFL;
    private static final long HALF = 0x8000L;
    private static final long QUARTER = 0x4000L;
    private static final long THREE_QUARTER = 0xC000L;

    private final BitOutputStream out;
    private long low;
    private long high;
    private long bitsToFollow;

    public ArithmeticEncoder(BitOutputStream out) {
        this.out = out;
        this.low = 0;
        this.high = MAX_RANGE;
        this.bitsToFollow = 0;
    }

    public void encode(int symbol, FrequencyTable freq) throws IOException {
        int total = freq.getTotal();
        int cumLow = freq.getCumulative(symbol);
        int cumHigh = freq.getCumulative(symbol + 1);

        long range = high - low + 1;
        high = low + (range * cumHigh) / total - 1;
        low = low + (range * cumLow) / total;

        while (true) {
            if (high < HALF) {
                writeBitWithFollow(0);
            } else if (low >= HALF) {
                writeBitWithFollow(1);
                low -= HALF;
                high -= HALF;
            } else if (low >= QUARTER && high < THREE_QUARTER) {
                bitsToFollow++;
                low -= QUARTER;
                high -= QUARTER;
            } else {
                break;
            }

            low = (low << 1) & MAX_RANGE;
            high = ((high << 1) & MAX_RANGE) | 1;
        }

        freq.update(symbol);
    }

    public void finish() throws IOException {
        bitsToFollow++;
        if (low < QUARTER) {
            writeBitWithFollow(0);
        } else {
            writeBitWithFollow(1);
        }
        out.flush();
    }

    private void writeBitWithFollow(int bit) throws IOException {
        out.writeBit(bit);
        while (bitsToFollow > 0) {
            out.writeBit(1 - bit);
            bitsToFollow--;
        }
    }

    public void close() throws IOException {
        out.close();
    }
}