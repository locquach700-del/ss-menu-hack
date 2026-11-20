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

/**
 * No-root first. Patches wallet.json when readable.
 * Dump: Coins=1 Keys=2 | Currencies@0x58 | SetCurrency RVA 0x3D7F8AC
 */
public class MemoryBridge {

    public static final int CURRENCY_COINS = 1;
    public static final int CURRENCY_KEYS = 2;

    private Context ctx;
    private File walletFile;
    private boolean fileMode;
    private boolean memMode;
    private int pid = -1;

    public void setContext(Context c) { ctx = c.getApplicationContext(); }

    public boolean attach(String pkg) {
        fileMode = false;
        memMode = false;
        walletFile = null;

        String[] candidates = new String[] {
                "/sdcard/Android/data/" + pkg + "/files/wallet.json",
                "/sdcard/Android/data/" + pkg + "/files/Profiles/wallet.json",
                "/storage/emulated/0/Android/data/" + pkg + "/files/wallet.json",
                "/sdcard/Android/data/" + pkg + "/files/sybo/wallet.json",
                Environment.getExternalStorageDirectory() + "/Android/data/" + pkg + "/files/wallet.json"
        };
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists() && f.canRead()) {
                walletFile = f;
                fileMode = true;
                return true;
            }
        }

        if (hasSu()) {
            pid = findPid(pkg);
            if (pid > 0) { memMode = true; return true; }
        }
        return false;
    }

    public void detach() {
        fileMode = false;
        memMode = false;
        walletFile = null;
        pid = -1;
    }

    public boolean isAttached() { return fileMode || memMode; }

    public String modeLabel() {
        if (fileMode) return "FILE (no root)";
        if (memMode) return "MEM (root)";
        return "NONE";
    }

    public void apply(HackConfig c) {
        if (fileMode) {
            if (c.unlimitedCoins) setCoins(c.coinAmount);
            if (c.unlimitedKeys) setKeys(c.keyAmount);
        }
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

    private boolean hasSu() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = r.readLine();
            p.waitFor();
            return line != null && line.contains("su");
        } catch (Exception e) { return false; }
    }

    private int findPid(String pkg) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "pidof " + pkg});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = r.readLine();
            p.waitFor();
            if (line != null && line.trim().length() > 0)
                return Integer.parseInt(line.trim().split("\\s+")[0]);
        } catch (Exception ignored) {}
        return -1;
    }
}
