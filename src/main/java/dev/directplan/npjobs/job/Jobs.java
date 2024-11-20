package dev.directplan.npjobs.job;

import dev.directplan.npjobs.keyed.Keyed;

/**
 * Internal Job Constants
 *
 * @author DirectPlan
 */
public enum Jobs implements Keyed {

    BUILD {
        @Override
        public String key() {
            return "build";
        }
    }
}
