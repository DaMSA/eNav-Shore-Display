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
package dk.frv.enav.esd.settings;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;
import java.util.Properties;

import com.bbn.openmap.util.PropUtils;

/**
 * General GUI settings
 */
public class GuiSettings implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String PREFIX = "gui.";

	private boolean maximized = false;
	private Point appLocation = new Point(10, 10);
	private Dimension appDimensions = new Dimension(1280, 800);
	private boolean fullscreen = false;
	private boolean multipleInstancesAllowed = false;
	private String workspace = "";
	private String wmsQuery = "";
	private boolean useWMS = false;

	/**
	 * Constructor
	 */
	public GuiSettings() {

	}

	public Dimension getAppDimensions() {
		return appDimensions;
	}

	public Point getAppLocation() {
		return appLocation;
	}

	public String getWmsQuery() {
		return wmsQuery;
	}

	public String getWorkspace() {
		return workspace;
	}

	public boolean isFullscreen() {
		return fullscreen;
	}

	public boolean isMaximized() {
		return maximized;
	}

	public boolean isMultipleInstancesAllowed() {
		return multipleInstancesAllowed;
	}

	
	/**
	 * Read the properties element and set the internal variables
	 * @param props
	 */
	public void readProperties(Properties props) {
		maximized = PropUtils.booleanFromProperties(props, PREFIX + "maximized", maximized);
		double x = PropUtils.doubleFromProperties(props, PREFIX + "appLocation_x", appLocation.getX());
		double y = PropUtils.doubleFromProperties(props, PREFIX + "appLocation_y", appLocation.getY());
		appLocation.setLocation(x, y);
		double w = PropUtils.doubleFromProperties(props, PREFIX + "appDimensions_w", appDimensions.getWidth());
		double h = PropUtils.doubleFromProperties(props, PREFIX + "appDimensions_h", appDimensions.getHeight());
		appDimensions.setSize(w, h);
		fullscreen = PropUtils.booleanFromProperties(props, PREFIX + "fullscreen", fullscreen);
		workspace = props.getProperty(PREFIX + "workspace");
		wmsQuery = props.getProperty(PREFIX + "wmsQuery");
		useWMS = PropUtils.booleanFromProperties(props, PREFIX + "useWMS", useWMS);
	}

	public void setAppDimensions(Dimension appDimensions) {
		this.appDimensions = appDimensions;
	}

	public void setAppLocation(Point appLocation) {
		this.appLocation = appLocation;
	}

	public void setFullscreen(boolean fullscreen) {
		this.fullscreen = fullscreen;
	}

	public void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}

	public void setMultipleInstancesAllowed(boolean multipleInstancesAllowed) {
		this.multipleInstancesAllowed = multipleInstancesAllowed;
	}

	/**
	 * Set the properties to the value from the internal, usually called
	 * when saving settings to file
	 * @param props
	 */
	public void setProperties(Properties props) {
		props.put(PREFIX + "maximized", Boolean.toString(maximized));
		props.put(PREFIX + "appLocation_x", Double.toString(appLocation.getX()));
		props.put(PREFIX + "appLocation_y", Double.toString(appLocation.getY()));
		props.put(PREFIX + "appDimensions_w", Double.toString(appDimensions.getWidth()));
		props.put(PREFIX + "appDimensions_h", Double.toString(appDimensions.getHeight()));
		props.put(PREFIX + "multipleInstancesAllowed", Boolean.toString(multipleInstancesAllowed));
		props.put(PREFIX + "fullscreen", Boolean.toString(fullscreen));
		props.put(PREFIX + "workspace", workspace);
		props.put(PREFIX + "wmsQuery", wmsQuery);
		props.put(PREFIX + "useWMS", Boolean.toString(useWMS));
	}

	public void setWmsQuery(String wmsQuery) {
		this.wmsQuery = wmsQuery;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public boolean useWMS() {
		return useWMS;
	}

	public void setUseWMS(boolean useWMS) {
		this.useWMS = useWMS;
	}
	

}
