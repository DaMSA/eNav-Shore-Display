/*
 * Copyright 2011 Danish Maritime Authority. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Danish Maritime Authority ``AS IS'' 
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
package dk.frv.enav.esd.layers.route;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.SwingUtilities;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMList;
import com.bbn.openmap.proj.coords.LatLonPoint;

import dk.frv.ais.geo.GeoLocation;
import dk.frv.enav.common.xml.metoc.MetocForecast;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import dk.frv.enav.esd.ESD;
import dk.frv.enav.esd.ais.AisAdressedRouteSuggestion;
import dk.frv.enav.esd.event.DragMouseMode;
import dk.frv.enav.esd.event.NavigationMouseMode;
import dk.frv.enav.esd.event.SelectMouseMode;
import dk.frv.enav.esd.gui.views.JMapFrame;
import dk.frv.enav.esd.gui.views.MapMenu;
import dk.frv.enav.esd.route.ActiveRoute;
import dk.frv.enav.esd.route.IRoutesUpdateListener;
import dk.frv.enav.esd.route.Route;
import dk.frv.enav.esd.route.RouteManager;
import dk.frv.enav.esd.route.RouteWaypoint;
import dk.frv.enav.esd.route.RoutesUpdateEvent;
import dk.frv.enav.ins.common.math.Vector2D;
//import dk.frv.enav.ins.gui.MapMenu;

/**
 * Layer for showing routes
 */
public class RouteLayer extends OMGraphicHandlerLayer implements IRoutesUpdateListener, MapMouseListener {

	private static final long serialVersionUID = 1L;

	private RouteManager routeManager = null;
//	private MainFrame mainFrame = null;
	private MetocInfoPanel metocInfoPanel = null;
	private WaypointInfoPanel waypointInfoPanel = null;
	private MapBean mapBean = null;
	
	private OMGraphicList graphics = new OMGraphicList();
	private OMGraphicList metocGraphics = new OMGraphicList();
	private boolean arrowsVisible = false;
	private OMGraphic closest = null;
	private OMGraphic selectedGraphic = null;
	private MetocGraphic routeMetoc;
	private SuggestedRouteGraphic suggestedRoute;
	private JMapFrame jMapFrame = null;
	private MapMenu routeMenu;
	private boolean dragging = false;
	
	
	public RouteLayer() {
		routeManager = ESD.getRouteManager();
		routeManager.addListener(this);
	}
	
	@Override
	public synchronized void routesChanged(RoutesUpdateEvent e) {
		if(e == RoutesUpdateEvent.ROUTE_MSI_UPDATE) {
			return;
		}
		
		graphics.clear();
		
		Stroke stroke = new BasicStroke(
				3.0f,                      // Width
			    BasicStroke.CAP_SQUARE,    // End cap
			    BasicStroke.JOIN_MITER,    // Join style
			    10.0f,                     // Miter limit
			    new float[] { 3.0f, 10.0f }, // Dash pattern
			    0.0f);
		Stroke activeStroke = new BasicStroke(
				3.0f,                      // Width
                BasicStroke.CAP_SQUARE,    // End cap
                BasicStroke.JOIN_MITER,    // Join style
                10.0f,                     // Miter limit
                new float[] { 10.0f, 8.0f }, // Dash pattern
                0.0f);                     // Dash phase
		Color ECDISOrange = new Color(213, 103, 45, 255);
		
		int activeRouteIndex = routeManager.getActiveRouteIndex();
		for (int i = 0; i < routeManager.getRoutes().size(); i++) {
			Route route = routeManager.getRoutes().get(i);
			if(route.isVisible() && i != activeRouteIndex){				
				RouteGraphic routeGraphic = new RouteGraphic(route, i, arrowsVisible, stroke, ECDISOrange);
				graphics.add(routeGraphic);
			}
		}
		
		if (routeManager.isRouteActive()) {
			ActiveRoute activeRoute = routeManager.getActiveRoute();
			if (activeRoute.isVisible()) {
				ActiveRouteGraphic activeRouteExtend = new ActiveRouteGraphic(activeRoute, activeRouteIndex, arrowsVisible, activeStroke, Color.RED);
				graphics.add(activeRouteExtend);
			}
		}
		
		// Handle route metoc
		metocGraphics.clear();
		for (int i = 0; i < routeManager.getRoutes().size(); i++) {
			Route route = routeManager.getRoutes().get(i);
			boolean activeRoute = false;
			
			if (routeManager.isActiveRoute(i)) {
				route = routeManager.getActiveRoute();
				activeRoute = true;
			}
			
			if (routeManager.showMetocForRoute(route)) {
				routeMetoc = new MetocGraphic(route, activeRoute);
				metocGraphics.add(routeMetoc);
			}
		}
		if (metocGraphics.size() > 0) {
			graphics.add(0, metocGraphics);
		}
		
		for (AisAdressedRouteSuggestion routeSuggestion : routeManager.getAddressedSuggestedRoutes()) {
			if(!routeSuggestion.isHidden()){
				suggestedRoute = new SuggestedRouteGraphic(routeSuggestion, stroke);
				graphics.add(suggestedRoute);
			}
		}
		
		graphics.project(getProjection(), true);

		doPrepare();
	}
	
