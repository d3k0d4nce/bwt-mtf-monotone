package ru.kishko;

import java.util.Arrays;

public class BWT {

    public static Result encode(byte[] input) {
        int n = input.length;
        if (n == 0) return new Result(new byte[0], 0);

        Integer[] rotations = new Integer[n];
        for (int i = 0; i < n; i++) rotations[i] = i;

        Arrays.sort(rotations, (a, b) -> {
            for (int i = 0; i < n; i++) {
                int ia = (a + i) % n;
                int ib = (b + i) % n;
                int va = input[ia] & 0xFF;
                int vb = input[ib] & 0xFF;
                if (va != vb) return Integer.compare(va, vb);
            }
            return 0;
        });

        byte[] transformed = new byte[n];
        int index = -1;

        for (int row = 0; row < n; row++) {
            int start = rotations[row];
            int last = (start + n - 1) % n;
            transformed[row] = input[last];
            if (start == 0) index = row;
        }

        return new Result(transformed, index);
    }

    public static byte[] decode(byte[] transformed, int index) {
        int n = transformed.length;
        if (n == 0) return new byte[0];
        if (index < 0 || index >= n) throw new IllegalArgumentException("Invalid BWT index");

        int[] freq = new int[256];
        for (byte b : transformed) freq[b & 0xFF]++;

        int[] firstPos = new int[256];
        int sum = 0;
        for (int i = 0; i < 256; i++) {
            firstPos[i] = sum;
            sum += freq[i];
        }

        int[] next = new int[n];
        int[] used = new int[256];
        for (int i = 0; i < n; i++) {
            int sym = transformed[i] & 0xFF;
            next[i] = firstPos[sym] + used[sym];
            used[sym]++;
        }

        byte[] result = new byte[n];
        int cur = index;
        for (int i = n - 1; i >= 0; i--) {
            result[i] = transformed[cur];
            cur = next[cur];
        }

        return result;
    }

    public static final class Result {
        public final byte[] data;
        public final int index;

        public Result(byte[] data, int index) {
            this.data = data;
            this.index = index;
        }
    }
}