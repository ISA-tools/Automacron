package org.isatools.macros.loaders.isa;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 09/05/2012
 *         Time: 17:42
 */
public enum ISAWorkflowRelationships implements RelationshipType {

    DERIVES, PROPERTY_OF, SAMPLE_FOR, TRANSFORMED_BY, EXPERIMENT_OF

}
