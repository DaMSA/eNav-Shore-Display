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
package dk.frv.enav.esd.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import dk.frv.enav.esd.ais.AISRouteExchangeListener;
import dk.frv.enav.esd.event.ToolbarMoveMouseListener;
import dk.frv.enav.esd.gui.utils.ComponentFrame;
import dk.frv.enav.esd.msi.IMsiUpdateListener;
import dk.frv.enav.esd.msi.MsiHandler;
import dk.frv.enav.esd.service.ais.AisServices;

/**
 * Class for setting up the notification area of the application
 */
public class NotificationArea extends ComponentFrame implements IMsiUpdateListener, AISRouteExchangeListener {

	private static final long serialVersionUID = 1L;
	private Boolean locked = false;
	private JLabel moveHandler;
	private JPanel masterPanel;
	private JPanel notificationPanel;
	private static int moveHandlerHeight = 18;
	private static int notificationHeight = 25;
	private static int notificationWidth = 130;
	private static int notificationPanelOffset = 4;
	private HashMap<String, JPanel> notifications = new HashMap<String, JPanel>();
	private HashMap<String, String> services = new HashMap<String, String>();
	private HashMap<String, Integer> unreadMessages = new HashMap<String, Integer>();
	private HashMap<String, JLabel> unreadMessagesLabels = new HashMap<String, JLabel>();
	private HashMap<String, JLabel> indicatorLabels = new HashMap<String, JLabel>();
	public int width;
	public int height;
	private MsiHandler msiHandler;
	private AisServices aisService;

