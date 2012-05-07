package dk.frv.enav.esd.layers.ais;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;
import dk.frv.ais.message.AisMessage;

import dk.frv.enav.ins.ais.VesselStaticData;

public class Vessel extends OMGraphicList {
	private static final long serialVersionUID = 1L;
	private VesselLayer vessel;
	private OMCircle vesCirc;
	private HeadingLayer heading;
	private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);;
	private OMText callSign = null;
	private OMText nameMMSI = null;
	private long MMSI;
	private double lat;
	private double lon;
	private double sog;
	private double cogR;
	private double trueHeading;
	private OMLine speedVector;
	private LatLonPoint startPos = null;
	private LatLonPoint endPos = null;
	public static final float STROKE_WIDTH = 1.5f;
	private Color shipColor = new Color(78, 78, 78);

	public Vessel(long MMSI) {
		super();
		this.MMSI = MMSI;

		// Vessel layer
		vessel = new VesselLayer(MMSI);

		// Vessel circle layer
		vesCirc = new OMCircle(0, 0, 0.01);
		vesCirc.setFillPaint(shipColor);

		// Heading layer
		heading = new HeadingLayer(MMSI, new int[] { 0, 0 }, new int[] { 0, -30 });
		heading.setFillPaint(new Color(0, 0, 0));

		// Speed vector layer
		speedVector = new OMLine(0, 0, 0, 0, OMLine.LINETYPE_STRAIGHT);
		speedVector.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
				new float[] { 10.0f, 8.0f }, 0.0f));
		speedVector.setLinePaint(new Color(255, 0, 0));

		// Call sign layer
		callSign = new OMText(0, 0, 0, 0, "", font, OMText.JUSTIFY_CENTER);

		// MSI / Name layer
		nameMMSI = new OMText(0, 0, 0, 0, Long.toString(MMSI), font, OMText.JUSTIFY_CENTER);

		this.add(vessel);
		this.add(vesCirc);
		this.add(heading);
		this.add(speedVector);
		this.add(callSign);
		this.add(nameMMSI);
	}

	public void updateLayers(double trueHeading, double lat, double lon, VesselStaticData staticData, double sog,
			double cogR, float mapScale) {
		vessel.setLocation(lat, lon);
		vessel.setHeading(trueHeading);

		heading.setLocation(lat, lon, OMGraphic.DECIMAL_DEGREES, Math.toRadians(trueHeading));

		String name = "ID:" + this.MMSI;
		if (staticData != null) {
			vessel.setImageIcon(staticData.getShipType().toString());
			callSign.setData("Call Sign: " + staticData.getCallsign());
			name = AisMessage.trimText(staticData.getName());
		}
		nameMMSI.setData(name);

		if (this.lat != lat || this.lon != lon || this.sog != sog || this.cogR != cogR
				|| this.trueHeading != trueHeading) {
			this.lat = lat;
			this.lon = lon;
			this.sog = sog;
			this.cogR = cogR;
			this.trueHeading = trueHeading;

			vesCirc.setLatLon(lat, lon);

			callSign.setLat(lat);
			callSign.setLon(lon);
			if (trueHeading > 90 && trueHeading < 270) {
				callSign.setY(-25);
			} else {
				callSign.setY(35);
			}

			double[] speedLL = new double[4];
			speedLL[0] = (float) lat;
			speedLL[1] = (float) lon;
			startPos = new LatLonPoint.Double(lat, lon);
			float length = (float) Length.NM.toRadians(6.0 * (sog / 60.0));
			endPos = startPos.getPoint(length, cogR);
			speedLL[2] = endPos.getLatitude();
			speedLL[3] = endPos.getLongitude();
			speedVector.setLL(speedLL);

			nameMMSI.setLat(lat);
			nameMMSI.setLon(lon);
			if (trueHeading > 90 && trueHeading < 270) {
				nameMMSI.setY(-10);
			} else {
				nameMMSI.setY(20);
			}
		}

		boolean b1 = mapScale < 750000;
		showHeading(b1);
		showSpeedVector(b1);
		showCallSign(b1);
		showName(b1);
		boolean b2 = mapScale < 1500000;
		showVesselIcon(b2);
		showVesselCirc(!b2);
	}
	
	public void showVesselIcon(boolean b) {
		vessel.setVisible(b);
	}
	
	public void showVesselCirc(boolean b) {
		vesCirc.setVisible(b);
	}

	public void showHeading(boolean b) {
		heading.setVisible(b);
	}

	public void showSpeedVector(boolean b) {
		speedVector.setVisible(b);
	}

	public void showCallSign(boolean b) {
		callSign.setVisible(b);
	}

	public void showName(boolean b) {
		nameMMSI.setVisible(b);
	}

}
