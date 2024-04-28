package com.damc.driver_action.accelerationHelper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt


class Accelerometer(context: Context) {
    // Create an interface with one method
    interface Listener {
        // Method with all 3 axis translation as argument
        fun onTranslation(acceleration: Float)
    }

    // Instance of the listener interface
    private var listener: Listener? = null

    // Method to set the listener instance
    fun setListener(l: Listener?) {
        listener = l
    }

    private val sensorManager: SensorManager
    private val sensor: Sensor
    private val sensorEventListener: SensorEventListener

    private var lastTimestamp: Long = 0
    private var lastAcceleration: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var gravity: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    // Constructor with context as argument
    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                sensorEvent?.let {
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val alpha = 0.9f // low-pass filter coefficient

                        // Apply low-pass filter to smooth out gravity
                        gravity[0] = alpha * gravity[0] + (1 - alpha) * it.values[0]
                        gravity[1] = alpha * gravity[1] + (1 - alpha) * it.values[1]
                        gravity[2] = alpha * gravity[2] + (1 - alpha) * it.values[2]

                        // Subtract gravity from accelerometer readings
                        val acceleration = floatArrayOf(
                            it.values[0] - gravity[0],
                            it.values[1] - gravity[1],
                            it.values[2] - gravity[2]
                        )

                        // Calculate magnitude of acceleration
                        val magnitude = sqrt(
                            acceleration[0] * acceleration[0] +
                                    acceleration[1] * acceleration[1] +
                                    acceleration[2] * acceleration[2]
                        )

                        // Notify listener with the magnitude of acceleration
                        listener?.onTranslation(magnitude)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    fun register() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregister() {
        sensorManager.unregisterListener(sensorEventListener)
    }
}