# All2PDF

Offline Android converter for turning common Office documents, slide decks, spreadsheets, images, PDFs and text/web files into PDF.

The Android app has no INTERNET permission. Conversion happens on-device. Office/PDF/image rendering uses the open-source `@zrimo/viewer` browser/WASM engine and Android writes the rendered pages into a PDF. Output is saved to `Downloads/All2PDF/`.

## Inputs

PPT/PPTX/PPTM/PPSX, DOC/DOCX/DOCM, XLS/XLSX/XLSM, PDF, PNG/JPG/WebP/GIF/BMP/TIFF/SVG, CSV/TSV, TXT/Markdown/HTML/JSON/XML/RTF and common source-code text files.

## Source bundle

This branch is an isolated All2PDF workspace because the connected GitHub integration cannot create a new repository. The complete project source is stored as `All2PDF-source.tgz.b64`.

Expand it with:

```bash
base64 -d All2PDF-source.tgz.b64 | tar -xz
```

The workflow in `.github/workflows/all2pdf-bootstrap.yml` performs the same expansion, builds the bundled web/WASM engine, compiles Android with SDK 35 / Gradle 8.9 / Java 17, and uploads the debug APK as a GitHub Actions artifact.

## Build status

The initial APK build completed successfully on 2026-09-10. The APK is also available from the ChatGPT conversation that created this branch.
