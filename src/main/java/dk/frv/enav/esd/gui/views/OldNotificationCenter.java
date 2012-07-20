package dk.frv.enav.esd.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import dk.frv.enav.common.xml.msi.MsiMessage;
import dk.frv.enav.esd.event.ToolbarMoveMouseListener;
import dk.frv.enav.esd.gui.msi.MsiTableModel;
import dk.frv.enav.esd.gui.route.RouteExchangeTableModel;
import dk.frv.enav.esd.gui.settingtabs.GuiStyler;
import dk.frv.enav.esd.gui.utils.ComponentFrame;
import dk.frv.enav.esd.msi.IMsiUpdateListener;
import dk.frv.enav.esd.msi.MsiHandler;
import dk.frv.enav.esd.msi.MsiHandler.MsiMessageExtended;
import dk.frv.enav.esd.service.ais.AisServices;

public class OldNotificationCenter extends ComponentFrame implements ListSelectionListener, ActionListener,
		IMsiUpdateListener {

	private static final long serialVersionUID = 1L;

	Border paddingLeft = BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(65, 65, 65));
	Border paddingBottom = BorderFactory.createMatteBorder(0, 0, 5, 0, new Color(83, 83, 83));
	Border notificationPadding = BorderFactory.createCompoundBorder(paddingBottom, paddingLeft);
	Border notificationsIndicatorImportant = BorderFactory.createMatteBorder(0, 0, 0, 10, new Color(206, 120, 120));
	Border paddingLeftPressed = BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(45, 45, 45));
	Border notificationPaddingPressed = BorderFactory.createCompoundBorder(paddingBottom, paddingLeftPressed);
	
	
	private MainFrame mainFrame;
	private HashMap<String, Integer> unreadMessages = new HashMap<String, Integer>();
	private HashMap<String, JLabel> unreadMessagesLabels = new HashMap<String, JLabel>();
	private HashMap<String, JLabel> indicatorLabels = new HashMap<String, JLabel>();
	
	private static int notificationHeight = 25;
	private static int notificationWidth = 125;
	private JTable msiTable;
	private JTable routeTable;
	
	private MsiHandler msiHandler;
	private MsiTableModel msiTableModel;
	
	private AisServices aisService;
	private RouteExchangeTableModel routeTableModel;
	
	
	private JPanel pane_3;
	private JScrollPane scrollPane_1;
	private JPanel msiPanel;
	
	private JPanel routePanel;
	
	private Color leftButtonColor = Color.DARK_GRAY;
	private Color leftButtonColorClicked = new Color(45, 45, 45);
	private Color backgroundColor = new Color(83, 83, 83);
	private JTextPane area = new JTextPane();
	private StringBuilder doc = new StringBuilder();
	private JLabel but_read;
	private JLabel but_goto;
	private JLabel but_delete;
	private int selectedRow;
	private JLabel moveHandler;
	private JPanel masterPanel;
	private static int moveHandlerHeight = 18;
	private JPanel mapPanel;
	private int selectedService;
	private DefaultListSelectionModel values;
	private JPanel headerPanel;
	
	private JScrollPane leftScrollPane;
	private int currentService = -1;

	public OldNotificationCenter() {
		super("NOTCENTER", false, true, false, false);

		// Strip off window looks
		setRootPaneCheckingEnabled(false);
		((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
		this.setBorder(null);

		// Map tools
		mapPanel = new JPanel(new GridLayout(1, 3));
		mapPanel.setPreferredSize(new Dimension(500, moveHandlerHeight));
		mapPanel.setOpaque(true);
		mapPanel.setBackground(Color.DARK_GRAY);
		mapPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 30, 30)));

		// Placeholder - for now
		mapPanel.add(new JLabel());

		// Movehandler/Title dragable)
		moveHandler = new JLabel("Notification Center", JLabel.CENTER);
		moveHandler.setFont(new Font("Arial", Font.BOLD, 9));
		moveHandler.setForeground(new Color(200, 200, 200));
		// actions = moveHandler.getListeners(MouseMotionListener.class);
		mapPanel.add(moveHandler);

		// The tools (minimize, maximize and close)
		JPanel mapToolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		mapToolsPanel.setOpaque(false);
		mapToolsPanel.setPreferredSize(new Dimension(60, 50));

		JLabel close = new JLabel(new ImageIcon("images/window/close.png"));
		close.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				toggleVisibility();
			}

		});
		close.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 2));
		mapToolsPanel.add(close);
		mapPanel.add(mapToolsPanel);

		JPanel buttonPanel = new JPanel();

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		buttonPanel.setPreferredSize(new Dimension(900, 600 - moveHandlerHeight));
		buttonPanel.setSize(new Dimension(900, 600 - moveHandlerHeight));
		buttonPanel.setBackground(backgroundColor);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 135, 365, 400 };
		gridBagLayout.rowHeights = new int[] { 100, 540 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 1.0 };
		buttonPanel.setLayout(gridBagLayout);

		JPanel pane = new JPanel();
		pane.setLayout(new FlowLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		pane.setBackground(backgroundColor);
		GridBagConstraints gbc_pane = new GridBagConstraints();
		gbc_pane.fill = GridBagConstraints.BOTH;
		gbc_pane.gridx = 0;
		gbc_pane.gridy = 0;
		gbc_pane.gridheight = 2;
		buttonPanel.add(pane, gbc_pane);

		JPanel labelContainer = new JPanel();
		labelContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelContainer.setPreferredSize(new Dimension(notificationWidth, notificationHeight*3));
		((FlowLayout) labelContainer.getLayout()).setVgap(5);
		labelContainer.setBackground(backgroundColor);
		pane.add(labelContainer);

		Integer messageCountMSI = unreadMessages.get("MSI");
		if (messageCountMSI == null)
			messageCountMSI = 0;
		
		Integer messageCountRoute = unreadMessages.get("Route");
		if (messageCountRoute == null)
			messageCountRoute = 0;

		// Style the MSI notification panel
		msiPanel = new JPanel();
		msiPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		msiPanel.setBackground(new Color(65, 65, 65));
		msiPanel.setPreferredSize(new Dimension(notificationWidth, notificationHeight));
		msiPanel.setSize(new Dimension(notificationWidth, notificationHeight));
		
		// Style the Route notification panel
		routePanel = new JPanel();
		routePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		routePanel.setBackground(new Color(65, 65, 65));
		routePanel.setPreferredSize(new Dimension(notificationWidth, notificationHeight));
		routePanel.setSize(new Dimension(notificationWidth, notificationHeight));

		// Create labels for each service

		// MSI
		JLabel notification = new JLabel("  MSI");
		notification.setPreferredSize(new Dimension(98, notificationHeight));
		notification.setSize(new Dimension(76, notificationHeight));
		notification.setFont(new Font("Arial", Font.PLAIN, 11));
		notification.setForeground(new Color(237, 237, 237));
		msiPanel.add(notification);
		// Unread messages
		JLabel messages = new JLabel(messageCountMSI.toString(), SwingConstants.RIGHT);
		messages.setPreferredSize(new Dimension(20, notificationHeight));
		messages.setSize(new Dimension(20, notificationHeight));
		messages.setFont(new Font("Arial", Font.PLAIN, 9));
		messages.setForeground(new Color(100, 100, 100));
		messages.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		msiPanel.add(messages);
		// The unread indicator
		JLabel unreadIndicator = new JLabel();
		unreadIndicator.setPreferredSize(new Dimension(7, notificationHeight));
		unreadIndicator.setSize(new Dimension(7, notificationHeight));
		msiPanel.add(unreadIndicator);

		labelContainer.add(msiPanel);

		
		
		// Route Exchange
		JLabel notificationRoute = new JLabel("  Route Exchange");
		notificationRoute.setPreferredSize(new Dimension(98, notificationHeight));
		notificationRoute.setSize(new Dimension(76, notificationHeight));
		notificationRoute.setFont(new Font("Arial", Font.PLAIN, 11));
		notificationRoute.setForeground(new Color(237, 237, 237));
		routePanel.add(notificationRoute);
		// Unread messages
		JLabel routeMessages = new JLabel(messageCountRoute.toString(), SwingConstants.RIGHT);
		routeMessages.setPreferredSize(new Dimension(20, notificationHeight));
		routeMessages.setSize(new Dimension(20, notificationHeight));
		routeMessages.setFont(new Font("Arial", Font.PLAIN, 9));
		routeMessages.setForeground(new Color(100, 100, 100));
		routeMessages.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		routePanel.add(routeMessages);
		// The unread indicator
		JLabel unreadIndicatorRoute = new JLabel();
		unreadIndicatorRoute.setPreferredSize(new Dimension(7, notificationHeight));
		unreadIndicatorRoute.setSize(new Dimension(7, notificationHeight));
		routePanel.add(unreadIndicator);

		labelContainer.add(routePanel);
		
		
		
		
		// Make list of labels to use when updating service
		indicatorLabels.put("MSI", unreadIndicator);
		unreadMessagesLabels.put("MSI", messages);

		// Make list of labels to use when updating service
		indicatorLabels.put("Route", unreadIndicatorRoute);
		unreadMessagesLabels.put("Route", routeMessages);

		
		
		// Center
		// MARKER GOES HERE
		headerPanel = new JPanel(new FlowLayout(0));
		headerPanel.setBackground(new Color(39, 39, 39));
		headerPanel.setPreferredSize(new Dimension(300, 10));
		headerPanel.setSize(new Dimension(300, 10));
		((FlowLayout) headerPanel.getLayout()).setHgap(0);
		GridBagConstraints gbc_test = new GridBagConstraints();
		gbc_test.fill = GridBagConstraints.HORIZONTAL;
		gbc_test.gridx = 1;
		gbc_test.gridy = 0;
		gbc_test.gridheight = 1;
		gbc_test.insets = new Insets(0, 0, 0, 0);
		buttonPanel.add(headerPanel, gbc_test);

		leftScrollPane = new JScrollPane();
		leftScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		leftScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 1;
		gbc_scrollPane_2.gridy = 1;
		gbc_scrollPane_2.gridheight = 1;
		gbc_scrollPane_2.insets = new Insets(-10, 0, 0, 0);
		buttonPanel.add(leftScrollPane, gbc_scrollPane_2);
		String[] colHeadings = { "ID", "Title" };
		DefaultTableModel model = new DefaultTableModel(30, colHeadings.length);
		model.setColumnIdentifiers(colHeadings);
		
		//Create the msi table
		
		msiTable = new JTable(model) {
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
				Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
				if (Index_row % 2 == 0) {
					comp.setBackground(new Color(49, 49, 49));
				} else {
					comp.setBackground(new Color(65, 65, 65));
				}

				if (isCellSelected(Index_row, Index_col)) {
					comp.setForeground(Color.white);
					comp.setBackground(new Color(85, 85, 85));
				}

				//Paint based on awk
				if (msiTableModel != null) {
					if (msiTableModel.isAwk(Index_row) && Index_col == 0) {
						comp.setForeground(new Color(130, 165, 80));
					} else if (!msiTableModel.isAwk(Index_row) && Index_col == 0) {
						comp.setForeground(new Color(165, 80, 80));
					}
				}

				return comp;
			}

			public boolean isCellEditable(int rowIndex, int vColIndex) {
				return false;
			}
		};
		
		msiTable.setTableHeader(null);
		msiTable.setBorder(new EmptyBorder(0, 0, 0, 0));
		msiTable.setIntercellSpacing(new Dimension(0, 0));
		msiTable.setBackground(new Color(49, 49, 49));
		msiTable.setShowVerticalLines(false);
		msiTable.setShowHorizontalLines(false);
		msiTable.setShowGrid(false);
		msiTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		msiTable.setForeground(Color.white);
		msiTable.setSelectionForeground(Color.gray);
		msiTable.setRowHeight(20);
		msiTable.setFocusable(false);
		msiTable.setAutoResizeMode(0);
		msiTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		msiTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		headerPanel.removeAll();

		
		
		//Create the route table
		routeTable = new JTable(model) {
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
				Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
				if (Index_row % 2 == 0) {
					comp.setBackground(new Color(49, 49, 49));
				} else {
					comp.setBackground(new Color(65, 65, 65));
				}

				if (isCellSelected(Index_row, Index_col)) {
					comp.setForeground(Color.white);
					comp.setBackground(new Color(85, 85, 85));
				}

				return comp;
			}

			public boolean isCellEditable(int rowIndex, int vColIndex) {
				return false;
			}
		};
		
		routeTable.setTableHeader(null);
		routeTable.setBorder(new EmptyBorder(0, 0, 0, 0));
		routeTable.setIntercellSpacing(new Dimension(0, 0));
		routeTable.setBackground(new Color(49, 49, 49));
		routeTable.setShowVerticalLines(false);
		routeTable.setShowHorizontalLines(false);
		routeTable.setShowGrid(false);
		routeTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		routeTable.setForeground(Color.white);
		routeTable.setSelectionForeground(Color.gray);
		routeTable.setRowHeight(20);
		routeTable.setFocusable(false);
		routeTable.setAutoResizeMode(0);
		routeTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		routeTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		headerPanel.removeAll();	

		
		
		//initialize by setting msiTable as main table
		leftScrollPane.getViewport().setBackground(backgroundColor);
		
		
		leftScrollPane.setViewportView(msiTable);
