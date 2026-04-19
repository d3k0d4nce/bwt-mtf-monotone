package ru.kishko;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable {

    private final InputStream in;

    private int buffer;

    private int bitsLeft;

    public BitInputStream(InputStream in) {
        this.in = in;
        this.buffer = 0;
        this.bitsLeft = 0;
    }

    public int readBit() throws IOException {
        if (bitsLeft == 0) {
            buffer = in.read();
            if (buffer == -1) {
                return -1;
            }
            bitsLeft = 8;
        }
        int bit = (buffer >> (bitsLeft - 1)) & 1;
        bitsLeft--;
        return bit;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}