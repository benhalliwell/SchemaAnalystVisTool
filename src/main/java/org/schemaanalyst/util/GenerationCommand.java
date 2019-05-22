/**
 * Front-end for generating test data generation with SchemaAnalyst
 * @author Cody Kinneer
 */
package org.schemaanalyst.util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Generate test data with SchemaAnalyst")
public class GenerationCommand {

  @Parameter(names = {"--sql","--inserts"}, description = "Enable writing INSERT statements")
  protected boolean sql = false;

  @Parameter(names = {"--testSuitePackage","-p"}, description = "Target package for writing JUnit test suite")
  protected String testSuitePackage = "generatedtest";

  // should default to Test + schema + .java
  @Parameter(names = {"--testSuite","-t"}, description = "Target file for writing JUnit test suite")
  protected String testSuite = "TestSchema";
  
  // select to boot visualisation tool
  @Parameter(names = {"--visualise", "-v"}, description = "Visualise generated test data using visualisation tool")
  protected boolean visualise = false;
}
