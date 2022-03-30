package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.camera.core.*
import androidx.camera.core.FocusMeteringAction.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentCameraBinding
import com.example.myapplication.faceDetection.FaceContourDetectionProcessor
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.abs

class CameraXFragment : Fragment() {
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val viewModel: CameraXViewModel by viewModels()
    private lateinit var binding: FragmentCameraBinding

    private val mainExecutor by lazy {
        ContextCompat.getMainExecutor(requireContext())
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera

    // Orientation
    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
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
    }

    // Camera selector
    private lateinit var cameraSelector: CameraSelector

    // Preview
    private lateinit var preview: Preview

    // Camera capture
    private lateinit var imageCapture: ImageCapture

    // Analysis
    private val imageAnalysis by lazy {
        ImageAnalysis.Builder()
//            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(mainExecutor, faceDetectionProcessor)
            }
    }

    // Face detection
    private val faceDetectionProcessor by lazy {
        FaceContourDetectionProcessor(binding.graphicOverlay)
    }

    private var cameraFacing = CameraSelector.LENS_FACING_BACK
        set(value) {
            field = value
            startCamera()
        }
    private var flashOn = false
        set(value) {
            field = value
            binding.btnFlash.text = if (value) "Flash On" else "Flash Off"
        }

    private val focusView by lazy {
        ProgressBar(requireContext()).apply {
            id = View.generateViewId()
            layoutParams =
                ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.layout)
            constraintSet.connect(
                this.id,
                ConstraintSet.START,
                binding.layout.id,
                ConstraintSet.START,
                0
            )
            constraintSet.connect(
                this.id,
                ConstraintSet.TOP,
                binding.layout.id,
                ConstraintSet.TOP,
                0
            )
            constraintSet.applyTo(binding.layout)
        }
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
        initEvents()

        startCamera()
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
        faceDetectionProcessor.stop()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        binding.btnFlipCamera.setOnClickListener {
            cameraFacing = abs(cameraFacing - 1)
        }
        binding.btnFlash.setOnClickListener {
            flashOn = flashOn.not()
        }
        binding.btnCapture.setOnClickListener {
            it.isEnabled = false
            takePhoto()
        }
        binding.viewFinder.let { surfaceView ->
            surfaceView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    launch {
                        resetExposure()
                        viewModel.focusEvent.emit(Pair(event.x, event.y))
                    }
                }
                true
            }
        }
        binding.exposureBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(view: SeekBar?, value: Int, p2: Boolean) {
                val exposureState = camera.cameraInfo.exposureState
                if (exposureState.exposureCompensationIndex != value) {
                    camera.cameraControl.setExposureCompensationIndex(value)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }

    private fun initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    private fun initEvents() {
        viewModel.apply {
            launch {
                focusEvent.collectLatest {
                    focusAtPosition(it.first, it.second)
                }
            }
        }
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider = cameraProviderFuture.get()

                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Camera selector
                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing)
                    .build()

                // Preview
                preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build().also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                // Image capture
                imageCapture = ImageCapture.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
                updateExposureBar()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    private fun takePhoto() {
        val file = File(requireContext().filesDir, TEMP_FILE_NAME)
        val outPutOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.flashMode = if (flashOn)
            ImageCapture.FLASH_MODE_ON
        else
            ImageCapture.FLASH_MODE_OFF
        imageCapture.takePicture(
            outPutOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    launch {
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

    private fun focusAtPosition(x: Float, y: Float) {
        // Display animation focus view
        addFocusView(x, y)

        // Request focus
        val meteringPoint = binding.viewFinder.meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(meteringPoint)
            .addPoint(meteringPoint, FLAG_AF or FLAG_AE or FLAG_AWB)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        camera.cameraControl.startFocusAndMetering(action).apply {
            addListener({
                removeFocusView()
            }, mainExecutor)
        }
    }

    private fun addFocusView(x: Float, y: Float) {
        // Remove if it existed
        removeFocusView()

        val offset = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            24f,
            resources.displayMetrics
        )
        focusView.translationX = x - offset
        focusView.translationY = y - offset

        binding.layout.addView(focusView)
    }

    private fun removeFocusView() {
        binding.layout.removeView(focusView)
    }

    private fun updateExposureBar() {
        camera.cameraInfo.exposureState.let {
            binding.exposureBar.apply {
                isVisible = it.isExposureCompensationSupported
                min = it.exposureCompensationRange.lower
                max = it.exposureCompensationRange.upper
                progress = it.exposureCompensationIndex
            }
        }
    }

    private fun resetExposure() {
        val max = binding.exposureBar.max
        val min = binding.exposureBar.min
        binding.exposureBar.progress = (max + min) / 2
    }

    private fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewLifecycleOwner.lifecycleScope.launch(context, start, block)
    }

    companion object {
        private const val TEMP_FILE_NAME = "temp_photo.jpeg"

        fun newInstance(): CameraXFragment {
            return CameraXFragment()
        }
    }
}