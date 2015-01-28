/**
 * T�tulo: Aceler�metro
 * Licencia P�blica General de GNU (GPL) versi�n 3 
 * Autores:
 * - Jos� Francisco Bravo S�nchez
 * - Pedro Fern�ndez Bosch
 * Fecha de la �ltima modificaci�n: 06/01/2015
 */

package com.pambudev.pandacelerometro;

public interface AccelerometerInterface {
	
	public float getAccelX();

    public float getAccelY();

    public float getAccelZ();

    public long getAtTime();
    
    public float getPower();
    
    /**
     * Actualize previous axis values
     */
    public void actPrevAxisValues();
    
    /**
     * Actualize movement values
     * @param timeDiff time needed to calculate between actual position and previous position
     */
    public void actAxisMov(long timeDiff);
    
    public boolean isPositiveMovX();
    
    public boolean isNegativeMovX();
    
    public boolean isPositiveMovY();
    
    public boolean isNegativeMovY();
    
    public boolean isPositiveMovZ();
    
    public boolean isNegativeMovZ();
    
    public float getTotalMov();
    
    public float getMovXValue();
    
    public float getMovYValue();
    
    public float getMovZValue();
}
