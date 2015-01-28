package com.jorgechp.npi.practica4.acelerometro;
/*
*


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
import java.util.LinkedList;
import java.util.Queue;

import com.pambudev.pandacelerometro.*;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.media.MediaPlayer;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
/**
 * Actividad principal de la apliacion
 * 
 * Monitoriza gestos del usuario 
 * haciendo uso de la biblioteca de clases creada por
 * 
 * - Jose Francisco Bravo Sanchez
 * - Pedro Fernandez Bosch
 * 
 * Tanto su biblioteca como este codigo utilizan una licencia
 * GNU GPL
 * 
 * 
 * 
 * @author jorge
 *
 */
public class MainActivity extends Activity {

	/**
	 * Declaraci�n de variables 
	 */
	Accelerometer accelerometer;
    Float accelX, accelY, accelZ;
    long lastUpdate, lastMov;
    int sdk = android.os.Build.VERSION.SDK_INT;
    final Handler myHandler = new Handler();
    /**
     * Intervalos de tiempo para los que se realiza la medicion
     */
    int deltaTime = 40;
    boolean accelerometerInitiated;
    private int stateApp = 1;
    private MediaPlayer mpOK, mpVictoria;
 // Contiene movimiento basicos
    public enum TipoMovimiento{
    	NINGUNO, ARRIBA, ABAJO, DERECHA, IZQUIERDA, ARRIBAPROFUNDO,ABAJOPROFUNDO
    }
 // Contiene descripcione de movimientos compuestos por una serie de movimientos basicos
    public enum TipoMovimientoAvanzado{
    	NINGUNO, EJERCICIO1, EJERCICIO2, EJERCICIO3
    }
	/**
	 * Apunta al tipo de movimiento basico que hay que realizar
	 */
    private TipoMovimiento tipoMov = TipoMovimiento.NINGUNO;
	/**
	 * Apunta al movimiento compuesto que se esta evaluando actualmente
	 */
    private TipoMovimientoAvanzado tipoMovAvanzado = TipoMovimientoAvanzado.NINGUNO;
    Queue<TipoMovimiento> colaMovimientos = new LinkedList<TipoMovimiento>();
    private boolean siguienteMovimiento;
    private RatingBar rb1, rb2, rb3, rbFinal;
    private Button btEj1, btEj2, btEj3;
    
