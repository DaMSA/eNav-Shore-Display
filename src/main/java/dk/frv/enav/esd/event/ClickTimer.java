/*
 * Copyright 2012 Danish Maritime Authority. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY Danish Maritime Safety Administration ``AS IS'' 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of Danish Maritime Authority.
 * 
 */
package dk.frv.enav.esd.event;

/**
 * A synchronized singleton made for only keeping track of clicks once for all layers
 */
public class ClickTimer {
	
	private static ClickTimer clickTimer;
	public synchronized static ClickTimer getClickTimer(){
		if(clickTimer == null){
			clickTimer = new ClickTimer();
		}
		return clickTimer;
	}
	private long startTime;
	
	private int interval;
	
	/**
	 * Constructor
	 */
	private ClickTimer() {
		
	}
	
	/**
	 * Has he interval been exceeded
	 * @return
	 */
	public boolean isIntervalExceeded(){
		long endTime = System.currentTimeMillis();
		long difference = endTime - startTime;
		return difference > interval;
	}
	
	/**
	 * Set interval
	 * @param interval
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	/**
	 * Start timer
	 */
	public void startTime(){
		startTime = System.currentTimeMillis();
	}
}
