package dk.frv.enav.esd.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.ColorUIResource;

import dk.frv.enav.esd.event.ToolbarMoveMouseListener;

public class JMapFrame extends JInternalFrame implements MouseListener  {
	
	private static final long serialVersionUID = 1L;
	private ChartPanel chartPanel;
	boolean locked = false;
	boolean alwaysInFront = false;
	MouseMotionListener[] actions;
	private int id;
	private final MainFrame mainFrame;
	private JPanel glassPanel;
	private JLabel moveHandler;
	private JPanel masterPanel;
	private static int moveHandlerHeight = 18;
	
	public JMapFrame(int id, MainFrame mainFrame) {
		super("New Window "+id, true, true, true, true);

		this.mainFrame = mainFrame;
		this.id = id;
		chartPanel = new ChartPanel(mainFrame);
		//this.setContentPane(chartPanel);
		this.setVisible(true);
	
		initGlassPane();
		
		chartPanel.initChart();
		makeKeyBindings();
		
		((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).getNorthPane().addMouseListener(this);
		actions = (MouseMotionListener[])((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).getNorthPane().getListeners(MouseMotionListener.class);
		
		// Strip off
		setRootPaneCheckingEnabled(false);
		((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).setNorthPane(null);
		this.setBorder(null);
		
        // Create the top movehandler (for dragging)
        moveHandler = new JLabel("Map Window", JLabel.CENTER);
        moveHandler.setForeground(new Color(200, 200, 200));
        moveHandler.setOpaque(true);
        moveHandler.setBackground(Color.DARK_GRAY);
        moveHandler.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 30, 30)));
        moveHandler.setFont(new Font("Arial", Font.BOLD, 9));
        moveHandler.setPreferredSize(new Dimension(500, moveHandlerHeight));
        ToolbarMoveMouseListener mml = new ToolbarMoveMouseListener(this, mainFrame);
        moveHandler.addMouseListener(mml);
        moveHandler.addMouseMotionListener(mml);
        
        final JInternalFrame test = this;
        JLabel minimize = new JLabel("Minimize");
        minimize.addMouseListener(new MouseAdapter() {  
		    public void mouseReleased(MouseEvent e) { 
		    	try {
					test.setIcon(true);
				} catch (PropertyVetoException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	System.out.println("minimizing");
		    }
        });
        
        JLabel maximize = new JLabel("Maximize");
        maximize.addMouseListener(new MouseAdapter() {  
		    public void mouseReleased(MouseEvent e) { 
		    	test.setMaximizable(true);
		    	System.out.println("Maximizing");
		    }
        });
        
        JLabel close = new JLabel("Close");
        close.addMouseListener(new MouseAdapter() {  
		    public void mouseReleased(MouseEvent e) { 
		    	try {
					test.setClosed(true);
				} catch (PropertyVetoException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	System.out.println("Closing");
		    }
        });
        
        
        
     // Create the masterpanel for aligning
	    masterPanel = new JPanel(new BorderLayout());
	    masterPanel.add(moveHandler, BorderLayout.NORTH);
	    masterPanel.add(chartPanel, BorderLayout.SOUTH);
	    //masterPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, new Color(30, 30, 30), new Color(45, 45, 45)));
	    //this.getContentPane().add(masterPanel);
        
	    this.setContentPane(masterPanel);
	}
	
	public JMapFrame(int id, MainFrame mainFrame, Point2D center, float scale) {
		super("New Window "+id, true, true, true, true);

		this.mainFrame = mainFrame;
		this.id = id;
		chartPanel = new ChartPanel(mainFrame);
		this.setContentPane(chartPanel);
		this.setVisible(true);
	
		chartPanel.initChart(center, scale);
		makeKeyBindings();
		
		((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).getNorthPane().addMouseListener(this);
		actions = (MouseMotionListener[])((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).getNorthPane().getListeners(MouseMotionListener.class);
	
		// Strip off
		setRootPaneCheckingEnabled(false);
		((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).setNorthPane(null);
		this.setBorder(null);
	}
	
	private void initGlassPane() {
		glassPanel = (JPanel)getGlassPane();
		glassPanel.setLayout(null);
		glassPanel.setVisible(false);
	}

	public int getId(){
		return id;
	}
	
	public void lockUnlockWindow(){
		if (locked){
//			for (int i = 0; i < actions.length; i++)
//				northPanel.addMouseMotionListener( actions[i] );
			this.setResizable(true);
			setRootPaneCheckingEnabled(true);
			this.updateUI();
			((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).getNorthPane().addMouseListener(this);
			
			
//			System.out.println(northPanel.getMouseListeners().length);


			locked = false;
		}else{
			
//			for (int i = 0; i < actions.length; i++)
//				northPanel.removeMouseMotionListener( actions[i] );

			this.setResizable(false);
			setRootPaneCheckingEnabled(false);
			((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).setNorthPane(null);
			this.setBorder(null);
//			this.updateUI();
//			this.updateUI();
			locked = true;
		}
	}
	
	public void alwaysFront(){
		if (alwaysInFront){
			alwaysInFront = false;
		}else{
			alwaysInFront = true;
		}
		mainFrame.getDesktop().getManager().addToFront(id, this);
	}
	
	public boolean isLocked(){
		return locked;
	}
	
	public boolean isInFront(){
		return alwaysInFront;
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	private void makeKeyBindings(){
	      JPanel content = (JPanel) getContentPane();
	      InputMap inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
	    @SuppressWarnings("serial")
		Action zoomIn = new AbstractAction() {
	        public void actionPerformed(ActionEvent actionEvent) {
	        	chartPanel.doZoom(0.5f);
	        }
	      };
	      
		@SuppressWarnings("serial")
		Action zoomOut = new AbstractAction() {
		    public void actionPerformed(ActionEvent actionEvent) {
		    	chartPanel.doZoom(2f);
		        }
		      };	      
			
		@SuppressWarnings("serial")
		Action panUp = new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				chartPanel.pan(1);
				}
			};
		@SuppressWarnings("serial")
		Action panDown = new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				chartPanel.pan(2);
				}
			};			
			
		@SuppressWarnings("serial")
		Action panLeft = new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				chartPanel.pan(3);
				}
			};
		@SuppressWarnings("serial")
		Action panRight = new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				chartPanel.pan(4);
				}
			};			
	   			
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, 0), "ZoomIn");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, 0), "ZoomIn");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, 0), "ZoomOut");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, 0), "ZoomOut");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), "panUp");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), "panDown");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "panLeft");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "panRight");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_UP, 0), "panUp");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_DOWN, 0), "panDown");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_LEFT, 0), "panLeft");
	      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_RIGHT, 0), "panRight");	      
   
	      
	      content.getActionMap().put("ZoomOut", zoomOut);
	      content.getActionMap().put("ZoomIn", zoomIn);
	      content.getActionMap().put("panUp", panUp);	
	      content.getActionMap().put("panDown", panDown);	
	      content.getActionMap().put("panLeft", panLeft);	
	      content.getActionMap().put("panRight", panRight);	
	      
	}
	
	public void rename(){
		String title =
	        JOptionPane.showInputDialog(this, "Enter a new title:", this.getTitle());
		if (title != null){
		this.setTitle(title);
		mainFrame.renameMapWindow(this);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getClickCount() == 1){
//			rename();
		}
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
		// TODO Auto-generated method stub
		
	}

	
}
