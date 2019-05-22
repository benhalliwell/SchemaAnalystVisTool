package org.schemaanalyst.visualise;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;

import org.schemaanalyst.data.Row;
import org.schemaanalyst.sqlrepresentation.Table;
import org.schemaanalyst.testgeneration.TestCase;
import org.schemaanalyst.testgeneration.coveragecriterion.TestRequirement;
/**
 * Database Test Visualisation Tool
 * Model Code
 * @author Ben Halliwell
 */
public class VisCase {
	
	private int testCaseID;
	private List<VisTable> caseTables;
	private TestCase testCaseObject;
	
	//constructor
	public VisCase(int id, List<VisTable> tables, TestCase testCase) {
		testCaseID = id;
		caseTables = tables;
		testCaseObject = testCase;
	}

	//accessors 
	protected int getTestCaseID() {
		return testCaseID;
	}
	
	protected List<VisTable> getTables() {
		return caseTables;
	}
	
	protected List<String> getTableNames() {
		List<String> tabNames = new ArrayList<String>();
		for (VisTable table : caseTables) {
			tabNames.add(table.getTableName());
		}
		return tabNames;
	}
	
	protected TestCase getTestCaseObject() {
		return testCaseObject;
	}
	
	protected TestRequirement getTestRequirement() {
		return testCaseObject.getTestRequirement();
	}
	
	protected Boolean getExpectedResult() {
		return testCaseObject.getTestRequirement().getResult();
	}
	
	protected List<Boolean> getResults() {
		return testCaseObject.getDBMSResults();
	}
	
	/**
	   * Test Case Stepping Method 
	   * This method takes a target number of steps and
	   * creates a set of VisTable objects that display the target
	   * number of execution steps
	   * @param targetSteps This is the target number of steps
	   * @return List<VisTable> This method returns a list of VisTable objects
	   */
	protected List<VisTable> tablesForSteps(int targetSteps) {
		List<VisTable> tables = this.getTables();
		List<VisTable> stepTables = new ArrayList<VisTable>();
		int currentStep = 0;
		for (VisTable tab : tables) {
			stepTables.add(new VisTable(tab.getTableName(), tab.getColumns(), new ArrayList<Row>(), new ArrayList<Row>()));
			currentStep++;
			if (currentStep==targetSteps) {
				setStepInfo(new String[] {"Table creation", "No data has been added", "Create the " + tab.getTableName() + " table"});
				return stepTables;
			}
		}
		for (int i=0; i<tables.size(); i++) {
			List<Row> stateRows = tables.get(i).getStateRows();
			VisTable currentStepTable = stepTables.get(i);
			for (Row r : stateRows) {
				currentStepTable.addStateRow(r);
				currentStep++;
				if (currentStep==targetSteps) {
					setStepInfo(new String[] {"Add the database pre-test state.", r.getCells().toString(), "Row has been added to the " + currentStepTable.getTableName() + " table"});
					return stepTables;
				}
			}
		}
		for (int i=0; i<tables.size(); i++) {
			List<Row> caseRows = tables.get(i).getCaseRows();
			VisTable currentStepTable = stepTables.get(i);
			List<Boolean> result = this.getTestCaseObject().getDBMSResults();
			for (Row r : caseRows) {
				currentStepTable.addCaseRow(r);
				currentStep++;
				if (currentStep==targetSteps) {
					if (result.size()>1) {
						if (result.get(i) == true) {
							setStepInfo(new String[] {"Add the database test case row.", r.getCells().toString(), "Row has been added to the " +
									currentStepTable.getTableName() + " table"});
						} else if(result.get(i)== false) {
							setStepInfo(new String[] {"Add the database test case row.", r.getCells().toString(), "Row was rejected when attempting to INSERT into the " +
									currentStepTable.getTableName() + " table"});
						}
					} else {
						if (result.get(0) == true) {
							setStepInfo(new String[] {"Add the database test case row.", r.getCells().toString(), "Row has been added to the " +
									currentStepTable.getTableName() + " table"});
						} else if(result.get(0)== false) {
							setStepInfo(new String[] {"Add the database test case row.", r.getCells().toString(), "Row was rejected when attempting to INSERT into the " +
									currentStepTable.getTableName() + " table"});
						}
					}
					return stepTables;
				}
			}
		}
		return stepTables;
	}
	
