package dev.directplan.npjobs.job.internal.build;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Represents an iterator for {@link Placeable}s wrapped in a supplier
 *
 * @see CuboidIterator
 * @author DirectPlan
 */
public interface PlaceableIterator extends Iterator<Supplier<Placeable>> {

    PlaceableIterator EMPTY = new PlaceableIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Supplier<Placeable> next() {
            throw new NoSuchElementException("empty");
        }
    };
}
