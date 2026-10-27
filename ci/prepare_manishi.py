#!/usr/bin/env python3
"""Temporary CI-only Manishi Telegram branding for pinned upstream source.
No Telegram API secrets are stored in this repository. Experimental debug only.
"""
from pathlib import Path
import os
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else "upstream")
api_id = os.environ.get("MANISHI_API_ID", "")
api_hash = os.environ.get("MANISHI_API_HASH", "")
key_path = os.environ.get("MANISHI_KEYSTORE_PATH", "")
if not re.fullmatch(r"[1-9][0-9]{3,11}", api_id):
    sys.exit("HOLD_MISSING_VALID_API_ID")
if not re.fullmatch(r"[a-fA-F0-9]{32}", api_hash):
    sys.exit("HOLD_MISSING_VALID_API_HASH")
if not key_path or not Path(key_path).is_file():
    sys.exit("HOLD_PRIVATE_SIGNING_KEYSTORE_MISSING")

def replace_once(path, old, new):
    p = root / path
    data = p.read_text(encoding="utf-8")
    n = data.count(old)
    if n != 1:
        sys.exit("HOLD_PINNED_SOURCE_DRIFT: " + path + " count=" + str(n))
    p.write_text(data.replace(old, new), encoding="utf-8")

vars_file = "TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java"
replace_once(vars_file, "public static int APP_ID = 4;", f"public static int APP_ID = {api_id};")
replace_once(vars_file, 'public static String APP_HASH = "014b35b6184100b085b0d0572f9b5103";',
             f'public static String APP_HASH = "{api_hash.lower()}";')
replace_once(vars_file, "public static boolean SUPPORTS_PASSKEYS = true;",
             "public static boolean SUPPORTS_PASSKEYS = false;")
replace_once(vars_file, 'public static String SAFETYNET_KEY = "AIzaSyDqt8P-7F7CPCseMkOiVRgb1LY8RN1bvH8";',
             'public static String SAFETYNET_KEY = "";')
replace_once(vars_file, 'public static String GOOGLE_AUTH_CLIENT_ID = "760348033671-81kmi3pi84p11ub8hp9a1funsv0rn2p9.apps.googleusercontent.com";',
             'public static String GOOGLE_AUTH_CLIENT_ID = "";')
replace_once(vars_file, 'public static String PLAYSTORE_APP_URL = "https://play.google.com/store/apps/details?id=org.telegram.messenger";',
             'public static String PLAYSTORE_APP_URL = "";')
replace_once(vars_file, "public static boolean IS_BILLING_UNAVAILABLE = false;",
             "public static boolean IS_BILLING_UNAVAILABLE = true;")

lib = "TMessagesProj/build.gradle"
app = "TMessagesProj_App/build.gradle"
replace_once(lib, "    defaultConfig {\n        minSdkVersion 21",
             "    defaultConfig {\n        ndk { abiFilters 'arm64-v8a' }\n        minSdkVersion 21")
replace_once(lib, "apply plugin: 'com.google.gms.google-services'",
             "// Manishi: official Firebase app config is not reused in this fork.")
replace_once(app, "defaultConfig.applicationId = APP_PACKAGE",
             'defaultConfig.applicationId = "com.manishi.telegram"')
replace_once(app, '            applicationIdSuffix ".beta"',
             "            // No upstream .beta suffix: separate applicationId.")
replace_once(app, "apply plugin: 'com.google.gms.google-services'",
             "// Manishi: official Firebase app config is not reused in this fork.")
replace_once(app,
             '        debug {\n            storeFile file("../TMessagesProj/config/release.keystore")\n            storePassword RELEASE_STORE_PASSWORD\n            keyAlias RELEASE_KEY_ALIAS\n            keyPassword RELEASE_KEY_PASSWORD\n        }',
             '        debug {\n            storeFile file(System.getenv("MANISHI_KEYSTORE_PATH"))\n            storePassword "android"\n            keyAlias "androiddebugkey"\n            keyPassword "android"\n        }')
