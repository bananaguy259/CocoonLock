package com.luke.cocoonlock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button

class LockAccessibilityService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val enteredPin = StringBuilder()
    private val dotIds = intArrayOf(R.id.dot0, R.id.dot1, R.id.dot2, R.id.dot3)

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                showLock()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val info = AccessibilityServiceInfo().apply {
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        serviceInfo = info

        registerReceiver(screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))

        // Lock immediately when the service (re)starts, e.g. after boot.
        showLock()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Swallow the back key while the lock overlay is showing.
        if (overlayView != null && event.keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyEvent(event)
    }

    private fun showLock() {
        if (overlayView != null) return // already showing

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_lock, null)
        overlayView = view

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        // Intentionally focusable (no FLAG_NOT_FOCUSABLE) so D-pad key events route here.

        windowManager.addView(view, params)
        wirePinPad(view)
        view.post {
            view.findViewById<Button>(R.id.btn1).requestFocus()
        }
    }

    private fun hideLock() {
        overlayView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { /* already removed */ }
        }
        overlayView = null
        enteredPin.clear()
    }

    private fun wirePinPad(view: View) {
        val keyIds = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9"
        )
        keyIds.forEach { (id, digit) ->
            view.findViewById<Button>(id).setOnClickListener { onDigit(view, digit) }
        }
        view.findViewById<Button>(R.id.btnDel).setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                refreshDots(view)
            }
        }
    }

    private fun onDigit(view: View, digit: String) {
        if (enteredPin.length >= 4) return
        enteredPin.append(digit)
        refreshDots(view)
        if (enteredPin.length == 4) {
            if (PinStore.verify(this, enteredPin.toString())) {
                hideLock()
            } else {
                enteredPin.clear()
                refreshDots(view)
                // TODO: add a shake animation / error color flash here if desired
            }
        }
    }

    private fun refreshDots(view: View) {
        dotIds.forEachIndexed { index, dotId ->
            val filled = index < enteredPin.length
            view.findViewById<View>(dotId).setBackgroundResource(
                if (filled) R.drawable.dot_filled else R.drawable.dot_empty
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(screenOnReceiver) } catch (e: Exception) { }
        hideLock()
    }
}
