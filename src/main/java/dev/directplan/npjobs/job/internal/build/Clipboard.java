package dev.directplan.npjobs.job.internal.build;

import dev.directplan.npjobs.job.internal.build.worldedit.WorldEditClipboard;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * <h1>Internal implementations:</h1>
 * <ol>
 *     <li>{@link ClipboardImpl}</ul>
 *     <li>{@link WorldEditClipboard}</ul>
 * </ol>
 * @author DirectPlan
 */
public interface Clipboard extends Iterable<Supplier<Placeable>> {

    /**
     * Gets the context of this clipboard. Like whether it contains entities or
     * air blocks.
     *
     * @return the clipboard context
     */
    @NotNull
    ClipboardContext getContext();

    /**
     * Gets the location of where this clipboard was copied at.
     *
     * @return the origin
     */
    @NotNull
    Vector getOrigin();

    /**
     * Gets the volume of this clipboard
     *
     * @return the volume
     */
    long getVolume();

    /**
     * Gets the width of this clipboard
     *
     * @return the width
     */
    int getWidth();

    /**
     * Gets the height of this clipboard
     *
     * @return the height
     */
    int getHeight();

    /**
     * Gets the length of this clipboard
     *
     * @return the length
     */
    int getLength();

    /**
     * An iterator of {@link Placeable}s (blocks and entities) contained
     * in a supplier.
     * <p>
     * {@link CuboidIterator} is an internal iterator currently implemented in both the default clipboard
     * and the WorldEdit clipboard. It iterates over a non-null, memoized set of suppliers.
     * </p>
     * <p>
     * Suppliers of {@link Placeable} may supply a null instance if this clipboard
     * is configured to ignore air ({@link ClipboardContext#isIgnoreAir()}
     * and the block at a traversed position is also air.
     * </p>
     *
     * @return iterator of this clipboard
     */
    @NotNull
    PlaceableIterator iterator();

    /**
     * Creates an internal implementation of {@link Clipboard}
     * using the given selection and context.
     *
     * @param selection cuboid selection
     * @param context clipboard context
     * @return a new clipboard instance with the provided selection and context.
     */
    @NotNull
    static Clipboard select(Selection selection, ClipboardContext context) {
        return new ClipboardImpl(selection, context);
    }
}