    protected void sensorThread() {
        Thread t = new Thread() {

            public void run() {
                while(true) {
                    try {
                        Thread.sleep(deltaTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(stateApp == 1)
                    	myHandler.post(ejecutarAccion);
                }
            }
        };
        t.start();
    }

	 /**
	  * La deteccion de gestos se realiza desde una hebra
		 * Funcion principal Acelerometro
		 * Valores ejes X, Y, Z
		 * Valores de aceleracion 
		 * 
		 * Se comprueba el valor de cada eje y, si coincide
		 * con el movimiento basico que se esta evaluando
		 * 
		 * Se procesa ese movimiento y se saca de la pila
		 * 
		 */
    final Runnable ejecutarAccion = new Runnable(){
        public void run(){
            synchronized (this) {
                long currentTime = accelerometer.getAtTime();
                int limit = 35000000; //0,035 segundos
            	/**
            	 * Distancia minima a evaluar. Cuando mas bajo es el valor, menos sensibilidad
            	 * tiene el sistema
            	 */
                float minMov = 1E-6f;
                float mov, movX, movY, movZ;
                long timeDiff;
                
                

                accelX = accelerometer.getAccelX();
                accelY = accelerometer.getAccelY();
                accelZ = accelerometer.getAccelZ();

                ((TextView) findViewById(R.id.valueX)).setText(accelX.toString());
                ((TextView) findViewById(R.id.valueY)).setText(accelY.toString());
                ((TextView) findViewById(R.id.valueZ)).setText(accelZ.toString());

                if (!accelerometerInitiated) {
                    lastUpdate = currentTime;
                    lastMov = currentTime;
                    accelerometer.actPrevAxisValues();
                    accelerometerInitiated = true;
                }

                timeDiff = currentTime - lastUpdate;
               
                if (timeDiff > 0) {
                	
                    if (currentTime - lastMov >= limit) { //intervalo en el cual realizar las comprobaciones
                    	
                    	accelerometer.actAxisMov(timeDiff);
                    	mov = accelerometer.getTotalMov();
                    	movX = accelerometer.getMovXValue();
                    	movY = accelerometer.getMovYValue();
                    	movZ = accelerometer.getMovZValue();
                    	 
                    	if (mov > minMov) {
                            ((TextView) findViewById(R.id.valueMovX)).setText(""+movX);
                            ((TextView) findViewById(R.id.valueMovY)).setText(""+movY);
                            ((TextView) findViewById(R.id.valueMovZ)).setText("" + movZ);
                            
                            switch(tipoMov){
	                            case DERECHA:
	                            	if(accelerometer.isPositiveMovX())
	                            		siguienteMovimiento = true;
	                            	break;
	                            case IZQUIERDA:
	                        		if(accelerometer.isNegativeMovX())
	                        			siguienteMovimiento = true;
	                            	break;
	                            case ARRIBA:
	                            	if(accelerometer.isPositiveMovY())
	                            		siguienteMovimiento = true;
	                            	break;
	                            case ABAJO:
	                            	if(accelerometer.isNegativeMovY())
	                            		siguienteMovimiento = true;
	                            	break;
	                            case ABAJOPROFUNDO:
	                            	if(accelerometer.isPositiveMovZ())
	                            		siguienteMovimiento = true;
	                            	break;
	                            case ARRIBAPROFUNDO:
	                            	if(accelerometer.isNegativeMovZ())
	                            		siguienteMovimiento = true;
	                            	break;
	                            default:
	                            	break;
                            }
                        }

                        lastMov = currentTime;
                    }
                    
                    ((TextView) findViewById(R.id.sensorPowerValue)).setText("" + accelerometer.getPower());
                    

                    if(siguienteMovimiento){
                    	siguienteMovimiento = false;
                    	
                    	if(procesarCola()){
                    		procesarPremio(); 
                    		mpVictoria.start();
                    	}else{
                    		mpOK.start();
                    	}                    	
                    	changeFeedbackText();
                    	
                    	
                    	
                    }
        	        accelerometer.actPrevAxisValues();
                    lastUpdate = currentTime;
                }
            }
        }
    };

    /**
	 * OnCreate Method Override 
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ =  0f;
        accelerometerInitiated = false;
        
		rb1 = (RatingBar) findViewById(R.id.rb1);
		rb2 = (RatingBar) findViewById(R.id.rb2);
		rb3 = (RatingBar) findViewById(R.id.rb3);
		rbFinal = (RatingBar) findViewById(R.id.rbFinal);    
		
		rb1.setEnabled(false);
		rb2.setEnabled(false);
		rb3.setEnabled(false);
		rbFinal.setEnabled(false);
 
		btEj1 = (Button) findViewById(R.id.btEj1);        
		btEj2 = (Button) findViewById(R.id.btEj2); 
		btEj3 = (Button) findViewById(R.id.btEj3);
		
		setListenersBotones();
		
		this.mpOK = MediaPlayer.create(this, R.raw.oksound);
		this.mpVictoria = MediaPlayer.create(this, R.raw.victorysound);
		
        sensorThread(); //Thread simultaneo para manejo del acelerometro (sensorEventListener)

    }

    /**
     * Añade listeners a los botones de la interfaz
     */
    private void setListenersBotones() {
    	OnClickListener listenerBt1 = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tipoMovAvanzado = TipoMovimientoAvanzado.EJERCICIO1;
				procesarEjercicio();
				procesarCola();
				changeFeedbackText();
			}
		};
    	OnClickListener listenerBt2 = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tipoMovAvanzado = TipoMovimientoAvanzado.EJERCICIO2;
				procesarEjercicio();
				procesarCola();
				changeFeedbackText();
			}
		};
    	OnClickListener listenerBt3 = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tipoMovAvanzado = TipoMovimientoAvanzado.EJERCICIO3;
				procesarEjercicio();
				procesarCola();
				changeFeedbackText();
			}
		};
    	
    	
    	btEj1.setOnClickListener(listenerBt1);
    	btEj2.setOnClickListener(listenerBt2);
    	btEj3.setOnClickListener(listenerBt3);
		
	}

    /**
     * Introduce en la cola de movimientos
     * basicos los movimientos a realizar para completar
     * un movimiento compuesto
     */
    protected void procesarEjercicio(){
    	switch(this.tipoMovAvanzado){
    	case EJERCICIO1:
    		colaMovimientos.add(TipoMovimiento.ABAJO);
    		colaMovimientos.add(TipoMovimiento.ARRIBA);
    		colaMovimientos.add(TipoMovimiento.ABAJO);
    		colaMovimientos.add(TipoMovimiento.ARRIBA);
    		break;    	
		case EJERCICIO2:
    		colaMovimientos.add(TipoMovimiento.ABAJO);
    		colaMovimientos.add(TipoMovimiento.IZQUIERDA);
    		colaMovimientos.add(TipoMovimiento.ARRIBA);	
			break;
		case EJERCICIO3:
    		colaMovimientos.add(TipoMovimiento.ABAJOPROFUNDO);
    		colaMovimientos.add(TipoMovimiento.ARRIBAPROFUNDO);
    		colaMovimientos.add(TipoMovimiento.IZQUIERDA);	
			break;	
		default:
			break;
		}
    }
    
    /**
     * Si el jugador ha ganado, procesa su recompensa
     */
	protected void procesarPremio() {
    	switch(this.tipoMovAvanzado){
    	case EJERCICIO1:
    		rb1.setRating(1);    		
    		break;    	
		case EJERCICIO2:
			rb2.setRating(1);			
			break;
		case EJERCICIO3:
			rb3.setRating(1);			
			break;	
		default:
			break;
		}
    	int rating = (int) rbFinal.getRating();    	
    	if (rating == 2){
    		rbFinal.setRating(4);
    	}else{
    		rbFinal.setRating(rating+1);
    	}
    	
		this.tipoMov = TipoMovimiento.NINGUNO;
		this.tipoMovAvanzado = TipoMovimientoAvanzado.NINGUNO;
		
		
		
	}

	/**
	 * Obtiene el siguiente movimiento basico a realizar
	 * @return true si la cola esta vacia, false en caso contrario
	 */
	protected boolean procesarCola() {
		if(this.colaMovimientos.isEmpty()){
			return true;
		}
		this.tipoMov = this.colaMovimientos.poll();
		return false;
	}


	

	
	/**
	 * Cambia el texto de informacion al usuario sobre 
	 * el movimiento que debe realizar a continuacion
	 */
	private void changeFeedbackText(){
				
		switch(tipoMov){
			case ARRIBA:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_ARRIBA));
				break;
			case ABAJO:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_ABAJO));
				break;
			case IZQUIERDA:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_IZQUIERDA));
				break;
			case DERECHA:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_DERECHA));
				break;
			case ABAJOPROFUNDO:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_TI));
				break;
			case ARRIBAPROFUNDO:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_NO_TI));
				break;
			default:
				((TextView) findViewById(R.id.tvFeedback)).setText(getString(R.string.HAZ_ELIGE));
				break;			
		}
	}
	


	
  

	/**
	 * Create Context Menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo)
	{
	    super.onCreateContextMenu(menu, v, menuInfo);
	 	
	}
	
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * item selected for Context Menu
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	boolean res = false;  
        
        return res;		
    }
    
}
