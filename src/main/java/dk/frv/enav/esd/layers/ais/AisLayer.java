package dk.frv.enav.esd.layers.ais;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;

import dk.frv.ais.message.AisMessage;
import dk.frv.enav.esd.ESD;
import dk.frv.enav.esd.ais.AisHandler.AisMessageExtended;
import dk.frv.enav.esd.ais.VesselAisHandler;
import dk.frv.enav.esd.event.DragMouseMode;
import dk.frv.enav.esd.event.NavigationMouseMode;
import dk.frv.enav.esd.event.SelectMouseMode;
import dk.frv.enav.esd.gui.ChartPanel;
import dk.frv.enav.esd.nmea.IVesselAisListener;
import dk.frv.enav.ins.ais.VesselPositionData;
import dk.frv.enav.ins.ais.VesselTarget;
import dk.frv.enav.ins.gui.MainFrame;
import dk.frv.enav.ins.layers.ais.AisTargetInfoPanel;

public class AisLayer extends OMGraphicHandlerLayer implements Runnable, IVesselAisListener, MapMouseListener {
	private static final long serialVersionUID = 1L;
	private OMGraphicList list = new OMGraphicList();
	private VesselAisHandler aisHandler;
	private List<AisMessageExtended> shipList;
	private ChartPanel chartPanel;
	private AisTargetInfoPanel aisTargetInfoPanel = new AisTargetInfoPanel();

	private HashMap<Long, Vessel> drawnVessels = new HashMap<Long, Vessel>();
	private Vessel vesselComponent;
	private VesselPositionData location;
	private MainFrame mainFrame;
	volatile boolean shouldRun = true;
	private float mapScale = 0;

	@Override
	public void run() {
		while (shouldRun) {
			ESD.sleep(1000);
			drawVessels();
		}
	}

	public AisLayer() {
		(new Thread(this)).start();
	}

	public void stop() {
		shouldRun = false;
	}

	public void mapClearTargets() {
		list.clear();
		drawnVessels.clear();
	}

	private void drawVessels() {
		if (aisHandler != null) {

			if (chartPanel.getMap().getScale() != mapScale) {
				mapScale = chartPanel.getMap().getScale();
				mapClearTargets();
			}

			shipList = aisHandler.getShipList();
			for (int i = 0; i < shipList.size(); i++) {
				if (aisHandler.getVesselTargets().containsKey(shipList.get(i).MMSI)) {

					// Get information
					AisMessageExtended vessel = shipList.get(i);
					VesselTarget vesselTarget = aisHandler.getVesselTargets().get(vessel.MMSI);
					location = vesselTarget.getPositionData();

					double trueHeading = location.getTrueHeading();
					if (trueHeading == 511) {
						trueHeading = location.getCog();
					}

					if (!drawnVessels.containsKey(vessel.MMSI)) {
						vesselComponent = new Vessel(vessel.MMSI);
						list.add(vesselComponent);
						drawnVessels.put(vessel.MMSI, vesselComponent);
					}
					drawnVessels.get(vessel.MMSI).updateLayers(trueHeading, location.getPos().getLatitude(),
							location.getPos().getLongitude(), vesselTarget.getStaticData(), location.getSog(),
							Math.toRadians(location.getCog()), mapScale);
				}
			}
			doPrepare();
		}
	}

	@Override
	public synchronized OMGraphicList prepare() {
		list.project(getProjection());
		return list;
	}

	public MapMouseListener getMapMouseListener() {
		return this;
	}

	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof VesselAisHandler) {
			aisHandler = (VesselAisHandler) obj;
		}
		if (obj instanceof ChartPanel) {
			chartPanel = (ChartPanel) obj;
		}
		if (obj instanceof MainFrame) {
			mainFrame = (MainFrame) obj;
			mainFrame.getGlassPanel().add(aisTargetInfoPanel);
		}

	}

	@Override
	public void receive(AisMessage arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveOwnMessage(AisMessage aisMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getMouseModeServiceList() {
		String[] ret = new String[3];
		ret[0] = DragMouseMode.modeID; // "DragMouseMode"
		ret[1] = NavigationMouseMode.modeID; // "ZoomMouseMoude"
		ret[1] = SelectMouseMode.modeID; // "SelectMouseMode"
		return ret;
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(MouseEvent e) {
		/*
		 * OMGraphic newClosest = null; OMList<OMGraphic> allClosest =
		 * list.findAll(e.getX(), e.getY(), 3.0f); for (OMGraphic omGraphic :
		 * allClosest) { if(omGraphic instanceof VesselLayer){ newClosest =
		 * omGraphic; break; } }
		 * 
		 * if(allClosest.size() == 0){ list.remove(infoBox); }
		 * 
		 * if (newClosest != closest) { list.remove(boxMMSI);
		 * list.remove(boxSog); list.remove(boxCog); list.remove(infoBox);
		 * 
		 * if (newClosest instanceof VesselLayer) { VesselLayer vessel =
		 * (VesselLayer) newClosest; VesselTarget vesselTarget =
		 * aisHandler.getVesselTargets().get(vessel.getMMSI());
		 * 
		 * // Add MouseOverBox location = vesselTarget.getPositionData(); double
		 * lat = location.getPos().getLatitude(); double lon =
		 * location.getPos().getLongitude(); double trueHeading =
		 * location.getTrueHeading(); int[] xpoints = { 10, 10, 200, 200 };
		 * int[] ypoints = { 0, -50, -50, 0 }; infoBox = new OMPoly(lat, lon,
		 * xpoints, ypoints, OMGraphic.DECIMAL_DEGREES); boxMMSI = new
		 * OMText(lat, lon, 15, 0, "MMSI: " + vessel.getMMSI(), font,
		 * OMText.JUSTIFY_LEFT); boxSog = new OMText(lat, lon, 15, 0, "Sog: " +
		 * location.getSog() + " kn", font, OMText.JUSTIFY_LEFT); boxCog = new
		 * OMText(lat, lon, 15, 0, "Cog: " + location.getCog() + " degrees",
		 * font, OMText.JUSTIFY_LEFT); if (trueHeading > 90 && trueHeading <
		 * 270) { int[] ypoints2 = { 0, 50, 50, 0 }; infoBox.setYs(ypoints2);
		 * boxMMSI.setY(15); boxSog.setY(25); boxCog.setY(35); } else {
		 * boxMMSI.setY(-35); boxSog.setY(-25); boxCog.setY(-15); }
		 * infoBox.setFillPaint(new Color(225, 225, 225)); list.add(boxMMSI);
		 * list.add(boxSog); list.add(boxCog); list.add(infoBox); closest =
		 * newClosest; doPrepare(); return true; } } return false;
		 */
		return false;
	}

	@Override
	public void mouseMoved() {
		// TODO Auto-generated method stub

	}
}
