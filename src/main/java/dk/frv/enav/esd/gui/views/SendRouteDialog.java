package dk.frv.enav.esd.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import dk.frv.ais.message.AisMessage;
import dk.frv.enav.esd.ESD;
import dk.frv.enav.esd.ais.AisHandler;
import dk.frv.enav.esd.event.ToolbarMoveMouseListener;
import dk.frv.enav.esd.gui.settingtabs.GuiStyler;
import dk.frv.enav.esd.gui.utils.ComponentFrame;
import dk.frv.enav.esd.route.Route;
import dk.frv.enav.esd.route.RouteManager;
import dk.frv.enav.esd.service.ais.AisServices;
import dk.frv.enav.ins.ais.VesselTarget;

public class SendRouteDialog extends ComponentFrame implements MouseListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean locked = false;
	boolean alwaysInFront = false;
	MouseMotionListener[] actions;
	private JLabel moveHandler;
	private JPanel mapPanel;
	private JPanel masterPanel;

	private JLabel sendLbl;
	private JLabel cancelLbl;
	private JLabel zoomLbl;

	private static int moveHandlerHeight = 18;

	private JPanel mainPanel;
	private SendRouteDialog sendRoute = null;
	@SuppressWarnings("rawtypes")
	private JComboBox mmsiListComboBox;
	private JLabel callsignLbl;
	private JLabel nameLbl;
	@SuppressWarnings("rawtypes")
	private JComboBox routeListComboBox;
	private JLabel routeLengthLbl;
	private JLabel statusLbl;
	private AisHandler aisHandler;
	private RouteManager routeManager;

	private Route route;
	private long mmsi;
	private boolean loading = false;

	/**
	 * Create the frame.
	 */
	public SendRouteDialog() {
		super("Route Exchange", false, true, false, false);

		setResizable(false);
		setTitle("Route Exchange");
		setBounds(100, 100, 275, 400);

		initGUI();
	}

	public void initGUI() {

		// Strip off
		setRootPaneCheckingEnabled(false);
		((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		// Map tools
		mapPanel = new JPanel(new GridLayout(1, 3));
		mapPanel.setPreferredSize(new Dimension(500, moveHandlerHeight));
		mapPanel.setOpaque(true);
		mapPanel.setBackground(Color.DARK_GRAY);
		mapPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 30, 30)));

		ToolbarMoveMouseListener mml = new ToolbarMoveMouseListener(this, ESD.getMainFrame());
		mapPanel.addMouseListener(mml);
		mapPanel.addMouseMotionListener(mml);

		// Placeholder - for now
		mapPanel.add(new JLabel());

		// Movehandler/Title dragable)
		moveHandler = new JLabel("Route Exchange", JLabel.CENTER);
		moveHandler.setFont(new Font("Arial", Font.BOLD, 9));
		moveHandler.setForeground(new Color(200, 200, 200));
		moveHandler.addMouseListener(this);
		moveHandler.addMouseListener(mml);
		moveHandler.addMouseMotionListener(mml);
		actions = moveHandler.getListeners(MouseMotionListener.class);
		mapPanel.add(moveHandler);

		sendRoute = this;

		// The tools (only close for send route dialog)
		JPanel mapToolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		mapToolsPanel.setOpaque(false);
		mapToolsPanel.setPreferredSize(new Dimension(60, 50));

		JLabel close = new JLabel(new ImageIcon("images/window/close.png"));
		close.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				sendRoute.setVisible(false);
			}

		});
		close.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 2));
		mapToolsPanel.add(close);
		mapPanel.add(mapToolsPanel);

		createGUIContent();

		// Create the masterpanel for aligning
		masterPanel = new JPanel(new BorderLayout());
		masterPanel.add(mapPanel, BorderLayout.NORTH);
		masterPanel.add(mainPanel, BorderLayout.SOUTH);

		masterPanel.setBackground(new Color(45, 45, 45));
		masterPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, new Color(30, 30, 30), new Color(
				45, 45, 45)));

		this.setContentPane(masterPanel);
		
		

	}

	/**
	 * Function for setting up custom GUI for the map frame
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createGUIContent() {

		this.setBackground(GuiStyler.backgroundColor);
		mainPanel = new JPanel();

		mainPanel.setSize(264, 384);
		mainPanel.setPreferredSize(new Dimension(264, 384));

		mainPanel.setLayout(null);

		mainPanel.setBackground(GuiStyler.backgroundColor);
		mainPanel.setBorder(new MatteBorder(1, 1, 1, 1, new Color(70, 70, 70)));

		JPanel targetPanel = new JPanel();
		targetPanel.setBackground(GuiStyler.backgroundColor);
		targetPanel.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, new Color(70, 70, 70)), "Target",
				TitledBorder.LEADING, TitledBorder.TOP, GuiStyler.defaultFont, GuiStyler.textColor));

		targetPanel.setBounds(10, 25, 244, 114);
		mainPanel.add(targetPanel);
		targetPanel.setLayout(null);

		JLabel mmsiTitleLbl = new JLabel("MMSI:");
		mmsiTitleLbl.setBounds(10, 22, 46, 14);
		targetPanel.add(mmsiTitleLbl);
		GuiStyler.styleText(mmsiTitleLbl);

		JLabel nameTitlelbl = new JLabel("Name:");
		nameTitlelbl.setBounds(10, 47, 46, 14);
		targetPanel.add(nameTitlelbl);
		GuiStyler.styleText(nameTitlelbl);

		mmsiListComboBox = new JComboBox();
		mmsiListComboBox.setModel(new DefaultComboBoxModel(new String[] { "992199003", "232004630", "244557000" }));
		mmsiListComboBox.setBounds(91, 21, 88, 17);
		GuiStyler.styleDropDown(mmsiListComboBox);
		targetPanel.add(mmsiListComboBox);

		JLabel callsignTitlelbl = new JLabel("Call Sign:");
		callsignTitlelbl.setBounds(10, 72, 46, 14);
		targetPanel.add(callsignTitlelbl);
		GuiStyler.styleText(callsignTitlelbl);

		callsignLbl = new JLabel("N/A");
		callsignLbl.setBounds(91, 72, 143, 14);
		targetPanel.add(callsignLbl);
		GuiStyler.styleText(callsignLbl);

		nameLbl = new JLabel("N/A");
		nameLbl.setBounds(91, 47, 143, 14);
		targetPanel.add(nameLbl);
		GuiStyler.styleText(nameLbl);

		JPanel routePanel = new JPanel();
		routePanel.setBackground(GuiStyler.backgroundColor);
		routePanel.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, new Color(70, 70, 70)), "Route",
				TitledBorder.LEADING, TitledBorder.TOP, GuiStyler.defaultFont, GuiStyler.textColor));
		routePanel.setBounds(10, 153, 244, 114);
		mainPanel.add(routePanel);
		routePanel.setLayout(null);

		JLabel routeNameTitleLbl = new JLabel("Route name:");
		routeNameTitleLbl.setBounds(10, 23, 77, 14);
		routePanel.add(routeNameTitleLbl);
		GuiStyler.styleText(routeNameTitleLbl);

		JLabel routeLengthTitleLbl = new JLabel("Route Length:");
		routeLengthTitleLbl.setBounds(10, 48, 77, 14);
		routePanel.add(routeLengthTitleLbl);
		GuiStyler.styleText(routeLengthTitleLbl);

		// JButton zoomToBtn = new JButton("Zoom to");
		// zoomToBtn.setBounds(10, 80, 89, 23);
		// routePanel.add(zoomToBtn);

		zoomLbl = new JLabel("Zoom To", new ImageIcon("images/buttons/zoom.png"), JLabel.CENTER);
		GuiStyler.styleButton(zoomLbl);
		zoomLbl.setBounds(10, 80, 75, 20);
		routePanel.add(zoomLbl);

		routeListComboBox = new JComboBox();
		routeListComboBox.setModel(new DefaultComboBoxModel(new String[] { "" }));
		routeListComboBox.setBounds(91, 20, 143, 20);
		routePanel.add(routeListComboBox);
		GuiStyler.styleDropDown(routeListComboBox);

		routeLengthLbl = new JLabel("N/A");
		routeLengthLbl.setBounds(91, 48, 143, 14);
		routePanel.add(routeLengthLbl);
		GuiStyler.styleText(routeLengthLbl);

		JPanel sendPanel = new JPanel();
		sendPanel.setBackground(GuiStyler.backgroundColor);
		sendPanel.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, new Color(70, 70, 70)), "Send",
				TitledBorder.LEADING, TitledBorder.TOP, GuiStyler.defaultFont, GuiStyler.textColor));

		sendPanel.setBounds(10, 278, 244, 95);
		mainPanel.add(sendPanel);
		sendPanel.setLayout(null);

		sendLbl = new JLabel("SEND", new ImageIcon("images/buttons/ok.png"), JLabel.CENTER);
		sendLbl.setBounds(10, 61, 75, 20);
		GuiStyler.styleButton(sendLbl);
		sendPanel.add(sendLbl);
		sendLbl.setEnabled(false);

		// JButton sendBtn = new JButton("Send");
		// sendBtn.setBounds(10, 61, 89, 23);
		// sendPanel.add(sendBtn);

		statusLbl = new JLabel("");
		statusLbl.setBounds(10, 11, 224, 39);
		sendPanel.add(statusLbl);
		GuiStyler.styleText(statusLbl);

		// JButton cancelBtn = new JButton("Cancel");
		// cancelBtn.setBounds(145, 61, 89, 23);
		// sendPanel.add(cancelBtn);

		cancelLbl = new JLabel("CANCEL", new ImageIcon("images/buttons/cancel.png"), JLabel.CENTER);
		GuiStyler.styleButton(cancelLbl);
		cancelLbl.setBounds(160, 61, 75, 20);
		sendPanel.add(cancelLbl);

		sendLbl.addMouseListener(this);
		cancelLbl.addMouseListener(this);
		zoomLbl.addMouseListener(this);

		mmsiListComboBox.addActionListener(this);
		routeListComboBox.addActionListener(this);
		

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

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
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		if (arg0.getSource() == zoomLbl && route.getWaypoints() != null) {
			
			
			 if (ESD.getMainFrame().getActiveMapWindow() != null) {
			 ESD.getMainFrame().getActiveMapWindow().getChartPanel()
			 .zoomToPoint(route.getWaypoints().getFirst().getPos());
			 } else if (ESD.getMainFrame().getMapWindows().size() > 0) {
			 ESD.getMainFrame().getMapWindows().get(0).getChartPanel()
			 .zoomToPoint(route.getWaypoints().getFirst().getPos());
			 }
		}
		
		if (arg0.getSource() == sendLbl && sendLbl.isEnabled()) {

			int mmsiTarget = Integer.parseInt((String) mmsiListComboBox.getSelectedItem());
//			mmsiTarget = 219230000;

			AisServices service = ESD.getAisServices();
			
			if (route == null && routeListComboBox.getSelectedIndex() != -1){
				route = routeManager.getRoutes().get(routeListComboBox.getSelectedIndex());
//				System.out.println("no route");
			}
			
			if (mmsi == -1){
				mmsi = (Long) mmsiListComboBox.getSelectedItem();
//				System.out.println("no mmsi");
			}
			
			service.sendRouteSuggestion(mmsiTarget, route);	
			

			// Send it
//			System.out.println("Selected the mmsi: " + mmsiListComboBox.getSelectedItem() + " Hardcoded to: 219230000");
//			System.out.println("Selected the route: " + route.getName());
//			System.out.println("The route is index: " + routeListComboBox.getSelectedIndex());

			 this.setVisible(false);

			this.mmsi = -1;
			this.route = null;

		}
		if (arg0.getSource() == cancelLbl) {

			// Hide it
			this.setVisible(false);
		}

		if (arg0.getSource() == zoomLbl) {

			// go to the route on the map

		}
	}

	@SuppressWarnings("unchecked")
	public void loadData() {
//		System.out.println("load data");
		loading = true;
//		mmsiListComboBox.removeAllItems();
		for (int i = 0; i < aisHandler.getShipList().size(); i++) {
			mmsiListComboBox.addItem(Long.toString(aisHandler.getShipList().get(i).MMSI));
		}

		routeListComboBox.removeAllItems();

		for (int i = 0; i < routeManager.getRoutes().size(); i++) {
			routeListComboBox.addItem(routeManager.getRoutes().get(i).getName() + "                                                 " + i);
		}
		
		
		loading = false;

	}

	@Override
	public void findAndInit(Object obj) {

		if (obj instanceof AisHandler) {
			aisHandler = (AisHandler) obj;

		}
		if (obj instanceof RouteManager) {
			routeManager = (RouteManager) obj;
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == mmsiListComboBox && !loading) {

			if (mmsiListComboBox.getSelectedItem() != null) {
				mmsi = Long.valueOf((String) mmsiListComboBox.getSelectedItem());
//				System.out.println("mmsi selected to set to " + mmsi);
				VesselTarget selectedShip = aisHandler.getVesselTargets().get(mmsi);

				if (selectedShip != null){
					
				
				if (selectedShip.getStaticData() != null) {
					nameLbl.setText(AisMessage.trimText(selectedShip.getStaticData().getName()));
					callsignLbl.setText(AisMessage.trimText(selectedShip.getStaticData().getCallsign()));
				} else {
					nameLbl.setText("N/A");
					callsignLbl.setText("N/A");
				}
				
				}else{
					statusLbl.setText("The ship is not visible on AIS");
					
				}
			}
		}

		if (arg0.getSource() == routeListComboBox && !loading) {
//			System.out.println("Selected route");
			if (routeListComboBox.getSelectedItem() != null) {
				
				route = routeManager.getRoute(routeListComboBox.getSelectedIndex());
				routeLengthLbl.setText(Integer.toString(route.getWaypoints().size()));
			}
			if (route.getWaypoints().size() > 8){
				statusLbl.setText("<html>The Route has more than 8 waypoints.<br>Only the first 8 will be sent to the ship</html>");
			}else{
				statusLbl.setText("");
			}
			
		}
		
		if (mmsi != -1 && route != null){
			sendLbl.setEnabled(true);
		}
		
		

	}

	public void setSelectedMMSI(long mmsi) {
		this.mmsi = mmsi;
//		System.out.println("MMSI is set to: " + mmsi);
		
		selectAndLoad();
	}

	public void setSelectedRoute(Route route) {
		if (!this.isVisible()){
			mmsiListComboBox.setSelectedIndex(0);
		}
		
		this.route = route;
		loadData();
		selectAndLoad();
	}

	private void selectAndLoad() {
		loadData();
		
		if (mmsi != -1) {
			for (int i = 0; i < mmsiListComboBox.getItemCount(); i++) {
				if (mmsiListComboBox.getItemAt(i).equals(Long.toString(mmsi))) {
					mmsiListComboBox.setSelectedIndex(i);
				}
			}
		}
		
		if (route != null) {
			for (int i = 0; i < ESD.getMainFrame().getRouteManagerDialog().getRouteManager().getRoutes().size(); i++) {
				if (ESD.getMainFrame().getRouteManagerDialog().getRouteManager().getRoutes().get(i) == route){
					routeListComboBox.setSelectedIndex(i);
				}
			}
		}	
		
	}

}
