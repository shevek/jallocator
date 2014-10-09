/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import com.google.common.base.Objects;
import com.google.common.math.LongMath;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/*
 BlackRock cipher

 (h/t Marsh Ray @marshray for this idea)

 This is a randomization/reshuffling function based on a crypto
 "Feistel network" as described in the paper:

 'Ciphers with Arbitrary Finite Domains' 
 by John Black and Phillip Rogaway 
 http://www.cs.ucdavis.edu/~rogaway/papers/subset.pdf

 This is a crypto-like construction that encrypts an arbitrary sized
 range. Given a number in the range [0..9999], it'll produce a mapping 
 to a distinct different number in the same range (and back again).
 In other words, it randomizes the order of numbers in a sequence.

 For example, it can be used to  randomize the sequence [0..9]:
    
 0 ->      6
 1 ->      4
 2 ->      8
 3 ->      1
 4 ->      9
 5 ->      3
 6 ->      0
 7 ->      5
 8 ->      2
 9 ->      7

 As you can see on the right hand side, the numbers are in random
 order, and they don't repeaet.

 This is create for port scanning. We can take an index variable
 and increment it during a scan, then use this function to
 randomize it, yet be assured that we've probed every IP and port
 within the range.

 The cryptographic strength of this construction depends upon the 
 number of rounds, and the exact nature of the inner "F()" function.
 Because it's a Feistal network, that "F()" function can be almost
 anything.

 We don't care about cryptographic strength, just speed, so we are
 using a trivial F() function.

 This is a class of "format-preserving encryption". There are 
 probably better constructions than what I'm using.
 */
/**
 * http://www.cs.ucdavis.edu/~rogaway/papers/subset.pdf
 * https://raw.github.com/robertdavidgraham/masscan/master/src/rand-blackrock.c
 * 
 *
 * @author shevek
 */
public class PermutationGenerator {

    // private static final Logger LOG = LoggerFactory.getLogger(PermutationGenerator.class);
    /***************************************************************************
     * It's an s-box. You gotta have an s-box
     ***************************************************************************/
    private static char[] sbox = new char[]{
        /* block 1 */
        0x91, 0x58, 0xb3, 0x31, 0x6c, 0x33, 0xda, 0x88,
        0x57, 0xdd, 0x8c, 0xf2, 0x29, 0x5a, 0x08, 0x9f,
        0x49, 0x34, 0xce, 0x99, 0x9e, 0xbf, 0x0f, 0x81,
        0xd4, 0x2f, 0x92, 0x3f, 0x95, 0xf5, 0x23, 0x00,
        0x0d, 0x3e, 0xa8, 0x90, 0x98, 0xdd, 0x20, 0x00,
        0x03, 0x69, 0x0a, 0xca, 0xba, 0x12, 0x08, 0x41,
        0x6e, 0xb9, 0x86, 0xe4, 0x50, 0xf0, 0x84, 0xe2,
        0xb3, 0xb3, 0xc8, 0xb5, 0xb2, 0x2d, 0x18, 0x70,
        /* block 2 */
        0x0a, 0xd7, 0x92, 0x90, 0x9e, 0x1e, 0x0c, 0x1f,
        0x08, 0xe8, 0x06, 0xfd, 0x85, 0x2f, 0xaa, 0x5d,
        0xcf, 0xf9, 0xe3, 0x55, 0xb9, 0xfe, 0xa6, 0x7f,
        0x44, 0x3b, 0x4a, 0x4f, 0xc9, 0x2f, 0xd2, 0xd3,
        0x8e, 0xdc, 0xae, 0xba, 0x4f, 0x02, 0xb4, 0x76,
        0xba, 0x64, 0x2d, 0x07, 0x9e, 0x08, 0xec, 0xbd,
        0x52, 0x29, 0x07, 0xbb, 0x9f, 0xb5, 0x58, 0x6f,
        0x07, 0x55, 0xb0, 0x34, 0x74, 0x9f, 0x05, 0xb2,
        /* block 3 */
        0xdf, 0xa9, 0xc6, 0x2a, 0xa3, 0x5d, 0xff, 0x10,
        0x40, 0xb3, 0xb7, 0xb4, 0x63, 0x6e, 0xf4, 0x3e,
        0xee, 0xf6, 0x49, 0x52, 0xe3, 0x11, 0xb3, 0xf1,
        0xfb, 0x60, 0x48, 0xa1, 0xa4, 0x19, 0x7a, 0x2e,
        0x90, 0x28, 0x90, 0x8d, 0x5e, 0x8c, 0x8c, 0xc4,
        0xf2, 0x4a, 0xf6, 0xb2, 0x19, 0x83, 0xea, 0xed,
        0x6d, 0xba, 0xfe, 0xd8, 0xb6, 0xa3, 0x5a, 0xb4,
        0x48, 0xfa, 0xbe, 0x5c, 0x69, 0xac, 0x3c, 0x8f,
        /* block 4 */
        0x63, 0xaf, 0xa4, 0x42, 0x25, 0x50, 0xab, 0x65,
        0x80, 0x65, 0xb9, 0xfb, 0xc7, 0xf2, 0x2d, 0x5c,
        0xe3, 0x4c, 0xa4, 0xa6, 0x8e, 0x07, 0x9c, 0xeb,
        0x41, 0x93, 0x65, 0x44, 0x4a, 0x86, 0xc1, 0xf6,
        0x2c, 0x97, 0xfd, 0xf4, 0x6c, 0xdc, 0xe1, 0xe0,
        0x28, 0xd9, 0x89, 0x7b, 0x09, 0xe2, 0xa0, 0x38,
        0x74, 0x4a, 0xa6, 0x5e, 0xd2, 0xe2, 0x4d, 0xf3,
        0xf4, 0xc6, 0xbc, 0xa2, 0x51, 0x58, 0xe8, 0xae,};

