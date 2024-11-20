package dev.directplan.npjobs.keyed;

/**
 * @author DirectPlan
 */
public interface NamespaceKeyed extends Keyed {

    char NAMESPACE = ':';

    String namespace();

    String namespaceKey();

    static NamespaceKeyed simple(String namespace, String key) {
        return new NamespaceKeyed() {
            @Override
            public String namespace() {
                return namespace;
            }

            @Override
            public String namespaceKey() {
                return key;
            }

            @Override
            public String key() {
                return namespace().toLowerCase()
                        + NAMESPACE
                        + namespaceKey().toLowerCase();
            }
        };
    }
}
