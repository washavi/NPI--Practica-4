/**
 
 * @author Jorge Chamorro Padial
 
 
 
 
 “This License” refers to version 3 of the GNU General Public License.

“Copyright” also means copyright-like laws that apply to other kinds of works, such as semiconductor masks.

“The Program” refers to any copyrightable work licensed under this License. Each licensee is addressed as “you”. 
“Licensees” and “recipients” may be individuals or organizations.

To “modify” a work means to copy from or adapt all or part of the work in a fashion requiring copyright permission, 
other than the making of an exact copy. The resulting work is called a “modified version” of the earlier work or a work “based on” the earlier work.

A “covered work” means either the unmodified Program or a work based on the Program.

To “propagate” a work means to do anything with it that, without permission, would make you directly or 
secondarily liable for infringement under applicable copyright law, except executing it on a computer or modifying a private copy. 
Propagation includes copying, distribution (with or without modification), making available to the public, and in some countries other activities as well.

To “convey” a work means any kind of propagation that enables other parties to make or receive copies. 
Mere interaction with a user through a computer network, with no transfer of a copy, is not conveying.

An interactive user interface displays “Appropriate Legal Notices” to the extent that it includes a convenient 
and prominently visible feature that (1) displays an appropriate copyright notice, and (2) tells the user that there is no warranty 
for the work (except to the extent that warranties are provided), that licensees may convey the work under this License, and how to view a copy
 
 
 more information on http://www.gnu.org/licenses/gpl.html
 
 You can also see license.txt 
 */

package com.jorgechp.npi.practica4.PuntoGestosQR;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * Actividad que contiene:
 * - Una vista de tipo GestureOverLayView donde se pueden recoger gestos de los usuarios
 * - Un capturador de código QR que hace uso de la biblioteca ZXing
 * - Informacion sobre las coordenadas QR
 * 
 * 
 * Parte de este codigo ha sido extraido del tutorial de uso de la libreria
 * ZXing que puedes encontrar en
 * 
 * http://androidcookbook.com/Recipe.seam?recipeId=3324
 * 
 * 
 * @author jorge
 * 
 * Ver documento license.tx para mas informacion sobre la licencia de uso
 *
 */
public class MainActivity extends Activity{

	/**
	 * Necesario para utilizar esta actividad dentro de clases anonimas
	 */
	private Activity tActivity = this;
	
	/**
	 * Enlace a la biblioteca de gestos
	 */
	private GestureLibrary mLibrary;
	/**
	 * Vista donde se recogen los gestos del usuario
	 */
	private GestureOverlayView gow;
	/**
	 * Array de cadenas de diferentes gestos
	 */
	private String[] gestos = new String[5];
	/**
	 * Muestran informacion de latitud y longitud
	 */
	private TextView tvLatitud,tvLongitud;
	/**
	 * Necesario para descartar gestos que no se correspondan con los definidos
	 */
	private float SENSIBILIDAD = (float) 3.0; 
    
	/**
	 * Instancia el array de gestos con aquellos gestos previamente
	 * definidos en el archivo gestures.txt
	 */
    private void instanciarGestos(){
		gestos[0] = "Ele";
		gestos[1] ="M";
		gestos[2] ="N";
		gestos[3] ="Zeta";
		gestos[4] ="uve";
	}
	

		
	  @Override
	  /**
	   * Se ejecuta tras la captura del codigo QR, obteniendo informacion 
	   * recopilada por el codigo y mostrando en la interfaz
	   * de la aplicacion la informacion relativa
	   * a Latitud y a Longitud
	   */
	  protected void onActivityResult(int requestCode, int resultCode, Intent data){
		  IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		  if (scanningResult.getContents() != null) {
			  String[] scanContent = scanningResult.getContents().split("_");
			  
			  //El formato de cadena es el siguiente LATITUD_37.19716626365634_LONGITUD_-3.624230806207606
			  // Y ha de tener cuatro elementos
			  if(scanContent.length == 4){
				  double latitud = Double.parseDouble(scanContent[1]);
				  double longitud = Double.parseDouble(scanContent[3]);
				  
				  tvLatitud.setText(scanContent[1]);
				  tvLongitud.setText(scanContent[3]);
			  }
			  
			}
		  else {
			    Toast toast = Toast.makeText(getApplicationContext(),
			    getString(R.string.NO_DATA_RECEIVED), Toast.LENGTH_SHORT);
			    toast.show();
			}

	  }
	  

	  /**
	     * Comprueba si el dispositivo del usuario tiene o no una cámara
	     * http://www.androidhive.info/2013/09/android-working-with-camera-api/
	     * */
	    private boolean isDeviceSupportCamera() {
	        if (getApplicationContext().getPackageManager().hasSystemFeature(
	                PackageManager.FEATURE_CAMERA)) {
	            // this device has a camera
	            return true;
	        } else {
	            // no camera on this device
	            return false;
	        }
	    }
	  

	  
	@Override
	/**
	 * Instancia todos los elementos de la interfaz
	 * y crea el listener que trabaja con la prediccion de elemento
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		instanciarGestos();
		tvLatitud = (TextView) findViewById(R.id.tvLatitudData);
		tvLongitud = (TextView) findViewById(R.id.tvLongitudData);
		   mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		   if (!mLibrary.load()) {		     finish();
		   }
		 
		   gow = (GestureOverlayView) findViewById(R.id.gvGestures);
		   gow.addOnGesturePerformedListener(new OnGesturePerformedListener() {
			
			@Override
			/**
			 * Cuando un usuario termina un gesto en la interfaz, se analiza
			 * y se compara con los gestos preexistentes, otorgando una determinada
			 * puntuacion.
			 * 
			 * De entrada, se descartan todos los gestos que obtengan una puntuacion menor
			 * que el valor de SENSIBILIDAD
			 * 
			 * Si un gesto supera esa puntuacion, se llama a un Intent que analiza el codigo
			 */
			public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
				   ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
				   
				   if (predictions.size() > 0 && predictions.get(0).score >= SENSIBILIDAD) {
				     Prediction resultado = predictions.get(0);
				     
				     
				    	 String result = resultado.name;
					     for(int i = 0; i < predictions.size();++i){
					    	 if(result.equals(gestos[i])){
					    		 
					    		 /**
					    		  * Si el dispositivo tiene camara y se ha detectado un
					    		  * patron de los permitidos, se inicia el reconocimiento QR
					    		  */
					    		 if(isDeviceSupportCamera()){
					    			IntentIntegrator scanIntegrator = new IntentIntegrator(tActivity);
					    		 	scanIntegrator.initiateScan();
					    		 }
					    	 }
					     }
				 
				     
				   }
				
			}
		});
	}

	@Override
	/**Se ejecuta al crear el menu de opciones
	 * 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);

	}
	 
	/*
	 * Here we restore the fileUri again
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);

	}
	


}
