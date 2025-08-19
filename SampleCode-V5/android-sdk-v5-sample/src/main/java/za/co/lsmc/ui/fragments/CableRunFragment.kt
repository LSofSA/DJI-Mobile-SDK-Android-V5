package za.co.lsmc.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.models.LiveStreamVM
import dji.sampleV5.aircraft.util.ToastUtils
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.value.camera.CameraMode
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.sdk.keyvalue.value.common.EmptyMsg
import dji.sdk.keyvalue.value.file.FileListRequestTimeOrderType
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.et.create
import dji.v5.manager.KeyManager
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.datacenter.livestream.LiveStreamStatus
import dji.v5.manager.datacenter.livestream.LiveVideoBitrateMode
import dji.v5.manager.datacenter.livestream.StreamQuality
import dji.v5.manager.datacenter.media.PullMediaFileListParam
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.utils.common.NumberUtils
import dji.v5.utils.common.StringUtils
import za.co.lsmc.data.Category
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel
import java.util.Timer
import java.util.TimerTask

class CableRunFragment : Fragment() {

    private val cameraStreamManager = MediaDataCenter.getInstance().cameraStreamManager
    private val liveStreamVM: LiveStreamVM by viewModels()
    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    private lateinit var tvLiveInfo: TextView
    private lateinit var tvLiveError: TextView
    private lateinit var svCameraStream: SurfaceView
    private lateinit var lssaAccessRouteButtonBack: Button
    private lateinit var edtInterval: EditText
    private lateinit var btnInterval: Button
    private lateinit var btnActionTakePic: Button

    private lateinit var cameraIndex: ComponentIndexType
    private var cameraStreamSurface: Surface? = null
    private var cameraStreamWidth = -1
    private var cameraStreamHeight = -1
    private var cameraStreamScaleType: ICameraStreamManager.ScaleType = ICameraStreamManager.ScaleType.CENTER_INSIDE

    private var intervalTimer: Timer? = null
    private var isIntervalActive = false

    companion object {
        fun newInstance() = CableRunFragment()
        private const val TAG = "CableRun"
    }

