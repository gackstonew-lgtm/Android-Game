package com.gackstone.chase.android;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gackstone.chase.ChaseGame;

/**
 * Native Android entry point activity for Chase.
 * Configures hardware acceleration, immersive sticky full-screen landscape,
 * sensor battery optimization, and persistent preferences integration.
 */
public class AndroidLauncher extends AndroidApplication {

    private ChaseGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen active during gameplay
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Configure LibGDX Android Runtime
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = false; // GLES 2.0 ensures 100% universal Android hardware compatibility
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.numSamples = 2; // Lightweight MSAA
        config.useImmersiveMode = true;

        // Initialize persistent save bridge
        AndroidPreferencesBridge preferencesBridge = new AndroidPreferencesBridge(getApplicationContext());
        game = new ChaseGame(preferencesBridge);

        initialize(game, config);
        enableImmersiveStickyMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveStickyMode();
        }
    }

    private void enableImmersiveStickyMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
