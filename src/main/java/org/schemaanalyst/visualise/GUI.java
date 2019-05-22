package org.schemaanalyst.visualise;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.schemaanalyst.testgeneration.TestSuiteGenerationReport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;
/**
 * Database Test Visualisation Tool
 * View Code
 * @author Ben Halliwell
 */
public class GUI extends JFrame {
	private Toolkit kit = Toolkit.getDefaultToolkit();
	private Dimension screenDim = kit.getScreenSize();
	private JSplitPane splitPane = new JSplitPane();
	private JSplitPane bvSplit = new JSplitPane();
	private JSplitPane structureHSplit = new JSplitPane();
	private JSplitPane structureVSplit = new JSplitPane();
	private JSplitPane secondSplit = new JSplitPane();
	private JScrollPane tcPanel = new JScrollPane();
	private JScrollPane topScroll = new JScrollPane();
	private JScrollPane structureScroll = new JScrollPane();
	private Container contentPane = this.getContentPane();
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JPanel testCasePanel = new JPanel();
	private String FONT_NAME = "Segoe UI";
	private int FONT_SIZE = 13;
	
	//constructor
	public GUI(TestSuiteGenerationReport report)  {
		this.setTitle("SchemaAnalyst Test Visualisation Tool");	
		try {
			//set the look and feel of the application
			setLookAndFeel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String welcomeMessage = "SchemaAnalyst execution complete"	
								+"\nTest requirements covered: " + report.getNumTestRequirementsCovered() + "/" + report.getNumTestRequirementsAttempted()
								+"\nCoverage: " + report.coverage()+"%%"
								+"\nNum Evaluations (test cases only): " + report.getNumDataEvaluations(true)
								+"\nNum Evaluations (all): " + report.getNumEvaluations(false);				
		JOptionPane.showMessageDialog(this,String.format(welcomeMessage, 175, 175));  
		structureVSplit.setPreferredSize(new Dimension(1280, 680));
		structureVSplit.setDividerSize(0);
		structureHSplit.setDividerSize(0);
		topScroll.getHorizontalScrollBar().addAdjustmentListener(new ScrollListener());
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT); 
		splitPane.setDividerSize(0);
		splitPane.setPreferredSize(new Dimension(1280, 680));
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		menu.add(fileMenu);
		JMenuItem quitAction = new JMenuItem("Quit");
		quitAction.addActionListener(new ExitListener());
		JMenu helpMenu = new JMenu("Help");
		JMenuItem helpAction = new JMenuItem("Open User Guide");
		menu.add(helpMenu);
		helpAction.addActionListener(new ExitListener());
		helpMenu.add(helpAction);
		fileMenu.add(quitAction);
        this.setJMenuBar(menu);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	static class ExitListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
        		//listen for menu bar interactions
	        	if(e.getActionCommand()=="Quit") {
	        		System.exit(0);
	        } else if(e.getActionCommand()=="Open User Guide") {
		        	if (Desktop.isDesktopSupported()) {
		        	    try {
		        	        File myFile = new File("UserGuide.pdf");
		        	        Desktop.getDesktop().open(myFile);
		        	    } catch (IOException ex) {
		        	        // no application registered for PDFs
		        	    }
		        	}
	        }
        }
	}
	
	/**
	   * Sets the application to use the 'Nimbus' look and feel
	   */
	public void setLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        // adjust L&F defaults so that larger fonts are used
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        defaults.put("Label.font", new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        defaults.put("Table.font", new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        defaults.put("TableHeader.font", new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        defaults.put("CheckBox.font", new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        defaults.put("TextField.font", new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        defaults.put("RadioButton.font", new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
    }
	
	/**
	   * Created the main JTabbedPane and add the needed pages
	   */
	public void setupPage() {
		tabbedPane.addTab("Schema Inspection", structureVSplit);
		tabbedPane.addTab("Test Cases", testCasePanel);
		tabbedPane.setSelectedComponent(tabbedPane.getComponentAt(1));
		structureVSplit.setDividerLocation(screenDim.height/2);
		contentPane.add(tabbedPane);
		contentPane.repaint();
	}
	
	/**
	   * Create the Schema Inspection page
	   * @param tableList Panel containing the list of table names
	   * @param structurePanel Panel containing the list of column names
	   * @param colInfo Panel containing the column information
	   * @param sqlPanel Panel displaying the SQL table creation script
	   */
	public void drawStructurePane(JPanel tableList, JPanel structurePanel, JPanel colInfo, JPanel sqlPanel) {
		structureHSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		structureScroll.getViewport().add(structurePanel);
		structureHSplit.setLeftComponent(tableList);
		TitledBorder topBorder = new TitledBorder("Select a Table: ");
		structureHSplit.setBorder(topBorder);
		TitledBorder sqlBorder = new TitledBorder("SQL Table Creation Statement: ");
		sqlPanel.setBorder(sqlBorder);
		structureHSplit.setRightComponent(sqlPanel);
		structureVSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		structureVSplit.setTopComponent(structureHSplit);
		TitledBorder bottomBorder = new TitledBorder("Column Information: ");
		colInfo.setBorder(bottomBorder);
		secondSplit.setRightComponent(new JScrollPane(colInfo));
		secondSplit.setDividerLocation(screenDim.width/2);
		secondSplit.setDividerSize(0);
		TitledBorder colSelectBorder = new TitledBorder("Select a Column: ");
		structurePanel.setBorder(colSelectBorder);
		secondSplit.setLeftComponent(structurePanel);
		structureVSplit.setBottomComponent(secondSplit);
		structureHSplit.setDividerLocation(screenDim.width/2);
		structureVSplit.setDividerLocation(screenDim.height/2);
		tabbedPane.setComponentAt(0, structureVSplit);
	}
	
	public void updateColumnTab(JPanel colTab, JPanel sqlPanel) {
		TitledBorder sqlBorder = new TitledBorder("SQL Table Creation Statement: ");
		sqlPanel.setBorder(sqlBorder);
		structureHSplit.setBottomComponent(sqlPanel);
		structureVSplit.setTopComponent(structureHSplit);
		structureHSplit.setDividerLocation(screenDim.width/2);
		structureVSplit.setDividerLocation(screenDim.height/2);
		secondSplit.setDividerLocation(screenDim.width/2);
		TitledBorder colSelectBorder = new TitledBorder("Select a Column: ");
		colTab.setBorder(colSelectBorder);
		secondSplit.setDividerSize(0);
		secondSplit.setLeftComponent(colTab);
		tabbedPane.setComponentAt(0, structureVSplit);
	}
	
	public void updateColumnInfo(JPanel colInfo) {
		TitledBorder bottomBorder = new TitledBorder("Column Information: ");
		colInfo.setBorder(bottomBorder);
		secondSplit.setDividerLocation(screenDim.width/2);
		secondSplit.setRightComponent(new JScrollPane(colInfo));
	}
	
	public void drawTestCasePane(JPanel testCases, JPanel buttonPanel) {
		JPanel casesPanel = new JPanel(new BorderLayout());
		TitledBorder casesBorder = new TitledBorder("Select a Test Case:");
		testCases.setBorder(casesBorder);
		casesPanel.add(buttonPanel, BorderLayout.SOUTH);
		casesPanel.add(testCases);
		tabbedPane.setComponentAt(1, casesPanel);
	}
	
	public void drawMainScreen(JPanel casePanel, JPanel infoPanel, JPanel buttonPanel) {
		TitledBorder borderL = new TitledBorder("Please Select a Test Case:");
		TitledBorder borderRN = new TitledBorder("Test Details: ");
		JScrollPane testDetails = new JScrollPane();
		tcPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tcPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		tcPanel.getHorizontalScrollBar().setUnitIncrement(16);
		tcPanel.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		tcPanel.getViewport().add(casePanel);
		tcPanel.setBorder(borderL);
        bvSplit.setLeftComponent(tcPanel);
        infoPanel.setBorder(borderRN);
        testDetails.getViewport().add(infoPanel);
        bvSplit.setDividerLocation(screenDim.width/2);
        bvSplit.setRightComponent(testDetails);
        splitPane.setTopComponent(bvSplit); 
        contentPane.add(splitPane);
		contentPane.setVisible(true);
	}
	
	public void drawStatePanels(JPanel panel, int testCase, JPanel caseInfo, JPanel keyPanel, JPanel optionsPanel) {
		TitledBorder borderTop = new TitledBorder("Database State After Test Execution: ");
		TitledBorder borderBL = new TitledBorder("Test Case Information: ");
		TitledBorder borderBR = new TitledBorder("Table Key: ");
		TitledBorder keyBorder = new TitledBorder("Options: ");
		JSplitPane keySplit = new JSplitPane();
		JSplitPane optionSplit = new JSplitPane();
		optionSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        topScroll.getHorizontalScrollBar().setUnitIncrement(16);
        topScroll.getViewport().add(panel);
        topScroll.setBorder(borderTop);
        splitPane.setTopComponent(topScroll); 
		caseInfo.setBorder(borderBL);
		keyPanel.setBorder(borderBR);
		keySplit.setDividerLocation(screenDim.width-350);
		keySplit.setDividerSize(0);
		keySplit.setLeftComponent(new JScrollPane(caseInfo));
		keySplit.setRightComponent(optionSplit);
		optionsPanel.setBorder(keyBorder);
		optionSplit.setDividerLocation(225);
		optionSplit.setDividerSize(0);
		optionSplit.setTopComponent(optionsPanel);
		optionSplit.setBottomComponent(keyPanel);
		splitPane.setBottomComponent(keySplit);
		splitPane.setDividerLocation(520);
		tabbedPane.addTab("Visualise Test Case "+String.valueOf(testCase+1), splitPane);
		tabbedPane.setSelectedComponent(splitPane);
	}
	
	public void returnToCases() {
		tabbedPane.setSelectedIndex(1);
	}
	
	private int visScrollPos = 0;
	
	public void setVisScroll(int pos) {
		visScrollPos = pos;
	}
	
	public int getVisScroll() {
		return visScrollPos;
	}
	
	class ScrollListener implements AdjustmentListener {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			switch (e.getAdjustmentType()) {
			case AdjustmentEvent.TRACK:
				if(e.getValue()!=0) {
					setVisScroll(e.getValue());
				    break;
				}
			}
		}
	}
		
	public void drawTestCaseStep(JPanel statePanel, int testCase, JPanel infoPanel, JPanel buttonSteps, JPanel keyPanel) {
		TitledBorder borderTop = new TitledBorder("Database State After Most Recent Step: ");
		TitledBorder borderBL = new TitledBorder("Test Stepping Controls: ");
		TitledBorder borderBR = new TitledBorder("Table Key: ");
		TitledBorder infoBorder = new TitledBorder("Current Step: ");
		JSplitPane keySplit = new JSplitPane();
		JSplitPane buttonSplit = new JSplitPane();
        topScroll.getHorizontalScrollBar().setUnitIncrement(16);
        topScroll.getViewport().add(statePanel);
        topScroll.setBorder(borderTop);
        topScroll.getHorizontalScrollBar().setValue(getVisScroll());
		splitPane.setTopComponent(topScroll); 
		buttonSteps.setBorder(borderBL);
		keyPanel.setBorder(borderBR);
		keySplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		keySplit.setDividerLocation(225);
		keySplit.setDividerSize(0);
		keySplit.setTopComponent(buttonSteps);
		keySplit.setBottomComponent(keyPanel);
		infoPanel.setBorder(infoBorder);
		buttonSplit.setDividerLocation(screenDim.width-350);
		buttonSplit.setDividerSize(0);
		buttonSplit.setRightComponent(keySplit);
		buttonSplit.setLeftComponent(new JScrollPane(infoPanel));
		splitPane.setBottomComponent(buttonSplit);
		splitPane.setDividerLocation(520);
		tabbedPane.addTab("Stepping Through Test Case "+String.valueOf(testCase+1), splitPane);
		tabbedPane.setSelectedComponent(splitPane);
	}
}
