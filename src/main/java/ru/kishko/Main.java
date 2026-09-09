package ru.kishko;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static final int MAGIC = 0x41434431;

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

    private static void encode(String input, String output) throws Exception {
        byte[] original = Files.readAllBytes(Paths.get(input));

        BWT.Result bwt = BWT.encode(original);
        byte[] bwtData = bwt.data;
        int bwtIndex = bwt.index;

        try (FileOutputStream fos = new FileOutputStream(output);
             BufferedOutputStream buffered = new BufferedOutputStream(fos);
             DataOutputStream dos = new DataOutputStream(buffered);
             BitOutputStream bos = new BitOutputStream(dos)) {

            dos.writeInt(MAGIC);
            dos.writeInt(original.length);
            dos.writeInt(bwtIndex);

            MTF mtf = new MTF();
            FrequencyTable freq = new FrequencyTable();
            ArithmeticEncoder encoder = new ArithmeticEncoder(bos);

            for (byte b : bwtData) {
                int mtfIndex = mtf.encode(b & 0xFF);
                encoder.encode(mtfIndex, freq);
            }
            encoder.encode(FrequencyTable.EOF_SYMBOL, freq);
            encoder.finish();
        }
    }

    private static void decode(String input, String output) throws Exception {
        try (FileInputStream fis = new FileInputStream(input);
             BufferedInputStream buffered = new BufferedInputStream(fis);
             DataInputStream dis = new DataInputStream(buffered);
             BitInputStream bis = new BitInputStream(dis)) {

            int magic = dis.readInt();
            if (magic != MAGIC) throw new IOException("Invalid file format");

            int originalSize = dis.readInt();
            int bwtIndex = dis.readInt();

            if (originalSize == 0) {
                Files.write(Paths.get(output), new byte[0]);
                return;
            }

            FrequencyTable freq = new FrequencyTable();
            ArithmeticDecoder decoder = new ArithmeticDecoder(bis);
            MTF mtf = new MTF();

            byte[] bwtData = new byte[originalSize];
            int pos = 0;

            while (true) {
                int symbol = decoder.decode(freq);
                if (symbol == FrequencyTable.EOF_SYMBOL) break;
                if (symbol < 0 || symbol >= 256)
                    throw new IOException("Invalid MTF symbol: " + symbol);

                int decodedByte = mtf.decode(symbol);
                bwtData[pos++] = (byte) decodedByte;
                if (pos > originalSize) throw new IOException("Decoded data too large");
            }

            if (pos != originalSize)
                throw new IOException("Size mismatch: expected " + originalSize + ", got " + pos);

            byte[] decoded = BWT.decode(bwtData, bwtIndex);
            Files.write(Paths.get(output), decoded);
        }
    }
}