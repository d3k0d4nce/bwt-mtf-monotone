package ru.kishko;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EntropyCalculator {

    public static void main(String[] args) {
        String[] files = {
                "bib", "book1", "book2", "geo", "news", "obj1", "obj2",
                "paper1", "paper2", "paper3", "paper4", "paper5", "paper6",
                "pic", "progc", "progl", "progp", "trans"
        };

        String testPath = "tests/";

        System.out.println("File\t\tH(X)\tH(X|X)\tH(X|XX)");
        System.out.println("----------------------------------------");

        for (String file : files) {
            try {
                byte[] data = readFile(testPath + file);
                double h1 = entropyHX(data);
                double h2 = entropyHXX(data);
                double h3 = entropyHXXX(data);
                System.out.printf("%s\t\t%.4f\t%.4f\t%.4f\n", file, h1, h2, h3);
            } catch (IOException e) {
                System.err.println("Error: " + file + " - " + e.getMessage());
            }
        }
    }

    private static double entropyHX(byte[] data) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (byte b : data) {
            freq.merge(b & 0xFF, 1, Integer::sum);
        }
        double total = data.length;
        double entropy = 0.0;
        for (int count : freq.values()) {
            double p = count / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        return entropy;
    }

    private static double entropyHXX(byte[] data) {
        if (data.length < 2) return 0.0;
        Map<Integer, Integer> singleFreq = new HashMap<>();
        for (byte b : data) {
            singleFreq.merge(b & 0xFF, 1, Integer::sum);
        }
        Map<Integer, Integer> pairFreq = new HashMap<>();
        for (int i = 0; i < data.length - 1; i++) {
            int pair = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            pairFreq.merge(pair, 1, Integer::sum);
        }
        double total = data.length - 1;
        double entropy = 0.0;
        for (var entry : pairFreq.entrySet()) {
            int pair = entry.getKey();
            int count = entry.getValue();
            int first = pair >> 8;
            double p = count / total;
            double pCond = (double) count / singleFreq.get(first);
            entropy -= p * Math.log(pCond) / Math.log(2);
        }
        return entropy;
    }

    private static double entropyHXXX(byte[] data) {
        if (data.length < 3) return 0.0;
        Map<Integer, Integer> pairFreq = new HashMap<>();
        for (int i = 0; i < data.length - 2; i++) {
            int pair = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            pairFreq.merge(pair, 1, Integer::sum);
        }
        Map<Integer, Integer> tripleFreq = new HashMap<>();
        for (int i = 0; i < data.length - 2; i++) {
            int triple = ((data[i] & 0xFF) << 16) |
                    ((data[i + 1] & 0xFF) << 8) |
                    (data[i + 2] & 0xFF);
            tripleFreq.merge(triple, 1, Integer::sum);
        }
        double total = data.length - 2;
        double entropy = 0.0;
        for (var entry : tripleFreq.entrySet()) {
            int triple = entry.getKey();
            int count = entry.getValue();
            int pair = (triple >> 8) & 0xFFFF;
            double p = count / total;
            double pCond = (double) count / pairFreq.get(pair);
            entropy -= p * Math.log(pCond) / Math.log(2);
        }
        return entropy;
    }

    private static byte[] readFile(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            return data;
        }
    }
}