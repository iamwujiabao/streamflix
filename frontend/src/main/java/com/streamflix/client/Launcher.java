package com.streamflix.client;

/**
 * Plain Java launcher used by the shaded jar.
 *
 * <p>JavaFX 11+ is no longer part of the JDK and refuses to launch
 * an Application from a class that <i>extends</i> Application unless
 * the modules are present. This launcher avoids that constraint by
 * simply delegating to {@link StreamflixApp#main}.
 */
public final class Launcher {
    public static void main(String[] args) {
        StreamflixApp.main(args);
    }
}
