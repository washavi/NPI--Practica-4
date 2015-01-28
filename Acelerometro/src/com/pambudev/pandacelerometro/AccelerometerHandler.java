/**
 * T�tulo: Aceler�metro
 * Licencia P�blica General de GNU (GPL) versi�n 3 
 * Autores:
 * - Jos� Francisco Bravo S�nchez
 * - Pedro Fern�ndez Bosch
 * Fecha de la �ltima modificaci�n: 06/01/2015
 */

package com.pambudev.pandacelerometro;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

public class AccelerometerHandler implements SensorEventListener{
	private float accelX;
    private float accelY;
    private float accelZ;
    private long time;
    private float power;
    private Sensor accelerometer;

    public AccelerometerHandler(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE); //el manager que se va a usar para el listener

        if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            accelerometer = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No hacemos nada
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {
            accelX = event.values[0];
            accelY = event.values[1];
            accelZ = event.values[2];
            time = event.timestamp;
            power = accelerometer.getPower();
        }
    } //Guardamos los valores del acelerometro desde array event.values

    public float getAccelX() {
        return accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public long getAtTime(){
    	return time;
    }
    
    public float getPower(){
    	return power;
    }
}
