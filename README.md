# Subway Surfers Menu Hack

External floating menu for **Subway Surfers** (`com.kiloo.subwaysurf`).

## Features
- Auto-attach when service starts
- Floating **SS** bubble → open menu
- Unlimited Coins / Keys (slider)
- Unlock ALL Characters / Boards / Outfits
- Free Shop
- God Mode / No Hit / Noclip
- Mega Jump / Super Speed / Coin Magnet
- Double Coins / Score x10
- Mod Skin toggle

## Dump map (dump.cs)
| Symbol | Role |
|--------|------|
| `WalletModel` | `wallet.json` currencies |
| `Currencies` @ 0x58 | Dictionary CurrencyType → SafeInt |
| `OwnedCharacterState` | character unlock |
| `OwnedCharacters` | collection |

Offsets in `MemoryBridge.java` are **placeholders**. Fill RVA after pointer scan on current game build.

## Build
GitHub Actions builds debug APK on push to main.

## Use
1. Install APK, grant Overlay
2. Open Subway Surfers
3. Open this app → **Bat Menu**
4. Tap yellow **SS** icon → toggles
5. Prefer root / VM for memory write

## Note
Online anti-cheat / cloud wallet may reset values. Local wallet edits work offline; online sync can overwrite.
