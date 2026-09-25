package app.edgewindow.extension;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Method;

/** Runtime helper called from each patched Activity's onResume method. */
public final class DisplayCutoutCompat {
    private static final int MODE_SHORT_EDGES = 1;
    // LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS was added in API 30.
    private static final int MODE_ALWAYS = 3;

    private DisplayCutoutCompat() {
    }

    public static void apply(final Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < 28) return;

        try {
            applyToWindow(activity);

            // Some apps adjust their window during onResume. Apply once more
            // after that callback has finished to restore the requested mode.
            final Window window = activity.getWindow();
            final View decorView = window == null ? null : window.getDecorView();
            if (decorView != null) {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (activity.isFinishing()) return;
                        try {
                            applyToWindow(activity);
                        } catch (RuntimeException ignored) {
                            // A delayed OEM window failure must not crash the host app.
                        } catch (LinkageError ignored) {
                            // Keep the delayed hook fail-soft on older framework builds.
                        }
                    }
                });
            }
        } catch (RuntimeException ignored) {
            // Unsupported OEM window implementations must not break app startup.
        } catch (LinkageError ignored) {
            // Keep the hook fail-soft on devices with incomplete framework APIs.
        }
    }

    private static void applyToWindow(Activity activity) {
        Window window = activity.getWindow();
        if (window == null) return;

        WindowManager.LayoutParams attributes = window.getAttributes();
        int requestedMode = Build.VERSION.SDK_INT >= 30 ? MODE_ALWAYS : MODE_SHORT_EDGES;
        if (attributes.layoutInDisplayCutoutMode != requestedMode) {
            attributes.layoutInDisplayCutoutMode = requestedMode;
            window.setAttributes(attributes);
        }

        if (Build.VERSION.SDK_INT >= 30) {
            if (!setDecorFitsSystemWindows(window)) {
                applyLegacyEdgeToEdgeFlags(window.getDecorView());
            }
        } else {
            applyLegacyEdgeToEdgeFlags(window.getDecorView());
        }
    }

    private static boolean setDecorFitsSystemWindows(Window window) {
        try {
            Method method = Window.class.getMethod("setDecorFitsSystemWindows", boolean.class);
            method.invoke(window, false);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static void applyLegacyEdgeToEdgeFlags(View decorView) {
        if (decorView == null) return;

        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(flags);
    }
}
