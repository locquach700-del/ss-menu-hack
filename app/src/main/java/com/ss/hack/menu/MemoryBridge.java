package com.ss.hack.menu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * Subway Surfers memory bridge (com.kiloo.subwaysurf).
 * Dump: WalletModel, OwnedCharacterState, CurrencyType
 * Fill OFF_* after pointer scan on current build.
 */
public class MemoryBridge {

    public static long OFF_IL2CPP = 0;
    public static long OFF_WALLET_INSTANCE = 0;
    public static long OFF_CURRENCIES = 0x58;
    public static long OFF_COINS_VALUE = 0;
    public static long OFF_KEYS_VALUE = 0;
    public static long OFF_OWNED_CHARS = 0;
    public static long OFF_PLAYER_HP_OR_HIT = 0;
    public static long OFF_NOCLIP = 0;
    public static long OFF_SCORE_MUL = 0;

    private int pid = -1;
    private long base;
    private boolean ok;

    public boolean attach(String pkg) {
        detach();
        pid = findPid(pkg);
        if (pid <= 0) return false;
        base = findBase(pid, "libil2cpp.so");
        OFF_IL2CPP = base;
        ok = base != 0 || canRead(pid);
        return ok;
    }

    public void detach() {
        ok = false;
        pid = -1;
        base = 0;
    }

    public boolean isAttached() { return ok && pid > 0; }

    public void apply(HackConfig c) {
        if (!isAttached()) return;
        if (OFF_WALLET_INSTANCE == 0) return;
        try {
            if (c.unlimitedCoins && OFF_COINS_VALUE != 0)
                writeInt(resolve(OFF_COINS_VALUE), c.coinAmount);
            if (c.unlimitedKeys && OFF_KEYS_VALUE != 0)
                writeInt(resolve(OFF_KEYS_VALUE), c.keyAmount);
        } catch (Throwable ignored) {}
    }

    public void setCoins(int v) {
        if (OFF_COINS_VALUE != 0) writeInt(resolve(OFF_COINS_VALUE), v);
    }

    public void setKeys(int v) {
        if (OFF_KEYS_VALUE != 0) writeInt(resolve(OFF_KEYS_VALUE), v);
    }

    private long resolve(long off) { return base + off; }

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

    private long findBase(int pid, String mod) {
        try {
            BufferedReader r = new BufferedReader(new FileReader("/proc/" + pid + "/maps"));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains(mod) && line.contains("r-xp")) {
                    r.close();
                    return Long.parseLong(line.split("-")[0], 16);
                }
            }
            r.close();
        } catch (Exception ignored) {}
        return 0;
    }

    private boolean canRead(int pid) {
        try {
            new FileReader("/proc/" + pid + "/maps").close();
            return true;
        } catch (Exception e) { return false; }
    }

    private void writeInt(long addr, int val) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo ok\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
        } catch (Exception ignored) {}
    }
}
