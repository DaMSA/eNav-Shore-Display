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
package dk.frv.enav.esd.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import org.apache.log4j.Logger;

import com.bbn.openmap.BufferedLayerMapBean;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

import dk.frv.ais.geo.GeoLocation;
import dk.frv.enav.esd.ESD;
import dk.frv.enav.esd.event.DragMouseMode;
import dk.frv.enav.esd.event.NavigationMouseMode;
import dk.frv.enav.esd.event.SelectMouseMode;
import dk.frv.enav.esd.layers.ais.AisLayer;
import dk.frv.enav.esd.layers.msi.MsiLayer;
import dk.frv.enav.esd.settings.MapSettings;
import dk.frv.enav.esd.msi.MsiHandler;

/**
 * The panel with chart. Initializes all layers to be shown on the map.
 */
public class ChartPanel extends OMComponentPanel implements MouseWheelListener {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(ChartPanel.class);

	private MapHandler mapHandler;
	private LayerHandler layerHandler;
	private MsiHandler msiHandler;
	private BufferedLayerMapBean map;
	private Layer encLayer;
	private Layer bgLayer;
	
	private NavigationMouseMode mapNavMouseMode;
	private DragMouseMode dragMouseMode;
	private SelectMouseMode selectMouseMode;
	
	
	private MouseDelegator mouseDelegator;
	public int maxScale = 5000;
	private AisLayer aisLayer;
	private MsiLayer msiLayer;
	private MainFrame mainFrame;
	private Color background = new Color(168, 228, 255);

	public void setMouseMode(int mode) {
		// Mode0 is mapNavMouseMode
		if (mode == 0) {
			mouseDelegator.setActive(mapNavMouseMode);
		}

		// Mode1 is DragNavMouseMode
		if (mode == 1) {
			mouseDelegator.setActive(dragMouseMode);
		}
		// Mode1 is Select
		if (mode == 2) {
			mouseDelegator.setActive(selectMouseMode);
		}
		
	}

	public ChartPanel(MainFrame mainFrame) {
		super();

		this.mainFrame = mainFrame;
		// Create the charts own maphandler
		mapHandler = new MapHandler();

		// Add the aishandler to this bean
		mapHandler.add(ESD.getAisHandler());
		mapHandler.add(ESD.getShoreServices());
		mapHandler.add(this);
		mapHandler.add(mainFrame);
		mapHandler.add(mainFrame.getStatusArea());

		// Set layout
		// setLayout(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// Set border
		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		// Max scale
		this.maxScale = ESD.getSettings().getMapSettings().getMaxScale();

	}

	public void initChart() {

		MapSettings mapSettings = ESD.getSettings().getMapSettings();

		initChartDefault();

		// Set last postion
		map.setCenter(mapSettings.getCenter());

		// Get from settings
		map.setScale(mapSettings.getScale());

		add(map);

		getMap().addMouseWheelListener(this);
		
	}

	public void initChartDefault() {
		Properties props = ESD.getProperties();
		
		map = new BufferedLayerMapBean();
		map.setDoubleBuffered(true);
		
//		System.out.println(map.getBackground());
		
		mouseDelegator = new MouseDelegator();
		mapHandler.add(mouseDelegator);
		
		mapNavMouseMode = new NavigationMouseMode(this);
		dragMouseMode = new DragMouseMode();
		selectMouseMode = new SelectMouseMode(this);
		
		mouseDelegator.addMouseMode(mapNavMouseMode);
		mouseDelegator.addMouseMode(dragMouseMode);
		mouseDelegator.addMouseMode(selectMouseMode);
		
		setMouseMode(mainFrame.getMouseMode());
		
		mapHandler.add(dragMouseMode);
		mapHandler.add(mapNavMouseMode);
		mapHandler.add(selectMouseMode);
		
		layerHandler = new LayerHandler();

		// Get plugin layers
		createPluginLayers(props);

		map.setBackground(background);

		// Add layer handler to map handler
		mapHandler.add(layerHandler);
		
		//Add AIS Layer
		aisLayer = new AisLayer();
		aisLayer.setVisible(true);
		mapHandler.add(aisLayer);

		//Add MSI Layer
		msiLayer = new MsiLayer();
		msiLayer.setVisible(true);
		mapHandler.add(msiLayer);

		
        // Create MSI handler
        msiHandler = new MsiHandler();
        mapHandler.add(msiHandler);

		// Create background layer
		String layerName = "background";
		bgLayer = new ShapeLayer();
		bgLayer.setProperties(layerName, props);
		bgLayer.setAddAsBackground(true);
		bgLayer.setVisible(true);
		mapHandler.add(bgLayer);

		if (encLayer != null) {
			mapHandler.add(encLayer);
		}

		// Add map to map handler
		mapHandler.add(map);
		
		// Force a MSI layer update
		msiLayer.doUpdate();
	}
	
