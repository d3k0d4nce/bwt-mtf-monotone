package ru.kishko;

import java.io.IOException;

public class ArithmeticDecoder {

    private static final long MAX_RANGE = 0xFFFFL;
    private static final long HALF = 0x8000L;
    private static final long QUARTER = 0x4000L;
    private static final long THREE_QUARTER = 0xC000L;

    private final BitInputStream in;
    private long low;
    private long high;
    private long code;

    public ArithmeticDecoder(BitInputStream in) throws IOException {
        this.in = in;
        this.low = 0;
        this.high = MAX_RANGE;
        this.code = 0;

        for (int i = 0; i < 16; i++) {
            int bit = in.readBit();
            if (bit == -1) {
                bit = 0;
            }
            code = (code << 1) | bit;
        }
    }

    public int decode(FrequencyTable freq) throws IOException {
        int total = freq.getTotal();
        long range = high - low + 1;

        long scaled = ((code - low + 1) * total - 1) / range;

        int symbol = 0;
        while (symbol < FrequencyTable.SYMBOL_COUNT - 1 &&
                freq.getCumulative(symbol + 1) <= scaled) {
            symbol++;
        }

        int cumLow = freq.getCumulative(symbol);
        int cumHigh = freq.getCumulative(symbol + 1);

        range = high - low + 1;
        high = low + (range * cumHigh) / total - 1;
        low = low + (range * cumLow) / total;

        while (true) {
            if (high < HALF) {
                // no adjustment
            } else if (low >= HALF) {
                code -= HALF;
                low -= HALF;
                high -= HALF;
            } else if (low >= QUARTER && high < THREE_QUARTER) {
                code -= QUARTER;
                low -= QUARTER;
                high -= QUARTER;
            } else {
                break;
            }

            low = (low << 1) & MAX_RANGE;
            high = ((high << 1) & MAX_RANGE) | 1;

            int bit = in.readBit();
            if (bit == -1) {
                bit = 0;
            }
            code = ((code << 1) & MAX_RANGE) | bit;
        }

        freq.update(symbol);
        return symbol;
    }

    public void close() throws IOException {
        in.close();
    }
}