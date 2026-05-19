# 🧠 LensaCerdas - Developer & Agent Handbook

Dokumen ini berisi rangkuman arsitektur, basis kode terbaru, status implementasi, dan panduan konfigurasi untuk proyek **LensaCerdas** (Aplikasi Android Jetpack Compose & Backend Serverless Node.js).

---

## 📱 Arsitektur Aplikasi Android (Kotlin & Jetpack Compose)

Aplikasi dibangun menggunakan **Jetpack Compose Single-Activity Architecture** dengan sistem state navigation (`currentScreen`) untuk transisi antar halaman yang mulus.

### 1. Struktur Halaman & Fitur (Screens)
*   **`SplashScreen.kt`**: Halaman pemuat awal dengan efek *ambient glow* dan transisi ke onboarding.
*   **`OnboardingScreen.kt`**: Pengenalan visual aplikasi menggunakan desain bento dan ilustrasi modern.
*   **`LoginScreen.kt`**: Mengintegrasikan **Google Credential Manager API** modern untuk Android 14+. Menghasilkan ID Token Google dan mengirimkannya ke backend.
*   **`DashboardScreen.kt`**: Halaman utama berbasis *Bento Grid* dengan shortcut cerdas:
    *   **Mulai Sekarang**: Navigasi langsung ke `SummarizeScreen`.
    *   **Unggah PDF**: Pengekstrak teks file PDF langsung ke `SummarizeScreen`.
    *   **Ringkas Tautan Web**: Modal tempel URL web, otomatis navigasi dengan URL siap ringkas.
    *   **Aktivitas Terkini**: Daftar dinamis menarik 2 riwayat ringkasan terakhir dari Supabase.
*   **`HistoryScreen.kt`**: Daftar riwayat ringkasan terisolasi per akun pengguna, dengan fitur **Edit Ringkasan**, **Hapus Ringkasan**, dan **Ekspor & Share PDF** via `iTextG`.
*   **`SummarizeScreen.kt`**: Halaman penginputan teks panjang atau file untuk diringkas menggunakan AI (Gemini-2.5-flash). Terintegrasi dengan **Google ML Kit (Vision Text)** untuk OCR dari Kamera & Galeri, pembaca PDF (`iTextG`), dan input tautan web.
*   **`ProfileScreen.kt`**: Info akun Google yang tersinkronisasi, menu pengaturan personal, dan tombol **Keluar dari Akun**.

### 2. Navigasi & Transisi Statis (`MainActivity.kt`)
*   Navigasi dibungkus di dalam `Box` tingkat tinggi dengan state-saveable navigation.
*   Sistem transisi menggunakan `AnimatedContent`.
*   **Optimalisasi Navigasi**: Perpindahan antar tab bawah (*Dashboard, History, Summarize, Profile*) menggunakan efek pudar (**Crossfade/Fade**) yang sangat ringan untuk menghemat memori. Animasi geser (*Slide*) hanya digunakan pada alur onboarding.
*   **Floating BottomNavBar**: Ditarik keluar dari masing-masing halaman sehingga **tetap diam (statis)** di bawah layar saat konten halaman di atasnya berpindah/slide.

---

## 🖥️ Arsitektur Backend (Node.js, Express, Supabase & Vercel)

Backend telah dimigrasikan dari MySQL lokal ke **Supabase Cloud (PostgreSQL)** untuk reliabilitas tinggi dan dideploy secara serverless ke **Vercel**.

### 1. Skema Database Relasional (Supabase/PostgreSQL)
Database menggunakan relasi `One-to-Many` antara Pengguna (`users`) dan Ringkasan (`summaries`).

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    google_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    photo_url TEXT,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE summaries (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_text TEXT NOT NULL,
    summary_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 2. Endpoint API Utama (`routes/api.js`)
*   `POST /api/login`: Sinkronisasi akun Google Android ke DB Supabase. Mengembalikan `user_id`.
*   `POST /api/summarize`: Memproses teks menggunakan **Gemini-2.5-flash AI** dan menyimpannya ke database.
*   `GET /api/history?user_id=X&limit=L&offset=O`: Mengambil histori ringkasan pengguna dengan **pagination** hemat bandwidth.
*   `PUT /api/update/:id`: Memperbarui judul dan isi teks ringkasan.
*   `DELETE /api/delete/:id`: Menghapus data ringkasan secara permanen.

---

## ⚡ Optimalisasi & Standar Kinerja

Untuk rilis produksi yang stabil dan hemat daya, aplikasi telah dioptimalkan pada aspek-aspek berikut:

1.  **Ukuran APK (ProGuard / R8)**:
    *   Mengaktifkan `isMinifyEnabled = true` dan `isShrinkResources = true` di `build.gradle.kts`.
    *   Menambahkan aturan aman di `proguard-rules.pro` untuk menjaga kelancaran library berat seperti Google ML Kit OCR, iTextG, Retrofit, dan Lottie.
2.  **Efisiensi CPU (Jetpack Compose)**:
    *   Menggunakan `derivedStateOf` pada `HistoryScreen.kt` agar filter pencarian teks riwayat tidak menghitung ulang di setiap recomposition kecil (mencegah HP panas & boros baterai).
3.  **Coroutine & Memory Leak Safety**:
    *   Coroutine berjalan di dalam lifecycle-aware scope (`LaunchedEffect` dan `rememberCoroutineScope`).
    *   Menangani `CancellationException` secara aman sehingga tidak memicu error Toast palsu sewaktu user berpindah layar dengan cepat.
4.  **Efisiensi Database (Supabase & Vercel)**:
    *   Endpoint API `GET /api/history` menggunakan seleksi kolom spesifik (tidak memakai `select *` untuk menghindari payload `original_text` yang sangat besar terkirim sia-sia).
    *   Menerapkan limit & range offset (pagination) di backend Express.js.

---

## 🌐 Konfigurasi Produksi

Aplikasi Android telah dikonfigurasi untuk langsung menembak server produksi Vercel:
*   **Base URL**: `https://backend-lensacerdas.vercel.app/` (didefinisikan di `ApiClient.kt`)

---

## 🛠️ Status Implementasi

*   [x] Migrasi XML ke Jetpack Compose penuh.
*   [x] Integrasi Google Sign-in dengan Credential Manager API.
*   [x] Efek Glassmorphism & Bento Design.
*   [x] Navigasi statis Bottom Nav dengan transisi pudar (Fade).
*   [x] Pembuatan Halaman Riwayat, Profil, dan Meringkas.
*   [x] Migrasi Database Lokal ke Supabase (PostgreSQL) Cloud.
*   [x] Deploy Backend ke Vercel Serverless.
*   [x] Integrasi API Android ke Node.js via Retrofit (Login Flow).
*   [x] Koneksi UI `SummarizeScreen` & `HistoryScreen` ke API Retrofit.
*   [x] Integrasi Ekstraksi PDF dan OCR ML Kit (Kamera & Galeri).
*   [x] Integrasi Ekspor ke PDF & Share PDF (`iTextG`).
*   [x] Fungsionalitas Penuh Shortcut Dashboard & Real Recent Activity (2 Ringkasan Terakhir).
*   [x] Optimalisasi R8/ProGuard, derivedStateOf, dan Bug Fix Edit dialog.
*   [x] Penambahan fitur Text-to-Speech (TTS) 'Dengar Ringkasan' di halaman Summarize & History.
*   [x] Penambahan fitur Custom Summary Style (Pilihan Gaya Bahasa & Panjang Ringkasan) dihubungkan ke backend Gemini AI.
