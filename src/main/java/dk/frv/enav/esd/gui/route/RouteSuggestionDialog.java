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
package dk.frv.enav.esd.gui.route;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import dk.frv.enav.esd.ESD;
import dk.frv.enav.esd.ais.AisAdressedRouteSuggestion;
import dk.frv.enav.esd.common.text.Formatter;
import dk.frv.enav.esd.gui.utils.ComponentFrame;
import dk.frv.enav.esd.gui.views.MainFrame;
import dk.frv.enav.esd.route.RouteManager;



/**
 * Dialog shown when route suggestion is received
 */
public class RouteSuggestionDialog extends ComponentFrame implements ActionListener, Runnable {
	private static final long serialVersionUID = 1L;
	
	private MainFrame mainFrame;
//	private RouteManager routeManager;
//	private ChartPanel chartPanel;
//	private GpsHandler gpsHandler;
//	
	private AisAdressedRouteSuggestion routeSuggestion;

	private JButton acceptBtn;
	private JButton rejectBtn;
	private JButton notedBtn;
	private JButton ignoreBtn;
	private JButton postponeBtn;
	private JLabel titleLbl;
	private JLabel routeInfoLbl;
	private JButton zoomBtn;
	private JPanel routePanel;
	private JPanel replyPanel;
	private JButton hideBtn;
	private JLabel wpInfoLabel;
	
	public RouteSuggestionDialog(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		setResizable(false);
		setTitle("AIS Route Suggestion");
		
		setSize(380, 406);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
//        setAlwaysOnTop(true);
//        setLocationRelativeTo(mainFrame);        

        initGui();
        
        (new Thread(this)).start();
	}
	
	public void showSuggestion(AisAdressedRouteSuggestion routeSuggestion) {
		this.routeSuggestion = routeSuggestion;
		
		titleLbl.setText("AIS Addressed route suggestion from MMSI: " + routeSuggestion.getSender());
		
		StringBuilder str = new StringBuilder();
		str.append("<html>");
		str.append("<table cellpadding='0' cellspacing='3'>");
		str.append("<tr><td>Received:</td><td>" + Formatter.formatShortDateTime(routeSuggestion.getReceived()) + "</td></tr>");
		str.append("<tr><td>Status:</td><td>" + Formatter.formatRouteSuggestioStatus(routeSuggestion.getStatus()) + "</td></tr>");
		str.append("<tr><td>Type:</td><td>" + Formatter.formatAisRouteType(routeSuggestion.getRouteType()) + "</td></tr>");
		str.append("<tr><td>ID:</td><td>" + routeSuggestion.getMsgLinkId() + "</td></tr>");
		str.append("<tr><td>ETA first wp:</td><td>" + Formatter.formatShortDateTime(routeSuggestion.getEtaFirst()) + "</td></tr>");
		str.append("<tr><td>ETA last wp:</td><td>" + Formatter.formatShortDateTime(routeSuggestion.getEtaLast()) + "</td></tr>");
		str.append("<tr><td>Duration:</td><td>" + Formatter.formatTime(routeSuggestion.getDuration()) + "</td></tr>");
		str.append("<tr><td>Avg speed:</td><td>" + Formatter.formatSpeed(routeSuggestion.getSpeed()) + "</td></tr>");
		str.append("</table>");
		str.append("</html>");
		routeInfoLbl.setText(str.toString());
		
		updateBtnStatus();
		updateWpInfo();
		
		showAndPosition();
	}
	
	private void updateWpInfo() {
		// Get current position
		StringBuilder str = new StringBuilder();
		str.append("<html><b>DST/BRG/TTG/SPD</b><br/>");
//		GpsData gpsData = gpsHandler.getCurrentData();
//		if (gpsData != null && !gpsData.isBadPosition() && routeSuggestion.getWaypoints().size() > 0) {
	

			str.append("N/A");
		
		str.append("</html>");
		
		wpInfoLabel.setText(str.toString());
	}
	