	Border paddingLeft = BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(65, 65, 65));
	Border paddingBottom = BorderFactory.createMatteBorder(0, 0, 5, 0, new Color(83, 83, 83));
	Border notificationPadding = BorderFactory.createCompoundBorder(paddingBottom, paddingLeft);
	Border notificationsIndicatorImportant = BorderFactory.createMatteBorder(0, 0, 0, 10, new Color(206, 120, 120));
	Border paddingLeftPressed = BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(45, 45, 45));
	Border notificationPaddingPressed = BorderFactory.createCompoundBorder(paddingBottom, paddingLeftPressed);

	/**
	 * Constructor for setting up the notification area
	 * 
	 * @param mainFrame
	 */
	public NotificationArea(final MainFrame mainFrame) {

		// Setup location
		this.setLocation((10 + moveHandlerHeight), (40 + mainFrame.getToolbar().getHeight()));
//		this.setSize(100, 400);
		this.setVisible(true);
		this.setResizable(false);

		// Strip off window looks
		setRootPaneCheckingEnabled(false);
		((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
		this.setBorder(null);

		// Create the top movehandler (for dragging)
		moveHandler = new JLabel("Notifications", JLabel.CENTER);
		moveHandler.setForeground(new Color(200, 200, 200));
		moveHandler.setOpaque(true);
		moveHandler.setBackground(Color.DARK_GRAY);
		moveHandler.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 30, 30)));
		moveHandler.setFont(new Font("Arial", Font.BOLD, 9));
		moveHandler.setPreferredSize(new Dimension(notificationWidth, moveHandlerHeight));
		ToolbarMoveMouseListener mml = new ToolbarMoveMouseListener(this, mainFrame);
		moveHandler.addMouseListener(mml);
		moveHandler.addMouseMotionListener(mml);

		// Create the grid for the notifications
		notificationPanel = new JPanel();
		notificationPanel.setLayout(new GridLayout(0, 1));
		notificationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		notificationPanel.setBackground(new Color(83, 83, 83));

		// Setup notifications (add here for more notifications)

		// Notification: MSI
		final JPanel msi = new JPanel();
		notifications.put("msi", msi);
		services.put("msi", "MSI");

		msi.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				msi.setBorder(notificationPaddingPressed);
				msi.setBackground(new Color(45, 45, 45));
			}

			public void mouseReleased(MouseEvent e) {
				msi.setBorder(notificationPadding);
				msi.setBackground(new Color(65, 65, 65));
				mainFrame.toggleNotificationCenter(0);
			}

		});
		
		// Notification: RouteExchange
		final JPanel routeExchange = new JPanel();
		notifications.put("routeExchange", routeExchange);
		services.put("routeExchange", "Route Exchange");

		routeExchange.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				routeExchange.setBorder(notificationPaddingPressed);
				routeExchange.setBackground(new Color(45, 45, 45));
			}

			public void mouseReleased(MouseEvent e) {
				routeExchange.setBorder(notificationPadding);
				routeExchange.setBackground(new Color(65, 65, 65));
				mainFrame.toggleNotificationCenter(1);
			}

		});



		// Create the masterpanel for aligning
		masterPanel = new JPanel(new BorderLayout());
		masterPanel.add(moveHandler, BorderLayout.NORTH);
		masterPanel.add(notificationPanel, BorderLayout.SOUTH);
		masterPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, new Color(30, 30, 30), new Color(
				45, 45, 45)));
		this.getContentPane().add(masterPanel);

		// And finally refresh the notification area
		repaintNotificationArea();

	}

	/**
	 * Function overriding from IMsiUpdateListener to set this class as a MSI
	 * listener
	 */
	@Override
	public void findAndInit(Object obj) {

		if (obj instanceof MsiHandler) {
			msiHandler = (MsiHandler) obj;
			msiHandler.addListener(this);
		}

		if (obj instanceof AisServices) {
			aisService = (AisServices) obj;
			aisService.addRouteExchangeListener(this);
		}

	}

	/**
	 * Function for getting the height of the notification area
	 * 
	 * @return height height of the notification area
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Function for getting the width of the notification area
	 * 
	 * @return width width of the notification area
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Function overriding from IMsiUpdateListener for doing stuff when MSI
	 * messages are incoming
	 */
	@Override
	public void msiUpdate() {

		try {
			setMessages("msi", msiHandler.getUnAcknowledgedMSI());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Function for blinking the unread indicator for a service
	 * 
	 * @param service
	 *            The service which should blink, indicating new unread messages
	 * @throws InterruptedException
	 */
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

	/**
	 * Function for refreshing the notification area after editing notifications
	 */
	public void repaintNotificationArea() {

		// Clear panel before adding services
		notificationPanel.removeAll();
		notificationPanel.updateUI();

		// Lets start by adding all the notifications
		for (Iterator<Entry<String, JPanel>> i = notifications.entrySet().iterator(); i.hasNext();) {

			Entry<String, JPanel> entry = i.next();

			// Get values for service
			String service = services.get(entry.getKey());
			
			
			Integer messageCount = unreadMessages.get(entry.getKey());

			if (messageCount == null)
				messageCount = 0;

			if (service == null)
				service = "";

			// Style the notification panel
			JPanel servicePanel = entry.getValue();
			servicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			servicePanel.setBackground(new Color(65, 65, 65));
			servicePanel.setBorder(notificationPadding);
			servicePanel.setPreferredSize(new Dimension(notificationWidth, notificationHeight));

			// Create labels for each service
			// The label
			JLabel notification = new JLabel(service);
			notification.setPreferredSize(new Dimension(80, notificationHeight));
			notification.setFont(new Font("Arial", Font.PLAIN, 11));
			notification.setForeground(new Color(237, 237, 237));
			servicePanel.add(notification);

			// Unread messages
			JLabel messages = new JLabel(messageCount.toString(), SwingConstants.RIGHT);
			messages.setPreferredSize(new Dimension(20, notificationHeight));
			messages.setFont(new Font("Arial", Font.PLAIN, 9));
			messages.setForeground(new Color(100, 100, 100));
			messages.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			servicePanel.add(messages);

			// The unread indicator
			JLabel unreadIndicator = new JLabel();
			unreadIndicator.setPreferredSize(new Dimension(7, notificationHeight));
			servicePanel.add(unreadIndicator);

			notificationPanel.add(servicePanel);

			// Make list of labels to use when updating service
			indicatorLabels.put(entry.getKey(), unreadIndicator);
			unreadMessagesLabels.put(entry.getKey(), messages);

		}

		// Then calculate the size of the notification area according to the
		// number of notifications
		width = notificationWidth;
		int innerHeight = (notifications.size() * (notificationHeight + 5)) + 5; // 5
																					// and
																					// 5
																					// for
																					// padding
		height = innerHeight + notificationPanelOffset;

		if (!locked)
			height = height + moveHandlerHeight;

		// And finally set the size and repaint it
		notificationPanel.setSize(width, innerHeight);
		notificationPanel.setPreferredSize(new Dimension(width, innerHeight));
		this.setSize(width, height);
		this.revalidate();
		this.repaint();

	}

	/**
	 * Function for setting the number of unread messages for a specific service
	 * 
	 * @param service
	 *            service for which the unread messages should be set
	 * @param messageCount
	 *            the number of unread messages to be set
	 * @throws InterruptedException
	 */
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

	/**
	 * Function for locking/unlocking the notification area
	 */
	public void toggleLock() {

		if (locked) {

			masterPanel.add(moveHandler, BorderLayout.NORTH);
			locked = false;
			repaintNotificationArea();

			// Align the notification area according to the height of the
			// movehandler
			int newX = (int) (this.getLocation().getX());
			int newY = (int) (this.getLocation().getY());
			Point new_location = new Point(newX, (newY - moveHandlerHeight));
			this.setLocation(new_location);

		} else {

			masterPanel.remove(moveHandler);
			locked = true;
			repaintNotificationArea();

			// Align the notification area according to the height of the
			// movehandler
			int newX = (int) (this.getLocation().getX());
			int newY = (int) (this.getLocation().getY());
			Point new_location = new Point(newX, (newY + moveHandlerHeight));
			this.setLocation(new_location);

		}
	}

	@Override
	public void aisUpdate() {

		try {
			setMessages("routeExchange", aisService.getUnkAck());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}