	public AisLayer getAisLayer() {
		return aisLayer;
	}

	public MsiHandler getMsiHandler() {
		return msiHandler;
	}

	public void initChart(Point2D center, float scale) {

		initChartDefault();

		// Set last postion
		map.setCenter(center);

		// Get from settings
		map.setScale(scale);

		add(map);

		getMap().addMouseWheelListener(this);
	}

	public void saveSettings() {
		MapSettings mapSettings = ESD.getSettings().getMapSettings();
		mapSettings.setCenter((LatLonPoint) map.getCenter());
		mapSettings.setScale(map.getScale());
	}

	public MapBean getMap() {
		return map;
	}

	public MapHandler getMapHandler() {
		return mapHandler;
	}

	public Layer getEncLayer() {
		return encLayer;
	}

	public void doZoom(float factor) {
		float newScale = map.getScale() * factor;
		if (newScale < maxScale) {
			newScale = maxScale;
		}
		map.setScale(newScale);
	}

	public void encVisible(boolean visible) {
		if (encLayer != null) {
			encLayer.setVisible(visible);
		}
	}

	/**
	 * Given a set of points scale and center so that all points are contained
	 * in the view
	 * 
	 * @param waypoints
	 */
	public void zoomTo(List<GeoLocation> waypoints) {
		if (waypoints.size() == 0) {
			return;
		}

		if (waypoints.size() == 1) {
			map.setCenter(waypoints.get(0).getLatitude(), waypoints.get(0).getLongitude());
			return;
		}

		// Find bounding box
		double maxLat = -91;
		double minLat = 91;
		double maxLon = -181;
		double minLon = 181;
		for (GeoLocation pos : waypoints) {
			if (pos.getLatitude() > maxLat) {
				maxLat = pos.getLatitude();
			}
			if (pos.getLatitude() < minLat) {
				minLat = pos.getLatitude();
			}
			if (pos.getLongitude() > maxLon) {
				maxLon = pos.getLongitude();
			}
			if (pos.getLongitude() < minLon) {
				minLon = pos.getLongitude();
			}
		}

		double centerLat = (maxLat + minLat) / 2.0;
		double centerLon = (maxLon + minLon) / 2.0;
		map.setCenter(centerLat, centerLon);

	}

	public MouseDelegator getMouseDelegator() {
		return mouseDelegator;
	}

	private void createPluginLayers(Properties props) {
		String layersValue = props.getProperty("eeins.plugin_layers");
		if (layersValue == null)
			return;
		String[] layerNames = layersValue.split(" ");
		for (String layerName : layerNames) {
			String classProperty = layerName + ".class";
			String className = props.getProperty(classProperty);
			if (className == null) {
				LOG.error("Failed to locate property " + classProperty);
				continue;
			}
			try {
				// Create it if you do...
				Object obj = java.beans.Beans.instantiate(null, className);
				if (obj instanceof Layer) {
					Layer l = (Layer) obj;
					// All layers have a setProperties method, and
					// should intialize themselves with proper
					// settings here. If a property is not set, a
					// default should be used, or a big, graceful
					// complaint should be issued.
					l.setProperties(layerName, props);
					l.setVisible(true);
					layerHandler.addLayer(l);
				}
			} catch (java.lang.ClassNotFoundException e) {
				LOG.error("Layer class not found: \"" + className + "\"");
			} catch (java.io.IOException e) {
				LOG.error("IO Exception instantiating class \"" + className + "\"");
			}
		}
	}

	public int getMaxScale() {
		return maxScale;
	}

	@Override
	public void findAndInit(Object obj) {

	}

	/**
	 * Call auto follow when zooming
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

	}

	/**
	 * 
	 * @param direction
	 *            1 == Up 2 == Down 3 == Left 4 == Right
	 * 
	 *            Moving by 100 units in each direction Map center is [745, 445]
	 */
	public void pan(int direction) {
		Point point = null;
		Projection projection = map.getProjection();

		int width = projection.getWidth();
		int height = projection.getHeight();

		switch (direction) {
		case 1:
			point = new Point(width / 2, height / 2 - 100);
			break;
		case 2:
			point = new Point(width / 2, height / 2 + 100);
			break;
		case 3:
			point = new Point(width / 2 - 100, height / 2);
			break;
		case 4:
			point = new Point(width / 2 + 100, height / 2);
			break;
		}

		Proj p = (Proj) projection;
		LatLonPoint llp = projection.inverse(point);
		p.setCenter(llp);
		map.setProjection(p);
	}

}
