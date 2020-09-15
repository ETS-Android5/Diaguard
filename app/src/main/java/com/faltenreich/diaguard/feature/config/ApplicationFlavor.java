package com.faltenreich.diaguard.feature.config;

import androidx.annotation.Nullable;

enum ApplicationFlavor {
    DEMO("demo"),
    BETA("beta"),
    STORE("store");

    private String identifier;

    ApplicationFlavor(String identifier) {
        this.identifier = identifier;
    }

    @Nullable
    static ApplicationFlavor fromIdentifier(String identifier) {
        for (ApplicationFlavor flavor : ApplicationFlavor.values()) {
            if (flavor.identifier.equals(identifier)) {
                return flavor;
            }
        }
        return null;
    }
}
