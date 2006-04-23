/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.service.archetype.query;

// java-core
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

// commons-lang
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.query.QueryContext.LogicalOperator;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeConstraint;
import org.openvpms.component.system.common.query.ArchetypeIdConstraint;
import org.openvpms.component.system.common.query.ArchetypeLongNameConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefArchetypeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.OrConstraint;
/**
 * Thie builder is responsible for building the HQL from an 
 * {@link ArchetypeQuery} instance.
 * 
 * NOTE Ths archetype service is dependent on HQL. We need to make this
 * runtime configurable.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class QueryBuilder {
    /**
     * The archetype service
     */
    private IArchetypeService service;
    
    
    /**
     * Create an instance of the builder
     * 
     * @param service
     *            a reference to the archetype service
     */
    public QueryBuilder(IArchetypeService service) {
        this.service = service;
    }

    /**     * Build the HQL from the specified {@link ArchetypeQuery}.
     * 
     * @param query 
     *            archetype service
     * @return QueryContext
     *            the build hql            
     */
    public QueryContext build(ArchetypeQuery query) {
        if ((query == null) ||
            (query.getArchetypeConstraint() == null)) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NullQuery);
        }
        
        QueryContext context = new QueryContext();
        processConstraint(query.getArchetypeConstraint(), context);
        
        return context;
    }
    
    
    /**
     * Process the {@link ArchetypeIdConstraint}
     * 
     * @param constraint
     *           the archetype id constrain to process
     * @param context
     *           the hql context           
     */
    private void process(ArchetypeIdConstraint constraint, QueryContext context) {
        
        context.pushLogicalOperator(LogicalOperator.And);
        context.pushDistinctTypes(getDistinctTypes(constraint));

        // process common portion of constraint
        processArchetypeConstraint(constraint, context);
        
        // pop the stack when we have finished processing this constraint
        context.popDistinctTypes();
        context.popLogicalOperator();
    }
    
    /**
     * Process the common portion of {@link ArchetypeIdConstraint}
     * 
     * @param constraint
     *           the archetype id constrain to process
     * @param joinType
     *           the join type (optional)            
     * @param context
     *           the hql context           
     */
    private void processArchetypeConstraint(ArchetypeIdConstraint constraint, QueryContext context) {
        ArchetypeId id = constraint.getArchetypeId();
        
        context.addWhereConstraint("archetypeId.rmName", RelationalOp.EQ,
                id.getRmName());
        context.addWhereConstraint("archetypeId.entityName", RelationalOp.EQ,
                id.getEntityName());
        context.addWhereConstraint("archetypeId.concept", RelationalOp.EQ,
                id.getConcept());
        
        // process the active flag
        if (constraint.isActiveOnly()) {
            context.addWhereConstraint("active", RelationalOp.EQ, new Boolean(true));
        }
        
        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
    }
    
    /**
     * Process the archetype short name constraint
     * 
     * @param constraint
     *           the archetype short name constraint
     * @param context
     *           the hql context           
     */
    private void process(ArchetypeShortNameConstraint constraint, QueryContext context) {
        // check that at least one short name is specified
        if ((constraint.getShortNames() == null) ||
            (constraint.getShortNames().length == 0)) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoShortNamesSpeified);
        }
        
        context.pushLogicalOperator(LogicalOperator.And);
        context.pushDistinctTypes(getDistinctTypes(constraint));
        
        // process the common portion of the archetype constraint
        processArchetypeConstraint(constraint, context);
        
        // pop the stack when we have finished processing this constraint
        context.popDistinctTypes();
        context.popLogicalOperator();
    }

    /**
     * Process the common portion of the archetype short name constraint
     * 
     * @param constraint
     *           the archetype short name constraint
     * @param context
     *           the hql context           
     */
    private void processArchetypeConstraint(ArchetypeShortNameConstraint constraint, 
            QueryContext context) {
        
        String[] shortNames = constraint.getShortNames();
        if (shortNames.length > 1) {
            context.pushLogicalOperator(LogicalOperator.Or);
        }
        
        for (int index = 0; index < shortNames.length; index++) {
            // derive the entityName and the shortName
            StringTokenizer tokens = new StringTokenizer(shortNames[index], ".");
            
            if (tokens.countTokens() != 2) {
                throw new QueryBuilderException(
                        QueryBuilderException.ErrorCode.InvalidShortName,
                        new Object[] {shortNames[index]});
            }
            
            String entityName = tokens.nextToken();
            String conceptName = tokens.nextToken();
            
            // push the logical and operator on the stack
            context.pushLogicalOperator(LogicalOperator.And);
            
            // process the entity name
            if ((entityName.endsWith("*")) || (entityName.startsWith("*"))) {
                context.addWhereConstraint("archetypeId.entityName", 
                        RelationalOp.EQ, entityName.replace("*", "%"));
            } else {
                context.addWhereConstraint("archetypeId.entityName", 
                        RelationalOp.EQ, entityName);
            }

            // process the concept name
            if ((conceptName.endsWith("*"))|| (conceptName.startsWith("*"))) {
                context.addWhereConstraint("archetypeId.concept", 
                        RelationalOp.EQ, conceptName.replace("*", "%"));
            } else {
                context.addWhereConstraint("archetypeId.concept", 
                        RelationalOp.EQ, conceptName);
            }
            
            // pop ir
            context.popLogicalOperator();
        }
    
        if (shortNames.length > 1) {
            context.popLogicalOperator();
        }
        
        // process the active flag
        if (constraint.isActiveOnly()) {
            context.addWhereConstraint("active", RelationalOp.EQ, new Boolean(true));
        }
        
        
        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
    }

    /**
     * Process an archetype constraint where the reference model name, entity name
     * and concept name are specified
     * 
     * @param constraint
     *            the constraint
     * @param context
     *           the hql context
     */
    private void process(ArchetypeLongNameConstraint constraint, QueryContext context) {
        String rmName = constraint.getRmName();
        String entityName = constraint.getEntityName();
        String conceptName = constraint.getConceptName();

        // check that not all of rmName, entityName and conceptName are empty
        if (StringUtils.isEmpty(rmName) &&
            StringUtils.isEmpty(entityName) &&
            StringUtils.isEmpty(conceptName)) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.InvalidLongNameSpecified,
                    new Object[] {rmName, entityName, conceptName});
        }
        
        context.pushLogicalOperator(LogicalOperator.And);
        context.pushDistinctTypes(getDistinctTypes(constraint));
        
        // process the common portion of the archetype constraint
        processArchetypeConstraint(constraint, context);
        
        // pop the stack when we have finished processing this constraint
        context.popDistinctTypes();
        context.popLogicalOperator();
    }

    /**
     * Process an common portion of this archetype constraint
     * 
     * @param constraint
     *            the constraint
     * @param context
     *           the hql context
     */
    private void processArchetypeConstraint(ArchetypeLongNameConstraint constraint, 
            QueryContext context) {
        String rmName = constraint.getRmName();
        String entityName = constraint.getEntityName();
        String conceptName = constraint.getConceptName();

        // process the rmName
        if (!StringUtils.isEmpty(rmName)) {
            if ((rmName.endsWith("*")) || rmName.startsWith("*")) {
                context.addWhereConstraint("archetypeId.rmName", 
                        RelationalOp.EQ, rmName.replace("*", "%"));
            } else {
                context.addWhereConstraint("archetypeId.rmName", 
                        RelationalOp.EQ, rmName);
            }
        }

        // process the entity name
        if (!StringUtils.isEmpty(entityName)) {
            if ((entityName.endsWith("*")) || (entityName.startsWith("*"))) {
                context.addWhereConstraint("archetypeId.entityName", 
                        RelationalOp.EQ, entityName.replace("*", "%"));
            } else {
                context.addWhereConstraint("archetypeId.entityName", 
                        RelationalOp.EQ, entityName);
            }
        }

        // process the concept name
        if (!StringUtils.isEmpty(conceptName)) {
            if ((conceptName.endsWith("*"))|| (conceptName.startsWith("*"))) {
                context.addWhereConstraint("archetypeId.concept", 
                        RelationalOp.EQ, conceptName.replace("*", "%"));
            } else {
                context.addWhereConstraint("archetypeId.concept", 
                        RelationalOp.EQ, conceptName);
            }
        }
        
        // process the active flag
        if (constraint.isActiveOnly()) {
            context.addWhereConstraint("active", RelationalOp.EQ, new Boolean(true));
        }
        
        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
        
    }

    /**
     * Process the embedded constraints using the logical 'or' operator
     * 
     * @param constraint
     *          the or constraint
     * @param context
     *          the hql context
     */
    private void process(OrConstraint constraint, QueryContext context) {
        // push the operator
        context.pushLogicalOperator(LogicalOperator.Or);
        
        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
        
        // pop the stack attributes
        context.popLogicalOperator();
    }

    /**
     * Prcoess the embedded constraints using the logical 'and' operator
     * 
     * @param constraint
     *            the and constraint
     * @param context
     *            the hql context
     */
    private void process(AndConstraint constraint, QueryContext context) {
        // push the operator
        context.pushLogicalOperator(LogicalOperator.And);
        
        
        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
        
        // pop the stack attributes
        context.popLogicalOperator();
    }

    /**
     * Process the node constraint
     * 
     * @param constraint
     *            the node constraint
     * @param context
     *            the context
     */
    private void process(NodeConstraint constraint, QueryContext context) {
        if ((constraint == null) ||
            (StringUtils.isEmpty(constraint.getNodeName()))) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.MustSpecifyNodeName);
        }
        
        NodeDescriptor ndesc = getMatchingNodeDescriptor(
                context.peekDistinctTypes().descriptors, constraint.getNodeName());
        if (ndesc == null) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoNodeDescriptorForName,
                    new Object[] {constraint.getNodeName()});
        }
        
        // get the name of the attribute
        String property = getProperty(ndesc);
        context.addWhereConstraint(property, constraint);
    }

    /**
     * Process the specified constraint on a object reference node
     * 
     * @param constraint
     *            the object reference node constraint
     * @param context
     *            the query context
     */
    private void process(ObjectRefNodeConstraint constraint, QueryContext context) {
        if ((constraint == null) ||
            (StringUtils.isEmpty(constraint.getNodeName()))) {
                throw new QueryBuilderException(
                        QueryBuilderException.ErrorCode.MustSpecifyNodeName);
            }
            
            NodeDescriptor ndesc = getMatchingNodeDescriptor(
                    context.peekDistinctTypes().descriptors, constraint.getNodeName());
            
            if (ndesc == null) {
                throw new QueryBuilderException(
                        QueryBuilderException.ErrorCode.NoNodeDescriptorForName,
                        new Object[] {constraint.getNodeName()});
            }
            
            // get the name of the attribute
            String property = getProperty(ndesc);
            context.addWhereConstraint(property + ".linkId", constraint.getOperator(), 
                    constraint.getObjectReference().getLinkId());
            context.addWhereConstraint(property + ".archetypeId.qualifiedName", constraint.getOperator(), 
                    constraint.getObjectReference().getArchetypeIdAsString());
    }

    /**
     * Process the specified object reference constraint
     * 
     * @param constraint
     *            the object reference constraint
     * @param context
     *            the query context
     */
    private void process(ObjectRefArchetypeConstraint constraint, QueryContext context) {
        if ((constraint == null) ||
            (constraint.getArchetypeId() == null)) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.InvalidObjectReferenceConstraint,
                    new Object[] {constraint});
        }
        
        ArchetypeId id = constraint.getArchetypeId();
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(id);
        DistinctTypesResultSet types = new DistinctTypesResultSet();
          
        if (adesc != null) {
            types.type = adesc.getClassName();
            types.descriptors.add(adesc);
        } else {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoArchetypesForId,
                    new Object[] {id});
        }
        
        context.pushLogicalOperator(LogicalOperator.And);
        context.pushDistinctTypes(types);
        
        context.addWhereConstraint("archetypeId.rmName", RelationalOp.EQ,
                id.getRmName());
        context.addWhereConstraint("archetypeId.entityName", RelationalOp.EQ,
                id.getEntityName());
        context.addWhereConstraint("archetypeId.concept", RelationalOp.EQ,
                id.getConcept());
        context.addWhereConstraint("linkId", RelationalOp.EQ, 
                constraint.getLinkId());
        
        
        // pop the stack when we have finished processing this constraint
        context.popDistinctTypes();
        context.popLogicalOperator();
    }
    
    /**
     * Process the collection constraints, which involves a join across tables
     * 
     * @param constraint
     *            the collection constraint               
     * @param context
     *            the context
     */
    private void process(CollectionNodeConstraint constraint, QueryContext context) {
        DistinctTypesResultSet types = context.peekDistinctTypes();
        NodeDescriptor ndesc = getMatchingNodeDescriptor(types.descriptors, 
                constraint.getNodeName());
        if (ndesc == null) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoNodeDescriptorForName,
                    new Object[] {constraint.getNodeName()});
        }
        
        // process the type
        context.pushDistinctTypes(getDistinctTypes(constraint.getArchetypeConstraint()), 
                getProperty(ndesc), constraint.getJoinType());

        // process common portion of constraint
        if (constraint.getArchetypeConstraint() != null) {
            context.pushLogicalOperator(LogicalOperator.And);
            processArchetypeConstraint(constraint.getArchetypeConstraint(), 
                    context);
            context.popLogicalOperator();
        }
        
        // pop the stack when we have finished processing this constraint
        context.popDistinctTypes();
    }

    /**
     * Process the specified archetype sort constraint.
     * 
     * @param constraint
     *            an archetype sort constraint          
     * @param context
     *            the context
     */
    private void process(ArchetypeSortConstraint constraint, QueryContext context) {
        String property = null;
        
        switch (constraint.getProperty()) {
        case EntityName:
            property = "archetypeId.entityName";
            break;
            
        case ConceptName:
            property = "archetypeId.concept";
            break;
            
        case ReferenceModelName:
            property = "archetypeId.rmName";
            break;
        }
        
        context.addSortConstraint(property, constraint.isAscending());
    }

    /**
     * Process the specified sort constraint on a property.
     * 
     * @param constraint
     *            a sort constraint on a node
     * @param context
     *            the context
     */
    private void process(NodeSortConstraint constraint, QueryContext context) {
        NodeDescriptor ndesc = getMatchingNodeDescriptor(
                context.peekDistinctTypes().descriptors, constraint.getNodeName());
        if (ndesc == null) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoNodeDescriptorForName,
                    new Object[] {constraint.getNodeName()});
        }
        
        // get the name of the attribute
        String property = getProperty(ndesc);
        context.addSortConstraint(property, constraint.isAscending());
    }

    /**
     * Return the property for the specified node descriptor. This will only 
     * return a top level property. The property is derived from the descriptor's
     * path.
     * 
     * @param ndesc
     *            the node descriptor
     * @return String
     */
    private String getProperty(NodeDescriptor ndesc) {
        // stip the leading /, if it exists
        String aprop = ndesc.getPath();
        if (aprop.startsWith("/")) {
            aprop = ndesc.getPath().substring(1);
        }
        
        // now check for any more / characters
        if (aprop.contains("/")) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.CanOnlySortOnTopLevelNodes,
                    new Object[] {ndesc.getName()});
        }
        
        return aprop;
    }

    /**
     * Return the {@link DistinctTypeResultSet} given an {@link ArchetypeIdConstraint}
     * 
     * @param constraint
     *            the arhcetype id constraint
     * @return DistinctTypeResultSet  
     * @throws QueryBuilderException
     *            if there are no matching archetypes          
     */
    private DistinctTypesResultSet getDistinctTypes(ArchetypeIdConstraint constraint) {
        ArchetypeId id = constraint.getArchetypeId();
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(id);
        DistinctTypesResultSet types = new DistinctTypesResultSet();
          
        if (adesc != null) {
            types.type = adesc.getClassName();
            types.descriptors.add(adesc);
        } else {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoArchetypesForId,
                    new Object[] {id});
        }

        return types;
    }
    
    /**
     * Return the {@link DistinctTypeResultSet} given an {@link ArchetypeShortNameConstraint}
     * 
     * @param constraint
     *            the arhcetype short name constraint
     * @return DistinctTypeResultSet            
     * @throws QueryBuilderException
     *            if there are no matching archetypes          
     */
    private DistinctTypesResultSet getDistinctTypes(ArchetypeShortNameConstraint constraint) {
        DistinctTypesResultSet types = getDistinctTypes(constraint.getShortNames(), 
                constraint.isPrimaryOnly());
        
        // check that we have at least one match
        if ((types == null) ||
            (StringUtils.isEmpty(types.type))) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoMatchingArchetypesForId,
                    new Object[] {ArrayUtils.toString(constraint.getShortNames())});
        }
        
        return types;
    }
    
    /**
     * Return the {@link DistinctTypeResultSet} given an {@link ArchetypeLongNameConstraint}
     * 
     * @param constraint
     *            the arhcetype long name constraint
     * @return DistinctTypeResultSet            
     * @throws QueryBuilderException
     *            if there are no matching archetypes          
     */
    private DistinctTypesResultSet getDistinctTypes(ArchetypeLongNameConstraint constraint) {
        DistinctTypesResultSet types =  getDistinctTypes(constraint.getRmName(), 
                constraint.getEntityName(), constraint.getConceptName(), 
                constraint.isPrimaryOnly());
        
        // check that we have at least one match
        if ((types == null) ||
            (StringUtils.isEmpty(types.type))) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoMatchingArchetypesForId,
                    new Object[] {constraint.getRmName(), constraint.getEntityName(), 
                            constraint.getConceptName()});
        }
        
        return types;
    }
    
    /**
     * Get the Java class that is denoted by the specified parameters. If wild
     * cards characters are specified then all the descriptors must resolve
     * to the same type otherwise an exception is raised.
     * 
     * @param rmName
     *            the reference model name (complete or partial)
     * @param entityName
     *            the entity name (complete or partial)
     * @param concept
     *            the concept name (complete )            
     * @param primaryOnly
     *            determines whether to restrict processing to primary only            
     * @return Class
     */
    private DistinctTypesResultSet getDistinctTypes(String rmName, String entityName,
            String concept, boolean primaryOnly) {
        DistinctTypesResultSet results = new DistinctTypesResultSet();
        
        String modRmName = (rmName == null) ? null : 
            rmName.replace(".", "\\.").replace("*", ".*");
        String modEntityName = (entityName == null) ? null : 
            entityName.replace(".", "\\.").replace("*", ".*");
        String modConcept = (concept == null) ? null :
            concept.replace(".", "\\.").replace("*", ".*");
        
        // search through the cache for matching archetype descriptors
        for (ArchetypeDescriptor desc : service.getArchetypeDescriptors()) {
            ArchetypeId archId = desc.getType();
            if ((primaryOnly) &&
                (desc.isPrimary() == false)) {
                continue;
            }

            if (((StringUtils.isEmpty(modRmName)) ||
                 (archId.getRmName().matches(modRmName))) &&
                ((StringUtils.isEmpty(modEntityName)) || 
                 (archId.getEntityName().matches(modEntityName))) &&
                ((StringUtils.isEmpty(modConcept)) || 
                 (archId.getConcept().matches(modConcept)))) {
                results.descriptors.add(desc);
                if (StringUtils.isEmpty(results.type)) {
                    results.type = desc.getClassName();
                } else {
                    if (!results.type.equals(desc.getClassName())) {
                        throw new QueryBuilderException(
                                QueryBuilderException.ErrorCode.CannotQueryAcrossTypes,
                                new Object[] {results.type, desc.getClassName()});
                    }
                }
            }
        }

        return results;
    }

    /**
     * Iterate through all the archetype short names and return the single
     * type that matches all names. If the short names refer to different 
     * types then we need to throw an exception
     * 
     * @param shortNames
     *            a list of short names to search against
     * @param primaryOnly
     *            determines whether to restrict processing to primary only            
     * @return DistinctTypesResultSet
     */
    private DistinctTypesResultSet getDistinctTypes(String[] shortNames, boolean primaryOnly) {
        DistinctTypesResultSet results = new DistinctTypesResultSet();

        if ((shortNames == null) ||
            (shortNames.length == 0)) {
            return results;
        }
        
        // first go through the list of short names and translate '*' to
        // '.*' to perform the regular expression matches
        String[] modShortNames = new String[shortNames.length];
        for (int index = 0; index < shortNames.length; index++) {
            modShortNames[index] = shortNames[index]
                          .replace(".", "\\.")
                          .replace("*", ".*");
        }
        // adjust the reference model name
        for (String name : service.getArchetypeShortNames()) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(name); 
            if ((primaryOnly) &&
                (!desc.isPrimary())) {
                    continue;
            }
            
            for (String modShortName : modShortNames) {
                if (name.matches(modShortName)) {
                    results.descriptors.add(desc);
                    if (StringUtils.isEmpty(results.type)) {
                        results.type = desc.getClassName();
                    } else {
                        if (!results.type.equals(desc.getClassName())) {
                            throw new QueryBuilderException(
                                    QueryBuilderException.ErrorCode.CannotQueryAcrossTypes,
                                    new Object[] {results.type, desc.getClassName()});
                        }
                    }
                    break;
                }
            }
        }

        return results;
    }
    
    /**
     * Iterate through all the {@link ArchetypeDescriptor} instances and return
     * a typical {@link NodeDescriptor}. The only requirement is that the path
     * and the type are same for every archetype descriptor
     * 
     * @param adescs
     *            the set of archetype descriptors
     * @param node
     *            the node to search for
     * @return NodeDescriptor
     *            a typical node descriptor
     * @throws ArchetypeServiceException            
     */
    private NodeDescriptor getMatchingNodeDescriptor(Set<ArchetypeDescriptor> adescs, 
            String nodeName) {
        NodeDescriptor matching = null;
        
        if (StringUtils.isEmpty(nodeName)) {
            return null;
        }
        
        // ensure the property is defined in all archetypes
        NodeDescriptor ndesc = null;
        
        for (ArchetypeDescriptor adesc : adescs) {
            ndesc = adesc.getNodeDescriptor(nodeName);
            if (ndesc == null) {
                throw new QueryBuilderException(
                        QueryBuilderException.ErrorCode.NoNodeDescWithName,
                        new Object[] {adesc.getName(), nodeName});
            }
            
            // now check against the matching node descriptor
            if (matching == null) {
                matching = ndesc;
            } else {
                if (ndesc.getPath().equals(matching.getPath()) &&
                    ndesc.getType().equals(matching.getType())) {
                    // this descriptor matches the node descriptor
                } else {
                    throw new QueryBuilderException(
                            QueryBuilderException.ErrorCode.NodeDescriptorsDoNotMatch,
                            new Object[] {nodeName});
                }
            }
        }
        
        return matching;
    }

    /**
     * Delegate to the appropraite type
     * 
     * @param archetypeConstraint
     * @return DistinctTypesResultSet
     */
    private DistinctTypesResultSet getDistinctTypes(ArchetypeConstraint constraint) {
        if (constraint instanceof ArchetypeIdConstraint) {
            return getDistinctTypes((ArchetypeIdConstraint)constraint);
        } else if (constraint instanceof ArchetypeShortNameConstraint) {
            return getDistinctTypes((ArchetypeShortNameConstraint)constraint);
        } else if (constraint instanceof ArchetypeLongNameConstraint) {
            return getDistinctTypes((ArchetypeLongNameConstraint)constraint);
        } else {
            // raise an exception
            return null;
        }
    }

    /**
     * Delegate to the appropriate type
     * 
     * @param constraint
     *            the constraint
     * @param context
     */
    private void processArchetypeConstraint(ArchetypeConstraint constraint, 
            QueryContext context) {
        if (constraint instanceof ArchetypeIdConstraint) {
            processArchetypeConstraint((ArchetypeIdConstraint)constraint,
                    context);
        } else if (constraint instanceof ArchetypeShortNameConstraint) {
            processArchetypeConstraint((ArchetypeShortNameConstraint)constraint,
                    context);
        } else if (constraint instanceof ArchetypeLongNameConstraint) {
            processArchetypeConstraint((ArchetypeLongNameConstraint)constraint,
                    context);
        } else {
            // raise an exception
        }
    }

    /**
     * Process the appropriate constraint
     * 
     * @param constraint
     *            the constraint to process
     * @param context 
     *            the hql context           
     */
    private void processConstraint(IConstraint constraint, QueryContext context) {
        if (constraint instanceof ObjectRefArchetypeConstraint) {
            process((ObjectRefArchetypeConstraint)constraint, context);
        } else if (constraint instanceof ArchetypeIdConstraint) {
            process((ArchetypeIdConstraint)constraint, context);
        } else if (constraint instanceof ArchetypeLongNameConstraint) {
            process((ArchetypeLongNameConstraint)constraint, context);
        } else if (constraint instanceof ArchetypeShortNameConstraint) {
            process((ArchetypeShortNameConstraint)constraint, context);
        } else if (constraint instanceof CollectionNodeConstraint) {
            process((CollectionNodeConstraint)constraint, context);
        } else if (constraint instanceof NodeConstraint) {
            process((NodeConstraint)constraint, context);
        } else if (constraint instanceof ObjectRefNodeConstraint) {
            process((ObjectRefNodeConstraint)constraint, context);
        } else if (constraint instanceof AndConstraint) {
            process((AndConstraint)constraint, context);
        } else if (constraint instanceof OrConstraint) {
            process((OrConstraint)constraint, context);
        } else if (constraint instanceof NodeSortConstraint) {
            process((NodeSortConstraint)constraint, context);
        } else if (constraint instanceof ArchetypeSortConstraint) {
            process((ArchetypeSortConstraint)constraint, context);
        } else {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.ConstraintTypeNotSupported,
                    new Object[] {constraint.getClass().getName()});
        } 
    }

    /**
     * Private anonymous class to hold the results of {@link getDistinctTypes}
     */
    class DistinctTypesResultSet {
        /**
         * The set of distinct archetypes
         */
        Set<ArchetypeDescriptor> descriptors =
            new HashSet<ArchetypeDescriptor>();
        
        /**
         * The type matches all the specfied descriptors.
         */
        String type;
    }
}
