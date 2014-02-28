/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class PermutationGeneratorTest {

    private static final Log LOG = LogFactory.getLog(PermutationGeneratorTest.class);

    private void verify(PermutationGenerator generator) {
        LOG.info("Testing " + generator);

        int range = (int) generator.getRange();
        byte[] count = new byte[range];

        /* For all numbers in the range, verify increment the counter for the
         * the output. */
        for (int in = 0; in < range; in++) {
            long out = generator.shuffle(in);
            count[(int) out]++;
            long rin = generator.unshuffle(out);
            assertEquals(in, rin);
        }

        // LOG.info("range=" + range + "; counts=" + Arrays.toString(count));

        /* Now check the output to make sure that every counter is set exactly
         * to the value of '1'. */
        for (int out = 0; out < count.length; out++) {
            assertEquals(1, count[out]);
        }
    }

    @Test
    public void testPermutationGenerator() {
        long[] seeds = new long[]{1, 54, 12311, 8173722, 1380821045564L, System.currentTimeMillis(), System.nanoTime()};
        long[] ranges = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 254, 255, 256, 257, 65534, 65535, 65536, 262143, 262144, 262154};
        for (long seed : seeds)
            for (long range : ranges)
                verify(new PermutationGenerator(range, seed));
    }

    @Test
    public void testPermutationSingle() {
        // These magic numbers caused a failure in a prior release.
        PermutationGenerator generator = new PermutationGenerator(4, 1380821045564L);
        for (long in = 3; in < generator.getRange(); in++) {
            long out = generator.shuffle(in);
            long rin = generator.unshuffle(out);
            LOG.info(in + " -> " + out + " -> " + rin);
            assertEquals(in, rin);
        }
    }
}