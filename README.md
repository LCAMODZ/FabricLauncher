# Simple Fabric Launcher - Version Update Guide

> **Current Version:** Minecraft 1.21.4 (Fabric)  
> **Example Update:** Minecraft 1.21.4 → 1.21.8

---

## 📋 Prerequisites

> ⚠️ **IMPORTANT:** Launch Vanilla Minecraft 1.21.8 **ONCE** before updating!

1. Open the official Minecraft Launcher
2. Start Vanilla Minecraft **1.21.8**
3. Wait until fully loaded
4. Close the game

This downloads all required assets and libraries to your `.minecraft` folder.

---

## 🔧 Update Steps

### Step 1️⃣: Update `Main.java`

Change these two constants:

```java
private static final String MINECRAFT_VERSION = "1.21.8";
private static final String ASSETS_ID = "26";
```

### Step 2️⃣: Update `LauncherLogic.java`

#### ① Replace Fabric JSON

- Navigate to: `.minecraft/versions/fabric-loader-X.XX.X-1.21.8/`
- Copy the **entire JSON** from the fabric-loader JSON file
- Replace the complete JSON in `LauncherLogic.java`

#### ② Update Asset ID in JSON

Find and replace the asset index:

```json
// Find:
"assetIndex": { "id": "19" }

// Change to:
"assetIndex": { "id": "26" }
```

### Step 3️⃣: Build & Run

- Recompile your project
- Launch and enjoy! 🎮

---

## 📊 Asset Index Reference

| Minecraft Version | Asset ID |
|-------------------|----------|
| 1.21.4            | 19       |
| 1.21.8            | 26       |

> 💡 **Tip:** You can find Asset IDs in the vanilla `version.json` file located in `.minecraft/versions/[version]/`

---

## ✅ Quick Checklist

- [ ] Launched Vanilla 1.21.8 once
- [ ] Updated `MINECRAFT_VERSION` in `Main.java`
- [ ] Updated `ASSETS_ID` in `Main.java`
- [ ] Replaced Fabric JSON in `LauncherLogic.java`
- [ ] Changed Asset ID in JSON to `"26"`
- [ ] Recompiled project
- [ ] Tested launcher successfully

---

## 🛠️ Troubleshooting

**Problem:** Assets not found  
**Solution:** Launch vanilla Minecraft 1.21.8 again to download assets

**Problem:** Missing libraries  
**Solution:** Check `.minecraft/libraries` folder exists and has content

**Problem:** Game won't start  
**Solution:** Check `.minecraft/logs/latest.log` for detailed error messages

---

## 📝 Notes

- This process works for **any** Minecraft version update
- Always launch the vanilla version first to ensure all files are downloaded
- Asset IDs increment with each major Minecraft update
- Keep backups of your working launcher before updating

---

<div align="center">

**Made with ❤️ for the Minecraft modding community**

[Report Bug](../../issues) · [Request Feature](../../issues)

</div>
