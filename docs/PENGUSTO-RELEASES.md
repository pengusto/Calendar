# Pengusto Calendar

Unofficial Fossify Calendar build with readable Outlook/Exchange HTML descriptions.

Once a signed release is published, download the APK from https://github.com/pengusto/Calendar/releases/latest and install it on Android 8 or newer. Android may ask you to allow installations from your downloading app. Package: `io.github.pengusto.calendar`. It installs beside the official Fossify app. Grant calendar access, enable calendar synchronization in the app settings and select your desired Android calendars. Synced calendars can be shared with the official app; local-only events/settings do not transfer automatically. Export local-only events to ICS before migrating them.

Updates use the same package and signing certificate. Keep the original release signing key backed up; never rotate it casually. Users install new release APKs over this app without uninstalling. Actual Outlook provider/server round trips require a device test; native HTML parsing and local save/widget tests have passed.

## Releases

Tags `pengusto-v*` on the release branch trigger `.github/workflows/pengusto-release.yml`. The workflow builds/lints/tests, signs with repository secrets, verifies the APK and uploads it plus a SHA-256 checksum to GitHub Releases. Release version codes must increase. GitHub Actions run numbers provide increasing version codes; do not reset the workflow history or reuse an old code. Signing is isolated to the fork and never runs for pull requests.

Repository secrets: `PENGUSTO_KEYSTORE_BASE64`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD`. The keystore is decoded only on the runner and removed after the job. Source and modifications remain available under GPL-3.0 in this repository.

Upstream fixes are kept on `fix/outlook-html-descriptions`; distribution settings live on `release/pengusto-calendar`. For updates, review a stable Fossify release, apply our still-needed fixes, run tests, then tag the reviewed commit. No automatic merging or publication of unreviewed upstream changes.
