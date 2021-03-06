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
package dk.frv.enav.esd.msi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bbn.openmap.MapHandlerChild;

import dk.frv.ais.geo.GeoLocation;
import dk.frv.enav.common.xml.msi.MsiMessage;
import dk.frv.enav.common.xml.msi.response.MsiResponse;
import dk.frv.enav.esd.ESD;
import dk.frv.enav.esd.layers.msi.MsiLayer;
import dk.frv.enav.esd.services.shore.ShoreServiceException;
import dk.frv.enav.esd.services.shore.ShoreServices;
import dk.frv.enav.esd.settings.EnavSettings;
import dk.frv.enav.ins.gps.GpsData;
import dk.frv.enav.ins.gps.IGpsDataListener;
import dk.frv.enav.ins.route.IRoutesUpdateListener;
import dk.frv.enav.ins.route.RouteManager;
import dk.frv.enav.ins.route.RoutesUpdateEvent;

/**
 * Component for handling MSI messages
 */
public class MsiHandler extends MapHandlerChild implements Runnable, IRoutesUpdateListener, IGpsDataListener {

	/**
	 * Internal Msi Extended message class
	 * 
	 */
	public class MsiMessageExtended {
		public MsiMessage msiMessage;
		public boolean acknowledged;
		public boolean visible;
		public boolean relevant;

		public MsiMessageExtended(MsiMessage msiMessage, boolean acknowledged, boolean visible, boolean relevant) {
			this.msiMessage = msiMessage;
			this.acknowledged = acknowledged;
			this.visible = visible;
			this.relevant = relevant;
		}

		/**
		 * Is a given date valid
		 * 
		 * @param date
		 * @return
		 */
		public synchronized boolean isValidAt(Date date) {
			return (msiMessage.getValidFrom() == null || msiMessage.getValidFrom().before(date));
		}
	}

	private static final Logger LOG = Logger.getLogger(MsiHandler.class);

	private MsiLayer msiLayer;

	private ShoreServices shoreServices;
	private RouteManager routeManager;
	// private MsiLayer msiLayer;

	private MsiStore msiStore;
	private Date lastUpdate;
	private long pollInterval;
	private boolean pendingImportantMessages;
	// do not serialize these members
	transient private GeoLocation calculationPosition = null;
	// transient private GeoLocation currentPosition = null;

	private Set<IMsiUpdateListener> listeners = new HashSet<IMsiUpdateListener>();
	// private GpsHandler gpsHandler;
	private boolean gpsUpdate = false;

	/**
	 * Constructor
	 */
	public MsiHandler(EnavSettings enavSettings) {
		pollInterval = enavSettings.getMsiPollInterval();
		msiStore = MsiStore.loadFromFile();
		ESD.startThread(this, "MsiHandler");
	}

	/**
	 * Add a listener to the msihandler
	 * 
	 * @param listener
	 */
	public synchronized void addListener(IMsiUpdateListener listener) {
		listeners.add(listener);
	}

