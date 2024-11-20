package dev.directplan.npjobs.keyed;

import java.util.Locale;

/**
 * @author DirectPlan
 */
public interface Keyed {

    String key();

    static Keyed simple(String key) {
        return () -> key.toLowerCase(Locale.ROOT);
    }
}