//		scrollPane_2.setViewportView(routeTable);
		
		
		leftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		
		// Right
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 2;
		gbc_scrollPane_3.gridy = 0;
		gbc_scrollPane_3.insets = new Insets(10, 0, 0, 0);
		pane_3 = new JPanel();
		pane_3.setBackground(backgroundColor);
		pane_3.setLayout(new FlowLayout());
		pane_3.setVisible(false);

		but_read = new JLabel("Read", new ImageIcon("images/notificationcenter/tick.png"), JLabel.CENTER);
		GuiStyler.styleButton(but_read);
		but_read.setPreferredSize(new Dimension(75, 20));
		pane_3.add(but_read);

		but_goto = new JLabel("Goto", new ImageIcon("images/notificationcenter/map-pin.png"), JLabel.CENTER);
		GuiStyler.styleButton(but_goto);
		but_goto.setPreferredSize(new Dimension(75, 20));
		pane_3.add(but_goto);

		but_delete = new JLabel("Delete", new ImageIcon("images/notificationcenter/cross.png"), JLabel.CENTER);
		GuiStyler.styleButton(but_delete);
		but_delete.setPreferredSize(new Dimension(75, 20));
		pane_3.add(but_delete);

		buttonPanel.add(pane_3, gbc_scrollPane_3);

		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 1;
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setVisible(false);
		buttonPanel.add(scrollPane_1, gbc_scrollPane_1);

		area.setEditable(false);
		area.setContentType("text/html");
		area.setPreferredSize(new Dimension(100, 100));
		area.setLocation(0, 0);
		area.setLayout(null);
		area.setBackground(backgroundColor);
		area.setMargin(new Insets(10, 10, 10, 10));

		scrollPane_1.setViewportView(area);

		addMouseListeners();

		masterPanel = new JPanel(new BorderLayout());
		masterPanel.add(mapPanel, BorderLayout.NORTH);
		masterPanel.add(buttonPanel, BorderLayout.SOUTH);
		masterPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, new Color(30, 30, 30), new Color(
				45, 45, 45)));
		this.getContentPane().add(masterPanel);
	}

	public void newMessage(final String service) throws InterruptedException {

		final int blinks = 20;

		final Runnable doChangeIndicator = new Runnable() {

			JLabel unreadIndicator = indicatorLabels.get(service);
			boolean changeColor = false;

			public void run() {

				if (changeColor = !changeColor) {
					unreadIndicator.setBackground(new Color(165, 80, 80));
				} else {
					unreadIndicator.setBackground(new Color(206, 120, 120));
				}

			}
		};

		Runnable doBlinkIndicator = new Runnable() {

			public void run() {
				for (int i = 0; i < blinks; i++) {
					try {
						EventQueue.invokeLater(doChangeIndicator);
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
				}
			}

		};

		new Thread(doBlinkIndicator).start();

	}

	public void setMessages(String service, int messageCount) throws InterruptedException {

		JLabel unread = unreadMessagesLabels.get(service);
		JLabel unreadIndicator = indicatorLabels.get(service);
		Integer currentCount = unreadMessages.get(service);

		if (currentCount == null)
			currentCount = 0;

		// If no unread messages, remove the red indicator for the service
		if (messageCount == 0)
			unreadIndicator.setOpaque(false);

		// Update the unread messages label if it differs
		if (messageCount != currentCount)
			unread.setText(Integer.toString(messageCount));

		// If new unread messages are received, start the blinking indicator
		if (messageCount > currentCount) {
			unreadIndicator.setOpaque(true);
			newMessage(service);
		}

		unreadMessages.put(service, messageCount);
	}

	public void removeArea() {
		area.setText(doc.toString());
		pane_3.setVisible(false);
		scrollPane_1.setVisible(false);
	}

	public void styleButton(JLabel label, Color color) {
		label.setPreferredSize(new Dimension(125, 25));
		label.setFont(new Font("Arial", Font.PLAIN, 11));
		label.setForeground(new Color(237, 237, 237));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		label.setBackground(color);
		label.setOpaque(true);
	}


	public void addMouseListeners() {

		msiPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				msiPanel.setBackground(leftButtonColorClicked);
			}

			public void mouseReleased(MouseEvent e) {
				msiPanel.setBackground(leftButtonColor);
			}

			public void mouseClicked(MouseEvent e) {
				// Activate and update table
				leftScrollPane.setViewportView(msiTable);
				msiTable.setFocusable(true);
				showMiddleTable(0); // 0 = MSI

			}
		});
		
		
		
		routePanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				routePanel.setBackground(leftButtonColorClicked);
			}

			public void mouseReleased(MouseEvent e) {
				routePanel.setBackground(leftButtonColor);
			}

			public void mouseClicked(MouseEvent e) {
				// Activate and update table
				leftScrollPane.setViewportView(routeTable);
				routeTable.setFocusable(true);
				showMiddleTable(1); // 0 = Route Exchange
			}
		});
		

		but_read.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (selectedService == 0) { // MSI
					int rowAfter = selectedRow;

					MsiMessage msiMessage = msiHandler.getMessageList().get(selectedRow).msiMessage;
					msiHandler.setAcknowledged(msiMessage);
					msiUpdate();
					showMiddleTable(0);
					msiTable.changeSelection(rowAfter, 0, false, false);

				}
			}
		});

		but_goto.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (mainFrame.getActiveMapWindow() != null) {
					mainFrame.getActiveMapWindow().getChartPanel()
							.zoomToPoint(msiTableModel.getMessageLatLon(selectedRow));
				} else if (mainFrame.getMapWindows().size() > 0) {
					mainFrame.getMapWindows().get(0).getChartPanel()
							.zoomToPoint(msiTableModel.getMessageLatLon(selectedRow));
				}
			}
		});

		but_delete.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (selectedService == 0) { // MSI

					MsiMessage msiMessage = msiHandler.getMessageList().get(selectedRow).msiMessage;
					msiHandler.deleteMessage(msiMessage);
					msiUpdate();
					showMiddleTable(0);
				}
			}
		});
	}
	
	

	public void showMiddleTable(int service) {

		if (service == currentService) {
			return;
		}

		switch (service) {
		case 0:
			// MSI
			selectedService = service;
			
			msiTable.setModel(msiTableModel);
			msiTable.getColumnModel().getColumn(0).setPreferredWidth(40);
			msiTable.getColumnModel().getColumn(1).setPreferredWidth(60);
			msiTable.getColumnModel().getColumn(2).setPreferredWidth(90);
			msiTable.getColumnModel().getColumn(3).setPreferredWidth(155);
			msiTable.getSelectionModel().addListSelectionListener(new MSIRowListener());
			headerPanel.removeAll();
			headerPanel.add(createHeaderColumn(msiTableModel.getColumnName(0), 40));
			headerPanel.add(createHeaderColumn(msiTableModel.getColumnName(1), 60));
			headerPanel.add(createHeaderColumn(msiTableModel.getColumnName(2), 90));
			headerPanel.add(createHeaderColumn(msiTableModel.getColumnName(3), 155));
			currentService = service;

			initialMsg();
			removeArea();
			break;
			
		case 1:
			// Route Exchange
			selectedService = service;
			
			routeTable.setModel(routeTableModel);
			routeTable.getColumnModel().getColumn(0).setPreferredWidth(40);
			routeTable.getColumnModel().getColumn(1).setPreferredWidth(60);
			routeTable.getColumnModel().getColumn(2).setPreferredWidth(90);
			routeTable.getColumnModel().getColumn(3).setPreferredWidth(155);
			routeTable.getSelectionModel().addListSelectionListener(new MSIRowListener());
			headerPanel.removeAll();
			headerPanel.add(createHeaderColumn(routeTable.getColumnName(0), 40));
			headerPanel.add(createHeaderColumn(routeTable.getColumnName(1), 60));
			headerPanel.add(createHeaderColumn(routeTable.getColumnName(2), 90));
			headerPanel.add(createHeaderColumn(routeTable.getColumnName(3), 155));
			currentService = service;

			removeArea();
			break;	
		default:
			break;
		}
	}

	public void showMessage(int service, int msgId) {

		int index = -1;

		List<MsiMessageExtended> messages = msiTableModel.getMessages();
		for (int i = 0; i < messages.size(); i++) {
			MsiMessageExtended message = messages.get(i);
			if (message.msiMessage.getMessageId() == msgId) {

				index = i;
				break;
			}
		}
		msiTable.changeSelection(index, 0, false, false);
	}

	private JPanel createHeaderColumn(String name, int width) {
		JPanel container = new JPanel();
		container.setSize(width, 10);
		container.setPreferredSize(new Dimension(width, 10));
		container.setBackground(new Color(39, 39, 39));
		container.setBounds(0, 0, width, 15);
		((FlowLayout) container.getLayout()).setVgap(0);
		((FlowLayout) container.getLayout()).setHgap(0);

		JLabel col = new JLabel(name);
		col.setSize(width, 10);
		col.setPreferredSize(new Dimension(width, 10));
		col.setForeground(Color.white);
		col.setFont(new Font("Arial", Font.BOLD, 9));
		if (name == "ID")
			col.setHorizontalAlignment(0);
		container.add(col);
		return container;
	}

	public void initialMsg() {
		// Show buttons and area in right pane
		pane_3.setVisible(true);
		scrollPane_1.setVisible(true);

		selectedRow = 0;
		if (msiTableModel.isAwk(selectedRow)) {
			but_read.setVisible(false);
		} else {
			but_read.setVisible(true);
		}

		// Update area
		doc.delete(0, doc.length());
		doc.append("<font size=\"2\" face=\"times, serif\" color=\"white\">");
		for (int i = 0; i < ((MsiTableModel) msiTable.getModel()).areaGetColumnCount(); i++) {

			doc.append("<u><b>" + ((MsiTableModel) msiTable.getModel()).areaGetColumnName(i) + ":</b></u><br />"
					+ ((MsiTableModel) msiTable.getModel()).areaGetValueAt(0, i) + "<br /><br />");
		}
		doc.append("</font>");
		area.setText(doc.toString());
	}

	private class MSIRowListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent event) {

			if (event.getValueIsAdjusting()) {
				return;
			}

			values = (DefaultListSelectionModel) event.getSource();

			// Show buttons and area in right pane
			pane_3.setVisible(true);
			scrollPane_1.setVisible(true);

			selectedRow = values.getAnchorSelectionIndex();
			if (msiTableModel.isAwk(selectedRow)) {
				but_read.setVisible(false);
			} else {
				but_read.setVisible(true);
			}

			// Update area
			doc.delete(0, doc.length());
			doc.append("<font size=\"2\" face=\"times, serif\" color=\"white\">");
			for (int i = 0; i < ((MsiTableModel) msiTable.getModel()).areaGetColumnCount(); i++) {
				if (values.getAnchorSelectionIndex() == -1) {
					removeArea();
					return;
				}
				doc.append("<u><b>" + ((MsiTableModel) msiTable.getModel()).areaGetColumnName(i) + ":</b></u><br />"
						+ ((MsiTableModel) msiTable.getModel()).areaGetValueAt(values.getAnchorSelectionIndex(), i)
						+ "<br /><br />");
			}
			doc.append("</font>");
			area.setText(doc.toString());

		}
	}

	@Override
	public void findAndInit(Object obj) {
		if (obj instanceof MsiHandler) {
			msiHandler = (MsiHandler) obj;
			msiHandler.addListener(this);
			msiTableModel = new MsiTableModel(msiHandler);
		}
		if (obj instanceof MainFrame) {
			mainFrame = (MainFrame) obj;
			ToolbarMoveMouseListener mml = new ToolbarMoveMouseListener(this, mainFrame);
			mapPanel.addMouseListener(mml);
			mapPanel.addMouseMotionListener(mml);
		}
		if (obj instanceof AisServices) {
			aisService = (AisServices) obj;
//			msiHandler.addListener(this);
			routeTableModel = new RouteExchangeTableModel(aisService);
		}
	}

	@Override
	public void msiUpdate() {
		try {
			setMessages("MSI", msiHandler.getUnAcknowledgedMSI());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Change the visiblity
	 */
	public void toggleVisibility() {
		setVisible(!this.isVisible());
	}

}
