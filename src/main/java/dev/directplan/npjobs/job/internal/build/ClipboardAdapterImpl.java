package dev.directplan.npjobs.job.internal.build;

import org.bukkit.entity.Player;

/**
 * An internal implementation of {@link ClipboardAdapter}
 * and should not be directly used. Use {@link Clipboard#select(Selection, ClipboardContext)} instead.
 * 
 * @author DirectPlan
 */
public final class ClipboardAdapterImpl implements ClipboardAdapter {

    ClipboardAdapterImpl() {}

    @Override
    public Clipboard select(Player player, ClipboardContext context) {
        throw new UnsupportedOperationException("Player clipboard is not supported");
    }
}
