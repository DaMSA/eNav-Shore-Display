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
package dk.frv.enav.esd.gui.msi;

import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import dk.frv.ais.geo.GeoLocation;
import dk.frv.enav.common.xml.msi.MsiLocation;
import dk.frv.enav.esd.msi.MsiHandler;
import dk.frv.enav.ins.common.text.Formatter;

/**
 * Table model for MSI dialog
 */
public class MsiTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private static final String[] AREA_COLUMN_NAMES = { "ID", "Ver", "Priority", "Updated", "Main Area", "Message", "Valid from", "Valid until" };
	private static final String[] COLUMN_NAMES = { "ID", "Priority", "Updated", "Main Area" };
	
	private MsiHandler msiHandler;
	private List<MsiHandler.MsiMessageExtended> messages;
	private boolean filtered = false;

	/**
	 * Constructor for creating the msi table model
	 * @param msiHandler
	 */
	public MsiTableModel(MsiHandler msiHandler) {
		super();
		this.msiHandler = msiHandler;
		updateMessages();
	}
	
	public GeoLocation getMessageLatLon(int rowIndex){
		return messages.get(rowIndex).msiMessage.getLocation().getCenter();
	}
	
	public boolean isAwk(int rowIndex){
		if(rowIndex == -1){
			return false;
		}
		return messages.get(rowIndex).acknowledged;
	}

	/**
	 * Get column class at specific index
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Object value = getValueAt(0, columnIndex);
		if (value == null) {
			return String.class;
		}
		return value.getClass();
	}

	/**
	 * Get the column count
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}
	
	public int areaGetColumnCount() {
		return AREA_COLUMN_NAMES.length;
	}

	/**
	 * Return the column names
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
	
	public String areaGetColumnName(int column) {
		return AREA_COLUMN_NAMES[column];
	}

	/**
	 * Return messages
	 * 
	 * @return
	 */
	public List<MsiHandler.MsiMessageExtended> getMessages() {
		return messages;
	}

	/**
	 * Get the row count
	 */
	@Override
	public int getRowCount() {
		return messages.size();
	}

	/**
	 * Get the value at a specific row and colum index
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex == -1) return "";
		MsiHandler.MsiMessageExtended message = messages.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			return message.msiMessage.getId();
		case 1:
			return message.msiMessage.getPriority();
		case 2:
			Date updated = message.msiMessage.getUpdated();
			if (updated == null) {
				updated = message.msiMessage.getCreated();
			}
			return Formatter.formatShortDateTime(updated);
		case 3:
			MsiLocation location = message.msiMessage.getLocation();
			if (location != null) {
				return location.getArea();
			}
			return "";
		default:
			return "";

		}
	}
	
	public Object areaGetValueAt(int rowIndex, int columnIndex) {
		if(rowIndex == -1) return "";
		MsiHandler.MsiMessageExtended message = messages.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			return message.msiMessage.getId();
		case 1:
			return message.msiMessage.getVersion();
		case 2:
			return message.msiMessage.getPriority();
		case 3:
			Date updated = message.msiMessage.getUpdated();
			if (updated == null) {
				updated = message.msiMessage.getCreated();
			}
			return Formatter.formatShortDateTime(updated);
		case 4:
			MsiLocation location = message.msiMessage.getLocation();
			if (location != null) {
				return location.getArea();
			}
			return "";
		case 5:
			String msgShort = message.msiMessage.getMessage();
			if (msgShort == null) {
				msgShort = "";
			}
			// if (msgShort.length() > 32) {
			// msgShort = msgShort.substring(0, 28) + " ...";
			// }
			return msgShort;
		case 6:
			return Formatter.formatShortDateTime(message.msiMessage.getValidFrom());
		case 7:
			return Formatter.formatShortDateTime(message.msiMessage.getValidTo());
		default:
			return "";

		}
	}

	/**
	 * Update messages
	 */
	public void updateMessages() {
		// Is filtered or not?
		if (filtered) {
			messages = msiHandler.getFilteredMessageList();
		} else {
			messages = msiHandler.getMessageList();
		}
	}

}