	/**
	 * Delete a message from the msi
	 * 
	 * @param msiMessage
	 */
	public synchronized void deleteMessage(MsiMessage msiMessage) {
		msiStore.deleteMessage(msiMessage);
		saveToFile();
		reCalcMsiStatus();
		notifyUpdate();
	}

	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof ShoreServices) {
			shoreServices = (ShoreServices) obj;
		}
		if (obj instanceof RouteManager) {
			routeManager = (RouteManager) obj;
			routeManager.addListener(this);
		}
		if (obj instanceof MsiLayer) {
			msiLayer = (MsiLayer) obj;
		}
		if (obj instanceof IMsiUpdateListener) {
			addListener((IMsiUpdateListener) obj);
		}
		// if (gpsHandler == null && obj instanceof GpsHandler) {
		// gpsHandler = (GpsHandler) obj;
		// gpsHandler.addListener(this);
		// }
	}

	@Override
	public void findAndUndo(Object obj) {
		// if (gpsHandler == obj) {
		// gpsHandler.removeListener(this);
		// gpsHandler = null;
		// }
	}

	/**
	 * Get the list of filtered messages
	 * 
	 * @return
	 */
	public synchronized List<MsiMessageExtended> getFilteredMessageList() {
		List<MsiMessageExtended> list = new ArrayList<MsiMessageExtended>();
		for (Integer msgId : msiStore.getMessages().keySet()) {
			MsiMessage msiMessage = msiStore.getMessages().get(msgId);
			boolean acknowledged = msiStore.getAcknowledged().contains(msgId);
			boolean visible = msiStore.getVisible().contains(msgId);
			boolean relevant = msiStore.getRelevant().contains(msgId);
			MsiMessageExtended msiMessageExtended = new MsiMessageExtended(msiMessage, acknowledged, visible, relevant);
			if (visible) {
				list.add(msiMessageExtended);
			}
		}
		return list;
	}

	/**
	 * Get the first none acknolwedged msi message
	 * 
	 * @return
	 */
	public synchronized int getFirstNonAcknowledged() {
		int index = 0;
		List<MsiMessageExtended> list = getMessageList();
		while (index < list.size()) {
			if (!list.get(index).acknowledged) {
				return index;
			}
			index++;
		}
		return list.size() - 1;
	}

	/**
	 * Get the first none acknolwedged msi message from the filtered list
	 * 
	 * @return
	 */
	public synchronized int getFirstNonAcknowledgedFiltered() {
		int index = 0;
		List<MsiMessageExtended> list = getFilteredMessageList();
		while (index < list.size()) {
			if (!list.get(index).acknowledged) {
				return index;
			}
			index++;
		}
		return list.size() - 1;
	}

	public synchronized Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Get the list of MSI messages
	 * 
	 * @return
	 */
	public synchronized List<MsiMessageExtended> getMessageList() {
		List<MsiMessageExtended> list = new ArrayList<MsiMessageExtended>();
		for (Integer msgId : msiStore.getMessages().keySet()) {
			MsiMessage msiMessage = msiStore.getMessages().get(msgId);
			boolean acknowledged = msiStore.getAcknowledged().contains(msgId);
			boolean visible = msiStore.getVisible().contains(msgId);
			boolean relevant = msiStore.getRelevant().contains(msgId);
			MsiMessageExtended msiMessageExtended = new MsiMessageExtended(msiMessage, acknowledged, visible, relevant);
			list.add(msiMessageExtended);
		}
		return list;
	}

	/**
	 * Get all the msi messages
	 * 
	 * @return
	 */
	public synchronized Collection<MsiMessage> getMessages() {
		return msiStore.getMessages().values();
	}

	/**
	 * Get the amount of unacknowledged msi messages
	 * 
	 * @return
	 */
	public int getUnAcknowledgedMSI() {
		List<MsiMessageExtended> messageList = getMessageList();
		int counter = 0;

		for (int i = 0; i < messageList.size(); i++) {
			if (messageList.get(i).acknowledged == false) {
				counter++;
			}
		}

		return counter;
	}

	@Override
	public void gpsDataUpdate(GpsData arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Check if a msi with a given ID is acknowleged
	 * 
	 * @param msgId
	 * @return
	 */
	public synchronized boolean isAcknowledged(int msgId) {
		return msiStore.getAcknowledged().contains(msgId);
	}

	/**
	 * Get the pending important messages
	 * 
	 * @return
	 */
	public synchronized boolean isPendingImportantMessages() {
		return pendingImportantMessages;
	}

	/**
	 * Notify listeners and layers using this handler of a change
	 */
	public void notifyUpdate() {
		// Update layer
		if (msiLayer != null) {
			msiLayer.doUpdate();
		}
		// Notify of MSI change
		for (IMsiUpdateListener listener : listeners) {
			listener.msiUpdate();
		}
	}

	/**
	 * Get new msi messages from server and call update
	 * @return
	 * @throws ShoreServiceException
	 */
	public boolean poll() throws ShoreServiceException {
		if (shoreServices == null) {
			return false;
		}
		MsiResponse msiResponse = shoreServices.msiPoll(msiStore.getLastMessage());
		if (msiResponse == null || msiResponse.getMessages() == null || msiResponse.getMessages().size() == 0) {
			return false;
		}
		LOG.info("Received " + msiResponse.getMessages().size() + " new MSI messages");
		msiStore.update(msiResponse.getMessages(), calculationPosition);
		return true;
	}

	/**
	 * Returns true if status has changed
	 * 
	 * @return
	 */
	private synchronized boolean reCalcMsiStatus() {
		// Determine if there are pending relevant MSI
		boolean previous = pendingImportantMessages;

		// pendingImportantMessages = msiStore.hasValidUnacknowledged();
		pendingImportantMessages = msiStore.hasValidVisibleUnacknowledged();
		return (previous != pendingImportantMessages);

		// TODO check against current position and active route
		// Is the messages in the vicinity of position or route
		// Only check with unacknowledged messages
	}

	/**
	 * Recalculate if a msi is visible
	 * @return
	 */
	private synchronized boolean reCalcMsiVisibility() {
		boolean updated = false;
		if (gpsUpdate) {
			gpsUpdate = false;
			msiStore.setVisibility(calculationPosition);
			updated = true;
		}
		msiStore.setVisibility();
		updated = true;
		return updated;
	}

	@Override
	public void routesChanged(RoutesUpdateEvent e) {
		if (e == RoutesUpdateEvent.ROUTE_ACTIVATED) {
			// msiStore.setRelevance(routeManager.getActiveRoute());
			// notifyUpdate();
		}
		if (e == RoutesUpdateEvent.ROUTE_DEACTIVATED) {
			msiStore.clearRelevance();
			notifyUpdate();
		}
		if (e == RoutesUpdateEvent.ROUTE_MSI_UPDATE || e == RoutesUpdateEvent.ROUTE_ADDED
				|| e == RoutesUpdateEvent.ROUTE_REMOVED || e == RoutesUpdateEvent.ROUTE_CHANGED) {
			updateMsi();
		}
		if (reCalcMsiStatus()) {
			notifyUpdate();
		}
	}

	@Override
	public void run() {
		while (true) {
			ESD.sleep(30000);
			updateMsi();
		}
	}

	/** 
	 * Save the msi to a file
	 */
	public synchronized void saveToFile() {
		msiStore.saveToFile();
	}

	// /**
	// * Only set a new calculation position if it is a certain range away from
	// previous point
	// */
	// @Override
	// public void gpsDataUpdate(GpsData gpsData) {
	// currentPosition = gpsData.getPosition();
	// if(calculationPosition == null) {
	// calculationPosition = currentPosition;
	// gpsUpdate = true;
	// return;
	// }
	// Double range = Calculator.range(currentPosition, calculationPosition,
	// Heading.GC);
	// if(range >
	// EeINS.getSettings().getEnavSettings().getMsiRelevanceGpsUpdateRange()) {
	// gpsUpdate = true;
	// calculationPosition = currentPosition;
	// }
	// }

	/**
	 * Set a msi message as acknowleged
	 * @param msiMessage
	 */
	public synchronized void setAcknowledged(MsiMessage msiMessage) {
		msiStore.getAcknowledged().add(msiMessage.getMessageId());
		saveToFile();
		reCalcMsiStatus();
		notifyUpdate();
	}

	/**
	 * Set last msi update
	 * @param lastUpdate
	 */
	private synchronized void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Update the msi
	 */
	public void updateMsi() {
		boolean msiUpdated = false;

		Date now = new Date();
		if (getLastUpdate() == null || (now.getTime() - getLastUpdate().getTime() > pollInterval * 1000)) {
			// Poll for new messages from shore
			try {
				if (poll()) {
					msiUpdated = true;
				}
				setLastUpdate(now);
			} catch (ShoreServiceException e) {
				LOG.error("Failed to get MSI from shore: " + e.getMessage());
			}
		}

		// Cleanup msi store
		if (msiStore.cleanup()) {
			msiUpdated = true;
		}

		// Check if new pending messages
		if (reCalcMsiStatus()) {
			LOG.debug("reCalcMsiStatus() changed MSI status");
			msiUpdated = true;
		}

		if (reCalcMsiVisibility()) {
			LOG.debug("reCalcMsiRelevance() changed MSI relevance");
			msiUpdated = true;
		}

		// Notify if update
		if (msiUpdated) {
			notifyUpdate();
		}
	}

}
