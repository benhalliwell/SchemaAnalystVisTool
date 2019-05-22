package org.schemaanalyst.visualise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.schemaanalyst.data.Data;
import org.schemaanalyst.data.Row;
import org.schemaanalyst.sqlrepresentation.*;
import org.schemaanalyst.sqlrepresentation.constraint.CheckConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.Constraint;
import org.schemaanalyst.sqlrepresentation.constraint.ForeignKeyConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.NotNullConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.UniqueConstraint;
import org.schemaanalyst.sqlwriter.ConstraintSQLWriter;
import org.schemaanalyst.sqlwriter.DataTypeSQLWriter;
import org.schemaanalyst.sqlwriter.ExpressionSQLWriter;
import org.schemaanalyst.sqlwriter.SQLWriter;
import org.schemaanalyst.sqlwriter.ValueSQLWriter;
import org.schemaanalyst.testgeneration.TestCase;
import org.schemaanalyst.testgeneration.TestSuite;
/**
 * Database Test Visualisation Tool
 * Controller Code
 * @author Ben Halliwell
 */
public class Visualise {
	
	private Schema schema;
	private TestSuite testSuite;
	private GUI view;
	private List<VisCase> visCases;
	private List<VisTable> structureTables;
	private JTable caseTable;
	private JTable nameList;
	private JTable colTable;
	private VisTable chosenTable;
	private Column chosenColumn;
	private VisTable choice;
	private int selectedCase = 0;
	private int maximumStepper;
	private int stepperTarget = 1;
	private JPanel testCases = new JPanel();
	
	public Visualise(Schema schema, TestSuite testSuite, GUI view) {
		this.schema = schema;
		this.testSuite = testSuite;
		this.view = view;
		structureTables = createStructureTables(schema);
		visCases = createVisCases(testSuite);
	}
	
	public List<VisTable> getStructureTables(){
		return structureTables;
	}
	
	protected List<VisCase> getVisCases() {
		return visCases;
	}
	
	public void defaultColumnChoice(List<VisTable> tables) {
		chosenColumn = tables.get(0).getColumns().get(0);
	}
	
	
	protected void setChosenColumn(Column col) {
		chosenColumn = col;
	}
	
	public Column getChosenColumn() {
		return chosenColumn;
	}
	
	public void defaultTableChoice(List<VisTable> tables) {
		choice = tables.get(0);
		chosenTable = choice;
	}
	
	protected void setChosenTable(VisTable table) {
		chosenTable = table;
	}
	
	public VisTable getChosenStructureTable() {
		return chosenTable;
	}
	
	protected void defaultTestCase() {
		selectedCase = 0;
	}
	
	protected void setSelectedCase(int newCase) {
		selectedCase = newCase;
	}
	
	protected int getSelectedCase() {
		return selectedCase;
	}
	
	/**
	   * This method creates a JTable containing test case information 
	   * from the pre-created list of VisCase objects
	   * @return JPanel containing JTable of test case information
	   */
	public JPanel testCaseTable() {
		testCases.setLayout(new BoxLayout(testCases, BoxLayout.PAGE_AXIS));
		caseTable = new JTable(new DefaultTableModel(new String[] {"ID", "Description", "Tables Involved", "Test Result"}, 4) {
		    @Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		});
		caseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		caseTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		DefaultTableModel mod = (DefaultTableModel) caseTable.getModel();
		mod.setRowCount(0);
		for (VisCase c : visCases) {
			if(c.getResults().get(c.getResults().size()-1) == c.getExpectedResult()) {
				mod.addRow(new String[] {String.valueOf(c.getTestCaseID()), c.getTestRequirement().getDescriptors().toString(), c.getTableNames().toString(), "PASS"});
			} else {
				mod.addRow(new String[] {String.valueOf(c.getTestCaseID()), c.getTestRequirement().getDescriptors().toString(), c.getTableNames().toString(), "FAIL"});
			}
		}
		caseTable.setRowHeight(30);
		testCases.add(new JScrollPane(caseTable));
		caseTable.getSelectionModel().addListSelectionListener(new SelectListener());
		setPreferredWidth(caseTable);
		return testCases;
	}
	
