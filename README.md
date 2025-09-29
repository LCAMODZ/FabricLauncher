╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║         SIMPLE FABRIC LAUNCHER - VERSION UPDATE GUIDE         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

  Current Version: Minecraft 1.21.4 (Fabric)
  Updating to: Minecraft 1.21.8


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📋 PREREQUISITES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ⚠️  IMPORTANT: Launch Vanilla Minecraft 1.21.8 ONCE first!

  1. Open the official Minecraft Launcher
  2. Start Vanilla Minecraft 1.21.8
  3. Wait until fully loaded
  4. Close the game

  This downloads all required assets and libraries.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🔧 UPDATE STEPS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌───────────────────────────────────────────────────────────────┐
│ STEP 1: Update Main.java                                     │
└───────────────────────────────────────────────────────────────┘

  Change these two lines:

    private static final String MINECRAFT_VERSION = "1.21.8";
    private static final String ASSETS_ID = "26";


┌───────────────────────────────────────────────────────────────┐
│ STEP 2: Update LauncherLogic.java                            │
└───────────────────────────────────────────────────────────────┘

  ① Update Fabric JSON:
     • Go to: .minecraft/versions/fabric-loader-X.XX.X-1.21.8/
     • Copy the entire JSON from the fabric-loader JSON file
     • Replace the complete JSON in LauncherLogic.java

  ② Change Assets ID in JSON:
     • Find:   "assetIndex": { "id": "19"
     • Change: "assetIndex": { "id": "26"


┌───────────────────────────────────────────────────────────────┐
│ STEP 3: Build & Run                                          │
└───────────────────────────────────────────────────────────────┘

  • Recompile your project
  • Launch and enjoy! 🎮


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📊 ASSET INDEX REFERENCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ┌──────────────────┬────────────────┐
  │ Minecraft Version│   Asset ID     │
  ├──────────────────┼────────────────┤
  │     1.21.4       │      19        │
  │     1.21.8       │      26        │
  └──────────────────┴────────────────┘

  💡 Tip: Find Asset IDs in vanilla's version.json file


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ✅ QUICK CHECKLIST
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ☐ Launched Vanilla 1.21.8 once
  ☐ Updated MINECRAFT_VERSION in Main.java
  ☐ Updated ASSETS_ID in Main.java
  ☐ Replaced Fabric JSON in LauncherLogic.java
  ☐ Changed Asset ID in JSON to "26"
  ☐ Recompiled project
  ☐ Tested launcher

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Need help? Check the .minecraft/logs/latest.log for errors!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
