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
package dk.frv.enav.esd.layers.msi;

import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMList;
import com.bbn.openmap.proj.coords.LatLonPoint;

import dk.frv.ais.geo.GeoLocation;
import dk.frv.enav.common.xml.msi.MsiLocation;
import dk.frv.enav.common.xml.msi.MsiMessage;
import dk.frv.enav.common.xml.msi.MsiPoint;
import dk.frv.enav.esd.gui.MainFrame;
import dk.frv.enav.esd.msi.MsiHandler;
import dk.frv.enav.esd.msi.MsiHandler.MsiMessageExtended;
import dk.frv.enav.ins.common.Heading;
import dk.frv.enav.ins.common.util.Calculator;
import dk.frv.enav.ins.gps.GnssTime;


public class MsiLayer extends OMGraphicHandlerLayer implements MapMouseListener {	
	private static final long serialVersionUID = 1L;

	private MsiHandler msiHandler = null;
	
	private OMGraphicList graphics = new OMGraphicList();
	private MapBean mapBean = null;
	private MainFrame mainFrame = null;	

	private OMGraphic closest = null;
	private OMGraphic selectedGraphic;

	private MouseDelegator mouseDelegator;
	private LatLonPoint mousePosition;
	
	public MsiLayer() {
		
	}
	
	public void doUpdate() {
		graphics.clear();
		Date now = GnssTime.getInstance().getDate();
		// Get messages
		List<MsiHandler.MsiMessageExtended> messages = msiHandler.getMessageList();
		for (MsiHandler.MsiMessageExtended message : messages) {
			
			// Not able to show messages without location
			if (!message.msiMessage.hasLocation()) {
				continue;
			}
			
			// Is it valid now
			if (!message.isValidAt(now)) {
				continue;
			}
			
			// Create MSI graphic
			MsiGraphic msiGraphic = new MsiGraphic(message);
			graphics.add(msiGraphic);
			
			if(mapBean != null && message.relevant){
				MsiDirectionalIcon direction = new MsiDirectionalIcon(mapBean);
				direction.setMarker(message);
				graphics.add(direction);
			}
		}
		doPrepare();
	}
	
	/**
	 * Calculates the spherical distance from an MSI warning to the ship's position.
	 * Currently just a test-implementation where the mouse simulates the ship's position
	 * @param msiMessageExtended MSI message to calculate distance for
	 * @return Arc distance `c'
	 */
	private double distanceToShip(MsiMessageExtended msiMessageExtended) {
		List<MsiPoint> msiPoints = msiMessageExtended.msiMessage.getLocation().getPoints();
		Double distance = Double.MAX_VALUE;
		for (MsiPoint msiPoint : msiPoints) {
//			double currentDistance = GreatCircle.sphericalDistance(ProjMath.degToRad(mousePosition.getLatitude()),
//					ProjMath.degToRad(mousePosition.getLongitude()),
//					ProjMath.degToRad(msiPoint.getLatitude()),
//					ProjMath.degToRad(msiPoint.getLongitude()));
			GeoLocation mouseLocation = new GeoLocation(mousePosition.getLatitude(), mousePosition.getLongitude());
			GeoLocation msiLocation = new GeoLocation(msiPoint.getLatitude(), msiPoint.getLongitude());
			double currentDistance = Calculator.range(mouseLocation, msiLocation, Heading.GC);
			distance = Math.min(currentDistance, distance);
		}
		return distance;
	}
	
	@Override
	public synchronized OMGraphicList prepare() {
//		for (OMGraphic graphic : graphics) {
//			MsiGraphic msiGraphic = (MsiGraphic) graphic;
//			if(mapBean.getProjection().getScale() <= EeINS.getSettings().getEnavSettings().getMsiTextboxesVisibleAtScale()
//					&& !msiGraphic.getMessage().acknowledged){
//				if(!msiGraphic.getTextBoxVisible())
//					msiGraphic.showTextBox();
//			} else {
//				if(msiGraphic.getTextBoxVisible())
//					msiGraphic.hideTextBox();
//			}
//		}
		graphics.project(getProjection());
		return graphics;
	}
	
	public void zoomTo(MsiMessage msiMessage) {		
		if (!msiMessage.hasLocation()) {
			return;
		}
		
		MsiLocation msiLocation = msiMessage.getLocation();
		GeoLocation center = msiLocation.getCenter();
		mapBean.setCenter(center.getLatitude(), center.getLongitude());
		mapBean.setScale(80000);		
	}
	
	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof MsiHandler) {
			msiHandler = (MsiHandler)obj;
		}
		if (obj instanceof MapBean){
			mapBean = (MapBean)obj;
		}
		if (obj instanceof MainFrame){
			mainFrame = (MainFrame) obj;
		}
		if (obj instanceof MouseDelegator) {
			mouseDelegator = (MouseDelegator) obj;
		}
	}

	public MapMouseListener getMapMouseListener() {
        return this;
    }
	
	@Override
	public String[] getMouseModeServiceList() {
        String[] ret = new String[2];
//        ret[0] = NavigationMouseMode.modeID; // "Gestures"
//        ret[1] = RouteEditMouseMode.modeID;
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
			if (omGraphic instanceof MsiSymbolGraphic || omGraphic instanceof MsiDirectionalIcon) {
				selectedGraphic = omGraphic;
				break;
			}
		}
		
		if(selectedGraphic instanceof MsiSymbolGraphic){
			MsiSymbolGraphic msi = (MsiSymbolGraphic) selectedGraphic;
			mainFrame.getGlassPane().setVisible(false);
			return true;
		}
		if(selectedGraphic instanceof MsiDirectionalIcon) {
			MsiDirectionalIcon direction = (MsiDirectionalIcon) selectedGraphic;
			mainFrame.getGlassPane().setVisible(false);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
//		if(mouseDelegator.getActiveMouseModeID() == RouteEditMouseMode.modeID) {
//			mousePosition = null;
//			doUpdate();
//		}
	}

	@Override
	public void mouseMoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean mouseMoved(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
//		if (e.getButton() == MouseEvent.BUTTON1) {
//			int mouseX = e.getX();
//			int mouseY = e.getY();
//
//			OMGraphic newClosest = null;
//			OMList<OMGraphic> allClosest = graphics.findAll(mouseX, mouseY, 1.0f);
//
//			for (OMGraphic omGraphic : allClosest) {
//				if (omGraphic instanceof MsiSymbolGraphic) {
//					newClosest = omGraphic;
//					break;
//				}
//			}
//
//			if (newClosest instanceof MsiSymbolGraphic) {
//				closest = newClosest;
//				MsiSymbolGraphic msiSymbolGraphic = (MsiSymbolGraphic) newClosest;
//				if (topPanel != null && topPanel.getMsiDialog() != null) {
//					topPanel.getMsiDialog().showMessage(msiSymbolGraphic.msiMessage.getMessageId());
//					return true;
//				}
//			}
//
//		}
		return false;
	}	

}