p = root / app
content = p.read_text(encoding="utf-8")
abis = 'abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"'
if content.count(abis) != 3:
    sys.exit("HOLD_PINNED_ABI_FILTER_DRIFT")
p.write_text(content.replace(abis, 'abiFilters "arm64-v8a"'), encoding="utf-8")

manifest = "TMessagesProj/config/debug/AndroidManifest.xml"
replace_once(manifest, 'android:label="@string/AppNameBeta"', 'android:label="Manishi Telegram"')
replace_once(manifest, 'android:icon="@mipmap/ic_launcher"', 'android:icon="@mipmap/manishi_launcher"')
replace_once(manifest, 'android:roundIcon="@mipmap/ic_launcher_round"', 'android:roundIcon="@mipmap/manishi_launcher_round"')
replace_once(manifest,
             '<meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="AIzaSyA-t0jLPjUt2FxrA8VPK2EiYHcYcboIR6k" />',
             "<!-- Google Maps key requires the app owner's own configuration. -->")
values = "TMessagesProj/src/main/res/values/strings.xml"
replace_once(values, '<string name="AppName">Telegram</string>',
             '<string name="AppName">Manishi Telegram</string>')
replace_once(values, '<string name="AppNameBeta">Telegram Beta</string>',
             '<string name="AppNameBeta">Manishi Telegram</string>')

main = root / "TMessagesProj/src/main/AndroidManifest.xml"
s = main.read_text(encoding="utf-8")
for n in (2, 3, 4, 5, 6):
    for suffix in ("launcher", "launcher_round"):
        old = f'@mipmap/icon_{n}_{suffix}'
        if s.count(old) != 1:
            sys.exit("HOLD_ALTERNATE_ICON_ANCHOR_DRIFT")
        s = s.replace(old, "@mipmap/manishi_launcher" + ("_round" if suffix.endswith("_round") else ""))
# Use original Manishi artwork for remaining shortcut/intent icons.
s = s.replace("@mipmap/ic_launcher_round", "@mipmap/manishi_launcher_round")
s = s.replace("@mipmap/ic_launcher", "@mipmap/manishi_launcher")
main.write_text(s, encoding="utf-8")

res = root / "TMessagesProj/src/main/res"
shape = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#421B2A" android:pathData="M0,0h108v108h-108z"/>
    <path android:fillColor="#EBC477" android:pathData="M54,16 A38,38 0,1 1,53.99 16z"/>
    <path android:fillColor="#421B2A" android:pathData="M54,22 A32,32 0,1 1,53.99 22z"/>
    <path android:fillColor="#EBC477" android:pathData="M31,74 L31,35 L39,35 L54,55 L69,35 L77,35 L77,74 L68,74 L68,49 L54,68 L40,49 L40,74 Z"/>
</vector>
'''
foreground = res / "drawable/manishi_foreground.xml"
foreground.write_text(shape, encoding="utf-8")
base = res / "mipmap-anydpi"
base.mkdir(parents=True, exist_ok=True)
adaptive = res / "mipmap-anydpi-v26"
adaptive.mkdir(parents=True, exist_ok=True)
adaptive_xml = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/manishi_icon_bg"/>
    <foreground android:drawable="@drawable/manishi_foreground"/>
    <monochrome android:drawable="@drawable/manishi_foreground"/>
</adaptive-icon>
'''
for name in ("manishi_launcher.xml", "manishi_launcher_round.xml"):
    (base / name).write_text(shape, encoding="utf-8")
    (adaptive / name).write_text(adaptive_xml, encoding="utf-8")
(res / "values/manishi_icon_colors.xml").write_text(
    '<?xml version="1.0" encoding="utf-8"?><resources><color name="manishi_icon_bg">#421B2A</color></resources>\n',
    encoding="utf-8")

print("CI_BRANDING=PASS")
print("PACKAGE=com.manishi.telegram")
print("SIGNING=PRIVATE_KEYSTORE")
print("API_ID_HASH=INJECTED_WITHOUT_LOGGING")
print("FIREBASE_PUSH=NOT_CONFIGURED")
print("BUILD_STAGE=EXPERIMENTAL_DEBUG_ONLY")
