package dev.directplan.npjobs.job.internal.build;

/**
 * @author DirectPlan
 */
public final class ClipboardContext {

    public static final ClipboardContext DEFAULT = builder().build();

    private final boolean ignoreAir;
    private final boolean copyEntities;

    private ClipboardContext(boolean ignoreAir, boolean copyEntities) {
        this.ignoreAir = ignoreAir;
        this.copyEntities = copyEntities;
    }

    public boolean isIgnoreAir() {
        return ignoreAir;
    }

    public boolean isCopyEntities() {
        return copyEntities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean ignoreAir = true;
        private boolean copyEntities = true;

        private Builder() {}

        public Builder ignoreAir(boolean ignoreAir) {
            this.ignoreAir = ignoreAir;
            return this;
        }

        public Builder copyEntities(boolean copyEntities) {
            this.copyEntities = copyEntities;
            return this;
        }

        public ClipboardContext build() {
            return new ClipboardContext(ignoreAir, copyEntities);
        }
    }
}
