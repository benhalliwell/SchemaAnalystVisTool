
package org.schemaanalyst.mutation.pipeline;

import org.schemaanalyst.mutation.equivalence.SchemaEquivalenceChecker;
import org.schemaanalyst.mutation.operator.*;
import org.schemaanalyst.mutation.quasimutant.DBMSRemover;
import org.schemaanalyst.mutation.redundancy.EquivalentMutantRemover;
import org.schemaanalyst.mutation.redundancy.PrimaryKeyColumnNotNullRemover;
import org.schemaanalyst.mutation.redundancy.PrimaryKeyUniqueOverlapConstraintRemover;
import org.schemaanalyst.mutation.redundancy.RedundantMutantRemover;
import org.schemaanalyst.sqlrepresentation.Schema;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mutation pipeline with all operators and classifiers for equivalent and 
 * redundant mutants, with schema modification, using the DBMS to identify 
 * quasi-mutants. This has the following properties:
 * <p><ul>
 * <li> Includes all mutation operators
 * <li> Includes equivalence/redundancy detection, using schema modification
 * <li> Equivalent/redundant mutants are removed
 * <li> Stillborn mutants are removed
 * <li> Impaired mutants are retained
 * </ul><p>
 * 
 * @author Chris J. Wright
 */
public class QSICDBMSRemoversPipeline extends MutationPipeline<Schema> {

    private static final Logger LOGGER = Logger.getLogger(AllOperatorsWithRemoversPipeline.class.getName());

    public QSICDBMSRemoversPipeline(Schema schema) {
        addProducer(new CCNullifier(schema));
        addProducer(new CCInExpressionRHSListExpressionElementR(schema));
        addProducer(new CCRelationalExpressionOperatorE(schema));
        addProducer(new FKCColumnPairA(schema));
        addProducer(new FKCColumnPairR(schema));
        addProducer(new FKCColumnPairE(schema));
        // Implement FKCColumnE ('one sided')?
        addProducer(new PKCColumnA(schema));
        addProducer(new PKCColumnR(schema));
        addProducer(new PKCColumnE(schema));
        addProducer(new NNCA(schema));
        addProducer(new NNCR(schema));
        addProducer(new UCColumnA(schema));
        addProducer(new UCColumnR(schema));
        addProducer(new UCColumnE(schema));

        addRemover(new EquivalentMutantRemover<>(new SchemaEquivalenceChecker(), schema));
        addRemover(new RedundantMutantRemover<>(new SchemaEquivalenceChecker()));
    }

    public void addDBMSSpecificRemovers(String dbms) {
        switch (dbms) {
            case "Postgres":
                addRemoverToFront(new PrimaryKeyUniqueOverlapConstraintRemover());
                addRemoverToFront(new DBMSRemover());
                addRemoverToFront(new PrimaryKeyColumnNotNullRemover());
                break;
            case "SQLite":
                addRemoverToFront(new PrimaryKeyUniqueOverlapConstraintRemover(true));
                addRemoverToFront(new DBMSRemover());
                break;
            case "HyperSQL":
                addRemoverToFront(new DBMSRemover());
                addRemoverToFront(new PrimaryKeyColumnNotNullRemover());
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown DBMS name in pipeline");
        }
    }
}
