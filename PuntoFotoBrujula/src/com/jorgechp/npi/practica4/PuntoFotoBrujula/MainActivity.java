package com.jorgechp.npi.practica4.PuntoFotoBrujula;

/**
  Copyright 2014 by the contributors

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

This program incorporates work covered by the following copyright and
permission notices:  
 */

/**
 * @author Jorge Chamorro Padial
 * @author Germán Iglesias Padial
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.animation.Animation.AnimationListener;



/**
 * Actividad principal de la aplicacion, el código ha sido tomado de:
 * http://www.techrepublic.com/article/pro-tip-create-your-own-magnetic-compass-using-androids-internal-sensors/
 * 
 * Y adaptado a las especificaciones de la practica
 * 
 * Utiliza el magnetometro y el acelerometro para determinar la orientacion
 * Tambien, en ausencia de estos sensores, se utiliza unicamente el GPS
 * aunque en este caso la precision es muy pobre.
 * 
 * 
 * 
 * 
 * 
 * @author jorge
 *
 */
public class MainActivity extends Activity  implements SensorEventListener {
	private ImageView mPointer, mObjetivo,mImageView;
	private MediaPlayer mp;
	private EditText textoEditable;
	private Button botonIr;
	private CheckBox useGPS;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean datoAcelerometro = false;
    private boolean datoMagnetometro = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private float posicionObjetivo = 0f;
    private boolean isObjetivoActivo = false;
    private boolean isGPS = false;
    private LocationListener loclist;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private final int GRADOS_PRECISION = 5;
	private LocationManager locationManager;
    