	/**
	 * Calculate distance between displayed METOC-points projected onto the screen
	 * @param metocGraphic METOC-graphics containing METOC-points
	 * @return The smallest distance between displayed METOC-points projected onto the screen
	 */
	public double calculateMetocDistance(MetocGraphic metocGraphic){
		List<OMGraphic> forecasts = metocGraphic.getTargets();
		double minDist = 0;
		for (int i = 0; i < forecasts.size(); i++) {
			if(i < forecasts.size()-2){
				MetocPointGraphic metocForecastPoint = (MetocPointGraphic) forecasts.get(i);
				MetocPointGraphic metocForecastPointNext = (MetocPointGraphic) forecasts.get(i+1);
				double lat = metocForecastPoint.getLat();
				double lon = metocForecastPoint.getLon();
				
				double latnext = metocForecastPointNext.getLat();
				double lonnext = metocForecastPointNext.getLon();
				
				Point2D current = getProjection().forward(lat, lon);
				Point2D next = getProjection().forward(latnext, lonnext);
				
				Vector2D vector = new Vector2D(current.getX(),current.getY(),next.getX(),next.getY());
				
				double newDist = vector.norm();
				
				if(i == 0){
					minDist = newDist;
				}
				
				if(minDist > newDist){
					minDist = newDist;
				}
			}
		}
		return minDist;
	}
	
	/**
	 * Calculate distance between each METOC-point projected onto the screen
	 * @param route The route which contains metoc data (check for this before!)
	 * @return The smallest distance between METOC-points projected onto the screen
	 */
	public double calculateMetocDistance(Route route){
		MetocForecast routeMetoc  = route.getMetocForecast();
		List<MetocForecastPoint> forecasts = routeMetoc.getForecasts();
		double minDist = 0;
		for (int i = 0; i < forecasts.size(); i++) {
			if(i < forecasts.size()-2){
				MetocForecastPoint metocForecastPoint = forecasts.get(i);
				MetocForecastPoint metocForecastPointNext = forecasts.get(i+1);
				double lat = metocForecastPoint.getLat();
				double lon = metocForecastPoint.getLon();
				
				double latnext = metocForecastPointNext.getLat();
				double lonnext = metocForecastPointNext.getLon();
				
				Point2D current = getProjection().forward(lat, lon);
				Point2D next = getProjection().forward(latnext, lonnext);
				
				Vector2D vector = new Vector2D(current.getX(),current.getY(),next.getX(),next.getY());
				
				double newDist = vector.norm();
				
				if(i == 0){
					minDist = newDist;
				}
				
				if(minDist > newDist){
					minDist = newDist;
				}
			}
		}
		return minDist;
	}
	
