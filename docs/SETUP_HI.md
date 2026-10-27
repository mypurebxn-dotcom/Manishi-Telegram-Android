# Manishi Telegram — Native Android development (Hindi)

**Status:** experimental, unofficial third-party Telegram client. This is not the final production APK.

## Current build design
- Original Telegram Android source: `TelegramOrg/Telegram-Android`, pinned commit `dc780e81ed1261c369c27870e8e0999a1eb0b600`
- Build script: `ci/prepare_manishi.py`; workflow: `.github/workflows/manishi-branded-arm64.yml`
- Unique package: `com.manishi.telegram`
- Original temporary launcher artwork: gold M on maroon field. It is not Telegram's paper-plane logo.
- Stable debug signing (user-owned), independent Telegram API registration, no reused Telegram Firebase/Maps/Passkey credentials.
- Current feature work: upstream Telegram native functionality, **not** the earlier WebApp feature changes.
- Firebase push, Google Maps, original Telegram-specific billing and passkeys are not configured for this fork.

## One-time preparation — never share secrets in chat or commit

1. Open https://my.telegram.org and select **API development tools**. Create your own application and obtain its `api_id` and `api_hash`. Do not use Telegram's bundled sample API ID for a public third-party client.
2. In Termux, create one persistent development-only signing key. Do not overwrite an existing key:

```bash
K="$HOME/.config/manishi-telegram/signing"
mkdir -p "$K"
chmod 700 "$K"
if [ -e "$K/debug.jks" ]; then
  echo "EXISTING_SIGNING_KEY_REUSE=YES"
else
  keytool -genkeypair -v -keystore "$K/debug.jks" \
    -alias androiddebugkey -keyalg RSA -keysize 3072 -validity 10000 \
    -storepass android -keypass android \
    -dname "CN=Manishi Telegram Development,OU=Dev,O=Manishi,C=IN" >/dev/null
  chmod 600 "$K/debug.jks"
fi
echo "DEBUG_KEYSTORE=READY"
```

3. Go to https://github.com/mypurebxn-dotcom/Manishi-Telegram-Android/settings/secrets/actions → **New repository secret**. Set these exact three secret names:

   - `MANISHI_API_ID` = your own numeric Telegram `api_id`
   - `MANISHI_API_HASH` = your own 32-character Telegram `api_hash`
   - `MANISHI_DEBUG_KEYSTORE_B64` = base64 encoding of your persistent `debug.jks`

To obtain the third secret locally, run `base64 -w 0 "$HOME/.config/manishi-telegram/signing/debug.jks"` and copy the output **directly into GitHub Secrets**, not into chat, screenshots, issues, commits, or logs. Keep an offline secure backup of `debug.jks`; losing it prevents installing updates over an existing app without uninstalling.

4. Run **Actions → Manishi Telegram — branded ARM64 debug → Run workflow** just once.

## Safety and limitations

- This repository is PUBLIC. Even with GitHub Actions secrets, Telegram API credentials embedded in a distributed APK can be extracted by anyone with the APK. Do not mistake a secret at build time for secrecy inside a published client. Follow Telegram's API terms.
- Release signing requires a distinct private release key; do not use the development key for production.
- This workflow does not upload the user's local dirty submodules, Termux logs, stored messages, or phone profiles.
- Do not sign into a primary account for experimental testing until the APK identity, trust, crash behavior, and source review are complete.
- The original Telegram Android source and this derivative are subject to the corresponding GPL licensing requirements. Preserve upstream copyright notices and make corresponding sources/modifications available when distributing.
- `BUILD=PASS` is not equal to `PRODUCTION_RELEASE=PASS`. It does not prove notifications, calls, background behavior, migration, uploads or downloads.
