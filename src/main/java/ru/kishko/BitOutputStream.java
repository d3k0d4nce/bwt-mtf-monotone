package ru.kishko;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements AutoCloseable {

    private final OutputStream out;

    private int buffer;

    private int bitsInBuffer;

    public BitOutputStream(OutputStream out) {
        this.out = out;
        this.buffer = 0;
        this.bitsInBuffer = 0;
    }

    public void writeBit(int bit) throws IOException {
        buffer = (buffer << 1) | bit;
        bitsInBuffer++;
        if (bitsInBuffer == 8) {
            out.write(buffer);
            buffer = 0;
            bitsInBuffer = 0;
        }
    }

    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            buffer <<= (8 - bitsInBuffer);
            out.write(buffer);
            buffer = 0;
            bitsInBuffer = 0;
        }
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }
}