    private static class Parameters {

        private final long a;
        private final long b;

        public Parameters(long a, long b) {
            this.a = a;
            this.b = b;
        }
    }
    private final long range;
    private final long seed;
    private final int rounds = 3;
    private final long a;
    private final long b;

    public PermutationGenerator(@Nonnegative long range, long seed) {
        this.range = range;
        this.seed = seed;
        Parameters parameters = init(range);
        this.a = parameters.a;
        this.b = parameters.b;
    }

    @Nonnegative
    public long getRange() {
        return range;
    }

    /***************************************************************************
     ***************************************************************************/
    private static Parameters init(final long range) {
        /* This algorithm gets very non-random at small numbers, so I'm going
         * to try to fix some constants here to make it work. It doesn't have
         * to be good, since it's kinda pointless having ranges this small */
        switch ((int) range) {
            case 0:
                return new Parameters(0, 0);
            case 1:
                return new Parameters(1, 1);
            case 2:
                return new Parameters(1, 2);
            case 3:
                return new Parameters(2, 2);
            case 4:
            case 5:
            case 6:
                return new Parameters(2, 3);
            case 7:
            case 8:
                return new Parameters(3, 3);
        }

        double foo = Math.sqrt((double) range);
        long a = (long) (foo - 1);
        long b = (long) (foo + 1);
        while (a * b <= range)
            b++;
        return new Parameters(a, b);
    }
    /***************************************************************************
     * This is a random meaningless function. Well, if we actually wanted
     * crypto-strength, we'd have to improve it, but for now, we just want
     * some random properties.
     ***************************************************************************/
    private static long[] primes = new long[]{961752031L, 982324657L, 15485843L, 961752031L,};

    @Nonnegative
    private static long F(@Nonnegative final int round, @Nonnegative final long R, final long seed) {
        long out = ((R << (R & 0x4)) + R + seed);
        out ^= sbox[(int) (out & 0xF)];
        out = (((primes[round] * out + 25L) ^ out) + round);
        // LOG.info("F(" + round + ", " + R + ", " + seed + " = " + out);
        return out;
    }

    @Nonnegative
    private static long fe(@Nonnegative int rounds, @Nonnegative long a, @Nonnegative long b, @Nonnegative long m, long seed) {
        long L = m % a;
        long R = m / a;

        for (int round = 1; round <= rounds; round++) {
            // LOG.info("round=" + round + ", L=" + L + ", R=" + R);
            long tmp;
            if ((round & 1) != 0) {
                tmp = LongMath.mod(L + F(round, R, seed), a);
            } else {
                tmp = LongMath.mod(L + F(round, R, seed), b);
            }
            L = R;
            R = tmp;
            assert L >= 0 && R >= 0 : "a=" + a + ", b=" + b + ", m=" + m + ", round=" + round + ", L=" + L + ", R=" + R;
        }

        if ((rounds & 1) != 0) {
            return a * L + R;
        } else {
            return a * R + L;
        }
    }

    @Nonnegative
    private static long unfe(@Nonnegative int rounds, @Nonnegative long a, @Nonnegative long b, @Nonnegative long m, long seed) {
        long L, R;

        if ((rounds & 1) != 0) {
            R = m % a;
            L = m / a;
        } else {
            L = m % a;
            R = m / a;
        }

        for (int round = rounds; round >= 1; round--) {
            long tmp;
            if ((round & 1) != 0) {
                tmp = F(round, L, seed);
                tmp = LongMath.mod(R - tmp, a);
            } else {
                tmp = F(round, L, seed);
                tmp = LongMath.mod(R - tmp, b);
            }
            R = L;
            L = tmp;
        }
        return a * R + L;
    }

    private void check(@Nonnegative long v, @Nonnull String purpose) {
        if (v < 0)
            throw new IllegalArgumentException("Illegal negative " + purpose + " " + v);
        if (v >= range)
            throw new IllegalArgumentException("Illegal " + purpose + " " + v + " > " + range);
    }

    @Nonnegative
    public long shuffle(@Nonnegative long m) {
        check(m, "input");
        long c = fe(rounds, a, b, m, seed);
        while (c >= range)
            c = fe(rounds, a, b, c, seed);
        if (c < 0)
            throw new IllegalStateException(this + " generated " + c + " for input " + m);
        return c;
    }

    @Nonnegative
    public long unshuffle(@Nonnegative long m) {
        check(m, "input");
        long c = unfe(rounds, a, b, m, seed);
        while (c >= range)
            c = unfe(rounds, a, b, c, seed);
        if (c < 0)
            throw new IllegalStateException(this + " generated " + c + " for input " + m);
        return c;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("range", range)
                .add("seed", seed)
                .add("rounds", rounds)
                .add("a", a)
                .add("b", b)
                .toString();
    }
}