    private fun setupClickListeners() {
        lssaAccessRouteButtonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnActionTakePic.setOnClickListener {
            takePhoto()
        }

        btnInterval.setOnClickListener {
            toggleIntervalPhotos()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.lssa_fragment_cable_run, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvLiveInfo = view.findViewById(R.id.tv_live_info)
        tvLiveError = view.findViewById(R.id.tv_live_error)
        svCameraStream = view.findViewById(R.id.sv_camera_stream)
        lssaAccessRouteButtonBack = view.findViewById(R.id.lssaAccessRouteButtonBack)

        initViews(view)
        initCamera()
        initCameraStream()
        initLiveData()
        setupClickListeners()
        startLiveStream()
    }

    private fun startLiveStream() {
        svCameraStream.visibility = View.VISIBLE
        putCameraStreamSurface()

        if (!liveStreamVM.isStreaming()) {
            val rtmpUrl = liveStreamVM.getRtmpUrl()
            if (rtmpUrl.isNotEmpty()) {
                liveStreamVM.startStream(object : CommonCallbacks.CompletionCallback {
                    override fun onSuccess() {
                        ToastUtils.showShortToast(StringUtils.getResStr(R.string.msg_start_live_stream_success))
                    }

                    override fun onFailure(error: IDJIError) {
                        ToastUtils.showLongToast(
                            StringUtils.getResStr(R.string.msg_start_live_stream_failed, error.description())
                        )
                    }
                })
            }
        }
    }

    private fun takePhoto() {
        getCameraMode { currentMode ->
            if (currentMode != CameraMode.PHOTO_NORMAL) {
                setCameraMode(CameraMode.PHOTO_NORMAL) { success ->
                    if (success) {
                        capturePhoto()
                    } else {
                        ToastUtils.showLongToast("Failed to switch to photo mode")
                        Log.e(TAG, "Failed to switch to photo mode")
                    }
                }
            } else {
                capturePhoto()
            }
        }
    }

    private fun getCameraMode(callback: (CameraMode) -> Unit) {
        val key = CameraKey.KeyCameraMode.create(cameraIndex)
        KeyManager.getInstance().getValue(key, object : CommonCallbacks.CompletionCallbackWithParam<CameraMode> {
            override fun onSuccess(mode: CameraMode?) {
                callback(mode ?: CameraMode.UNKNOWN)
            }

            override fun onFailure(error: IDJIError) {
                Log.e(TAG, "Failed to get camera mode: ${error.description()}")
                callback(CameraMode.UNKNOWN)
            }
        })
    }

    private fun setCameraMode(mode: CameraMode, callback: (Boolean) -> Unit) {
        val key = CameraKey.KeyCameraMode.create(cameraIndex)
        KeyManager.getInstance().setValue(key, mode, object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                Log.d(TAG, "Camera mode set to: $mode")
                callback(true)
            }

            override fun onFailure(error: IDJIError) {
                Log.e(TAG, "Failed to set camera mode: ${error.description()}")
                callback(false)
            }
        })
    }

    private fun capturePhoto() {
        val key = CameraKey.KeyStartShootPhoto.create(cameraIndex)
        KeyManager.getInstance().performAction(key, object : CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShortToast("Photo captured successfully")
                Log.d(TAG, "Photo captured successfully")

                handlePhotoCaptured()
            }

            override fun onFailure(error: IDJIError) {
                ToastUtils.showLongToast("Failed to capture photo: ${error.description()}")
                Log.e(TAG, "Failed to capture photo: ${error.description()}")
            }
        })
    }

    private fun handlePhotoCaptured() {
        getLatestPhotoFilename { filename ->
            if (filename != null) {
                viewModel.savePhotoToDatabase(filename, Category.ACCESS_ROUTE) { savedPhoto ->
                    activity?.runOnUiThread {
                        ToastUtils.showShortToast("Photo saved: ${savedPhoto.filename} (${savedPhoto.category})")
                    }
                }
            } else {
                ToastUtils.showLongToast("Could not retrieve photo filename")
            }
        }
    }

    private fun getLatestPhotoFilename(callback: (String?) -> Unit) {
        val mediaManager = MediaDataCenter.getInstance().mediaManager

        mediaManager?.let { manager ->
            val param = PullMediaFileListParam.Builder()
                .count(50)
                .mediaFileIndex(0)
                .orderType(FileListRequestTimeOrderType.NEW_FIRST)
                .build()

            manager.pullMediaFileListFromCamera(param, object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    val mediaFileListData = manager.getMediaFileListData()
                    val fileList = mediaFileListData.data
                    if (fileList.isNotEmpty()) {
                        val latestFile = fileList.first()
                        callback(latestFile.fileName)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "Failed to pull media file list: ${error.description()}")
                    callback(null)
                }
            })
        } ?: run {
            callback(null)
        }
    }

    private fun toggleIntervalPhotos() {
        if (isIntervalActive) {
            stopIntervalPhotos()
        } else {
            startIntervalPhotos()
        }
    }

    private fun startIntervalPhotos() {
        val intervalText = edtInterval.text.toString()
        if (intervalText.isEmpty()) {
            ToastUtils.showShortToast("Please enter interval in seconds")
            return
        }

        val intervalSeconds = intervalText.toLongOrNull()
        if (intervalSeconds == null || intervalSeconds <= 0) {
            ToastUtils.showShortToast("Please enter a valid interval")
            return
        }

        intervalTimer = Timer()
        intervalTimer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    takePhoto()
                }
            }
        }, 0, intervalSeconds * 1000)

        isIntervalActive = true
        btnInterval.text = getString(R.string.lssa_stop_interval)
        ToastUtils.showShortToast("Started interval photos every $intervalSeconds seconds")
    }

    private fun stopIntervalPhotos() {
        intervalTimer?.cancel()
        intervalTimer = null
        isIntervalActive = false
        btnInterval.text = getString(R.string.lssa_start_interval)
        ToastUtils.showShortToast("Stopped interval photos")
    }

    private fun initViews(view: View) {
        tvLiveInfo = view.findViewById(R.id.tv_live_info)
        tvLiveError = view.findViewById(R.id.tv_live_error)
        svCameraStream = view.findViewById(R.id.sv_camera_stream)
        lssaAccessRouteButtonBack = view.findViewById(R.id.lssaAccessRouteButtonBack)
        edtInterval = view.findViewById(R.id.edtInterval)
        btnInterval = view.findViewById(R.id.btnInterval)
        btnActionTakePic = view.findViewById(R.id.btnActionTakePic)
    }

    private fun initCamera() {
        cameraIndex = ComponentIndexType.LEFT_OR_MAIN
        cameraStreamSurface = svCameraStream.holder.surface

        liveStreamVM.setCameraIndex(cameraIndex)
        liveStreamVM.setLiveStreamQuality(StreamQuality.ORIGINAL)
        liveStreamVM.setLiveVideoBitRateMode(LiveVideoBitrateMode.AUTO)
        liveStreamVM.setLiveStreamScaleType(ICameraStreamManager.ScaleType.CENTER_CROP)

        cameraStreamScaleType = ICameraStreamManager.ScaleType.CENTER_INSIDE

        if (cameraStreamSurface != null && svCameraStream.width != 0) {
            putCameraStreamSurface()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initLiveData() {
        liveStreamVM.liveStreamStatus.observe(viewLifecycleOwner) { status ->
            var liveStreamStatus = status
            if (liveStreamStatus == null) {
                liveStreamStatus = LiveStreamStatus(0, 0, 0, 0, 0, false,
                    dji.v5.manager.datacenter.livestream.VideoResolution(0, 0)
                )
            }

            val liveWidth = liveStreamStatus.resolution?.width ?: 0
            val liveHeight = liveStreamStatus.resolution?.height ?: 0
            val sourceWidth = liveStreamVM.getAircraftStreamFrameInfo(cameraIndex)?.width ?: 0
            val sourceHeight = liveStreamVM.getAircraftStreamFrameInfo(cameraIndex)?.height ?: 0
            val sourceFps = liveStreamVM.getAircraftStreamFrameInfo(cameraIndex)?.frameRate ?: 0
            val liveGCD = NumberUtils.gcd(liveWidth, liveHeight)
            val sourceGCD = NumberUtils.gcd(sourceWidth, sourceHeight)

            val statusStr = StringBuilder().append(liveStreamStatus)
                .append("source width = $sourceWidth\n")
                .append("source height = $sourceHeight\n")
                .append("source fps = $sourceFps\n")

            if (liveGCD != 0) {
                statusStr.append("live ratio = ${liveWidth / liveGCD}/${liveHeight / liveGCD}\n")
            } else {
                statusStr.append("live ratio = NA\n")
            }

            if (sourceGCD != 0) {
                statusStr.append("source ratio = ${sourceWidth / sourceGCD}/${sourceHeight / sourceGCD}\n")
            } else {
                statusStr.append("source ratio = NA\n")
            }

            tvLiveInfo.text = statusStr.toString()
        }

        liveStreamVM.liveStreamError.observe(viewLifecycleOwner) { error ->
            if (error == null) {
                tvLiveError.text = ""
                tvLiveError.visibility = View.GONE
            } else {
                tvLiveError.text = "error : $error"
                tvLiveError.visibility = View.VISIBLE
            }
        }

        liveStreamVM.availableCameraList.observe(viewLifecycleOwner) { cameraIndexList ->
            if (cameraIndexList.isEmpty()) {
                stopLive()
            }
        }
    }

    private fun initCameraStream() {
        svCameraStream.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {}
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                cameraStreamWidth = width
                cameraStreamHeight = height
                cameraStreamSurface = holder.surface
                putCameraStreamSurface()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraStreamManager.removeCameraStreamSurface(holder.surface)
            }
        })
    }

    private fun putCameraStreamSurface() {
        cameraStreamSurface?.let {
            cameraStreamManager.putCameraStreamSurface(
                cameraIndex,
                it,
                cameraStreamWidth,
                cameraStreamHeight,
                cameraStreamScaleType
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLive()
    }

    private fun stopLive() {
        liveStreamVM.stopStream(null)
    }
}