	/**
	   * ListSelectionListener for the main test case table
	   * Adjusts the currently selected test case
	   */
	class SelectListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
            setSelectedCase(caseTable.getSelectedRow());
		}
	}

	/**
	   * VisCase Creation Method 
	   * This method takes a SchemaAnalyst TestSuite and returns
	   * a list of VisCase objects containing needed VisTable objects
	   * @param ts This is the SchemaAnalyst TestSuite object
	   * @return List<VisCase> This method returns a list of VisCase objects
	   */
	private List<VisCase> createVisCases(TestSuite ts) {
		List<VisCase> visCaseList = new ArrayList<VisCase>();
		int id = 0;
		//For each test case
		for(TestCase currentCase: ts.getTestCases()) {
			List<VisTable> tableList = new ArrayList<VisTable>();
			List<String> createdTables = new ArrayList<String>();
			//Create VisTables that contain pre-test state data
			for (Table t : currentCase.getState().getTables()) {
				Data state = currentCase.getState();
				List<Row> stateRows = state.getRows(t);
				List<Row> caseRows = new ArrayList<Row>();
				//Create a VisTable containing the needed test state rows
				tableList.add(new VisTable(t.getName(), t.getColumns(), stateRows, caseRows));
				createdTables.add(t.getName());
			}
			//Now either append case rows for created VisTables
			//or create a new VisTable if not created
			for (Table t : currentCase.getData().getTables()) {
				Data caseData = currentCase.getData(); 
				List<Row> stateRows = new ArrayList<Row>();
				List<Row> caseRows = caseData.getRows(t);
				if(!createdTables.contains(t.getName())) {
					//Content VisTable constructor used
					tableList.add(new VisTable(t.getName(), t.getColumns(), stateRows, caseRows));
					createdTables.add(t.getName());
				} else {
					for(int i=0; i< tableList.size(); i++) {
						if (tableList.get(i).getTableName().equals(t.getName())) {
							tableList.get(i).addCaseRows(caseRows);
						} 
					}
				}
			}
			visCaseList.add(new VisCase(id, tableList, currentCase));
			id++;
		}
		return visCaseList;
	}
	
	/**
	   * Structure VisTable Creation Method
	   * This method takes a SchemaAnalyst Schema and returns
	   * a list of VisTable objects using the structure table constructor
	   * @param schema This is the SchemaAnalyst Schema object
	   * @return List<VisTable> This method returns a list of VisTable objects
	   */
	private List<VisTable> createStructureTables(Schema schema) {
			List<Table> tables =  schema.getTables();
			List<VisTable> tableList = new ArrayList<VisTable>();
			//for each table, build up column to list of constraints map
			for (Table tab: tables) {
				HashMap<Column, List<Constraint>> consMap = new HashMap<Column, List<Constraint>>();
				for(Column c : tab.getColumns()) {
					List<Constraint> cons = new ArrayList<Constraint>();
					consMap.put(c, cons);
				}
				List<CheckConstraint> checks = new ArrayList<CheckConstraint>();
				if (schema.getCheckConstraints(tab) != null) {
					if (schema.getCheckConstraints(tab).size()>0) {
						checks = schema.getCheckConstraints(tab);
					}
				}
				if (schema.getPrimaryKeyConstraint(tab) != null) {
					for (Column pk: schema.getPrimaryKeyConstraint(tab).getColumns()) {
						List<Constraint> newList = consMap.get(pk);
						newList.add(schema.getPrimaryKeyConstraint(tab));
						consMap.put(pk, newList);
					}
				}
				if (schema.getForeignKeyConstraints(tab) != null) {
					for (ForeignKeyConstraint fk: schema.getForeignKeyConstraints(tab)) {
						for (Column c: fk.getColumns()) {
							List<Constraint> newList = consMap.get(c);
							newList.add(fk);
							consMap.put(c, newList);
						}
					}
				}
				if (schema.getNotNullConstraints(tab) != null) {
					for(NotNullConstraint nn : schema.getNotNullConstraints(tab)) {
						List<Constraint> newList = consMap.get(nn.getColumn());
						newList.add(nn);
						consMap.put(nn.getColumn(), newList);
					}
				}
				if (schema.getUniqueConstraints(tab) != null) {
					for (UniqueConstraint unique: schema.getUniqueConstraints(tab)) {
						for (Column c : unique.getColumns()) {
							List<Constraint> newList = consMap.get(c);
							newList.add(unique);
							consMap.put(c, newList);
						}
					}
				}
				//Create VisTable objects using the structure constructor
				//Store constraints and checks alongside table name and columns
				tableList.add(new VisTable(tab.getName(), tab.getColumns(), consMap, checks));
		}
		return tableList;
	}
	
	/**
	   * Schema Inspection page method
	   * This method accesses the list of structure VisTable objects
	   * Add table names to a JTable, add JTable to JPanel and return
	   * @return JPanel This is the JPanel containing the JTable of table names
	   */
	public JPanel tableListPanel() {
		JPanel tableList = new JPanel();
		tableList.setLayout(new BoxLayout(tableList, BoxLayout.PAGE_AXIS));
		//Create non-editable JTable
		nameList = new JTable(new DefaultTableModel(new Object[] {"Table Name"},1) {
		    @Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		});
		nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultTableModel mod = (DefaultTableModel) nameList.getModel();
		mod.setRowCount(0);
		//Add the rows containing the table names to the JTable
		for (VisTable tabs : structureTables) {
			mod.addRow(new String[] {tabs.getTableName()});
		}
		//Add ListSelectionListener to listen for row selection
		nameList.getSelectionModel().addListSelectionListener(new NameListener());
		nameList.setRowHeight(30);
		tableList.add(new JScrollPane(nameList));
		return tableList;
	}
	
	/**
	   * Schema Inspection page Listener
	   * ListSelectionListener for listening to the JTable containing table names
	   */
	class NameListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			for (VisTable t : structureTables) {
				if(t.getTableName().equals(nameList.getValueAt(nameList.getSelectedRow(), 0).toString())) {
					choice = t;
				}
			}
			setChosenTable(choice);
			updateColumnPanel();
		}
	}
	
	/**
	   * Schema Inspection page method
	   * Upon selection of a table, update the column list and SQL display panel
	   * Directly pass panels to the view to dynamically update page
	   */
	private void updateColumnPanel() {
		//Get a JPanel containing the JTable of column names and data types
		JPanel colPanel = createColumnTable(getChosenStructureTable());
		SQLWriter sql = new SQLWriter();
		//Get the SQL create table statement for the chosen table
		JTextArea sqlStatement = new JTextArea(sql.writeCreateTableStatement(schema, schema.getTable(getChosenStructureTable().getTableName())));
		sqlStatement.setFont(new Font(sqlStatement.getFont().getFontName(), Font.PLAIN, 13));
		JScrollPane sqlScroll = new JScrollPane();
		sqlScroll.getViewport().add(sqlStatement);
		sqlStatement.setEditable(false);
		JPanel tableSQLPanel = new JPanel(new BorderLayout());
		tableSQLPanel.add(sqlScroll, BorderLayout.CENTER);
		//Update the page using the view method
		view.updateColumnTab(colPanel, tableSQLPanel);
	}
	
	/**
	   * Schema Inspection page method
	   * This method takes a structure VisTable object as a parameter
	   * and populates a JTable with column names and data types
	   * @param tab This is the selected VisTable object
	   * @return JPanel A JPanel containing the JTable of column information is returned
	   */
	public JPanel createColumnTable(VisTable tab) {
		JPanel structureTables = new JPanel();
		structureTables.setLayout(new BoxLayout(structureTables, BoxLayout.PAGE_AXIS));
		//Non-editable JTable created
		colTable = new JTable(new DefaultTableModel(new Object[]{"Column Names", "Data Type"}, 2) {
		    @Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		});
		colTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultTableModel mod = (DefaultTableModel) colTable.getModel();
		mod.setRowCount(0);
		//Populate the JTable with rows containing column names and data types
		for (Column col : tab.getColumns()) {
			DataTypeSQLWriter sql = new DataTypeSQLWriter();
			mod.addRow(new String[]{col.getName(), sql.writeDataType(col)});
		}
		colTable.setRowHeight(30);
		structureTables.add(new JScrollPane(colTable));
		//Listen for user interaction
		colTable.getSelectionModel().addListSelectionListener(new StructureListener());
		setPreferredWidth(colTable);
		return structureTables;
	}

	class StructureListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource().equals(colTable.getSelectionModel())) {
				setChosenColumn(getChosenStructureTable().getColumn(colTable.getValueAt(colTable.getSelectedRow(), 0).toString()));
				JPanel colInfoPanel = columnInformationTab(getChosenStructureTable(), getChosenColumn());
				view.updateColumnInfo(colInfoPanel);
			}
		}
	}
	
	/**
	   * Schema Inspection page method
	   * This method takes a structure VisTable and the chosen column
	   * and populates a set of column information fields organised in a GroupLayout
	   * @param table This is the selected VisTable object
	   * @param chosenColumn This is the selected column inside that table
	   * @return JPanel A JPanel containing the set of column information fields
	   */
	public JPanel columnInformationTab(VisTable table, Column chosenColumn) {
		JPanel colInfo = new JPanel();
		GroupLayout layout = new GroupLayout(colInfo);
		colInfo.setLayout(layout);
		GroupLayout.SequentialGroup leftGroupH = layout.createSequentialGroup();
		
        // hLabelsGroup is horizontal group containing labels
        ParallelGroup leftLabelsH = layout.createParallelGroup(Alignment.TRAILING);
        leftGroupH.addGroup(leftLabelsH);
        
        // hFieldsGroup is horizontal group containing textfields
        ParallelGroup leftFieldsH = layout.createParallelGroup(Alignment.LEADING, false);
        leftGroupH.addGroup(leftFieldsH);
        GroupLayout.SequentialGroup leftGroupV = layout.createSequentialGroup();
        DataTypeSQLWriter sql = new DataTypeSQLWriter();
        String[] labels = new String[] { "Table Name ", "Column Name ", "Data Type "};
        String[] fields = new String[] {table.getTableName(), chosenColumn.getName(), sql.writeDataType(chosenColumn)};
        for (int i=0; i<labels.length; i++) {
        		JLabel label = new JLabel(labels[i]);
        		colInfo.add(label);
        		
            JTextField field = new JTextField(fields[i]);
            field.setEditable(false);
            colInfo.add(field);
            leftLabelsH.addComponent(label);
            leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE);
            leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
        }
        
        String conLabel = "Constraint ";
        String checkLabel = "Check Constraint ";
        int checkCount = 1;
        int conCount = 1;
        List<Constraint> constraints = table.getConstraints().get(chosenColumn);
        ConstraintSQLWriter conSQL = new ConstraintSQLWriter();
        for (CheckConstraint check : table.getCheckConstraints()) {
        		ExpressionSQLWriter sqlExpr = new ExpressionSQLWriter();
        		ValueSQLWriter sqlVal = new ValueSQLWriter();
        		conSQL.setExpressionSQLWriter(sqlExpr);
        		sqlExpr.setValueSQLWriter(sqlVal);
			if (check != null) {
	    			JLabel label = new JLabel(checkLabel+checkCount);
	        		colInfo.add(label);
	        		JTextField field = new JTextField(conSQL.writeCheck(check));
	        		field.setEditable(false);
	        		colInfo.add(field);
	        		leftLabelsH.addComponent(label);
                leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
	                        GroupLayout.PREFERRED_SIZE);
                leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
	        		checkCount++;
			}
		}	

        for (Constraint con : constraints) {
        		JLabel label = new JLabel(conLabel+conCount);
        		colInfo.add(label);
        		JTextField field = new JTextField(conSQL.writeConstraint(con));
        		field.setEditable(false);
        		colInfo.add(field);
        		leftLabelsH.addComponent(label);
            leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.PREFERRED_SIZE);
            leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
        		conCount++;
        }
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(leftGroupH));
        layout.setVerticalGroup(layout.createParallelGroup().addGroup(leftGroupV));
        
		return colInfo;
	}

	
	private HashMap<String, List<Integer>> passMap = new HashMap<String, List<Integer>>();
	private HashMap<String, List<Integer>> failMap = new HashMap<String, List<Integer>>();
	
	/**
	   * Test Visualisation class
	   * Custom DefaultTableRenderer for use in table visualisation
	   * Functions as standard apart from some further logic for deciding on background colour
	   */
	public class TestCaseRenderer extends DefaultTableCellRenderer {
		   public Component getTableCellRendererComponent(
		            JTable table, Object value, boolean isSelected,
		            boolean hasFocus, int row, int column)
		   {   
			   
			   
			  
		      //If the current table and row are in the pass map, highlight with pass colour
		      if(passMap.containsKey(table.getName())&&passMap.get(table.getName()).contains(row)) {
		    	  	setBackground(Color.green);
		    	  //Or if both the table and row are in the fail map, highlight with fail colour
		      } else if (failMap.containsKey(table.getName())&&failMap.get(table.getName()).contains(row)) {
		    	  	setBackground(Color.red);
		      } else {
		    	  	//Otherwise the row must be a state row, highlight with state colour
		    	  	setBackground(Color.yellow);
		      }
		    	  
		      return super.getTableCellRendererComponent(table, value, isSelected,
		                                                 hasFocus, row, column);
		   }
		}
	
	/**
	   * Test Visualisation method
	   * This method creates the table visualisation elements and passes them
	   * to the view
	   */
	private void visualiseTestCase() {
		JPanel visualiseTestCase = new JPanel();
		passMap.clear();
		failMap.clear();
		VisCase currentCase = visCases.get(getSelectedCase());
		List<VisTable> stateTables = currentCase.getTables();
		JPanel dbState = new JPanel();
		dbState.setLayout(new BoxLayout(dbState, BoxLayout.X_AXIS));
		int rowCount=0;
		TestCaseRenderer renderTC = new TestCaseRenderer();
		for (VisTable table : stateTables) {
			dbState.add(table.visTableToPanel(currentCase, rowCount, renderTC, passMap, failMap));
			rowCount=rowCount+table.getCaseRows().size();
		}
		visualiseTestCase.add(dbState);
		JPanel caseInfo = currentCase.testCaseInformation();
		view.drawStatePanels(visualiseTestCase, getSelectedCase(), caseInfo, tableKeyPanel(), visualiseOptionsPanel());
	}
	
	/**
	   * Create the options panel to be displayed on the test visualisation screen
	   * @return JPanel A JPanel containing the options buttons for use during test visualisation
	   */
	private JPanel visualiseOptionsPanel() {
		JPanel buttonPanel = new JPanel();
		JButton button = new JButton("Step Through Case");
		button.addActionListener(new ArrowListener());
		buttonPanel.add(button);
		button = new JButton("Return To Test Cases");
		button.addActionListener(new ArrowListener());
		buttonPanel.add(button);
		return buttonPanel;
	}
	
	/**
	   * This method creates the elements for the test case stepper screen and 
	   * passes them to the view for display
	   * @param targetSteps The target number of execution steps to be stepped to
	   */
	private void testCaseStepper(int targetSteps) {
		JPanel stepTestCase = new JPanel();
		VisCase chosenCase = visCases.get(getSelectedCase());
		
		JPanel dbState = new JPanel();
		dbState.setLayout(new BoxLayout(dbState, BoxLayout.X_AXIS));
		int tableCount=0;
		TestCaseRenderer renderTC = new TestCaseRenderer();
		if(targetSteps<=getMaxSteps()) {
			List<VisTable> stepTables = chosenCase.tablesForSteps(targetSteps);
			for (VisTable table : stepTables) {
				dbState.add(table.visTableToPanel(chosenCase, tableCount, renderTC, passMap, failMap));
				tableCount++;
			}
			stepTestCase.add(dbState);
			JPanel buttonPanel = arrowButtons();
			JPanel stepInfo = chosenCase.getStepInfo(targetSteps, getMaxSteps());
			view.drawTestCaseStep(stepTestCase, getSelectedCase(), stepInfo, buttonPanel, tableKeyPanel());
		} else {
			//Prevent stepper from attempting to step beyond the maximum
			targetSteps=targetSteps-1;
		}
	}
	
	/**
	   * This method finds the maximum number of execution steps possible for a set of 
	   * VisTable objects and sets a private parameter to this value
	   * @param stateTables A list of VisTable objects that we want to step through
	   */
	private void setMaxSteps(List<VisTable> stateTables) {
		maximumStepper=0;
		for(VisTable tabs : stateTables) {
			maximumStepper++;
			maximumStepper = maximumStepper + tabs.getCaseSteps();
		}
	}
	
	/**
	   * Create the test stepping buttons for use on the test stepper screen
	   * @return int Returns the maxmimum possible steps for the current visualisation
	   */
	private int getMaxSteps() {
		return maximumStepper;
	}
	
	/**
	   * Create the test stepping buttons for use on the test stepper screen
	   * @return JPanel A JPanel containing the test stepping buttons 
	   */
	private JPanel arrowButtons() {
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JButton button = new JButton("Previous Step");
		button.addActionListener(new ArrowListener());
		buttonPanel.add(button, BorderLayout.WEST);
		button = new JButton("Reset Test");
		button.addActionListener(new ArrowListener());
		buttonPanel.add(button, BorderLayout.CENTER);
		button = new JButton("Next Step");
		button.addActionListener(new ArrowListener());
		buttonPanel.add(button, BorderLayout.EAST);
		button = new JButton("See Full Test Results");
		button.addActionListener(new ArrowListener());
		buttonPanel.add(button, BorderLayout.SOUTH);
		
		return buttonPanel;
	}
	
	/**
	   * ActionListener for the test case stepping function
	   * Update the current stepper target based on user interaction and recall
	   * test stepper with new target
	   * Further functionality added to deal with option selections from inside
	   * test visualisations
	   */
	class ArrowListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand()=="Next Step") {
				stepperTarget=stepperTarget+1;
				if(stepperTarget>getMaxSteps()) {
					stepperTarget=stepperTarget-1;
				}
				testCaseStepper(stepperTarget);
			} else if(e.getActionCommand()=="Reset Test") {
				stepperTarget=1;
				testCaseStepper(stepperTarget);
			} else if(e.getActionCommand()=="Previous Step") {
				stepperTarget=stepperTarget-1;
				if(stepperTarget<1) {
					stepperTarget=stepperTarget+1;
				}
				testCaseStepper(stepperTarget);
			} else if (e.getActionCommand()=="See Full Test Results") {
				visualiseTestCase();
			} else if (e.getActionCommand()=="Step Through Case") {
				passMap.clear();
				failMap.clear();
				view.setVisScroll(0);
				List<VisTable> stateTables = visCases.get(getSelectedCase()).getTables();
				setMaxSteps(stateTables);
				stepperTarget=1;
				testCaseStepper(stepperTarget);
			} else if (e.getActionCommand()=="Return To Test Cases") {
				view.returnToCases();
			}
		}
	}
	
	/**
	   * Create the options panel to be displayed on the main test case list screen
	   * @return JPanel A JPanel containing the options buttons for use on the main test list screen
	   */
	public JPanel buttonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new JLabel("Options: "));
		JButton button = new JButton("Visualise Test");
		button.addActionListener(new ButtonListener());
		buttonPanel.add(button);
		button = new JButton("Step-By-Step");
		button.addActionListener(new ButtonListener());
		buttonPanel.add(button);
		
		return buttonPanel;
	}
	
	/**
	   * ActionListener for the main test case table page
	   * Execute test visualisation/stepping based on the user's selected action
	   */
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand()=="Visualise Test") {
				visualiseTestCase();
			}
			if(e.getActionCommand()=="Step-By-Step") {
				passMap.clear();
				failMap.clear();
				view.setVisScroll(0);
				List<VisTable> stateTables = visCases.get(getSelectedCase()).getTables();
				setMaxSteps(stateTables);
				stepperTarget=1;
				testCaseStepper(stepperTarget);
			}
		}
	}
	
	/**
	   * This method takes a JTable as a parameter and adjusts the column widths 
	   * to make for the best reading experience
	   * @param table The populated JTable that we want column width adjusting
	   */
	public void setPreferredWidth(JTable table) {
		for (int column = 0; column < table.getColumnCount(); column++) {
		    TableColumn tableColumn = table.getColumnModel().getColumn(column);
		    int preferredWidth = tableColumn.getMinWidth();
		    int maxWidth = tableColumn.getMaxWidth();
		    for (int row = 0; row < table.getRowCount(); row++) {
		        TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		        Component c = table.prepareRenderer(cellRenderer, row, column);
		        int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
		        preferredWidth = Math.max(preferredWidth, width);
		        if (preferredWidth >= maxWidth) {
		            preferredWidth = maxWidth;
		            break;
		        }
		    }
		 tableColumn.setPreferredWidth( preferredWidth );
		}
	}
	
	/**
	   * Create the coloured table key to be displayed on the test visualisation screen
	   * @return JPanel A JPanel the coloured table key displayed in a JTable
	   */
	private JPanel tableKeyPanel() {
		JPanel tableKey = new JPanel(new BorderLayout());
		JTable table = new JTable(new DefaultTableModel(new Object[] {"Pre-test State", "TC Added", "TC Rejected"}, 3) {
		    @Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		});
		table.setRowSelectionAllowed(false);
		DefaultTableModel mod = (DefaultTableModel) table.getModel();
		mod.addRow(new String[] {"", "", ""});
		TestCaseKeyRenderer renderTC = new TestCaseKeyRenderer();
		table.setDefaultRenderer(table.getColumnClass(0), renderTC);
		tableKey.add(new JScrollPane(table), BorderLayout.CENTER);
		return tableKey;
	}
	
	/**
	   * Test Visualisation class
	   * Custom DefaultTableRenderer used to display a coloured key 
	   * Functions as standard apart from has fixed colours for the different columns
	   * in the key
	   */
	class TestCaseKeyRenderer extends DefaultTableCellRenderer {
		   public Component getTableCellRendererComponent(
		            JTable table, Object value, boolean isSelected,
		            boolean hasFocus, int row, int column)
		   {   
		      if(column==0) {
		    	  	setBackground(Color.yellow);
		      } else if(column==1) {
		    	  	setBackground(Color.green);
		      } else if(column==2) {
		    	  	setBackground(Color.red);
		      }
		      return super.getTableCellRendererComponent(table, value, isSelected,
		                                                 hasFocus, row, column);
		   }
		}

}
