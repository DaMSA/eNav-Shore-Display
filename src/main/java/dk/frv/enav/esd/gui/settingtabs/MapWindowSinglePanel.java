package dk.frv.enav.esd.gui.settingtabs;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import dk.frv.enav.esd.gui.JMapFrame;
import dk.frv.enav.esd.gui.MainFrame;
import dk.frv.enav.esd.settings.Settings;

public class MapWindowSinglePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainFrame mainFrame;
	private Settings settings;
	private JTextField textField;
	private JCheckBox chckbxLocked;
	private JCheckBox chckbxAlwaysOnTop;
	private int id;
	
	public MapWindowSinglePanel(MainFrame mainFrame, int id){
		super();
		
		this.mainFrame = mainFrame;
		this.settings = settings;
		this.id = id;
		
		
		setBackground(GuiStyler.backgroundColor);
		setBounds(10, 11, 493, 600);
		setLayout(null);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(GuiStyler.backgroundColor);
		panel_1.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(70, 70, 70)), "Map Window Settings", TitledBorder.LEADING, TitledBorder.TOP, GuiStyler.defaultFont, GuiStyler.textColor));
		panel_1.setBounds(10, 11, 407, 122);
		
		add(panel_1);
		panel_1.setLayout(null);
		
		JLabel windowName = new JLabel("Window Name:");
		windowName.setBounds(10, 33, 91, 14);
		GuiStyler.styleText(windowName);
		
		panel_1.add(windowName);
		
		chckbxLocked = new JCheckBox("Locked");
		chckbxLocked.setBounds(4, 54, 97, 23);
		panel_1.add(chckbxLocked);
		
		chckbxAlwaysOnTop = new JCheckBox("Always on top");
		chckbxAlwaysOnTop.setBounds(4, 80, 97, 23);
		panel_1.add(chckbxAlwaysOnTop);
		
		textField = new JTextField();
		textField.setBounds(90, 30, 127, 20);
		panel_1.add(textField);
		textField.setColumns(10);
		GuiStyler.styleTextFields(textField);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(GuiStyler.backgroundColor);
		panel_2.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(70, 70, 70)), "AIS Layer Settings", TitledBorder.LEADING, TitledBorder.TOP, GuiStyler.defaultFont, GuiStyler.textColor));

		panel_2.setBounds(10, 144, 407, 190);
		
		add(panel_2);
		panel_2.setLayout(null);
	}
	
	public void loadSettings(){
		JMapFrame mapWindow = mainFrame.getMapWindows().get(id);
		
		textField.setText(mapWindow.getTitle());
		
		System.out.println(textField.getText() + " locked: " + mapWindow.isLocked());
		
		chckbxLocked.setSelected(mapWindow.isLocked());
		chckbxAlwaysOnTop.setSelected(mapWindow.isInFront());
	}
}