package com.damc.driver_action

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    companion object {
        val BROADCAST_DETECTED_ACTIVITY = "activity_intent"
        internal val DETECTION_INTERVAL_IN_MILLISECONDS: Long = 1000
        val CONFIDENCE = 70
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rootLayout = findViewById<ConstraintLayout>(R.id.root)
        val rootView = rootLayout.rootView

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val keyboardHeight = screenHeight - rect.bottom

//            if (keyboardHeight > screenHeight * 0.15) {
//                val layoutParams = rootLayout.layoutParams as FrameLayout.LayoutParams
//                layoutParams.bottomMargin = keyboardHeight
//                rootLayout.layoutParams = layoutParams
//            } else {
//                val layoutParams = rootLayout.layoutParams as FrameLayout.LayoutParams
//                layoutParams.bottomMargin = 0
//                rootLayout.layoutParams = layoutParams
//            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}