package app.edgewindow.extension;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/** Runtime helper that hides the status bar and permits content in display cutouts. */
public final class DisplayCutoutCompat {
    private static final int MODE_SHORT_EDGES = 1;
    // LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS was added in API 30.
    private static final int MODE_ALWAYS = 3;

    private DisplayCutoutCompat() {
    }

    public static void apply(final Activity activity) {
        if (activity == null) return;

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

        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            int requestedMode = Build.VERSION.SDK_INT >= 30 ? MODE_ALWAYS : MODE_SHORT_EDGES;
            if (attributes.layoutInDisplayCutoutMode != requestedMode) {
                attributes.layoutInDisplayCutoutMode = requestedMode;
                window.setAttributes(attributes);
            }
        }

        if (Build.VERSION.SDK_INT >= 30) {
            try {
                if (!Api30.apply(window)) {
                    applyLegacyFullscreen(window);
                }
            } catch (RuntimeException ignored) {
                applyLegacyFullscreen(window);
            } catch (LinkageError ignored) {
                applyLegacyFullscreen(window);
            }
        } else {
            applyLegacyFullscreen(window);
        }
    }

    @SuppressWarnings("deprecation")
    private static void applyLegacyFullscreen(Window window) {
        if (Build.VERSION.SDK_INT < 16) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            return;
        }

        View decorView = window.getDecorView();
        if (decorView == null) return;

        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        decorView.setSystemUiVisibility(flags);
    }

    /** API 30 references are isolated so older Android releases can load this helper. */
    private static final class Api30 {
        private Api30() {
        }

        static boolean apply(Window window) {
            window.setDecorFitsSystemWindows(false);
            android.view.WindowInsetsController controller = window.getInsetsController();
            if (controller == null) return false;

            controller.setSystemBarsBehavior(
                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            controller.hide(android.view.WindowInsets.Type.statusBars());
            return true;
        }
    }
}
