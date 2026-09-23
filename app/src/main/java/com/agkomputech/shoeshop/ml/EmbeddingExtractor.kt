package com.agkomputech.shoeshop.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.Closeable

/**
 * Wraps a quantized MobileNetV1 TFLite model whose output has been redirected
 * (via a one-byte flatbuffer patch — see the model README) to its pre-classification
 * L2-normalized embedding layer, instead of the original 1001-class ImageNet output.
 *
 * Model file: mobilenet_v1_1.0_224_l2norm_quant_embedding.tflite — already bundled
 * in app/src/main/assets/. Source model: google-coral/test_data's
 * mobilenet_v1_1.0_224_l2norm_quant.tflite (Apache 2.0), CPU-compatible (no EdgeTPU
 * ops), originally built for on-device "weight imprinting" — which is exactly the
 * embedding-comparison use case this app needs.
 *
 * This is a quantized (uint8) model both in and out:
 *  - Input: 224x224x3 raw pixel bytes, 0..255, no separate normalization step —
 *    the model's own input quantization (scale 1/128, zero point 128) handles that.
 *  - Output: 1024 uint8 values; dequantized below using the tensor's own scale/zero
 *    point (measured directly off this exact model file) into a float embedding,
 *    then re-L2-normalized to absorb the small quantization rounding error.
 */
class EmbeddingExtractor(context: Context) : Closeable {

    companion object {
        private const val MODEL_FILE = "mobilenet_v1_1.0_224_l2norm_quant_embedding.tflite"
        private const val INPUT_SIZE = 224       // width & height in pixels the model expects
        const val OUTPUT_SIZE = 1024             // length of the embedding vector this model emits

        // Output tensor's quantization parameters, read directly off this exact
        // model file — do not reuse these if you ever swap in a different model.
        private const val OUTPUT_SCALE = 0.0010334347607567906f
        private const val OUTPUT_ZERO_POINT = 127
    }

    private val interpreter: Interpreter
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .build() // no NormalizeOp — this quantized model takes raw 0..255 pixel bytes directly

    init {
        val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
        interpreter = Interpreter(modelBuffer)
    }

    /** Runs one bitmap through the model and returns its L2-normalized embedding vector. */
    fun extract(bitmap: Bitmap): FloatArray {
        var tensorImage = TensorImage(org.tensorflow.lite.DataType.UINT8)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Output comes back as raw uint8 bytes — shape [1,1,1,1024] flattened to 1024.
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1, 1, OUTPUT_SIZE), org.tensorflow.lite.DataType.UINT8)
        interpreter.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        val quantized = outputBuffer.intArray // 0..255 values, one per embedding dimension
        val dequantized = FloatArray(OUTPUT_SIZE) { i ->
            (quantized[i] - OUTPUT_ZERO_POINT) * OUTPUT_SCALE
        }
        return l2Normalize(dequantized)
    }

    /** Normalizing to unit length lets ShoeMatcher use a plain dot product as cosine similarity. */
    private fun l2Normalize(vector: FloatArray): FloatArray {
        var sumSquares = 0f
        for (v in vector) sumSquares += v * v
        val norm = kotlin.math.sqrt(sumSquares).coerceAtLeast(1e-8f)
        return FloatArray(vector.size) { i -> vector[i] / norm }
    }

    override fun close() {
        interpreter.close()
    }
}
