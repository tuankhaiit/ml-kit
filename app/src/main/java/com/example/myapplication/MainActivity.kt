package com.example.myapplication

import android.Manifest
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initViews()
        initFlows()
    }

    private fun initViews() {
        binding.apply {
            btnCameraX.setOnClickListener {
                permissionCameraXRequestLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            }

            btnCamera.setOnClickListener {
                permissionCameraRequestLauncher.launch(Manifest.permission.CAMERA)
            }

            btnGallery.setOnClickListener {
                pickImageFromGallery()
            }
        }
    }

    private fun initFlows() {
        viewModel.apply {
            lifecycleScope.launchWhenCreated {
                takePictureFlow.collectLatest {
                    binding.imgImage.setImageBitmap(it)
                }
            }
        }
    }


    private fun textRecognizer(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener {
                val resultText = it.text
                for (block in it.textBlocks) {
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    blockFrame?.let {
                        drawRectangleBitmap(bitmap, blockFrame, Color.RED, 20)
                    }
                    for (line in block.lines) {
                        val lineText = line.text
                        val lineCornerPoints = line.cornerPoints
                        val lineFrame = line.boundingBox
                        lineFrame?.let {
                            drawRectangleBitmap(bitmap, lineFrame, Color.GREEN, 10)
                        }
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementCornerPoints = element.cornerPoints
                            val elementFrame = element.boundingBox
                            elementFrame?.let {
                                drawRectangleBitmap(bitmap, elementFrame, Color.BLUE, 5)
                            }
                        }
                    }
                    Log.e("khaitdt", blockText)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }

    }

    private fun faceRecognizer(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setMinFaceSize(0.1f)
            .build()
        val detector = FaceDetection.getClient(options)
        // Or, to use the default option:
        // val detector = FaceDetection.getClient();
        detector.process(image)
            .addOnSuccessListener { faces ->
                Toast.makeText(this, "${faces.size} face", Toast.LENGTH_LONG).show()
                for (face in faces) {
                    val bounds = face.boundingBox
                    drawRectangleBitmap(bitmap, bounds, Color.RED)
                    val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                    val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                    // nose available):
                    val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
                    leftEar?.let {
                        val leftEarPos = leftEar.position
                    }

                    // If contour detection was enabled:
                    val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
                    val upperLipBottomContour =
                        face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points

                    // If classification was enabled:
                    if (face.smilingProbability != null) {
                        val smileProb = face.smilingProbability
                    }
                    if (face.rightEyeOpenProbability != null) {
                        val rightEyeOpenProb = face.rightEyeOpenProbability
                    }

                    // If face tracking was enabled:
                    if (face.trackingId != null) {
                        val id = face.trackingId
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }

    }

    private fun drawRectangleBitmap(bitmap: Bitmap, rect: Rect, color: Int, padding: Int = 0) {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            this.style = Paint.Style.STROKE
            this.isAntiAlias = true
            this.isFilterBitmap = true
            this.isDither = true
            this.color = color
            this.strokeWidth = 3f
        }
        val left = rect.left - padding
        val right = rect.right + padding
        val top = rect.top - padding
        val bottom = rect.bottom + padding
        val newRect = Rect(left, top, right, bottom)

        canvas.drawRect(newRect, paint)
        binding.imgImage.invalidate()
    }

    private fun pickImageFromCamera() {
        val file = File(filesDir, TEMP_FILE_NAME)
        val uri =
            FileProvider.getUriForFile(this, applicationContext.packageName + ".fileprovider", file)
        takePictureLauncher.launch(uri)
    }

    private fun pickImageFromGallery() {
        getContentLauncher.launch("image/*")
    }

    private fun presentImage(bitmap: Bitmap) {
        binding.imgImage.setImageBitmap(bitmap)
    }

    private fun processBitmap(rawBitmap: Bitmap) {
        val bitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true)
        faceRecognizer(bitmap)
        presentImage(bitmap)
    }

    private fun openCameraX() {
        supportFragmentManager.beginTransaction().let {
            val fragment = CameraXFragment.newInstance()
            it.replace(android.R.id.content, fragment, "CameraX")
            it.addToBackStack("CameraX")
            it.commitAllowingStateLoss()
        }
    }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    processBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                lifecycleScope.launch {
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            val path = filesDir.path + File.separator + TEMP_FILE_NAME
                            BitmapFactory.decodeFile(path)
                        }
                        processBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }
        }

    private val permissionCameraRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImageFromCamera()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionCameraXRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if (map.values.all { it }) {
            openCameraX()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TEMP_FILE_NAME = "temp.jpeg"
    }
}