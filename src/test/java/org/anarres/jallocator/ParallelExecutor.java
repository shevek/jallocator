/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import com.google.common.base.Throwables;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author shevek
 */
public class ParallelExecutor {

    private static final Log LOG = LogFactory.getLog(ParallelExecutor.class);

    public interface Task {

        public void run() throws Exception;
    }
    private final Executor executor;
    private final CountDownLatch latch;
    private int count;
    private final AtomicReference<Throwable> throwable = new AtomicReference<Throwable>();

    public ParallelExecutor(Executor executor, int count) {
        this.executor = executor;
        this.latch = new CountDownLatch(count);
        this.count = count;
    }

    public void submit(final String message, final Task task) {
        if (count <= 0)
            throw new IllegalStateException("Already submitted enough tasks for latch.");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    if (!throwable.compareAndSet(null, t))
                        LOG.error("Additional failure: " + message + ": " + t.getMessage());
                } finally {
                    latch.countDown();
                }
            }
        });
        count--;
    }

    public void check() throws Exception {
        Throwable t = throwable.get();
        if (t != null)
            throw Throwables.propagate(t);
    }

    public void await() throws Exception {
        if (count != 0)
            throw new IllegalStateException("Not submitted enough tasks.");
        latch.await();
        check();
    }
}
