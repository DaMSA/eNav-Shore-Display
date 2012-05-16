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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import dk.frv.enav.esd.gui.msi.MsiTableModel;
import dk.frv.enav.esd.msi.IMsiUpdateListener;
import dk.frv.enav.esd.msi.MsiHandler;

/**
 * Notification Center responsible for display all notifications
 * 
 * @author David A. Camre (davidcamre@gmail.com)
 * 
 */
public class NotificationCenter extends ComponentFrame implements ListSelectionListener, ActionListener,
		IMsiUpdateListener {
	class MenuTable extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames = { "Service name", "Unread" };
		private Object[][] data = {
				// {"MSI", new Integer(10)},
				{ "MSI", msiHandler.getUnAcknowledgedMSI() }, { "Guard Zones", new Integer(2) },
				{ "AIS", new Integer(2) }, { "Risk Index", new Integer(0) } };

		/**
		 * Return the column count
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/**
		 * Return the column Names
		 */
		public String getColumnName(int col) {
			return columnNames[col];
		}

		/**
		 * Return the row count
		 */
		public int getRowCount() {
			return data.length;
		}

		/**
		 * Retrieve an object located at a specific row and col
		 */
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/**
		 * Set an object at a specific row and col
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}

	/**
	 * Row listener to detect events on the list
	 * 
	 */
	private class RowListener1 implements ListSelectionListener {
		/**
		 * Event called on value change
		 * 
		 * @param event
		 */
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			DefaultListSelectionModel values = (DefaultListSelectionModel) event.getSource();
			doc.delete(0, doc.length());
			doc.append(menu.getValueAt(values.getAnchorSelectionIndex(), 0));
			area.setText(doc.toString());
		}
	}

	/**
	 * 
	 * Row listener to detect events on the list
	 * 
	 */
	private class RowListener2 implements ListSelectionListener {
		/**
		 * Event called on value change
		 * 
		 * @param event
		 */
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			DefaultListSelectionModel values = (DefaultListSelectionModel) event.getSource();
			doc.delete(0, doc.length());
			for (int i = 0; i < msiTableModel.getColumnCount(); i++) {
				doc.append("<b>" + msiTableModel.getColumnName(i) + ":</b> "
						+ msiTableModel.getValueAt(values.getAnchorSelectionIndex(), i) + "<br /><br />");
			}
			area.setText(doc.toString());
		}
	}

	private static final long serialVersionUID = 1L;
	private JTextPane area = new JTextPane();
	private StringBuilder doc = new StringBuilder();

	private MenuTable menu;

	// private MSITableDummy rt;
	private MsiHandler msiHandler;

	private MsiTableModel msiTableModel;
	private JTable table;
	private JTable table_1;
	/**
	 * Notification Center constructor
	 */
	public NotificationCenter() {
		super("Notification Center", true, true, true, true);
		//setSize(800, 600);
		//setLocation(10, 10);
		//setVisible(false);
		initGui();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Find and init bean function used in initializing other classes
	 */
	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof MsiHandler) {
			msiHandler = (MsiHandler) obj;
			msiHandler.addListener(this);
			
		}
	}

	/**
	 * Initialize the GUI
	 */
	public void initGui() {
		getContentPane().setBackground(Color.DARK_GRAY);
		setBounds(100, 100, 450, 300);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JButton btnAwk = new JButton("AWK");
		GridBagConstraints gbc_btnAwk = new GridBagConstraints();
		gbc_btnAwk.insets = new Insets(0, 0, 5, 5);
		gbc_btnAwk.gridx = 0;
		gbc_btnAwk.gridy = 0;
		getContentPane().add(btnAwk, gbc_btnAwk);
		
		JButton btnDel = new JButton("DEL");
		GridBagConstraints gbc_btnDel = new GridBagConstraints();
		gbc_btnDel.insets = new Insets(0, 0, 5, 5);
		gbc_btnDel.gridx = 1;
		gbc_btnDel.gridy = 0;
		getContentPane().add(btnDel, gbc_btnDel);
		
		table = new JTable();
		table.setBackground(Color.GRAY);
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.gridwidth = 11;
		gbc_table.gridheight = 3;
		gbc_table.insets = new Insets(0, 0, 5, 0);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 2;
		gbc_table.gridy = 0;
		getContentPane().add(table, gbc_table);
		
		JButton btnGoto = new JButton("GOTO");
		GridBagConstraints gbc_btnGoto = new GridBagConstraints();
		gbc_btnGoto.insets = new Insets(0, 0, 5, 5);
		gbc_btnGoto.gridx = 0;
		gbc_btnGoto.gridy = 2;
		getContentPane().add(btnGoto, gbc_btnGoto);
		
		JButton btnRand = new JButton("RAND");
		GridBagConstraints gbc_btnRand = new GridBagConstraints();
		gbc_btnRand.insets = new Insets(0, 0, 5, 5);
		gbc_btnRand.gridx = 1;
		gbc_btnRand.gridy = 2;
		getContentPane().add(btnRand, gbc_btnRand);
		
		table_1 = new JTable();
		table_1.setBackground(Color.GRAY);
		GridBagConstraints gbc_table_1 = new GridBagConstraints();
		gbc_table_1.gridheight = 6;
		gbc_table_1.gridwidth = 2;
		gbc_table_1.insets = new Insets(0, 0, 0, 5);
		gbc_table_1.fill = GridBagConstraints.BOTH;
		gbc_table_1.gridx = 0;
		gbc_table_1.gridy = 3;
		getContentPane().add(table_1, gbc_table_1);
		
		JTextArea textArea = new JTextArea();
		textArea.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 11;
		gbc_textArea.gridheight = 6;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 2;
		gbc_textArea.gridy = 3;
		getContentPane().add(textArea, gbc_textArea);
		
		/*
		// Add main container
		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		add(mainContainer);

		// Add left container / menu
		menu = new MenuTable();
		JTable leftTable = new JTable(menu);
		leftTable.setShowVerticalLines(false);
		leftTable.setGridColor(new Color(224, 224, 224));
		leftTable.getSelectionModel().addListSelectionListener(new RowListener1());
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		mainContainer.add(new JScrollPane(leftTable), c);

		// Add right container
		JPanel rightContainer = new JPanel();
		rightContainer.setLayout(new GridBagLayout());
		c.gridx = 1;
		c.weightx = 0.8;
		mainContainer.add(new JScrollPane(rightContainer), c);

		// Test
		GridBagConstraints c2 = new GridBagConstraints();
		// rt = new MSITableDummy();
		msiTableModel = new MsiTableModel(msiHandler);
		JTable rightTable = new JTable(msiTableModel);
		rightTable.getSelectionModel().addListSelectionListener(new RowListener2());
		rightTable.setShowVerticalLines(false);
		rightTable.setGridColor(new Color(224, 224, 224));
		c2.gridx = 0;
		c2.gridy = 0;
		c2.weightx = 1;
		c2.fill = GridBagConstraints.BOTH;
		c2.weighty = 0.3;
		JScrollPane pane = new JScrollPane(rightTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setPreferredSize(new Dimension(500, 100));
		rightContainer.add(pane, c2);
		c2.weighty = 0.7;
		c2.gridy = 1;
		area.setEditable(false);
		area.setContentType("text/html");
		area.setPreferredSize(new Dimension(530, 400));
		rightContainer.add(new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), c2);
		doc.append("<table><tr><td><b>Notification</b></td><td style=\"color:red;\">Center</td></tr></table>");
		area.setText(doc.toString());*/
	}

	@Override
	public void msiUpdate() {
		// TODO Auto-generated method stub

	}

	/**
	 * Change the visiblity
	 */
	public void toggleVisibility() {
		setVisible(!this.isVisible());
		
//		if (this.isVisible())
//			this.setVisible(false);
//		else
//			this.setVisible(true);
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub

	}
}
