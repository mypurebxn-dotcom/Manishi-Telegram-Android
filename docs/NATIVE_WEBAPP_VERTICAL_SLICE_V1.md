# Manishi Telegram — Native/WebApp Vertical Slice V1 (2026-10-28)

## Primary source and provenance
The user's fresh source-only WebApp archive was **locally inspected** and matched the
Termux-provided SHA256:

`MANISHI_TELEGRAM_WEBAPP_SOURCE_ONLY_20261028_165523_3667.zip`
`9430b727919cd9aacd45fcefa63351dd92f8b44dfd75b61bb9019584a029548a`

That archive **is not committed to this public GitHub repository**. It contains
`webapp/index.html`, `webapp/src/app.js`, CSS, tests and PDF.js assets.
The existing WebApp uses local browser state, not native Telegram authentication.

## First real native implementation

- `native/ManishiChatsActivity.java`: independent **Manishi** chats surface.
- Visual ownership extracted from actual WebApp: light background `#EEF3F7`,
  white cards, accent `#229ED9`, search box, All/Unread/Groups/Channels tabs.
- Native data: `MessagesController.getDialogs(0)`, `dialogMessage`,
  user/chat objects, unread counts; no hard-coded sample chats.
- Account lifecycle: fragment account comes from the active `DialogsActivity`,
  notification observers are subscribed and removed for that account.
- Navigation: a visible **M** action-bar icon in Telegram's normal chats screen
  opens the custom chat list. Row clicks open the real `ChatActivity` through
  existing `checkCanOpenChat`; no fake send action.
- The existing app, account data, file transfers, contacts and sessions are
  not intentionally modified by this code.
- `ci/apply_manishi_native.py` only patches an isolated, pinned upstream
  checkout after the original Manishi brand script has run.

## What V1 does NOT do
- It does not replace the main default chat screen at app startup.
- It does not create a custom chat-detail screen, message composer,
  transfers screen or PDF/media viewer on Android.
- It does not integrate all previously prototyped WebApp screens.
- It does not claim that WebApp local features are live Telegram features.
- APK compilation success is **not** device UI validation or release approval.

## Acceptance gates
1. CI source contract and Java/Android compilation.
2. APK signature, independent package ID and ARM64 verification.
3. Manual Android device acceptance: log in to the experimental package,
   find the M icon in the chats screen, open Manishi Chats, verify real chat
   titles, unread/filter/search and open a conversation.
4. Compare actual rendering on phone against live WebApp screenshot.
5. Continue custom chat detail + attachments only when V1 gates succeed.

**RELEASE=HOLD** until verified on device. Avoid using this experimental
debug APK as the sole Telegram client.
