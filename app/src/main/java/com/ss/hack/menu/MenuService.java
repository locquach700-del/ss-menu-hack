package com.ss.hack.menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MenuService extends Service {

    public static HackConfig cfg = new HackConfig();

    private WindowManager wm;
    private View bubble;
    private View panel;
    private WindowManager.LayoutParams bubbleLp;
    private WindowManager.LayoutParams panelLp;
    private Handler handler;
    private Runnable tick;
    private MemoryBridge bridge;
    private boolean panelVisible;

    @Override
    public void onCreate() {
        super.onCreate();
        startFg();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        bridge = new MemoryBridge();
        bridge.setContext(this);
        handler = new Handler(Looper.getMainLooper());
        addBubble();
        boolean ok = bridge.attach("com.kiloo.subwaysurf");
        if (!ok) ok = bridge.attach("com.kiloo.subwaysurf.gp");
        Toast.makeText(this, ok ? ("OK " + bridge.modeLabel()) : "Mo SS roi Re-Attach (khong can root)", Toast.LENGTH_LONG).show();
        tick = new Runnable() {
            @Override public void run() {
                bridge.apply(cfg);
                handler.postDelayed(this, 500);
            }
        };
        handler.post(tick);
    }

    private void startFg() {
        String ch = "ss_menu";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(ch, "SS Menu",
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, ch) : new Notification.Builder(this);
        startForeground(2, b.setContentTitle("SS Menu Hack")
                .setContentText("No-root mode")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build());
    }

    private void addBubble() {
        TextView tv = new TextView(this);
        tv.setText("SS");
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.parseColor("#FFD600"));
        tv.setPadding(dp(18), dp(18), dp(18), dp(18));
        bubbleLp = baseLp(true);
        bubbleLp.width = dp(52);
        bubbleLp.height = dp(52);
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        bubbleLp.x = 30;
        bubbleLp.y = 200;
        tv.setOnTouchListener(new View.OnTouchListener() {
            int lx, ly;
            boolean moved;
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lx = (int) e.getRawX(); ly = (int) e.getRawY(); moved = false; return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) e.getRawX() - lx, dy = (int) e.getRawY() - ly;
                        if (Math.abs(dx) + Math.abs(dy) > 8) moved = true;
                        bubbleLp.x += dx; bubbleLp.y += dy;
                        lx = (int) e.getRawX(); ly = (int) e.getRawY();
                        wm.updateViewLayout(bubble, bubbleLp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) togglePanel();
                        return true;
                }
                return false;
            }
        });
        bubble = tv;
        wm.addView(bubble, bubbleLp);
    }

    private void togglePanel() {
        if (panelVisible) {
            if (panel != null) wm.removeView(panel);
            panel = null;
            panelVisible = false;
        } else {
            showPanel();
            panelVisible = true;
        }
    }

    private void showPanel() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.setBackgroundColor(Color.parseColor("#E6101820"));
        scroll.addView(root);

        TextView t = new TextView(this);
        t.setText("SS HACK (NO ROOT)");
        t.setTextColor(Color.parseColor("#FFD600"));
        t.setTextSize(16);
        root.addView(t);

        root.addView(cb("Unlimited Coins", cfg.unlimitedCoins, v -> {
            cfg.unlimitedCoins = ((CheckBox) v).isChecked();
            if (cfg.unlimitedCoins) bridge.setCoins(cfg.coinAmount);
        }));
        root.addView(cb("Unlimited Keys", cfg.unlimitedKeys, v -> {
            cfg.unlimitedKeys = ((CheckBox) v).isChecked();
            if (cfg.unlimitedKeys) bridge.setKeys(cfg.keyAmount);
        }));

        TextView coinL = new TextView(this);
        coinL.setText("Coins: " + cfg.coinAmount);
        coinL.setTextColor(Color.WHITE);
        root.addView(coinL);
        SeekBar coinBar = new SeekBar(this);
        coinBar.setMax(100);
        coinBar.setProgress(50);
        coinBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                cfg.coinAmount = 10000 + p * 100000;
                coinL.setText("Coins: " + cfg.coinAmount);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                if (cfg.unlimitedCoins) bridge.setCoins(cfg.coinAmount);
            }
        });
        root.addView(coinBar);

        root.addView(cb("Unlock ALL Characters", cfg.unlockAllChars, v -> cfg.unlockAllChars = ((CheckBox) v).isChecked()));
        root.addView(cb("Unlock ALL Boards", cfg.unlockAllBoards, v -> cfg.unlockAllBoards = ((CheckBox) v).isChecked()));
        root.addView(cb("Unlock ALL Outfits", cfg.unlockAllOutfits, v -> cfg.unlockAllOutfits = ((CheckBox) v).isChecked()));
        root.addView(cb("Free Shop", cfg.freeShop, v -> cfg.freeShop = ((CheckBox) v).isChecked()));
        root.addView(cb("God Mode", cfg.godMode, v -> cfg.godMode = ((CheckBox) v).isChecked()));
        root.addView(cb("No Hit", cfg.noHit, v -> cfg.noHit = ((CheckBox) v).isChecked()));
        root.addView(cb("Noclip", cfg.noclip, v -> cfg.noclip = ((CheckBox) v).isChecked()));
        root.addView(cb("Mega Jump", cfg.megaJump, v -> cfg.megaJump = ((CheckBox) v).isChecked()));
        root.addView(cb("Super Speed", cfg.superSpeed, v -> cfg.superSpeed = ((CheckBox) v).isChecked()));
        root.addView(cb("Coin Magnet", cfg.magnet, v -> cfg.magnet = ((CheckBox) v).isChecked()));
        root.addView(cb("Double Coins", cfg.doubleCoins, v -> cfg.doubleCoins = ((CheckBox) v).isChecked()));
        root.addView(cb("Score x10", cfg.scoreMultiplier, v -> cfg.scoreMultiplier = ((CheckBox) v).isChecked()));

        Button attach = new Button(this);
        attach.setText("Re-Attach (no root)");
        attach.setOnClickListener(v -> {
            boolean ok = bridge.attach("com.kiloo.subwaysurf");
            if (!ok) ok = bridge.attach("com.kiloo.subwaysurf.gp");
            Toast.makeText(this, ok ? ("OK " + bridge.modeLabel()) : "Chua thay wallet.json - cap All files access", Toast.LENGTH_LONG).show();
        });
        root.addView(attach);

        Button apply = new Button(this);
        apply.setText("Apply Coins/Keys NOW");
        apply.setBackgroundColor(Color.parseColor("#2E7D32"));
        apply.setOnClickListener(v -> {
            boolean a = bridge.setCoins(cfg.coinAmount);
            boolean b = bridge.setKeys(cfg.keyAmount);
            Toast.makeText(this, (a || b) ? ("Saved " + bridge.modeLabel()) : "Khong ghi duoc (All files / data)", Toast.LENGTH_LONG).show();
        });
        root.addView(apply);

        Button close = new Button(this);
        close.setText("Dong menu");
        close.setOnClickListener(v -> togglePanel());
        root.addView(close);

        Button kill = new Button(this);
        kill.setText("Tat het");
        kill.setOnClickListener(v -> stopSelf());
        root.addView(kill);

        panelLp = baseLp(false);
        panelLp.width = dp(280);
        panelLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        panelLp.gravity = Gravity.TOP | Gravity.START;
        panelLp.x = bubbleLp.x + dp(60);
        panelLp.y = bubbleLp.y;
        panel = scroll;
        wm.addView(panel, panelLp);
    }

    private CheckBox cb(String label, boolean on, View.OnClickListener l) {
        CheckBox c = new CheckBox(this);
        c.setText(label);
        c.setChecked(on);
        c.setTextColor(Color.WHITE);
        c.setTextSize(13);
        c.setOnClickListener(l);
        return c;
    }

    private WindowManager.LayoutParams baseLp(boolean notFocusable) {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (notFocusable) flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type, flags, PixelFormat.TRANSLUCENT);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(tick);
        if (bubble != null) wm.removeView(bubble);
        if (panel != null) wm.removeView(panel);
        bridge.detach();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
