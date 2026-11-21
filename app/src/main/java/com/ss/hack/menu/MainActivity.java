package com.ss.hack.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 9001;
    private TextView targetLabel;
    private String selectedPkg = "com.kiloo.subwaysurf";

    private static final String[] PRESETS = new String[] {
            "com.kiloo.subwaysurf",
            "com.kiloo.subwaysurf.gp",
            "com.sybo.subwaysurf",
            "com.imangi.templerun2",
            "com.dts.freefireth",
            "com.dts.freefiremax"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String saved = getSharedPreferences("ss", MODE_PRIVATE).getString("pkg", selectedPkg);
        if (saved != null && !saved.isEmpty()) selectedPkg = saved;

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundColor(Color.parseColor("#0A0E17"));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("SS Menu Hack");
        title.setTextColor(Color.parseColor("#FFD600"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Chon app → Overlay → Bat menu inject");
        sub.setTextColor(Color.parseColor("#AAAAAA"));
        sub.setTextSize(13);
        sub.setPadding(0, 12, 0, 20);
        root.addView(sub);

        targetLabel = new TextView(this);
        targetLabel.setText("Target: " + selectedPkg);
        targetLabel.setTextColor(Color.parseColor("#80CBC4"));
        targetLabel.setTextSize(13);
        targetLabel.setPadding(0, 0, 0, 16);
        root.addView(targetLabel);

        root.addView(btn("Chon app (danh sach)", v -> showAppPicker()));
        root.addView(btn("Nhap package thu cong", v -> showManualPkg()));

        Button files = btn("Cap All files access", v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } catch (Exception e) {
                    startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                }
            } else toast("Android < 11 OK");
        });
        root.addView(files);

        root.addView(btn("Cap Overlay", v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            } else toast("Overlay OK");
        }));

        Button start = btn("Bat Menu + Inject", v -> {
            if (!Settings.canDrawOverlays(this)) {
                toast("Can Overlay truoc");
                return;
            }
            getSharedPreferences("ss", MODE_PRIVATE).edit().putString("pkg", selectedPkg).apply();
            Intent i = new Intent(this, MenuService.class);
            i.putExtra("target_pkg", selectedPkg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
            else startService(i);
            toast("Inject → " + selectedPkg);
            moveTaskToBack(true);
        });
        start.setBackgroundColor(Color.parseColor("#1B5E20"));
        root.addView(start);

        root.addView(btn("Tat Menu", v -> {
            stopService(new Intent(this, MenuService.class));
            toast("Stopped");
        }));

        TextView note = new TextView(this);
        note.setText("\nNo-root: wallet.json Android/data/<package>\n"
                + "Dump: Coins=1 Keys=2\n"
                + "Chon app khac neu multi-account / ban khac.");
        note.setTextColor(Color.parseColor("#777777"));
        note.setTextSize(12);
        root.addView(note);

        setContentView(scroll);
    }

    private void showAppPicker() {
        final List<String> pkgs = new ArrayList<>();
        Collections.addAll(pkgs, PRESETS);
        try {
            PackageManager pm = getPackageManager();
            for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
                String p = ai.packageName;
                if (pkgs.contains(p)) continue;
                boolean sys = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (!sys) pkgs.add(p);
            }
        } catch (Exception ignored) {}

        final String[] items = pkgs.toArray(new String[0]);
        final String[] labels = new String[items.length];
        PackageManager pm = getPackageManager();
        for (int i = 0; i < items.length; i++) {
            String name = items[i];
            try {
                CharSequence label = pm.getApplicationLabel(pm.getApplicationInfo(items[i], 0));
                name = label + "\n" + items[i];
            } catch (Exception ignored) {}
            labels[i] = name;
        }

        new AlertDialog.Builder(this)
                .setTitle("Chon app de inject")
                .setItems(labels, (d, which) -> {
                    selectedPkg = items[which];
                    targetLabel.setText("Target: " + selectedPkg);
                    getSharedPreferences("ss", MODE_PRIVATE).edit().putString("pkg", selectedPkg).apply();
                    toast("Chon: " + selectedPkg);
                })
                .setNegativeButton("Huy", null)
                .show();
    }

    private void showManualPkg() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(selectedPkg);
        input.setTextColor(Color.WHITE);
        input.setHint("com.example.game");
        new AlertDialog.Builder(this)
                .setTitle("Package name")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    String p = input.getText().toString().trim();
                    if (!p.isEmpty()) {
                        selectedPkg = p;
                        targetLabel.setText("Target: " + selectedPkg);
                        getSharedPreferences("ss", MODE_PRIVATE).edit().putString("pkg", selectedPkg).apply();
                    }
                })
                .setNegativeButton("Huy", null)
                .show();
    }

    private Button btn(String t, android.view.View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor("#1A2332"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 12;
        b.setLayoutParams(lp);
        b.setOnClickListener(l);
        return b;
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
