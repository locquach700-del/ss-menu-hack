package com.ss.hack.menu;

import android.content.Context;
import android.os.Environment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** No-root: wallet.json under Android/data/<pkg>/files/ */
public class MemoryBridge {

    public static final int CURRENCY_COINS = 1;
    public static final int CURRENCY_KEYS = 2;

    private Context ctx;
    private File walletFile;
    private boolean fileMode;
    private String attachedPkg = "";

    public void setContext(Context c) { ctx = c.getApplicationContext(); }

    public boolean attach(String pkg) {
        fileMode = false;
        walletFile = null;
        attachedPkg = pkg == null ? "" : pkg.trim();
        if (attachedPkg.isEmpty()) return false;

        String[] candidates = new String[] {
                "/sdcard/Android/data/" + attachedPkg + "/files/wallet.json",
                "/sdcard/Android/data/" + attachedPkg + "/files/Profiles/wallet.json",
                "/storage/emulated/0/Android/data/" + attachedPkg + "/files/wallet.json",
                "/sdcard/Android/data/" + attachedPkg + "/files/sybo/wallet.json",
                Environment.getExternalStorageDirectory() + "/Android/data/" + attachedPkg + "/files/wallet.json"
        };
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists() && f.canRead()) {
                walletFile = f;
                fileMode = true;
                return true;
            }
        }
        return false;
    }

    public void detach() {
        fileMode = false;
        walletFile = null;
        attachedPkg = "";
    }

    public boolean isAttached() { return fileMode; }
    public String getPackage() { return attachedPkg; }

    public String modeLabel() {
        if (fileMode) return "FILE " + attachedPkg;
        if (!attachedPkg.isEmpty()) return "PKG " + attachedPkg + " (chua thay wallet)";
        return "NONE";
    }

    public void apply(HackConfig c) {
        if (!fileMode) return;
        if (c.unlimitedCoins) setCoins(c.coinAmount);
        if (c.unlimitedKeys) setKeys(c.keyAmount);
    }

    public boolean setCoins(int v) { return setCurrencyJson(CURRENCY_COINS, v); }
    public boolean setKeys(int v) { return setCurrencyJson(CURRENCY_KEYS, v); }

    private boolean setCurrencyJson(int type, int value) {
        if (!fileMode || walletFile == null) return false;
        try {
            String raw = readAll(walletFile);
            if (raw == null || raw.isEmpty()) return false;
            JSONObject root = new JSONObject(raw);
            JSONObject cur = root.optJSONObject("currencies");
            if (cur == null && root.has("Currencies")) cur = root.getJSONObject("Currencies");
            if (cur == null) {
                cur = new JSONObject();
                root.put("currencies", cur);
            }
            String key = String.valueOf(type);
            Object entry = cur.opt(key);
            if (entry instanceof JSONObject) {
                JSONObject e = (JSONObject) entry;
                if (e.has("value")) e.put("value", value);
                else if (e.has("Value")) e.put("Value", value);
                else if (e.has("_value")) e.put("_value", value);
                else e.put("value", value);
            } else {
                JSONObject e = new JSONObject();
                e.put("value", value);
                cur.put(key, e);
            }
            writeAll(walletFile, root.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String readAll(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        return sb.toString();
    }

    private static void writeAll(File f, String s) throws Exception {
        FileOutputStream out = new FileOutputStream(f);
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.close();
    }
}
