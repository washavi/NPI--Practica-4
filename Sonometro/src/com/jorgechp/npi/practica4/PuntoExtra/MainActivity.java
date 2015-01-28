package com.jorgechp.npi.practica4.PuntoExtra;

/*
 * Copyright 2014 by the contributors

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */

/**
 * @author Jorge Chamorro Padial
 * @author German Iglesias Padial
 */
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Actividad principal del sistema.
 * Trabaja con el sensor de gravedad, con el de luz
 * y mide los niveles de ruido.
 * 
 * Hace uso de la clase SoundMeter, que he obtenido en
 * http://stackoverflow.com/questions/14181449/android-detect-sound-level
 * si bien desconozco la autoría ya que circula por numerosas webs, foros
 * y tutoriales de android.
 * 
 * 
 * El codigo del sensor de gravedad se ha obtenido de
 * http://www.edumobile.org/android/android-development/get-device-orientation-from-gravity-sensor/
 * 
 * Si no existe movimiento del movil, el sensor de gravedad debe retornar resultados similares
 * a los que obtiene el acelerometro
 * 
 * @author jorge
 *
 */
public class MainActivity extends Activity{
	private SoundMeter sm;
	
	private Button btSonometro, btGravedad, btLuminosidad;
	private TextView twSonometro, tw_datax,tw_datay,tw_dataz, tw_luminosidad_valor, tw_luminosidad_max;
	
	private SensorManager mySensorManager;
	private Sensor myGravitySensor, lightSensor;
	private SensorEventListener sl1,sl3;
	private Handler handler = new Handler();
	private float maxLuminosidad = 0;
	private boolean activarLuz = true;
	private boolean activarGravedad = true;
	
		
	/**
	 * Obtiene los elementos de la interfaz y les asigna
	 * listeners correspondientes.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sm = new SoundMeter();
		
		btSonometro = (Button) findViewById(R.id.btActivarSonometro);
		twSonometro = (TextView) findViewById(R.id.tw_data_decibelios);
		
		
		mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        

        //Si no tenemos sensor de luz, se informa al usuario.
        if (lightSensor == null){
            Toast.makeText(this, 
              getString(R.string.NO_LIGTH_SENSOR), 
              Toast.LENGTH_LONG).show();
           }else{
        	   maxLuminosidad =  lightSensor.getMaximumRange();  
           }
 
        btLuminosidad = (Button) findViewById(R.id.btIluminacion);
        tw_luminosidad_valor = (TextView) findViewById(R.id.tw_luminosidad_valor);
        tw_luminosidad_max = (TextView) findViewById(R.id.tw_max_luminosidad);
        
        btGravedad = (Button) findViewById(R.id.btActivarGravedad);
		tw_datax = (TextView) findViewById(R.id.tw_data_x);
		tw_datay = (TextView) findViewById(R.id.tw_data_y);
		tw_dataz = (TextView) findViewById(R.id.tw_data_z);
		
       
        myGravitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        

		
		listenersBotones();
		listenerSensores();
	}

	/**
	 * Asigna listeners al sensor de luz y al de gravedad
	 */
	private void listenerSensores() {
		//Gravedad
		sl1 = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				if(activarGravedad){
					String x = Float.toString(event.values[0]);
					String y = Float.toString(event.values[1]);
					String z = Float.toString(event.values[2]);			
					
					tw_datax.setText(x);
					tw_datay.setText(y);
					tw_dataz.setText(z);
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
		
		//Luminosidad
		sl3 = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				if(activarLuz){
					String x = Float.toString(event.values[0]);
					String max = Float.toString(maxLuminosidad);
					
					tw_luminosidad_valor.setText(x);
					tw_luminosidad_max.setText(max);
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
		
		mySensorManager.registerListener(
				  sl3, 
				  lightSensor, 
		    SensorManager.SENSOR_DELAY_NORMAL);	
		
		  mySensorManager.registerListener(
				  sl1, 
		    myGravitySensor, 
		    SensorManager.SENSOR_DELAY_NORMAL);	
	}

	/**
	 * Asigna eventos de pulsacion de botones de la interfaz
	 */
	private void listenersBotones() {
		btSonometro.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				final Runnable r = new Runnable() {
				    public void run() {
				    	twSonometro.setText(Double.toString(sm.getAmplitude()));
				        handler.postDelayed(this, 1000);
				    }				    
				};
				
				try {
					sm.start();
				} catch (IllegalStateException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.postDelayed(r, 1000);
			}
		});
		
		btLuminosidad.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				activarLuz = !activarLuz;
			}
		});
		
		btGravedad.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				activarGravedad = !activarGravedad;
			}
		});
		
		
		

	}

		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

	 @Override
	 protected void onPause() {
	  super.onPause();

	 }
	 
	 @Override
	 protected void onResume() {
	  super.onResume();


	 }


}
