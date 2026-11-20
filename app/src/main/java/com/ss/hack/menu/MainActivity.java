package com.ss.hack.menu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.Gravity;
import android.util.TypedValue;

/**
 * Subway Surfers external menu launcher.
 * Package targets: com.kiloo.subwaysurf
 * Dump symbols: WalletModel, OwnedCharacterState, CurrencyType
 */
public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundColor(Color.parseColor("#0A0E17"));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Subway Surfers Menu");
        title.setTextColor(Color.parseColor("#FFD600"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("External menu · auto-attach\nCoins / Keys / Unlock all / Free shop / God / Noclip / Skin");
        sub.setTextColor(Color.parseColor("#AAAAAA"));
        sub.setTextSize(13);
        sub.setPadding(0, 12, 0, 28);
        root.addView(sub);

        Button overlay = btn("1. Cap quyen Overlay");
        overlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            } else toast("Overlay OK");
        });
        root.addView(overlay);

        Button start = btn("2. Bat Menu (auto inject)");
        start.setBackgroundColor(Color.parseColor("#1B5E20"));
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                toast("Can Overlay truoc");
                return;
            }
            Intent i = new Intent(this, MenuService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
            else startService(i);
            toast("Menu started - mo Subway Surfers");
            moveTaskToBack(true);
        });
        root.addView(start);

        Button stop = btn("3. Tat Menu");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, MenuService.class));
            toast("Stopped");
        });
        root.addView(stop);

        TextView note = new TextView(this);
        note.setText("\nPackage: com.kiloo.subwaysurf\n"
                + "WalletModel.Currencies (coins/keys)\n"
                + "OwnedCharacterState (unlock char)\n"
                + "Can root / VM de attach memory.\n"
                + "Offset OB thay doi - dien trong MemoryBridge.");
        note.setTextColor(Color.parseColor("#777777"));
        note.setTextSize(12);
        root.addView(note);

        setContentView(scroll);
    }

    private Button btn(String t) {
        Button b = new Button(this);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor("#1A2332"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 12;
        b.setLayoutParams(lp);
        return b;
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