	@Override
	public synchronized OMGraphicList prepare() {
//		System.out.println("Entering RouteLayer.prepare()");
//		long start = System.nanoTime();
		for (OMGraphic omgraphic : graphics) {
			if(omgraphic instanceof RouteGraphic){
				((RouteGraphic) omgraphic).showArrowHeads(getProjection().getScale() < ESD.getSettings().getNavSettings().getShowArrowScale());
			}
		}
		
		List<OMGraphic> metocList = metocGraphics.getTargets();
		for (OMGraphic omGraphic : metocList) {
			MetocGraphic metocGraphic = (MetocGraphic) omGraphic;
			Route route = metocGraphic.getRoute();
			if(routeManager.showMetocForRoute(route)){
				double minDist = calculateMetocDistance(route);
				int step = (int) (5/minDist);
				if(step < 1)
					step = 1;
				metocGraphic.setStep(step);
				metocGraphic.paintMetoc();				
			}
		}
		
		graphics.project(getProjection());
//		System.out.println("Finished RouteLayer.prepare() in " + EeINS.elapsed(start) + " ms\n---");
		return graphics;
	}
	
//	@Override
//	public void paint(Graphics g) {
//		System.out.println("Entering RouteLayer.paint)");
//		long start = System.nanoTime();
//		super.paint(g);
//		System.out.println("Finished RouteLayer.paint() in " + EeINS.elapsed(start) + " ms\n---");
//	}
	
	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof RouteManager) {
			System.out.println("yo");
			routeManager = (RouteManager)obj;
			routeManager.addListener(this);
		}
		
