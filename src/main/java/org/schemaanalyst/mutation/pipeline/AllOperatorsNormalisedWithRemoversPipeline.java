package org.schemaanalyst.mutation.pipeline;

import org.schemaanalyst.mutation.operator.*;
import org.schemaanalyst.mutation.quasimutant.HyperSQLRemover;
import org.schemaanalyst.mutation.quasimutant.PostgresRemover;
import org.schemaanalyst.mutation.quasimutant.SQLiteRemover;
import org.schemaanalyst.mutation.redundancy.EquivalentMutantRemover;
import org.schemaanalyst.mutation.redundancy.RedundantMutantRemover;
import org.schemaanalyst.sqlrepresentation.Schema;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.schemaanalyst.dbms.hypersql.HyperSQLSchemaNormaliser;
import org.schemaanalyst.dbms.postgres.PostgresSchemaNormaliser;
import org.schemaanalyst.dbms.sqlite.SQLiteSchemaNormaliser;
import org.schemaanalyst.mutation.equivalence.SchemaEquivalenceCheckerWithNormalisation;

/**
 * A mutation pipeline with all operators and removers for equivalent and 
 * redundant mutants, with schema normalisation. This has the following 
 * properties:
 * <p><ul>
 * <li> Includes all mutation operators
 * <li> Includes equivalence/redundancy detection, using schema normalisation
 * <li> Equivalent/redundant mutants are removed
 * <li> Stillborn mutants are removed
 * <li> Impaired mutants are removed
 * </ul><p>
 * 
 * @author Chris J. Wright
 */
public class AllOperatorsNormalisedWithRemoversPipeline extends MutationPipeline<Schema> {

    private static final Logger LOGGER = Logger.getLogger(AllOperatorsNormalisedWithRemoversPipeline.class.getName());
    private final Schema schema;

    public AllOperatorsNormalisedWithRemoversPipeline(Schema schema) {
        this.schema = schema;
        
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
    }

    public void addDBMSSpecificRemovers(String dbms) {
        switch (dbms) {
            case "Postgres":
                addRemoverToFront(new PostgresRemover());
                addRemover(new EquivalentMutantRemover<>(new SchemaEquivalenceCheckerWithNormalisation(new PostgresSchemaNormaliser()), schema));
                addRemover(new RedundantMutantRemover<>(new SchemaEquivalenceCheckerWithNormalisation(new PostgresSchemaNormaliser())));
                break;
            case "SQLite":
                addRemoverToFront(new SQLiteRemover());
                addRemover(new EquivalentMutantRemover<>(new SchemaEquivalenceCheckerWithNormalisation(new SQLiteSchemaNormaliser()), schema));
                addRemover(new RedundantMutantRemover<>(new SchemaEquivalenceCheckerWithNormalisation(new SQLiteSchemaNormaliser())));
                break;
            case "HyperSQL":
                addRemoverToFront(new HyperSQLRemover());
                addRemover(new EquivalentMutantRemover<>(new SchemaEquivalenceCheckerWithNormalisation(new HyperSQLSchemaNormaliser()), schema));
                addRemover(new RedundantMutantRemover<>(new SchemaEquivalenceCheckerWithNormalisation(new HyperSQLSchemaNormaliser())));
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown DBMS name in pipeline");
        }
    }

}
