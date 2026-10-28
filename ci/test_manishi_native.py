#!/usr/bin/env python3
"""Contract checks. Native Android compilation is a separate mandatory gate."""
from pathlib import Path
import re
from xml.etree import ElementTree

root=Path(__file__).resolve().parent.parent
java=(root/"native/ManishiChatsActivity.java").read_text(encoding="utf-8")
patch=(root/"ci/apply_manishi_native.py").read_text(encoding="utf-8")
ElementTree.parse(root/"native/manishi_home.xml")
for expected in (
    "getMessagesController().getDialogs(0)",
    "getMessagesController().dialogMessage.get(id)",
    "getMessagesController().checkCanOpenChat(b,this)",
    "presentFragment(new ChatActivity(b))",
    "getNotificationCenter().removeObserver",
    "NotificationCenter.dialogsNeedReload",
    "NotificationCenter.didReceiveNewMessages",
    '"Search Chats"', '"Unread"', '"Groups"', '"Channels"',
    "RecyclerView.Adapter",
):
    assert expected in java, "HOLD_REQUIRED_ACCOUNT_UI_CONTRACT: "+expected
assert 'menu.addItem(1149, R.drawable.manishi_home)' in patch
assert 'manishi.setCurrentAccount(currentAccount)' in patch
assert 'HOLD_PINNED_DIALOGS_ACTION_BAR_ANCHOR_DRIFT' in patch
assert '--accent' not in java  # Native colors defined explicitly, not CSS.
print("NATIVE_SOURCE_CONTRACT=PASS")
print("TELEGRAM_ACTUAL_DATA_BINDING=SOURCE_CONFIRMED")
print("ANDROID_COMPILATION=NOT_COVERED_BY_THIS_TEST")
