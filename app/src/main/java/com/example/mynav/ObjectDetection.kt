package com.example.mynav

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.example.mynav.databinding.ActivityObjectDetectionBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage.fromMediaImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.util.*

class ObjectDetection : AppCompatActivity(),TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityObjectDetectionBinding
    private lateinit var objectDetector: ObjectDetector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var tts: TextToSpeech? = null
  //  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_object_detection)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable{
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider = cameraProvider)
        },ContextCompat.getMainExecutor(this))
        val localModel = LocalModel.Builder()
            .setAssetFilePath("object_detection.tflite")
            .build()
        val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            //  .enableMultipleObjects()
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
      tts = TextToSpeech(this, this)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    //  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bindPreview(cameraProvider: ProcessCameraProvider){
        val preview = Preview.Builder().build()
        val cameraSelector =CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(720,1280))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),ImageAnalysis.Analyzer{ imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image
            if (image != null){
                val processImage= fromMediaImage(image,rotationDegrees)
                objectDetector
                    .process(processImage)
                    .addOnSuccessListener { objects ->
                        for (i in objects){
                            if(binding.parentLayout.childCount >1) binding.parentLayout.removeViewAt(1)
                            val element = Draw(context =this,
                                rect = i.boundingBox,
                                text = i.labels.firstOrNull()?.text ?: "Undefined")
                            binding.parentLayout.addView(element,1)

                            fun processTexttoSpeech() {
                                val objectname = i.labels.firstOrNull()?.text ?: "Undefined"

                                tts!!.speak(objectname, TextToSpeech.QUEUE_FLUSH, null, "")
                            }
                            processTexttoSpeech()

                        }

                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        Log.v("HomeFragment","Error- ${it.message}")
                        imageProxy.close()
                    }
            }
        })
        cameraProvider.bindToLifecycle(this as LifecycleOwner,cameraSelector,imageAnalysis,preview)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The language specified is not supported!!!")
            } else {
                Log.e("TTS", "Initilization Failed!!!")
            }
        }
    }



}