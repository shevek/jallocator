/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import com.google.common.collect.AbstractIterator;
import com.google.common.math.LongMath;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author shevek
 */
public class ResourceIterator<T> extends AbstractIterator<T> {

    private static final Log LOG = LogFactory.getLog(ResourceIterator.class);
    @Nonnull
    private final ResourceProvider<T> resourceProvider;
    @Nonnull
    private final PermutationGenerator generator;
    @Nonnull
    private final AtomicLong counter;
    @Nonnegative
    private final long counterStart;
    @Nonnegative
    private final long counterRange;

    public ResourceIterator(@Nonnull ResourceProvider<T> resourceProvider, @Nonnull PermutationGenerator generator, @Nonnull AtomicLong counter) {
        this.resourceProvider = resourceProvider;
        this.generator = generator;
        this.counter = counter;
        this.counterStart = counter.get();
        this.counterRange = resourceProvider.getResourceCount();
    }

    // Watch carefully...
    @Override
    protected T computeNext() {
        // TODO: This has a weakness that if it overruns, it will consume one more value
        // from the counter than it attempts to allocate. This could potentially prevent
        // another client from seeing (and allocating) that value, although if
        // the algorithm loops around the permutation faster than the expiry time
        // of resources, then the resource is known unallocated, as were it
        // allocated, we would have allocated it on the first pass.
        long counterValue = counter.getAndIncrement();
        // LOG.info("Start=" + counterStart + ", value=" + counterValue + ", range=" + counterRange);
        if (counterValue >= counterStart + counterRange)    // overflow will fuck us.
            return endOfData();
        long sequenceValue = LongMath.mod(counterValue, counterRange);
        long randomValue = generator.shuffle(sequenceValue);
        return resourceProvider.getResource(randomValue);
    }
}