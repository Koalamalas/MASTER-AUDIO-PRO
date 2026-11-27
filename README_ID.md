# MASTER AUDIO PRO — Android source v0.1

## Isi milestone ini
- Android project Kotlin, minSdk 26 / targetSdk 35.
- Antarmuka gelap-merah.
- 32 band graphic EQ (-12 dB sampai +12 dB).
- Preset Flat, Dangdut/Koplo, Vocal, Bass Boost, Pop, Rock.
- DSP core: 32 peaking biquad filters, compressor sederhana, limiter sample-peak.
- Pemilih file audio lokal dan kontrol play/pause.
- GitHub Actions untuk membangun debug APK.

## Status penting — belum versi final
**DSP core sudah ditulis tetapi belum disambungkan ke output player.** Player pada v0.1 masih menggunakan Android MediaPlayer biasa. Karena itu slider EQ/dynamics saat ini belum mengubah suara yang sedang diputar. Ini adalah source milestone awal, bukan APK mastering yang sudah selesai.

Global mode juga belum diaktifkan. Android Audio Playback Capture membutuhkan persetujuan MediaProjection dan aplikasi sumber harus mengizinkan capture; tidak semua aplikasi mengizinkannya. Audio global tidak dapat dijamin bekerja untuk semua aplikasi/perangkat.

## Build APK lewat GitHub dari HP
1. Buat repository GitHub bernama `MASTER-AUDIO-PRO`.
2. Ekstrak ZIP ini di HP.
3. Unggah isi folder project (bukan folder ZIP) ke root repository.
4. Buka tab **Actions** dan jalankan workflow **Build MASTER AUDIO PRO APK**.
5. Setelah selesai, buka hasil run dan unduh artifact `MASTER-AUDIO-PRO-debug`.
6. Ekstrak artifact ZIP dan pasang `app-debug.apk` di Redmi Note 11.

## Tahap pengembangan berikutnya
1. Hubungkan DspEngine ke pipeline pemutar (PCM decode -> DSP -> AudioTrack atau Media3 AudioProcessor).
2. Tambahkan MediaProjection Audio Playback Capture untuk Global mode, dengan indikator foreground service dan kontrol izin.
3. Routing output Bluetooth, pengukuran peak/RMS, preset tersimpan, dan uji latensi.
4. Uji pada Redmi Note 11 Android 13 / ArrowOS.
