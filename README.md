# 🧠 LensaCerdas - Smart AI-Powered Summarizer & Document Assistant

**LensaCerdas** adalah aplikasi Android modern berbasis **Jetpack Compose** dengan desain premium (Glassmorphism & Bento Grid) yang terintegrasi dengan **Gemini-2.5-flash AI** untuk membantu pengguna merangkum teks, mengekstrak dokumen PDF, membaca tautan web, serta memproses teks dari gambar (OCR) secara cepat dan cerdas.

Proyek ini dibangun sebagai sistem *Full-Stack* yang terdiri dari:
1. **📱 KotlinLensaCerdasAndroid**: Aplikasi Android modern (Jetpack Compose).
2. **🖥️ backend-LensaCerdas**: API Serverless Node.js (Express & Supabase Cloud PostgreSQL) yang dideploy di Vercel.

---

## 🚀 Fitur Utama

- **🤖 AI Summarization (Gemini-2.5-flash)**: Rangkum teks panjang secara instan dengan kustomisasi gaya bahasa & panjang ringkasan.
- **📷 Smart OCR (Google ML Kit)**: Ekstraksi teks dari kamera HP atau galeri foto secara instan.
- **📄 PDF Extractor & Generator**: Ekstrak teks dari file PDF dan ekspor ringkasan kembali ke file PDF (`iTextG`) untuk di-share.
- **🔗 Web URL Summarizer**: Tempel tautan web untuk diringkas otomatis.
- **🎙️ Text-to-Speech (TTS)**: Dengarkan hasil ringkasan suara dengan fitur "Dengar Ringkasan".
- **🔐 Google Sign-In Modern**: Autentikasi aman menggunakan **Google Credential Manager API** (Android 14+).
- **📂 Cloud-Synced History**: Riwayat pencarian dan ringkasan tersinkronisasi di awan secara aman (Supabase) dengan pagination hemat kuota.

---

## 🛠️ Arsitektur & Teknologi

### 📱 Frontend (Android App)
*   **Bahasa & Framework**: Kotlin 1.9+, Jetpack Compose (Single-Activity Architecture).
*   **UI/UX**: Bento Grid Layout, Glassmorphism Effect, Custom AnimatedContent Transition.
*   **Library Utama**:
    *   `Google ML Kit (Vision Text)` untuk deteksi OCR.
    *   `iTextG` untuk pengolahan & ekspor dokumen PDF.
    *   `Retrofit & OkHttp` untuk komunikasi API.
    *   `Kotlin Coroutines & Flow` untuk penanganan asinkron yang aman.

### 🖥️ Backend & Database
*   **Framework**: Node.js, Express.js.
*   **Database**: Supabase Cloud (PostgreSQL) - Relasional `users` & `summaries`.
*   **Deployment**: Vercel Serverless.
*   **AI Integration**: Google Generative AI (Gemini 2.5 Flash API).

---

## 📦 Struktur Repositori

```text
PROJECT/
├── KotlinLensaCerdasAndroid/   # Kode Sumber Aplikasi Android (Kotlin/Compose)
└── backend-LensaCerdas/        # Kode Sumber Backend (Node.js/Express)
```

---

## 🚀 Cara Memulai

### 1. Prasyarat
*   Android Studio Koala atau versi terbaru.
*   Node.js v18+.
*   Akun Supabase & Google Cloud Platform (untuk Google Sign-In & Gemini API).

### 2. Menjalankan Backend Secara Lokal
1. Masuk ke folder backend:
   ```bash
   cd backend-LensaCerdas
   ```
2. Instal dependensi:
   ```bash
   npm install
   ```
3. Buat file `.env` dan masukkan kredensial:
   ```env
   PORT=3000
   SUPABASE_URL=your_supabase_url
   SUPABASE_KEY=your_supabase_anon_key
   GEMINI_API_KEY=your_gemini_api_key
   ```
4. Jalankan server:
   ```bash
   npm start
   ```

### 3. Menjalankan Aplikasi Android
1. Buka folder `KotlinLensaCerdasAndroid` di Android Studio.
2. Buat file `local.properties` (jika belum ada) dan sesuaikan konfigurasi SDK.
3. Hubungkan ke backend dengan menyesuaikan `Base URL` di `ApiClient.kt`.
4. Jalankan aplikasi di Emulator atau Perangkat Fisik (disarankan untuk fitur Kamera/OCR).
