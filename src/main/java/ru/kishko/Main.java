package ru.kishko;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
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

    static void encode(String input, String output) throws Exception {
        byte[] data = new FileInputStream(input).readAllBytes();
        List<Integer> symbols = new ArrayList<>();
        for (byte b : data) symbols.add(b & 0xFF);
        symbols.add(256);

        int[] freq = new int[257];
        for (int s : symbols) freq[s]++;

        String[] codes = new String[257];
        buildCodes(freq, codes, "", 0, 257);

        StringBuilder bits = new StringBuilder();
        for (int s : symbols) bits.append(codes[s]);

        while (bits.length() % 8 != 0) bits.append("0");

        byte[] compressed = new byte[bits.length() / 8];
        for (int i = 0; i < compressed.length; i++) {
            compressed[i] = (byte) Integer.parseInt(bits.substring(i * 8, i * 8 + 8), 2);
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(output))) {
            dos.writeInt(data.length);
            dos.writeInt(compressed.length);
            dos.write(compressed);
            for (int i = 0; i < 257; i++) dos.writeInt(freq[i]);
        }
    }

    static void buildCodes(int[] freq, String[] codes, String prefix, int start, int end) {
        if (start + 1 == end) {
            codes[start] = prefix;
            return;
        }
        int total = 0;
        for (int i = start; i < end; i++) total += freq[i];
        int half = total / 2;
        int sum = 0;
        int split = start;
        for (int i = start; i < end; i++) {
            sum += freq[i];
            if (sum >= half) {
                split = i + 1;
                break;
            }
        }
        if (split == start) split = start + 1;
        if (split == end) split = end - 1;

        buildCodes(freq, codes, prefix + "0", start, split);
        buildCodes(freq, codes, prefix + "1", split, end);
    }

    static void decode(String input, String output) throws Exception {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(input))) {
            int origLen = dis.readInt();
            int compLen = dis.readInt();
            byte[] compressed = new byte[compLen];
            dis.read(compressed);
            int[] freq = new int[257];
            for (int i = 0; i < 257; i++) freq[i] = dis.readInt();

            String[] codes = new String[257];
            buildCodes(freq, codes, "", 0, 257);

            Map<String, Integer> reverse = new HashMap<>();
            for (int i = 0; i < 257; i++) reverse.put(codes[i], i);

            StringBuilder bits = new StringBuilder();
            for (byte b : compressed) {
                String bin = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                bits.append(bin);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String current = "";
            for (int i = 0; i < bits.length(); i++) {
                current += bits.charAt(i);
                if (reverse.containsKey(current)) {
                    int s = reverse.get(current);
                    if (s == 256) break;
                    baos.write(s);
                    current = "";
                }
            }

            byte[] decoded = baos.toByteArray();
            if (decoded.length != origLen) {
                throw new Exception("Size mismatch: expected " + origLen + ", got " + decoded.length);
            }
            try (FileOutputStream fos = new FileOutputStream(output)) {
                fos.write(decoded);
            }
        }
    }
}