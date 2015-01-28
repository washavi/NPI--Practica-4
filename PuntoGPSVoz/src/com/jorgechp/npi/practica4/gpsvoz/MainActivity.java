package com.jorgechp.npi.practica4.gpsvoz;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * GPSVoz
 * 
 * Muestra una serie de etiquetas:
 *  - Latitud y longitud objetivo
 *  - Latitud y longitud actuales
 *  - Ruta a seguir
 *  - Conmutador GPS y WIFI como proveedor de servicios de geolocalizacion
 *  - Boton para activar la toma de muestras de coordenadas
 *  
 * Las coordenadas se introducen mediante lenguaje oral. Con el siguiente formato
 * "latitud X longitud Y"
 * 
 * En cuanto el programa reconoce una de las dos palabras, asigna la siguiente como
 * valor para la magnitud que corresponda.
 * 
 * 
 * @author Jorge Chamorro Padial
 *
 */

/*
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
 NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 OR OTHER DEALINGS IN THE SOFTWARE.

 Except as contained in this notice, the name of a copyright holder shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization of the copyright holder. 

 Ver license.txt para mas detalles sobre la licencia GNU GPL
 **/

/**
 * Clase principal de la aplicacion contiene toda la funcionalidad del programa,
 * es decir: - reconocimiento de voz - calculo de la ruta a elegir
 * 
 * Requiere conexion a internet para realizar el reconocimiento de voz
 * 
 * @author jorge
 * 
 */
public class MainActivity extends Activity {
	/**
	 * Longitud y latitud objetivo
	 */
	private float latitud, longitud;
	/**
	 * Longitud y latitud actuales
	 */
	private float latitudGeo, longitudGeo;
	private ToggleButton tbProvider;
	private Button btStartVoz;
	private TextView tvLatitud, tvLongitud, tvPosActualLongitud,
			tvPosActualLatitud, tvRuta, tvProviderStatus;
	/**
	 * Identificador del intend
	 */
	private int VOICE_RECOGNITION_REQUEST_CODE = 4;

	/**
	 * Array de acciones, contiene dos valores "LATITUD" Y "LONGITUD"
	 */
	private ArrayList<String> acciones;
	private ArrayList<String> resultados;

	private LocationManager lm;
	private Location loc;
	private LocationListener ll;

	/**
	 * Precision en grados que se tolera
	 */
	private float precision = (float) 0.05;

	private String providerStatus = LocationManager.GPS_PROVIDER;

	/**
	 * Instancia todos los elementos que existen en la interfaz
	 */
	private void instanciarElementosInterfaz() {
		tvLatitud = (TextView) findViewById(R.id.tvDataLatitud);
		tvLongitud = (TextView) findViewById(R.id.tvDataLongitud);
		tvPosActualLongitud = (TextView) findViewById(R.id.tvPosActualLongitud);
		tvPosActualLatitud = (TextView) findViewById(R.id.tvPosActualLatitud);
		tvRuta = (TextView) findViewById(R.id.tvruta);
		tvProviderStatus = (TextView) findViewById(R.id.tvProviderStatus);

		btStartVoz = (Button) findViewById(R.id.btStartVoz);

		tbProvider = (ToggleButton) findViewById(R.id.tgProvider);
	}

