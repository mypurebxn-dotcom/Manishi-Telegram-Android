# Manishi Telegram — Android

**Independent and unofficial experimental Telegram client. Not affiliated with Telegram.**

This public repository contains a guarded GitHub Actions build pipeline and development instructions for an independent Android app identity. The native source is fetched at pinned upstream commit `dc780e81ed1261c369c27870e8e0999a1eb0b600` from `TelegramOrg/Telegram-Android` with its submodules.

**Current status:** Upstream ARM64 debug APK built and installed successfully. Manishi-branded APK has **not** yet been built or device-tested.

## Build paths

- **Upstream baseline:** `.github/workflows/native-arm64-baseline.yml` — verified upstream debugging only, not a Manishi release.
- **Manishi experimental build:** `.github/workflows/manishi-branded-arm64.yml` — requires the owner's separate Telegram API credentials and persistent debug signing key in GitHub Actions Secrets.
- **Source changes:** `ci/prepare_manishi.py` — builds a distinct `com.manishi.telegram` debug app, Manishi name and an original maroon/gold M launcher icon. It does not publish any local phone source or credentials.
- **Setup:** [हिंदी निर्देश](docs/SETUP_HI.md).

This experimental build has no configured independent Firebase push, maps credentials, or official Telegram passkeys/billing. Do not treat it as ready for production or as a fully reviewed secure client.

## Source, licensing and attribution

The source code is based on the Telegram Android project and is subject to upstream GPL licensing and applicable dependency licenses. See [LICENSE](LICENSE) and the original source repository. Retain upstream copyright notices. When distributing APKs, provide the corresponding source code and complete build modifications as required by the licenses.

Do not use the official Telegram logo for this derivative, and make clear that the app is unofficial.