	private void updateBtnStatus() {
		zoomBtn.setEnabled(!routeSuggestion.isHidden());
		
		if (routeSuggestion.isHidden()) {		
			hideBtn.setText("Show");
		} else {
			hideBtn.setText("Hide");
		}
		
		hideBtn.setVisible(!routeSuggestion.isAcceptable());
		
		acceptBtn.setEnabled(routeSuggestion.isAcceptable());
		rejectBtn.setEnabled(routeSuggestion.isRejectable());
		notedBtn.setEnabled(routeSuggestion.isNoteable());
		ignoreBtn.setEnabled(routeSuggestion.isIgnorable());
		postponeBtn.setEnabled(routeSuggestion.isPostponable());		

	}
	
	private void showAndPosition() {
		validate();
		Rectangle rect = mainFrame.getBounds();
		int x = (int)rect.getMaxX() - getWidth() - 20;
		int y = (int)rect.getMaxY() - getHeight() - 20;
		setLocation(x, y);		
		setVisible(true);
//		setState(JFrame.NORMAL);
	}
	

	
	@Override
	public void actionPerformed(ActionEvent e) {
//		if (e.getSource() == zoomBtn) {
////			chartPanel.zoomTo(routeSuggestion.getWaypoints());
//		} else if (e.getSource() == hideBtn) {
//			routeSuggestion.setHidden(!routeSuggestion.isHidden());
//			updateBtnStatus();
//			routeManager.notifyListeners(RoutesUpdateEvent.SUGGESTED_ROUTES_CHANGED);
//		} else if (e.getSource() == acceptBtn) {
//			routeManager.aisRouteSuggestionReply(routeSuggestion, AisAdressedRouteSuggestion.Status.ACCEPTED);
//			close();
//		} else if (e.getSource() == rejectBtn) {
//			routeManager.aisRouteSuggestionReply(routeSuggestion, AisAdressedRouteSuggestion.Status.REJECTED);
//			close();
//		} else if (e.getSource() == notedBtn) {
//			routeManager.aisRouteSuggestionReply(routeSuggestion, AisAdressedRouteSuggestion.Status.NOTED);
//			close();
//		} else if (e.getSource() == ignoreBtn) {
//			routeSuggestion.setStatus(AisAdressedRouteSuggestion.Status.IGNORED);
//			routeManager.notifyListeners(RoutesUpdateEvent.SUGGESTED_ROUTES_CHANGED);
//			close();
//		} else if (e.getSource() == postponeBtn) {
//			close();
//		}		
	}
	
	@Override
	public void run() {
		while (true) {
			ESD.sleep(5000);
			if (isVisible()) {
				updateWpInfo();
			}
		}		
	}

	
	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof RouteManager) {
//			routeManager = (RouteManager)obj;
		}
