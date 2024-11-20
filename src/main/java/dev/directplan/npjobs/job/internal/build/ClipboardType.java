package dev.directplan.npjobs.job.internal.build;

import org.bukkit.entity.Player;

/**
 * @author DirectPlan
 */
@FunctionalInterface
public interface ClipboardType {

    /**
     * Applies this function to the given {@link ClipboardAdapter adapter}.
     *
     * @param adapter the adapter.
     * @return a new instance of {@link Clipboard}.
     * @throws ClipboardException if something is wrong with the clipboard.
     */
    Clipboard apply(ClipboardAdapter adapter) throws ClipboardException;

    static ClipboardType copy(Selection selection, ClipboardContext context) {
        return adapter -> adapter.select(selection, context);
    }

    static ClipboardType copy(Player player, ClipboardContext context) {
        return adapter -> adapter.select(player, context);
    }
}
