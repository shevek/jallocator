/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.jallocator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public interface ResourceProvider<T> {

    @Nonnegative
    public long getResourceCount();

    @Nonnull
    public T getResource(@Nonnegative long index);
}
