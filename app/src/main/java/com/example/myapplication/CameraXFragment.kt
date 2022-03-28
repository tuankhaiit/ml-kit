package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.camera.core.*
import androidx.camera.core.FocusMeteringAction.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentCameraBinding
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class CameraXFragment : Fragment() {
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val executor by lazy {
        ContextCompat.getMainExecutor(requireContext())
    }

    // Preview
    private val preview by lazy {
        Preview.Builder()
            .build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
    }

    // Select back camera as a default
    private val cameraSelector by lazy {
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    // Camera capture
    private val imageCapture by lazy {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .build()
    }

    private var flashOn = false
        set(value) {
            field = value
            binding.btnCapture.text = if (value) "Flash On" else "Flash Off"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        R.layout.fragment_camera
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initCamera()
        startCamera()
    }

    private fun initViews() {
        binding.btnFlash.setOnClickListener {
            flashOn = flashOn.not()
        }
        binding.btnCapture.setOnClickListener {
            takePhoto()
        }
    }

    private fun initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            val orientationEventListener = object : OrientationEventListener(requireContext()) {
                override fun onOrientationChanged(orientation: Int) {
                    // Monitors orientation values to determine the target rotation value
                    val rotation: Int = when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }
                    imageCapture.targetRotation = rotation
                }
            }
            orientationEventListener.enable()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                binding.viewFinder.let { surfaceView ->
                    surfaceView.setOnTouchListener { _, motionEvent ->
                        val circle = CircularProgressIndicator(requireContext()).apply {
                            id = View.generateViewId()
                            layoutParams =
                                ConstraintLayout.LayoutParams(
                                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                                )
                        }
                        binding.layout.addView(circle)

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(binding.layout)
                        constraintSet.connect(
                            circle.id,
                            ConstraintSet.START,
                            binding.layout.id,
                            ConstraintSet.START,
                            200
                        )
                        constraintSet.connect(
                            circle.id,
                            ConstraintSet.TOP,
                            binding.layout.id,
                            ConstraintSet.TOP,
                            200
                        )
                        constraintSet.applyTo(binding.layout)

                        val meteringPoint = surfaceView.meteringPointFactory
                            .createPoint(motionEvent.x, motionEvent.y)
                        val action = FocusMeteringAction.Builder(meteringPoint)
                            .addPoint(meteringPoint, FLAG_AF or FLAG_AE or FLAG_AWB)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        camera.cameraControl.startFocusAndMetering(action).also {
                            it.addListener({
                                binding.layout.removeView(circle)
                            }, executor)
                        }
                        true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, executor)
    }

    private fun takePhoto() {
        val file = File(requireContext().filesDir, TEMP_FILE_NAME)
        val outPutOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            outPutOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        BitmapUtils.loadBitmap(file)?.let {
                            sharedViewModel.takePictureFlow.emit(it)
                        }
                        activity?.onBackPressed()
                    }

                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }

            })
    }

    companion object {
        private const val TEMP_FILE_NAME = "temp_photo.jpeg"

        fun newInstance(): CameraXFragment {
            return CameraXFragment()
        }
    }
}