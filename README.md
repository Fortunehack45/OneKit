# ONE — All-in-One Offline Utility App

> *“Whatever small thing you need to do, do it in ONE.”*

**ONE** is a production-quality, privacy-first, 100% offline utility Android application built with Kotlin and Jetpack Compose. It eliminates the need for dozens of sketchy, ad-riddled websites or bloated single-use apps by running powerful everyday tools entirely on-device.

---

## ✨ Features (Phase 1 MVP)

- **Pastel Bento-Grid UI**: Custom design aesthetic with soft lavender hero card, category capsule filters, asymmetric pastel bento grid (warm honey yellow, sky blue, bubblegum pink), and an obsidian floating dock navigation bar.
- **Universal Action / Intent Router**: Deterministic natural-language parser. Type queries like `"17% of 850000"`, `"turn these photos into a pdf"`, `"25 miles to km"`, or `"make image smaller"` to launch workflows instantly without cloud AI.
- **Image → PDF**: Full PDF creation engine using Android's native `PdfDocument`. Supports A4, Letter, Original page sizes, portrait/landscape, custom margins, fit/fill scaling, and page numbering with memory-safe downsampling.
- **Image Compressor**: Downsample and compress images with quality presets (Maximum, High, Medium, Small) and custom sliders. Shows before/after file sizes and exact space saved percentage.
- **On-Device Background Remover**: Pluggable `BackgroundRemovalEngine` providing local segmentation and transparent PNG exports without uploading photos to external servers.
- **Universal Calculator & Unit Converter**:
  - Natural mathematical expressions (e.g. `450000 * 0.17`).
  - Percentage calculator (`17% of 850000 = 144,500`).
  - Tip & Split bill calculator.
  - Offline unit converter (miles $\leftrightarrow$ km, kg $\leftrightarrow$ lbs, °C $\leftrightarrow$ °F, GB $\leftrightarrow$ MB).
- **QR Code Generator**: Generate high-resolution offline QR codes for URLs, text, and Wi-Fi credentials with one-tap Android Sharesheet integration.
- **Smart Chained Pipelines**: Multi-tool execution flows (e.g. *Select Images $\to$ Compress $\to$ PDF $\to$ Share*).

---

## 🔒 100% Offline & Privacy Guarantee

- **Zero Remote Databases**: No Firebase, Supabase, or cloud dependencies.
- **Zero Cloud Processing**: Every image, document, and mathematical operation is computed strictly on-device.
- **No Mandatory Accounts**: The app launches directly into productivity without sign-up or login screens.

---

## 🛠 Tech Stack

- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture (Core, Domain, Feature)
- **Concurrency**: Kotlin Coroutines & Flow
- **Image Processing**: Android `BitmapFactory` & `android.graphics.pdf.PdfDocument`
- **QR Engine**: ZXing Core (Local Matrix Generation)
- **Navigation**: Jetpack Navigation Compose

---

## 🚀 How to Open and Run

1. Open **Android Studio**.
2. Select **Open** and choose `c:\Users\USER PC\Desktop\player`.
3. Allow Gradle to sync dependencies.
4. Select an Android device or emulator (API 26+) and click **Run** (`Shift + F10`).
