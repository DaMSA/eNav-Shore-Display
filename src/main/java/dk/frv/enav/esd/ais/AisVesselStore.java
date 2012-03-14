package dk.frv.enav.esd.ais;

import dk.frv.enav.ins.ais.VesselTarget;

public class AisVesselStore extends AisStore {

	private static final long serialVersionUID = 1L;
	
	private VesselTarget ownShip; 
	
	public VesselTarget getOwnShip() {
		return ownShip;
	}
	
	public void setOwnShip(VesselTarget ownShip) {
		this.ownShip = ownShip;
	}

}
