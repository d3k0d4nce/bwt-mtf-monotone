package ru.kishko;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {

    private static final int MAGIC = 0x41434431;

    private static final int EOF = FrequencyTable.EOF_SYMBOL;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage:");
            System.out.println("  encoder infile zipfile");
            System.out.println("  decoder zipfile decfile");
            return;
        }

        String mode = args[0];
        String input = args[1];
        String output = args[2];

        try {
            if (mode.equals("encoder")) {
                encode(input, output);
            } else if (mode.equals("decoder")) {
                decode(input, output);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void encode(String input, String output) throws IOException {
        long fileSize = new File(input).length();

        try (InputStream in = new BufferedInputStream(new FileInputStream(input));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {

            out.writeInt(MAGIC);
            out.writeLong(fileSize);

            try (BitOutputStream bitOut = new BitOutputStream(out)) {
                ArithmeticEncoder encoder = new ArithmeticEncoder(bitOut);
                FrequencyTable freq = new FrequencyTable();

                int b;
                while ((b = in.read()) != -1) {
                    encoder.encode(b, freq);
                }
                encoder.encode(EOF, freq);
                encoder.finish();
            }
        }
    }

    private static void decode(String input, String output) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(input)))) {

            int magic = in.readInt();
            if (magic != MAGIC) {
                throw new IOException("Invalid file format");
            }

            long fileSize = in.readLong();

            try (BitInputStream bitIn = new BitInputStream(in);
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(output))) {

                ArithmeticDecoder decoder = new ArithmeticDecoder(bitIn);
                FrequencyTable freq = new FrequencyTable();

                long written = 0;
                while (true) {
                    int symbol = decoder.decode(freq);
                    if (symbol == EOF) {
                        break;
                    }
                    out.write(symbol);
                    written++;
                }

                if (written != fileSize) {
                    throw new IOException("Size mismatch: expected " + fileSize + ", got " + written);
                }
            }
        }
    }
}