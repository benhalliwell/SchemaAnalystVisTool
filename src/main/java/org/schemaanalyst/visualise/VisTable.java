package org.schemaanalyst.visualise;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.schemaanalyst.data.Cell;
import org.schemaanalyst.data.Row;
import org.schemaanalyst.sqlrepresentation.Column;
import org.schemaanalyst.sqlrepresentation.constraint.CheckConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.Constraint;
import org.schemaanalyst.visualise.Visualise.TestCaseRenderer;
/**
 * Database Test Visualisation Tool
 * Model Code
 * @author Ben Halliwell
 */
public class VisTable {
	
	private String tableName;
	private List<Column> columns;
	private List<Row> testStateRows;
	private List<Row> testCaseRows;

	//constructor for data containing VisTables
	public VisTable(String name, List<Column> cols, List<Row> stateRows, List<Row> caseRows) {
		tableName = name;
		columns = cols;
		testStateRows = stateRows;
		testCaseRows = caseRows;
	}
	
	private List<CheckConstraint> checkCons;
	private HashMap<Column, List<Constraint>> constraints;
	
	//constructor for structure information VisTables
	public VisTable(String name, List<Column> cols, HashMap<Column, List<Constraint>> cons, 
			List<CheckConstraint> checks) {
		tableName = name;
		columns = cols;
		constraints = cons;
		checkCons = checks;
	}
	
	//accessor and data addition methods
	public String getTableName() {
		return tableName;
	}
	
	public List<String> getColumnNames() {
		List<String> columnNames = new ArrayList<String>();
		for (Column c: columns) {
			columnNames.add(c.getName());
		}
		return columnNames;
	}
	
	public Column getColumn(String columnName) {
		for (Column col : columns) {
			if (col.getName().equals(columnName)) {
				return col;
			}
		}
		return null;
	}
	
	public List<Row> getStateRows() {
		return testStateRows;
	}
	
	public List<Row> getCaseRows() {
		return testCaseRows;
	}
	
	public List<Column> getColumns(){
		return columns;
	}
	
	public HashMap<Column, List<Constraint>> getConstraints() {
		return constraints;
	}
	
	public List<CheckConstraint> getCheckConstraints() {
		return checkCons;
	}
	
	public void addStateData(List<Row> rowData) {
		for (Row r : rowData) {
			testStateRows.add(r);
		}
	}
	
	public void addStateRow(Row rowData) {
		testStateRows.add(rowData);
	}
	
	public void addCaseRows(List<Row> rowData) {
		for (Row r : rowData) {
			testCaseRows.add(r);
		}
	}
	
	public void addCaseRow(Row rowData) {
		testCaseRows.add(rowData);
	}
	
	public int getCaseSteps() {
		int maximumStepper=0;
		maximumStepper=maximumStepper+getStateRows().size();
		maximumStepper=maximumStepper+getCaseRows().size();
		return maximumStepper;
	}
	
