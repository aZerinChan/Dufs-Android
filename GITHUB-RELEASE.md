# Dufs Android 0.1.0 — GitHub Release

## APK

`dufs-0.1.0.apk` is the only public APK. It requires Android 8.0 (API 26) or later and contains no CPU-specific JNI libraries. It uses `versionName=0.1.0`, `versionCode=14`, package `local.dufs.manage`, and the original signing certificate. Android treats it as an update to the earlier builds with version codes 10 through 13. No debug APK or debug certificate is shipped.

Long toolbar statuses fit in up to three inline lines: tighten line spacing before reducing the font size. Long-press an exceptionally long, truncated status for the complete message. Language choices use the themed native radio indicator on the left, matching the style of Appearance and Sort. File and folder timestamps use the selected language's date and time conventions and the device time zone; Bakhtiari explicitly uses Persian (Iran) date formatting because Bakhtiari-specific locale data is not available.

## Release 附件（仅四个）

| 文件 | 用途 |
| --- | --- |
| `dufs-0.1.0.apk` | 正式通用安装包 |
| `dufs-0.1.0.apk.sig` | APK 的分离签名 |
| `SHA256SUMS.txt` | 校验 APK、`.sig` 和公开证书 |
| `dufs-release-cert.pem` | 公开 X.509 签名证书，不含私钥 |

不要上传外层 ZIP、内部报告、单独的源码 ZIP、清单签名或调试 APK。此文档属于仓库，不是附件。GitHub 按标签自动生成的 Source code (zip/tar.gz) 不计入四个手动上传的文件。

## Verify before installing

Compare the official APK signing certificate SHA-256 with a trusted copy of this fingerprint, ideally obtained independently of the download:

```text
da2ccb75c8d413b4ee1231f8953183fc5ed7488dbf97b56d6b99fcb52a17ebc2
```

Embedded Android signature (Android SDK Build Tools):

```sh
apksigner verify --verbose --print-certs dufs-0.1.0.apk
```

Detached verification (OpenSSL; raw binary RSA PKCS#1 v1.5 / SHA-256, **not OpenPGP**):

```sh
openssl x509 -in dufs-release-cert.pem -noout -fingerprint -sha256
openssl x509 -in dufs-release-cert.pem -pubkey -noout > dufs-release-public.pem
sha256sum -c SHA256SUMS.txt
openssl dgst -sha256 -verify dufs-release-public.pem -signature dufs-0.1.0.apk.sig dufs-0.1.0.apk
```

`SHA256SUMS.txt` is not signed; it checks download integrity. Confirm authenticity using the pinned certificate fingerprint and the APK signature. On Windows use Git Bash for `sha256sum`, or compare individual checksums with `Get-FileHash -Algorithm SHA256 <file>` in PowerShell. JDK 17+ can verify the detached APK signature: from the source directory run `java ReleaseSignature.java verify <certificate-path> <apk-path>`, with the signature next to the APK.

## Publish checklist

- Upload only the four files listed above, from the new four-file set, not an older bundle. Do **not** upload any keystore, passwords, or private keys.
- Before making source public, confirm the original app's license and retain all third-party notices. Keep screenshots in the repository, not as Release assets.
- Tag: `v0.1.0`, title: `Dufs Android 0.1.0`. Check the source commit and inspect the four files on the draft before publishing; verify the public asset names and hashes afterward.

Language menu labels and order reference: [v2rayNG 2.3.15](https://github.com/2dust/v2rayNG/blob/2.3.15/V2rayNG/app/src/main/res/values/arrays.xml). The Dufs UI translations are separate from v2rayNG's string resources.
Bakhtiari is a draft and uses Persian for 28 entries pending native-speaker review. Runtime messages from Android or from a remote server may appear in their original language. Device rendering of the expanded language menu was not verified in this build.
