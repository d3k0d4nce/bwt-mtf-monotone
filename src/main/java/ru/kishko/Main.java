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

    public static final int MAGIC = 0x42574D43;

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

        // BWT
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

            // MTF + Монотонный код
            MTF mtf = new MTF();
            for (byte b : bwtData) {
                int mtfIndex = mtf.encode(b & 0xFF);
                MonotoneCoder.encode(mtfIndex, bos);
            }
            bos.flush();
        }
    }

    private static void decode(String input, String output) throws Exception {
        try (FileInputStream fis = new FileInputStream(input);
             BufferedInputStream buffered = new BufferedInputStream(fis);
             DataInputStream dis = new DataInputStream(buffered);
             BitInputStream bis = new BitInputStream(dis)) {

            if (dis.readInt() != MAGIC) throw new IOException("Invalid format");
            int size = dis.readInt();
            int bwtIndex = dis.readInt();

            if (size == 0) {
                Files.write(Paths.get(output), new byte[0]);
                return;
            }

            MTF mtf = new MTF();
            byte[] bwtData = new byte[size];

            for (int i = 0; i < size; i++) {
                int mtfIndex = MonotoneCoder.decode(bis);
                bwtData[i] = (byte) mtf.decode(mtfIndex);
            }

            Files.write(Paths.get(output), BWT.decode(bwtData, bwtIndex));
        }
    }
}