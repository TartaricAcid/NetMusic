package com.github.tartaricacid.netmusic.init;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

public class CompatRegistry {
    public static final String CLOTH_CONFIG = "cloth_config";
    public static final String SC = "sophisticatedcore";

    public static final VersionRange SC_VERSION_RANGE;

    static {
        try {
            SC_VERSION_RANGE = VersionRange.createFromVersionSpec("[1.4.2,)");
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }
}