	/**
	   * Table Visualisation Method
	   * Displays the test case information for the current VisCase object
	   * @return JPanel This method returns a panel containing VisCase information
	   */
	protected JPanel testCaseInformation() {
		JPanel caseInfo = new JPanel();
		TestCase chosenCase = this.getTestCaseObject();
		GroupLayout layout = new GroupLayout(caseInfo);
		caseInfo.setLayout(layout);
		GroupLayout.SequentialGroup leftGroupH = layout.createSequentialGroup();
        //Horizontal group containing labels
        ParallelGroup leftLabelsH = layout.createParallelGroup(Alignment.TRAILING);
        leftGroupH.addGroup(leftLabelsH);        
        //Horizontal group containing textfields
        ParallelGroup leftFieldsH = layout.createParallelGroup(Alignment.LEADING, false);
        leftGroupH.addGroup(leftFieldsH);      
        GroupLayout.SequentialGroup leftGroupV = layout.createSequentialGroup();
         
        Boolean result = chosenCase.getTestRequirement().getResult();
        Boolean dbmsResult = chosenCase.getLastDBMSResult();
        
        String[] labels = new String[] { "Test Case ID ", "Tables Involved ", "Test Description ", "Test Predicate ", "Test Data ", "Expected Result ", "Actual Result ", "Test Requirement Result"};
        String[] fields = new String[] {String.valueOf(this.getTestCaseID()), chosenCase.getData().getTables().toString(), chosenCase.getTestRequirement().getDescriptors().toString(), 
        		chosenCase.getTestRequirement().getPredicate().toString(), chosenCase.getData().toString(),  result.toString(), dbmsResult.toString(), String.valueOf(result != null && result != dbmsResult)};
        for (int i=0; i<labels.length; i++) {
        		JLabel label = new JLabel(labels[i]);
        		String fieldContent = fields[i];
        		Color backgroundColour = Color.WHITE;
        		fieldContent = fieldContent.replace("[", "");
        		fieldContent = fieldContent.replace("]", "");
            caseInfo.add(label);
            if (i==4) {
		    		int caseCount = 1;
		    		for (Table tab: chosenCase.getData().getTables()) {
		    			for (Row row: chosenCase.getData().getRows(tab)) {
		    				label = new JLabel(labels[i] + String.valueOf(caseCount));
		        			caseInfo.add(label);
		                JTextField field = new JTextField(row.toString());
		                field.setEditable(false);
		                caseInfo.add(field);
		                leftLabelsH.addComponent(label);
		                leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
		                        GroupLayout.PREFERRED_SIZE);
		                leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
		                caseCount++;
		    			}
		    		}
    			} else if (i==5 || i==6) {
	            	if (result.toString().equals(dbmsResult.toString())) {
	            		backgroundColour = Color.GREEN;
	            } else {
	            		backgroundColour = Color.RED;
	            }
            		fieldContent = fieldContent.replace("true", "PASS");
            		fieldContent = fieldContent.replace("false", "FAIL");
            } else if (i==7 && ((result != null && result != dbmsResult)==true)) {
            		fieldContent = fieldContent.replaceAll("true", ("WARNING--test requirement result (" + result + ") differs from DBMS result (" + dbmsResult + "):"));
            		backgroundColour = Color.RED;
            } else if (i==7 && ((result != null && result != dbmsResult)==false)) {
            		fieldContent = fieldContent.replace("false", "TEST REQUIREMENT SATISFIED");
            		backgroundColour = Color.GREEN;
            }
            if (i!=4) {
	            JTextField field = new JTextField(fieldContent);
	            field.setBackground(backgroundColour);
	            field.setEditable(false);
	            caseInfo.add(field);
	            leftLabelsH.addComponent(label);
	            leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
	                    GroupLayout.PREFERRED_SIZE);
	            leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
            }
        }
        
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(leftGroupH));
        layout.setVerticalGroup(layout.createParallelGroup().addGroup(leftGroupV));
        
		return caseInfo;
	}
	
	private String[] currentStepNotification = new String[3];
	
	/**
	   * Test Case Stepping Method 
	   * This method sets the attribute containing the set of test
	   * information strings
	   */
	public void setStepInfo(String[] message) {
		currentStepNotification = message;
	}
	
	/**
	   * Test Case Stepping Method 
	   * This method creates a JPanel containing information about the current 
	   * test execution step using the currentStepNotification attribute
	   * @param targetSteps This is the target number of steps
	   * @param maxStep This is the maximum number of steps for this VisCase
	   * @return JPanel This method returns a panel containing VisCase information
	   */
	protected JPanel getStepInfo(int targetStep, int maxStep) {
		JPanel stepInfo = new JPanel();
		GroupLayout layout = new GroupLayout(stepInfo);
		stepInfo.setLayout(layout);
		GroupLayout.SequentialGroup leftGroupH = layout.createSequentialGroup();
        //Horizontal group containing labels
        ParallelGroup leftLabelsH = layout.createParallelGroup(Alignment.TRAILING);
        leftGroupH.addGroup(leftLabelsH);
        //Horizontal group containing fields
        ParallelGroup leftFieldsH = layout.createParallelGroup(Alignment.LEADING, false);
        leftGroupH.addGroup(leftFieldsH);
       
        GroupLayout.SequentialGroup leftGroupV = layout.createSequentialGroup();
        String[] labels = new String[] { "Current Step: ", "Row Data: ", "Action: "};
        for (int i=0; i<currentStepNotification.length; i++) {
        		JLabel label = new JLabel(labels[i]);
        		stepInfo.add(label);
            JTextField field = new JTextField(currentStepNotification[i]);
            field.setEditable(false);
            stepInfo.add(field);
            leftLabelsH.addComponent(label);
            leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE);
            leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
        }
        
        if (targetStep == maxStep) {
	        	JLabel label = new JLabel("Execution: ");
	    		stepInfo.add(label);
	        JTextField field = new JTextField("Test execution has completed");
	        field.setEditable(false);
	        field.setBackground(Color.green);
	        stepInfo.add(field);
	        leftLabelsH.addComponent(label);
	        leftFieldsH.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
	                GroupLayout.PREFERRED_SIZE);
	        leftGroupV.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(field));
        }
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(leftGroupH));
        layout.setVerticalGroup(layout.createParallelGroup().addGroup(leftGroupV));
		return stepInfo;
	}

}