	/**
	 * Asigna un listener que compruebe cuando el conmutador de proveedores de
	 * servicios de geolocalizacion haya sido activado por el usuario
	 */
	private void instanciarToggleListener() {
		tbProvider.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tbProvider.isChecked())
					providerStatus = LocationManager.NETWORK_PROVIDER;
				else
					providerStatus = LocationManager.GPS_PROVIDER;
			}
		});

	}

	/**
	 * Genera un listener que compruebe cuando se produzcan datos en la
	 * geoposicion del sistema
	 * 
	 */
	private void instanciarLocationListener() {
		ll = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				tvProviderStatus.setText(getString(R.string.PROVIDER_ON));

			}

			@Override
			public void onProviderDisabled(String provider) {
				tvProviderStatus.setText(getString(R.string.PROVIDER_OFF));

			}

			@Override
			public void onLocationChanged(Location location) {
				loc = location;
				if (loc != null) {
					actualizarEtiquetasPosicion(loc.getLatitude(),
							loc.getLongitude());
				}

			}
		};
		;

		lm.requestLocationUpdates(providerStatus, 30000, 0, ll);
		loc = lm.getLastKnownLocation(providerStatus);
		if (loc != null) {
			actualizarEtiquetasPosicion(loc.getLatitude(), loc.getLongitude());
		} else {
			actualizarEtiquetasPosicion(0, 0);
		}
	}

	/**
	 * Actualiza el valor de las etiquetas que muestran tanto la posicion
	 * objetivo como la posicion actual.
	 * 
	 * Para establecer el texto de la etiqueta de ruta, ha de recalcular la ruta
	 * de esta
	 * 
	 * @param latitud
	 *            en la que se encuentra el sistema
	 * @param longitud
	 *            en la que se encuentra el sistema
	 */
	protected void actualizarEtiquetasPosicion(double latitud, double longitud) {
		latitudGeo = (float) latitud;
		longitudGeo = (float) longitud;
		tvPosActualLatitud.setText(String.valueOf(latitudGeo));
		tvPosActualLongitud.setText(String.valueOf(longitudGeo));
		tvRuta.setText(actualizarRuta());
	}

	/**
	 * Recalcula el camino que se ha de seguir para llegar al punto de destino
	 * 
	 * @return String con el valor de la ruta que se ha de seguir
	 */
	protected String actualizarRuta() {
		float distanciaLatitud = Math.abs(latitudGeo - latitud);
		float distanciaLongitud = Math.abs(longitudGeo - longitud);
		boolean latitudOk = distanciaLatitud <= precision;
		boolean longitudOk = distanciaLongitud <= precision;
		boolean bajar = latitudGeo > latitud;
		boolean derecha = longitudGeo > longitud;
		String mensaje = null;
		int posicion = 0;
		if (!latitudOk) {
			if (bajar)
				posicion = 6;
			else
				posicion = 3;
		}
		if (!longitudOk) {
			if (derecha)
				posicion += 1;
			else
				posicion += 2;
		}

		switch (posicion) {
		case 0:
			mensaje = getResources().getString(R.string.RUTA_DESTINO_OK);
			break;
		case 1:
			mensaje = getResources().getString(R.string.RUTA_DESTINO_DERECHA);
			break;
		case 2:
			mensaje = getResources().getString(R.string.RUTA_DESTINO_IZQUIERDA);
			break;
		case 3:
			mensaje = getResources().getString(R.string.RUTA_DESTINO_ARRIBA);
			break;
		case 4:
			mensaje = getResources().getString(
					R.string.RUTA_DESTINO_ARRIBA_DERECHA);
			break;
		case 5:
			mensaje = getResources().getString(
					R.string.RUTA_DESTINO_ARRIBA_IZQUIERDA);
			break;
		case 6:
			mensaje = getResources().getString(R.string.RUTA_DESTINO_ABAJO);
			break;
		case 7:
			mensaje = getResources().getString(
					R.string.RUTA_DESTINO_ABAJO_DERECHA);
			break;
		case 8:
			mensaje = getResources().getString(
					R.string.RUTA_DESTINO_ABAJO_IZQUIERDA);
			break;
		}

		return mensaje;
	}

	/**
	 * Se ejecuta cuando la actividad es creada
	 * 
	 * Se crean objetos para las propiedades de la clase se llama a las
	 * funciones que instancian elementos de la interfaz Se solicita el gestor
	 * de localizaciones que incorpora el sistema
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		acciones = new ArrayList<String>();
		resultados = new ArrayList<String>();

		instanciarElementosInterfaz();
		instanciarToggleListener();

		this.lm = (LocationManager) getSystemService(Activity.LOCATION_SERVICE);
		longitud = latitud = Float.MIN_VALUE;
		latitudGeo = longitudGeo = Float.MIN_VALUE;
		acciones.add(getResources().getString(R.string.LATITUD));
		acciones.add(getResources().getString(R.string.LONGITUD));

		btStartVoz.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity(getResources().getString(
						R.string.SAY_TEXT));
			}

		});

	}

	/**
	 * Se activa cuando se crea un evento de menu
	 */
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
	 * Procesa un array de String con coordenadas DMS
	 * y las convierte a formato decimal.
	 * 
	 * El formato DMS es el siguiente:
	 * a) [numero] grados [numero] minutos [numero] segundos [norte|sur|este|oeste]
	 * b) La cadena anterior dos veces, una para latitud y otra para longitud.
	 * 
	 * En el caso b, primero se ha de introducir la latitud y luego la longitud.
	 * 
	 * Una vez que se extraen los diferentes valores ,se realiza la conversión
	 * de grados, minutos y segundos a formato decimal
	 * @param palabras
	 */
	protected void procesarCoordenadasDMS(String[] palabras) {
		Locale spanish = new Locale("es", "ES");
		int size = palabras.length;
		float p, a, b, c;
		float eo, d, f, g;
		if (size == 7 || size == 14) {
			String posicion = palabras[6].toLowerCase(spanish);
			if (size == 7) {
				//estamos introduciendo una latitud
				if (posicion.equals(getString(R.string.NORTE)) || posicion.equals(getString(R.string.SUR))) {
					try {
						p = (posicion.equals(getString(R.string.NORTE))) ? 0 : 1;
						a = Float.parseFloat(palabras[0]);
						b = Float.parseFloat(palabras[2]);
						c = Float.parseFloat(palabras[4]);
						latitud = (float) (a + b / 60.0 + c / 3600.0);
						if (p == 1) {
							latitud *= -1;
						}
					} catch (NumberFormatException nfe) {
						latitud = Float.MIN_VALUE;

					}
					//estamos introduciendo una longitud
				} else if (posicion.equals(getString(R.string.ESTE)) || posicion.equals(getString(R.string.OESTE))) {
					try {

						p = (posicion.equals(getString(R.string.ESTE))) ? 0 : 1;
						a = Float.parseFloat(palabras[0]);
						b = Float.parseFloat(palabras[2]);
						c = Float.parseFloat(palabras[4]);
						longitud = (float) (a + b / 60.0 + c / 3600.0);
						if (p == 1) {
							longitud *= -1;
						}
					} catch (NumberFormatException nfe) {
						longitud = Float.MIN_VALUE;
					}
				}

			} else {
				//Caso b, se introduce tanto latitud como longitud
				String posicionEO = palabras[13].toLowerCase(spanish);
				if (posicion.equals(getString(R.string.NORTE)) || posicion.equals(getString(R.string.SUR))) {
					try {

						p = (posicion.equals(getString(R.string.NORTE))) ? 0 : 1;
						eo = (posicionEO.equals(getString(R.string.ESTE))) ? 0 : 1;

						a = Float.parseFloat(palabras[0]);
						b = Float.parseFloat(palabras[2]);
						c = Float.parseFloat(palabras[4]);
						d = Float.parseFloat(palabras[7]);
						f = Float.parseFloat(palabras[9]);
						g = Float.parseFloat(palabras[11]);

						latitud = (float) (a + b / 60.0 + c / 3600.0);
						longitud = (float) (d + f / 60.0 + g / 3600.0);

						if (p == 1) {
							latitud *= -1;
						}
						if (eo == 1) {
							longitud *= -1;
						}
					} catch (NumberFormatException nfe) {
						latitud = Float.MIN_VALUE;
						longitud = Float.MIN_VALUE;

					}
				}
			}
		}

	}

	/**
	 * Inicia el reconocimiento de codigo
	 * 
	 * En primer lugar, se extraen todas las palabras reconocidas y se formatea
	 * la cadena, esto es, se realizan ciertos cambios para que la cadena quede
	 * en formato <magnitud> <valor>
	 * 
	 * Si la cadena resultante tiene longitud 2 o 4. Significa que tenemos
	 * suficiente informacion para continuar el procesado de la palabra.
	 * 
	 * Se intenta convertir la cadena con <valor> a flotante, y si es posible,
	 * se fija ese valor como valor de la latitud
	 * 
	 * Cuando se asignen tanto la longitud como la latitud, se actualiza la
	 * interfaz y se inicia el proceso de geolocalizacion
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// Si hemos vuelto del intent para capturar código, vamos a indicar
			// las palabras claves encontradas:
			// Primero almacenamos en resultados el texto que hemos pronunciado
			resultados = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			String[] palabras = resultados.get(0).replaceAll(getString(R.string.MINUS)+" ", "-")
					.replace(',', '.').replaceAll("- ", "-").split(" ");
			// A continuación, vamos a comprobar resultados, para ver si se
			// encuentra alguna de las palabras de acciones
			// y las palabras indicadas las almacenamos en un String para
			// mostrarlas a continuación:
			procesarCoordenadasDMS(palabras);
			if (latitud != Float.MIN_VALUE) {
				tvLatitud.setText(Float.toString(latitud));
			}
			if (longitud != Float.MIN_VALUE) {
				tvLongitud.setText(Float.toString(longitud));
			}

			if (latitud != Float.MIN_VALUE && longitud != Float.MIN_VALUE) {
				instanciarLocationListener();
			}

		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Activa el reconocimiento de voz
	 * 
	 */
	private void startVoiceRecognitionActivity(String mensajeIntent) {
		// Definición del intent para realizar en análisis del mensaje
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// Indicamos el modelo de lenguaje para el intent
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// Definimos el mensaje que aparecerá en la parte superior del Intent
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mensajeIntent);
		// Lanzamos la actividad esperando resultados
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

}
