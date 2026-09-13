package ru.kishko;

public class MonotoneCoder {

    public static void encode(int n, BitOutputStream out) throws java.io.IOException {
        if (n == 0) {
            out.writeBit(0);
            return;
        }

        int k = 31 - Integer.numberOfLeadingZeros(n); // log2n

        // unar(k+1)
        for (int i = 0; i < k + 1; i++) out.writeBit(1);
        out.writeBit(0);

        // bin(n − 2^k, k)
        int remainder = n - (1 << k);
        for (int i = k - 1; i >= 0; i--) {
            out.writeBit((remainder >> i) & 1);
        }
    }

    public static int decode(BitInputStream in) throws java.io.IOException {
        int first = in.readBit();
        if (first == 0) return 0;

        int k = 1;
        while (true) {
            int b = in.readBit();
            if (b == 0) break;
            k++;
        }
        int log = k - 1;

        int remainder = 0;
        for (int i = 0; i < log; i++) {
            int b = in.readBit();
            remainder = (remainder << 1) | b;
        }

        return (1 << log) + remainder;
    }
}