	/**
	   * This method takes a JTable as a parameter and resizes the contained 
	   * columns according to the length of the string column header
	   * @param table This is the table that needs adjusting
	   * @return int The total width of the table is also returned
	   */
	public int resizeColumnWidth(JTable table) {
		int totalWidth = 0;
	    final TableColumnModel columnModel = table.getColumnModel();
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        int width = 15; // Min width
	        TableColumn tc = columnModel.getColumn(column);
	        if(tc.getHeaderValue().toString().length()<3) {
	        		width = table.getFontMetrics(new Font(table.getFont().getFontName(), Font.PLAIN, 13)).stringWidth(tc.getHeaderValue().toString())+40;
	        } else {
	        		width = table.getFontMetrics(new Font(table.getFont().getFontName(), Font.PLAIN, 13)).stringWidth(tc.getHeaderValue().toString())+25;
	        }
	        if(width > 300)
	            width=300;
	        tc.setPreferredWidth(width);
	        totalWidth = totalWidth + width;
	    }
	    return totalWidth;
	}
	
	/**
	   * This method takes a number of parameters to build up a JTable
	   * containing the needed state and case rows, adding to a panel with 
	   * a JLabel and returning
	   * @param chosenCase This is the chosen VisCase, giving access to results of the test
	   * @param rowCount This is the count of tables for this VisCase, used for multi-result tests
	   * @param renderTC This is my custom TableCellRenderer for row highlighting
	   * @param passMap This is the map of passing test case rows
	   * @param failMap This is the map of failing test case rows
	   * @return JPanel This method returns a JPanel containing a labelled JTable with row highlighting
	   */
	public JPanel visTableToPanel(VisCase chosenCase, int rowCount, TestCaseRenderer renderTC, HashMap<String, List<Integer>> passMap, HashMap<String, List<Integer>> failMap) {
		JPanel tablePan = new JPanel(new BorderLayout());
		//JTable is declared, overridden method to stop a user from editing the content
		JTable table = new JTable(new DefaultTableModel(this.getColumnNames().toArray(), this.getColumnNames().size()) {
			@Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		});
		table.setName(this.getTableName());
		table.setRowSelectionAllowed(false);
		//Use custom TableCellRenderer to add row highlighting
		table.setDefaultRenderer(table.getColumnClass(0), renderTC);
		DefaultTableModel mod = (DefaultTableModel) table.getModel();
		mod.setRowCount(0);		
		//Add pre-test state rows
		for (Row r: this.getStateRows()) {
			String[] values = new String[this.getColumnNames().size()];
			List<Cell> cells = r.getCells();
			for(int i=0; i<this.getColumnNames().size(); i++) {
				if(cells.get(i).getValue() == null) {
					//Ensure null values are displayed using the string 'NULL'
					values[i] = "NULL";
				} else {
					values[i] = cells.get(i).getValue().toString();
				}
			}
			mod.addRow(values);
		}  
		//Add test case rows
		List<Row> caseRows = this.getCaseRows();
		for (int i=0; i<this.getCaseRows().size(); i++) {
			String[] values = new String[this.getColumnNames().size()];
			List<Cell> cells = caseRows.get(i).getCells();
			for(int j=0; j<this.getColumnNames().size(); j++) {
				if(cells.get(j).getValue() == null) {
					values[j] = "NULL";
				} else {
					values[j] = cells.get(j).getValue().toString();
				}
				
			}
			mod.addRow(values);
			//Populate maps with passing and failing case rows for row highlighting
			List<Boolean> results = chosenCase.getTestCaseObject().getDBMSResults();
			Boolean resultCheck;
			//Check if multi-result test, if so ensure we are checking the correct result
			if(results.size()>1) {
				resultCheck = results.get(rowCount);
			} else {
				resultCheck = results.get(0);
			}
			//If row is passing then either create entry in passMap or add to existing entry
			if (resultCheck == true) {
				List<Integer> rowList = new ArrayList<Integer>();
				if (passMap.containsKey(table.getName())) {
					rowList = passMap.get(table.getName());
					rowList.add(mod.getRowCount()-1);
				} else {
					rowList.add(mod.getRowCount()-1);
				}
				passMap.put(table.getName(), rowList);
			//If row is failing then either create entry in passMap or add to existing entry
			} else if (resultCheck == false) {
				List<Integer> rowList = new ArrayList<Integer>();
				if (failMap.containsKey(table.getName())) {
					rowList = failMap.get(table.getName());
					rowList.add(mod.getRowCount()-1);
				} else {
					rowList.add(mod.getRowCount()-1);
				}
				failMap.put(table.getName(), rowList);
			}
			rowCount++;
		}
		table.setRowHeight(30);
		int maxWidth = resizeColumnWidth(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tablePan.add(new JLabel(table.getName() + " table"), BorderLayout.NORTH);
		JScrollPane tableScroll= new JScrollPane();
		table.setPreferredSize(new Dimension(maxWidth, 400));
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		tableScroll.getViewport().add(table);
		tablePan.add(tableScroll, BorderLayout.SOUTH);
		return tablePan;
	}
}