//		if (obj instanceof MainFrame) {
//			MainFrame mainFrame = (MainFrame) obj;
////			routeManager = mainFrame.getRouteManagerDialog();
//			System.out.println("yo yo yo yo yo");
//		}
		
		if (obj instanceof JMapFrame){
			if ((waypointInfoPanel == null) && (routeManager != null)) {
				waypointInfoPanel = new WaypointInfoPanel();
			}
			
			jMapFrame = (JMapFrame) obj;
			metocInfoPanel = new MetocInfoPanel();
			jMapFrame.getGlassPanel().add(metocInfoPanel);
			jMapFrame.getGlassPanel().add(waypointInfoPanel);
		}		
		if (obj instanceof MapBean){
			mapBean = (MapBean)obj;
		}
		if(obj instanceof MapMenu){
			routeMenu = (MapMenu) obj;
		}

	}
	
	@Override
	public void findAndUndo(Object obj) {
		if (obj == routeManager) {
			routeManager.removeListener(this);
		}
	}

	public MapMouseListener getMapMouseListener() {
        return this;
    }

	@Override
	public String[] getMouseModeServiceList() {
		String[] ret = new String[3];
		ret[0] = DragMouseMode.modeID; // "DragMouseMode"
		ret[1] = NavigationMouseMode.modeID; // "ZoomMouseMode"
		ret[2] = SelectMouseMode.modeID; // "SelectMouseMode"
		return ret;
	}

	@Override
	public boolean mouseClicked(MouseEvent e) {
		if(e.getButton() != MouseEvent.BUTTON3){
			return false;
		}
		
		selectedGraphic = null;
		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
		for (OMGraphic omGraphic : allClosest) {
			if (omGraphic instanceof SuggestedRouteGraphic || omGraphic instanceof WaypointCircle || omGraphic instanceof RouteLegGraphic) {
				selectedGraphic = omGraphic;
				break;
			}
		}
		

		if(selectedGraphic instanceof WaypointCircle){
			WaypointCircle wpc = (WaypointCircle) selectedGraphic;
			waypointInfoPanel.setVisible(false);
			routeMenu.routeWaypointMenu(wpc.getRouteIndex(), wpc.getWpIndex());
			routeMenu.setVisible(true);
			routeMenu.show(this, e.getX()-2, e.getY()-2);
			return true;
		}
		if(selectedGraphic instanceof RouteLegGraphic){
			RouteLegGraphic rlg = (RouteLegGraphic) selectedGraphic;
			waypointInfoPanel.setVisible(false);
			routeMenu.routeLegMenu(rlg.getRouteIndex(), rlg.getRouteLeg(), e.getPoint());
			routeMenu.setVisible(true);
			routeMenu.show(this, e.getX()-2, e.getY()-2);
			return true;
		}
//		
		return false;
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		if(!javax.swing.SwingUtilities.isLeftMouseButton(e)){
			return false;
		}
		
		if(!dragging){
			selectedGraphic = null;
			OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
			for (OMGraphic omGraphic : allClosest) {
				if (omGraphic instanceof WaypointCircle) {
					selectedGraphic = omGraphic;
					break;
				}
			}
		}
		
		if(selectedGraphic instanceof WaypointCircle){
			WaypointCircle wpc = (WaypointCircle) selectedGraphic;
			if(routeManager.getActiveRouteIndex() != wpc.getRouteIndex()){
				RouteWaypoint routeWaypoint = wpc.getRoute().getWaypoints().get(wpc.getWpIndex());
				LatLonPoint newLatLon = mapBean.getProjection().inverse(e.getPoint());
				GeoLocation newLocation = new GeoLocation(newLatLon.getLatitude(), newLatLon.getLongitude());
				routeWaypoint.setPos(newLocation);
				routesChanged(RoutesUpdateEvent.ROUTE_WAYPOINT_MOVED);
				routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_WAYPOINT_MOVED);
				dragging = true;
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved() {
        graphics.deselect();
        repaint();
    }

	@Override
	public boolean mouseMoved(MouseEvent e) {
		OMGraphic newClosest = null;
		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 2.0f);
		
		for (OMGraphic omGraphic : allClosest) {
			if (omGraphic instanceof MetocPointGraphic || omGraphic instanceof WaypointCircle) {
				newClosest = omGraphic;
				break;
			}
		}
		
		if (routeMetoc != null && metocInfoPanel != null) {
			if (newClosest != closest) {
				if (newClosest == null) {
					metocInfoPanel.setVisible(false);
					waypointInfoPanel.setVisible(false);
					closest = null;
				} else {
					if (newClosest instanceof MetocPointGraphic) {
						closest = newClosest;
						MetocPointGraphic pointGraphic = (MetocPointGraphic)newClosest;
						MetocForecastPoint pointForecast = pointGraphic.getMetocPoint();
						Point containerPoint = SwingUtilities.convertPoint(mapBean, e.getPoint(), jMapFrame);
						metocInfoPanel.setPos((int)containerPoint.getX(), (int)containerPoint.getY());
						metocInfoPanel.showText(pointForecast, pointGraphic.getMetocGraphic().getRoute().getRouteMetocSettings());												
						waypointInfoPanel.setVisible(false);
						jMapFrame.getGlassPane().setVisible(true);						
						return true;
					}
				}
			}
		}
		
		if (newClosest != closest) {
			if (newClosest instanceof WaypointCircle) {
				closest = newClosest;
				WaypointCircle waypointCircle = (WaypointCircle)closest;
				Point containerPoint = SwingUtilities.convertPoint(mapBean, e.getPoint(), jMapFrame);
				waypointInfoPanel.setPos((int)containerPoint.getX(), (int)containerPoint.getY() - 10);
				waypointInfoPanel.showWpInfo(waypointCircle.getRoute(), waypointCircle.getWpIndex());
				jMapFrame.getGlassPane().setVisible(true);				
				metocInfoPanel.setVisible(false);
				return true;
			} else {
				waypointInfoPanel.setVisible(false);
				closest = null;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
		if(dragging){
			dragging = false;
			routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_MSI_UPDATE);
			return true;
		}
		return false;
	}

}