//		if (obj instanceof ChartPanel) {
//			chartPanel = (ChartPanel)obj;
//		}
//		if (obj instanceof GpsHandler) {
//			gpsHandler = (GpsHandler)obj;
//		}
	}
	
	private void initGui() {
        acceptBtn = new JButton("Accept");
        acceptBtn.setToolTipText("Indicate that suggested route will be used");
        acceptBtn.addActionListener(this);
        
        rejectBtn = new JButton("Reject");
        rejectBtn.setToolTipText("Reject the suggested route");
        rejectBtn.addActionListener(this);
        
        notedBtn = new JButton("Noted");
        notedBtn.setToolTipText("Acknowledge reception, but route suggestion will or cannot be used");
        notedBtn.addActionListener(this);
        
        titleLbl = new JLabel("Addressed route suggestion from MMSI: 1293213");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));        
        
        routeInfoLbl = new JLabel("<html>\r\n<table cellspacing='1' cellpadding='0'>\r\n<tr><td>Received</td><td> sda</td></tr>\r\n<tr><td>Status</td><td> sda</td></tr>\r\n<tr><td>Type</td><td> sda</td></tr>\r\n<tr><td>ID</td><td> sda</td></tr>\r\n<tr><td>ETA first</td><td> sda</td></tr>\r\n<tr><td>ETA last</td><td> sda</td></tr>\r\n<tr><td>Duration</td><td> sda</td></tr>\r\n<tr><td>Avg speed</td><td> sda</td></tr>\r\n</table>\r\n</html>");
        routeInfoLbl.setVerticalAlignment(SwingConstants.TOP);
        
        zoomBtn = new JButton("Zoom to");
        zoomBtn.setToolTipText("Zoom to the suggested route on map");
        zoomBtn.addActionListener(this);
        
        hideBtn = new JButton("Hide");
        hideBtn.addActionListener(this);
        hideBtn.setToolTipText("Hide the suggested route on map");
        
        wpInfoLabel = new JLabel();
        wpInfoLabel.setText("dasd");
        wpInfoLabel.setToolTipText("Distance, bearing, time-to-go and speed to first waypoint");
        wpInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        routePanel = new JPanel();
        routePanel.setBorder(new TitledBorder(null, "Route", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        replyPanel = new JPanel();
        replyPanel.setBorder(new TitledBorder(null, "Reply", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        ignoreBtn = new JButton("Ignore");
        ignoreBtn.setToolTipText("Ignore suggestion and remove from map");
        ignoreBtn.addActionListener(this);
        
        postponeBtn = new JButton("Postpone");
        postponeBtn.setToolTipText("Postpone decision to later. Route will remain on map.");
        postponeBtn.addActionListener(this);
        
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
        					.addComponent(routePanel, GroupLayout.PREFERRED_SIZE, 355, Short.MAX_VALUE)
        					.addGap(9))
        				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
        					.addComponent(replyPanel, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
        					.addContainerGap())
        				.addGroup(groupLayout.createSequentialGroup()
        					.addComponent(ignoreBtn, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(postponeBtn, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap(183, Short.MAX_VALUE))))
        );
        groupLayout.setVerticalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(routePanel, GroupLayout.PREFERRED_SIZE, 263, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(replyPanel, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(ignoreBtn)
        				.addComponent(postponeBtn))
        			.addContainerGap(92, Short.MAX_VALUE))
        );
        
        GroupLayout gl_replyPanel = new GroupLayout(replyPanel);
        gl_replyPanel.setHorizontalGroup(
        	gl_replyPanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_replyPanel.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(acceptBtn, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(rejectBtn, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(notedBtn, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(60, Short.MAX_VALUE))
        );
        gl_replyPanel.setVerticalGroup(
        	gl_replyPanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_replyPanel.createSequentialGroup()
        			.addGroup(gl_replyPanel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(acceptBtn)
        				.addComponent(rejectBtn)
        				.addComponent(notedBtn))
        			.addContainerGap(35, Short.MAX_VALUE))
        );
        replyPanel.setLayout(gl_replyPanel);
                
        GroupLayout gl_routePanel = new GroupLayout(routePanel);
        gl_routePanel.setHorizontalGroup(
        	gl_routePanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_routePanel.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(gl_routePanel.createParallelGroup(Alignment.LEADING)
        				.addComponent(titleLbl, GroupLayout.PREFERRED_SIZE, 327, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_routePanel.createSequentialGroup()
        					.addComponent(zoomBtn)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(hideBtn, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE))
        				.addComponent(routeInfoLbl, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
        				.addComponent(wpInfoLabel))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gl_routePanel.setVerticalGroup(
        	gl_routePanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_routePanel.createSequentialGroup()
        			.addComponent(titleLbl)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(routeInfoLbl, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(wpInfoLabel)
        			.addPreferredGap(ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
        			.addGroup(gl_routePanel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(zoomBtn)
        				.addComponent(hideBtn)))
        );
        routePanel.setLayout(gl_routePanel);
        getContentPane().setLayout(groupLayout);
        
		
	}
}
