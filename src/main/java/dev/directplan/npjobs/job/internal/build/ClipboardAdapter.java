package dev.directplan.npjobs.job.internal.build;

import org.bukkit.entity.Player;

/**
 * @author DirectPlan
 */
public interface ClipboardAdapter {

    /**
     * Retrieves the selection of the specified player and
     * creates a new clipboard instance with said selection
     * and specified context.
     *
     * @param player the player
     * @param context clipboard context
     * @return a new clipboard instance based on the player's selection
     * and the provided context.
     * @throws ClipboardException If something is wrong with the player or clipboard.
     */
    Clipboard select(Player player, ClipboardContext context) throws ClipboardException;

    /**
     * Creates a new simple implementation of {@link Clipboard}
     * using the provided selection and context.
     *
     * @param selection cuboid selection
     * @param context clipboard context
     * @return a new clipboard instance
     * @throws ClipboardException if something is wrong with the selection or clipboard.
     * @see Clipboard#select(Selection, ClipboardContext)
     */
    default Clipboard select(Selection selection, ClipboardContext context) throws ClipboardException {
        return Clipboard.select(selection, context);
    }
}
