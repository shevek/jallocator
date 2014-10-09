/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author shevek
 */
@ThreadSafe
public class ResourceAllocator<T> implements Iterable<T> {

    // private static final Logger LOG = LoggerFactory.getLogger(ResourceAllocator.class);
    private static Random RANDOM = new Random();
    @Nonnull
    private ResourceProvider<T> resourceProvider;
    @Nonnull
    private PermutationGenerator generator;
    @Nonnull
    private final AtomicLong counter = new AtomicLong(0);

    public ResourceAllocator(@Nonnull ResourceProvider<T> resourceProvider) {
        setResourceProvider(resourceProvider);
    }

    @Nonnull
    public ResourceProvider<T> getResourceProvider() {
        return resourceProvider;
    }

    // TODO: I wish this were atomic. It'll violate the contract of ResourceProvider and presumably OOB otherwise.
    public void setResourceProvider(@Nonnull ResourceProvider<T> resourceProvider) {
        if (!Objects.equal(this.resourceProvider, resourceProvider)) {
            this.resourceProvider = Preconditions.checkNotNull(resourceProvider, "ResourceProvider was null.");
            this.generator = new PermutationGenerator(resourceProvider.getResourceCount(), System.currentTimeMillis() ^ RANDOM.nextLong());
        }
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return new ResourceIterator<T>(resourceProvider, generator, counter);
    }
}