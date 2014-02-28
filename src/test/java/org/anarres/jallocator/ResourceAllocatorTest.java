/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class ResourceAllocatorTest {

    private static final Log LOG = LogFactory.getLog(ResourceAllocatorTest.class);

    private static class LongResourceProvider implements ResourceProvider<Long> {

        private final long count;

        public LongResourceProvider(long count) {
            this.count = count;
        }

        @Override
        public long getResourceCount() {
            return count;
        }

        @Override
        public Long getResource(long index) {
            return index;
        }
    };

    @Test
    public void testProvider() {
        long count = 1634;
        ResourceProvider<Long> resourceProvider = new LongResourceProvider(count);
        ResourceAllocator<Long> allocator = new ResourceAllocator<Long>(resourceProvider);
        Set<Long> set = new HashSet<Long>();
        for (Long r : allocator)
            assertTrue(set.add(r));
        assertEquals(count, set.size());
    }

    @Test
    public void testParallel() throws Exception {
        long count = 1298371;
        ResourceProvider<Long> resourceProvider = new LongResourceProvider(count);
        final ResourceAllocator<Long> allocator = new ResourceAllocator<Long>(resourceProvider);
        final ConcurrentMap<Long, Class<Void>> set = new ConcurrentHashMap<Long, Class<Void>>();

        class AllocateTask implements ParallelExecutor.Task {

            int count = 0;

            @Override
            public void run() throws Exception {
                for (Long r : allocator)
                    if (set.putIfAbsent(r, Void.class) == null)
                        count++;
            }
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<AllocateTask> tasks = new ArrayList<AllocateTask>();
        for (int i = 0; i < 40; i++)
            tasks.add(new AllocateTask());

        ExecutorService service = Executors.newCachedThreadPool();
        ParallelExecutor executor = new ParallelExecutor(service, tasks.size());
        for (AllocateTask task : tasks)
            executor.submit("Task " + task, task);
        executor.await();
        service.shutdownNow();
        LOG.info("Run took " + stopwatch);

        // Make sure every resource was allocated at least once.
        assertEquals(count, set.size());

        // Make sure no resource was allocated more than once.
        int total = 0;
        for (AllocateTask task : tasks)
            total += task.count;
        assertEquals(count, total);
    }
}