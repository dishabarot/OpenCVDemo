package com.example.opencvdemo

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.hardware.Camera
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : CameraActivity() , CvCameraViewListener2, View.OnTouchListener, Tutorial3View.OnPictureTakenListener{

    private var mOpenCvCameraView: Tutorial3View? = null
    private var imageView: ImageView? = null
    private var mResolutionList: List<Camera.Size>? = null
    private var mMenu: Menu? = null
    private var mCameraStarted = false
    private var mMenuItemsCreated = false
    private lateinit var mEffectMenuItems: Array<MenuItem?>
    private var mColorEffectsMenu: SubMenu? = null
    private lateinit var mResolutionMenuItems: Array<MenuItem?>
    private var mResolutionMenu: SubMenu? = null
    private var onPictureTakenListener: Tutorial3View.OnPictureTakenListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        mOpenCvCameraView = findViewById(R.id.tutorial3_activity_java_surface_view)
        imageView = findViewById(R.id.result_image)

        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE

        mOpenCvCameraView!!.setCvCameraViewListener(this)
        mOpenCvCameraView!!.setCvCameraViewListener(this)
        mOpenCvCameraView!!.setOnPictureTakenListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu
        setupMenuItems()
        return true
    }

    private fun setupMenuItems() {
        if (mMenu == null || !mCameraStarted || mMenuItemsCreated) {
            return
        }
        val effects = mOpenCvCameraView!!.getEffectList()
        if (effects == null) {
            Log.e(
                TAG,
                "Color effects are not supported by device!"
            )
            return
        }
        mColorEffectsMenu = mMenu!!.addSubMenu("Color Effect")
        mEffectMenuItems = arrayOfNulls(effects.size)
        var idx = 0
        val effectItr = effects.listIterator()
        for (effect in effects) {
            mEffectMenuItems[idx] = mColorEffectsMenu!!.add(1, idx, Menu.NONE, effect)
            idx++
        }
        mResolutionMenu = mMenu!!.addSubMenu("Resolution")
        mResolutionList = mOpenCvCameraView!!.getResolutionList() as List<Camera.Size>?
        mResolutionMenuItems = arrayOfNulls(mResolutionList!!.size)
        idx = 0
        for (resolution in mResolutionList!!) {
            mResolutionMenuItems!![idx] = mResolutionMenu!!.add(
                2, idx, Menu.NONE,
                Integer.valueOf(resolution.width)
                    .toString() + "x" + Integer.valueOf(resolution.height).toString()
            )
            idx++
        }
        mMenuItemsCreated = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(
            TAG,
            "called onOptionsItemSelected; selected item: $item"
        )
        if (item.groupId == 1) {
            mOpenCvCameraView!!.setEffect(item.title as String)
            Toast.makeText(this, mOpenCvCameraView!!.getEffect(), Toast.LENGTH_SHORT).show()
        } else if (item.groupId == 2) {
            val id = item.itemId
            var resolution: Camera.Size? = mResolutionList!![id]
            mOpenCvCameraView!!.setResolution(resolution!!)
            resolution = mOpenCvCameraView!!.getResolution()
            val caption = Integer.valueOf(resolution!!.width).toString() + "x" + Integer.valueOf(
                resolution.height
            ).toString()
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show()
        }
        return true
    }


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(
                        TAG,
                        "OpenCV loaded successfully"
                    )
                    mOpenCvCameraView!!.enableView()
                    mOpenCvCameraView!!.setOnTouchListener(this@MainActivity)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                TAG,
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(
               TAG,
                "OpenCV library found inside package. Using it!"
            )
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase?> {
        return listOf(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val result = Mat()
        Imgproc.Canny(inputFrame.rgba(), result, 70.0, 100.0)
        return result
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mCameraStarted = true
        setupMenuItems()
    }

    override fun onCameraViewStopped() {}

    external fun FindFeatures(matAddrGr: Long, matAddrRgba: Long)

    companion object {
        private const val TAG = "MainActivity"
        init {
            ToolClassInSeparateJarInSharedDirectory.loadNativeLibrary()
        }
    }



    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.i(TAG, "onTouch event")
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        val currentDateandTime = sdf.format(Date())
        val fileName = Environment.getExternalStorageDirectory().path +
                "/sample_picture_" + currentDateandTime + ".jpg"
        mOpenCvCameraView!!.takePicture(fileName)
        Toast.makeText(this, "$fileName saved", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onPictureTakenCallback(bitmap: Bitmap?) {
        Toast.makeText(this, "setOnPictureTakenCallback", Toast.LENGTH_SHORT).show()

        imageView!!.visibility= View.VISIBLE
        mOpenCvCameraView!!.visibility = View.GONE
        imageView!!.setImageBitmap(bitmap);
    }

}