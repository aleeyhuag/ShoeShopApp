# Shoe Shop App

Offline Android app: scan a shoe with the camera, get back its saved price,
cost, lowest-acceptable price, and size stock — no names, no tags, no fixed
shelf location required.

## Full file tree

```
ShoeShopApp/
├── .gitignore
├── .github/workflows/
│   └── build-apk.yml             # builds a debug APK on every push, no local setup needed
├── build.gradle.kts               # project-level Gradle config
├── settings.gradle.kts
└── app/
    ├── build.gradle.kts           # Compose, Room, CameraX, TFLite dependencies
    └── src/main/
        ├── AndroidManifest.xml    # camera permission declared here
        ├── assets/
        │   ├── mobilenet_v1_1.0_224_l2norm_quant_embedding.tflite  # the actual model, bundled
        │   └── README_MODEL.txt   # exactly where it came from + the patch applied to it
        ├── res/
        │   ├── mipmap-*/ic_launcher.png, ic_launcher_round.png     # placeholder app icon
        │   └── values/themes.xml
        └── java/com/agkomputech/shoeshop/
            ├── ShoeShopApplication.kt      # wires DB + embedding extractor + repo as singletons
            ├── data/
            │   ├── local/
            │   │   ├── entity/
            │   │   │   ├── Shoe.kt         # one row per shoe STYLE (price lives here)
            │   │   │   ├── ShoePhoto.kt    # reference photo + embedding, FK to Shoe
            │   │   │   ├── ShoeSize.kt     # per-size stock, FK to Shoe
            │   │   │   └── Sale.kt         # sale record for receipts
            │   │   ├── dao/ShoeDao.kt      # Room queries
            │   │   └── AppDatabase.kt      # Room database class
            │   └── repository/
            │       └── ShoeRepository.kt   # addShoe(), findMatchesForFrame(), recordSale()
            ├── ml/
            │   ├── EmbeddingExtractor.kt   # TFLite wrapper: Bitmap -> embedding vector
            │   └── ShoeMatcher.kt          # cosine similarity ranking, brute-force in-memory
            └── ui/
                ├── MainActivity.kt         # camera permission + navigation host
                ├── theme/Theme.kt
                ├── common/
                │   └── CameraPreview.kt    # shared CameraX preview, used by both screens
                ├── scan/
                │   ├── ScanViewModel.kt    # scanning / matches-found / no-matches state
                │   └── ScanScreen.kt       # camera + Scan button + match list + detail card
                └── addshoe/
                    ├── AddShoeViewModel.kt # 3-photo capture state, form state, save()
                    └── AddShoeScreen.kt    # guided TOP/SIDE/DETAIL capture + price form + sizes
```

## What's wired end-to-end now

- **Database** — Shoe / ShoePhoto / ShoeSize / Sale, Room, with embeddings
  stored as compact byte blobs
- **Matching** — cosine-similarity ranking, brute-force in-memory (correct
  choice at a few-hundred-shoe scale)
- **The actual `.tflite` model** — bundled in `assets/`, a quantized
  MobileNetV1 patched to expose its L2-normalized embedding layer. Verified
  locally before bundling. Full provenance in `assets/README_MODEL.txt`
- **Scan screen** — live camera preview, a real "Scan shoe" button that
  captures a frame, runs it through the matcher, and shows ranked matches
  with a tap-to-select detail card (price / cost / lowest / sizes in stock)
- **Add-shoe screen** — guides you through capturing all 3 reference angles
  (TOP → SIDE → DETAIL) one at a time, then a price form and size/quantity
  entry, then saves everything through the repository
- **Navigation** — Scan screen has a "+ Add shoe" button; Add-shoe screen
  returns to Scan when saved or cancelled
- **Camera permission** — requested on launch, with a plain message shown
  if it's denied
- **App icon** — a simple placeholder generated for this scaffold, at every
  required density, so the build doesn't fail on a missing resource

## Still deliberately left out (not needed to test scanning/matching)

- Receipts/PDF generation — `Sale` table and `recordSale()` exist; PDF
  output would reuse the pharmacy app's `PdfDocument` approach
- Stock deduction on sale — sales are recorded but don't auto-reduce
  `ShoeSize.quantity` yet, same "receipts only for now" decision as the
  pharmacy project
- A real launcher icon/branding — current one is a plain placeholder

## Shipping this to GitHub and getting a downloadable APK

1. Create a new **empty** repository on GitHub (don't let GitHub add a
   README/.gitignore — this project already has both).
2. From inside the unzipped `ShoeShopApp/` folder:
   ```
   git init
   git add .
   git commit -m "Initial scaffold: scan, matching, add-shoe, CI build"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<repo-name>.git
   git push -u origin main
   ```
3. That push triggers `.github/workflows/build-apk.yml` automatically — no
   local Android Studio or Gradle install needed for this step. Watch it run
   under the repo's **Actions** tab.
4. When it finishes (green check), open that workflow run and download the
   **shoe-shop-debug-apk** artifact from the bottom of the page — that's
   your installable `app-debug.apk`.
5. Copy it to your phone (or download directly on the phone) and install it
   — you'll need "install from unknown sources" allowed for whichever app
   you use to open it, since it's not from the Play Store.

Every push to `main` after this rebuilds the APK automatically, so once
you're iterating, each new commit gives you a fresh APK to test.

## Suggested next steps

1. Push to GitHub as above and confirm the CI build succeeds and installs
2. Add a few real shoes through the Add-shoe screen, then test scanning
   against them — this is the point to judge whether match quality is good
   enough as-is, or worth fine-tuning later (see the limitation noted in
   `assets/README_MODEL.txt`)
3. Add the receipts/PDF screen, reusing the pharmacy app's approach
