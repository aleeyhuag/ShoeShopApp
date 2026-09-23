mobilenet_v1_1.0_224_l2norm_quant_embedding.tflite
====================================================

What it is:
  A quantized MobileNetV1 (224x224 input) with its output redirected to the
  pre-classification, L2-normalized 1024-dimension embedding layer — i.e. it
  outputs a "fingerprint" vector for an image instead of a class label.

Where it came from:
  Source file: mobilenet_v1_1.0_224_l2norm_quant.tflite
  From: https://github.com/google-coral/test_data (Apache License 2.0)
  This is Google's own "weight imprinting" base model — built specifically so
  its penultimate layer is a clean, L2-normalized embedding, which is exactly
  what similarity search (comparing a scanned shoe against saved shoes) needs.
  It's the CPU-only variant (no EdgeTPU custom ops), so it runs on an ordinary
  phone via the standard org.tensorflow:tensorflow-lite interpreter — no
  Coral hardware required.

What was changed:
  The original file's declared graph output is tensor index 95
  ("MobilenetV1/Predictions/Reshape_1", a [1,1001] ImageNet classification).
  That's not useful here — we don't care what ImageNet class a shoe looks like,
  we care about its embedding. Tensor index 91
  ("MobilenetV1/Logits/Normalize_1a/.../FakeQuantWithMinMaxVars", shape
  [1,1,1,1024]) is the L2-normalized embedding computed just before the
  classification head. A single 4-byte edit to the flatbuffer's SubGraph
  "outputs" field (95 -> 91) redirects the model to expose that tensor as its
  output instead — no weights changed, no layers removed, same file otherwise.

Input / output contract (see EmbeddingExtractor.kt):
  Input:  uint8 [1, 224, 224, 3] — raw 0..255 pixel bytes, no separate
          normalization (quantization scale 1/128, zero point 128 is baked
          into the model itself).
  Output: uint8 [1, 1, 1, 1024] — dequantize with scale 0.0010334347607567906
          and zero point 127 (both measured directly off this file) to get a
          float embedding, then re-L2-normalize.

Verified before bundling:
  Ran locally with ai-edge-litert (a lightweight TFLite interpreter) against
  synthetic test images — confirmed the model loads, the patched output shape
  is [1,1,1,1024], and two similar-color images produce a higher cosine
  similarity than two very different-color images, i.e. the embedding
  actually carries visual similarity signal rather than being random noise.

Known limitation to expect:
  This model was trained for general ImageNet object recognition (1000
  everyday object/animal classes), not specifically for shoes. It should
  reliably separate different shoe styles and colors from each other (see
  ShoeMatcher.kt for the similarity-ranking approach), but its ability to
  distinguish very close look-alikes (e.g. the same slide in two very similar
  shades) is inherently rougher than a model fine-tuned on shoe photos
  specifically would be. Fine-tuning is a reasonable next step once there's
  a real photo library to fine-tune against — not worth doing before that.