	@Override
	/**
	 * Asigna todos los objetos de la interfaz a variables, crea un archivo de sonido
	 * y establece listeners para los elemtnos de la interfaz.
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
	    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		datoMagnetometro = false;
		datoAcelerometro = false;
		
		mPointer = (ImageView) findViewById(R.id.imPosActual);
		mObjetivo = (ImageView) findViewById(R.id.imPosObjetivo);
		
		mImageView = (ImageView) findViewById(R.id.imResult);
		
		botonIr = (Button) findViewById(R.id.btIr);
		textoEditable = (EditText) findViewById(R.id.etPuntoCardinal);
		
		mp = MediaPlayer.create(this, R.raw.victorysound);

		useGPS = (CheckBox) findViewById(R.id.cbBearing);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		botonListener();
		checkBoxListener();
	}

	/**
	 * Activa o desactiva la orientacion por gps cuando
	 * se activa/desactiva el checkbox
	 */
	private void checkBoxListener() {
		useGPS.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				isGPS = !isGPS;				
				procLocationListener();
				
			}
		});
		
	}

	/**
	 * Cuando la orientacion es modificada,
	 * reasigna la orientacion de la imagen
	 * de la interfaz rotandola.
	 */
	protected void procLocationListener() {
		if(isGPS){
			loclist = new LocationListener() {
				
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					
					
				}
				
				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLocationChanged(Location location) {
					float grado = location.getBearing();
					if(isObjetivoActivo){
						procesarObjetivo(grado);
					}
		            RotateAnimation ra = new RotateAnimation(
		            		mCurrentDegree, 
		                    -grado,
		                    Animation.RELATIVE_TO_SELF, 0.5f, 
		                    Animation.RELATIVE_TO_SELF,
		                    0.5f);
		     
		            ra.setDuration(250);
		     
		            ra.setFillAfter(true);
		     
		            mPointer.startAnimation(ra);
		            mCurrentDegree = grado;
					
				}
			};
			locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                    3000,   // 3 sec
                    10,loclist );
		}else{
			locationManager.removeUpdates(loclist);
		}
		
	}

	/**
	 * Establece un nuevo objetivo	 
	 */
	private void botonListener() {
		botonIr.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = textoEditable.getText().toString();
				boolean formatError = false;
				if (text != null && text.length() != 0){
					try{
						//Comprobar que los datos son acordes a valores reales para los grados [0 , 360]
						float grado = Float.parseFloat(text);
						if(grado < 0 || grado > 360){
							throw new NumberFormatException();
						}
						
						actualizarObjetivo(grado);
					}catch(NumberFormatException ex){
						formatError = true;
					}
					
				}
				
				if(formatError){
					Toast.makeText(getBaseContext(),getString(R.string.TOAST_NUMBER_EXCEPTION), 
			                Toast.LENGTH_SHORT).show();					
				}
				
			}
		});
		
	}

	/**
	 * Actualiza el objetivo a cumplir, aplicando
	 * una rotacion sobre la brujula objetivo
	 * @param grado
	 */
	protected void actualizarObjetivo(float grado) {
		final RotateAnimation ra = new RotateAnimation(
        		mCurrentDegree, 
                -grado,
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF,
                0.5f);
		
		ra.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				ra.setFillAfter(true);
				ra.setFillEnabled(true);
				
			}
		});
		mObjetivo.startAnimation(ra);
		posicionObjetivo = grado;
		isObjetivoActivo = true;
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
	
	/**
	 * Vuelve a subscribirse a los sensores si la aplicacion pasa 
	 * a primer plano
	 */
	protected void onResume() {
		 super.onResume();
		 //Registramos el listener del sensor	
		 mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	     mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
		 }
	/**
	 * Si no estamos usando la aplicacion, no necesitamos usar los sensores
	 */
		protected void onPause() {
		    super.onPause();
		    mSensorManager.unregisterListener(this, mAccelerometer);
	        mSensorManager.unregisterListener(this, mMagnetometer);
	        

		}
		
		/**
		 * Si tenemmos datos sobre el acelerometro y el
		 * magnetometro, los actualizamos y rotamos
		 * la brujula en consecuencia.
		 */
		@Override
		public void onSensorChanged(SensorEvent event) {
			if(!isGPS){
				if (event.sensor == mAccelerometer) {
		            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
		            datoAcelerometro = true;
		        } else if (event.sensor == mMagnetometer) {
		            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
		            datoMagnetometro = true;
		        }
				
		        if (datoMagnetometro && datoAcelerometro) {	       
		            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
		            SensorManager.getOrientation(mR, mOrientation);
		            float azimuthInRadians = mOrientation[0];
		            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
		            RotateAnimation ra = new RotateAnimation(
		            		mCurrentDegree, 
		                    -azimuthInDegress,
		                    Animation.RELATIVE_TO_SELF, 0.5f, 
		                    Animation.RELATIVE_TO_SELF,
		                    0.5f);
		     
		            ra.setDuration(250);
		     
		            ra.setFillAfter(true);
		     
		            mPointer.startAnimation(ra);
		            mCurrentDegree = -azimuthInDegress;	            
	
		        }
	            if(isObjetivoActivo){
	            	procesarObjetivo(mCurrentDegree);
	            }
			}
		}
		
		/**
		 * Comprueba si nos encontramos en el intervalos tras el cual
		 * podemos considerar que estamos en el objetivo (determinado por la precision
		 * establecida).
		 * En caso afirmativo, dispara la camara.
		 * @param grado
		 */
		public void procesarObjetivo(float grado){
            
            	float intervaloInferior = (posicionObjetivo - GRADOS_PRECISION)%360;
            	float intervaloSuperior = (posicionObjetivo + GRADOS_PRECISION)%360;
            	if(grado == posicionObjetivo || 
            			(grado >= intervaloInferior && grado <= intervaloSuperior)){
            		isObjetivoActivo = false;
            		dispatchTakePictureIntent();
            		mp.start();
            	}
            
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
		
		
		
		/**
		 * Toma una foto, el codigo se basa en 
		 * http://developer.android.com/training/camera/photobasics.html
		 */
		private void dispatchTakePictureIntent() {
		    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
		        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		    }
		}
		
		@Override
		/**
		 * Muestra una imagen en miniatura de la foto tomada.
		 * El codigo se basa en
		 * http://developer.android.com/training/camera/photobasics.html
		 */
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
		        Bundle extras = data.getExtras();
		        Bitmap imageBitmap = (Bitmap) extras.get("data");
		        mImageView.setImageBitmap(imageBitmap);
		    }
		}
}
