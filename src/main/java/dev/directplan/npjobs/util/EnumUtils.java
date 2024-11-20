package dev.directplan.npjobs.util;

import com.google.common.base.Preconditions;

/**
 * @author DirectPlan
 */
public class EnumUtils {

    public static <E extends Enum<E>> void checkAscendance(E from, E to) {
        Preconditions.checkState(to.ordinal() >= from.ordinal(),
                "Cannot change enum from " + from + " to " + to + ".");
    }
}
