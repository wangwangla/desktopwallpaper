package kw.wallpaper;

import java.lang.annotation.*;

/** Annotate a launcher to make it discoverable in the shared wallpaper library. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WallpaperDefinition {
    String id();
    String title();
    String description();
    int accent();
    String thumbnail();
    String sceneTitle();
    String controllerTitle();
    String[] executables() default {};
}
