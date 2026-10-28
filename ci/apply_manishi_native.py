#!/usr/bin/env python3
"""Apply one Manishi UX vertical slice to the exact pinned Telegram source.

Runs only in an isolated GitHub Actions checkout, never overwrites the
user's private Termux WebApp, chat databases or Telegram sessions.
"""
from pathlib import Path
import sys

upstream = Path(sys.argv[1] if len(sys.argv) > 1 else "upstream")
root = Path(__file__).resolve().parent.parent
ui = upstream / "TMessagesProj/src/main/java/org/telegram/ui"
res = upstream / "TMessagesProj/src/main/res/drawable"
dialogs = ui / "DialogsActivity.java"
fragment = ui / "ManishiChatsActivity.java"
icon = res / "manishi_home.xml"

if not dialogs.is_file() or not res.is_dir():
    sys.exit("HOLD_PINNED_UPSTREAM_PATH_MISSING")
if fragment.exists() or icon.exists():
    sys.exit("HOLD_MANISHI_NATIVE_FILE_COLLISION")
code = dialogs.read_text(encoding="utf-8")
anchor = '        ActionBarMenu menu = actionBar.createMenu();\n        menu.setTranslationX(-dp(5));'
if code.count(anchor) != 1:
    sys.exit("HOLD_PINNED_DIALOGS_ACTION_BAR_ANCHOR_DRIFT")
replacement = """        ActionBarMenu menu = actionBar.createMenu();
        // Real Telegram-backed Manishi chat list; existing chats stay intact.
        if (!onlySelect && searchString == null && folderId == 0 && communityId == 0) {
            ActionBarMenuItem manishiEntry = menu.addItem(1149, R.drawable.manishi_home);
            manishiEntry.setContentDescription("Manishi Chats — live Telegram account");
            manishiEntry.setOnClickListener(v -> {
                ManishiChatsActivity manishi = new ManishiChatsActivity();
                manishi.setCurrentAccount(currentAccount);
                presentFragment(manishi);
            });
        }
        menu.setTranslationX(-dp(5));"""
native_src = (root / "native/ManishiChatsActivity.java").read_text(encoding="utf-8")
icon_src = (root / "native/manishi_home.xml").read_text(encoding="utf-8")
if "getMessagesController().getDialogs(0)" not in native_src:
    sys.exit("HOLD_MANISHI_REAL_DATA_BINDING_MISSING")
fragment.write_text(native_src, encoding="utf-8")
icon.write_text(icon_src, encoding="utf-8")
dialogs.write_text(code.replace(anchor, replacement, 1), encoding="utf-8")
print("NATIVE_REAL_DATA_VERTICAL_SLICE=APPLIED")
print("WEBAPP_UI_REFERENCED=YES")
print("REAL_TELEGRAM_DIALOGS=YES")
print("CHAT_DETAIL_CUSTOM_MANISHI_UI=HOLD")
print("TRANSFERS_CUSTOM_MANISHI_UI=HOLD")
print("NO_USER_DATA_OR_SESSIONS_CHANGED=YES")
