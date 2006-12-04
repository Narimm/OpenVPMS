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

package org.openvpms.component.business.service.archetype;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.dao.im.common.ResultCollectorFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.business.service.archetype.query.QueryBuilder;
import org.openvpms.component.business.service.archetype.query.QueryContext;
import org.openvpms.component.business.service.ruleengine.IStatelessRuleEngineInvocation;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.service.hibernate.EntityInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This basic implementation of an archetype service, which reads in the
 * archetypes from the specified XML document and creates an in memory registry.
 * <p/>
 * This implementation has the following constraints 1. All archetype
 * definitions must be deployed in a single directory. The name of hte directory
 * is specified on construction 2. The archetype records must be stored in a
 * single XML document and the structure of the document must comply with XML
 * Schema defined in <b>archetype.xsd</b>.
 * <p/>
 * NOTE: The archetype service is currently supporting both styles of queries
 * but this will change to support a single query api for all queries once it
 * has been validated.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeService implements IArchetypeService {

    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(ArchetypeService.class);

    /**
     * A reference to the archetype descriptor cache
     */
    private IArchetypeDescriptorCache dCache;

    /**
     * The DAO instance it will use.
     */
    private IMObjectDAO dao;

    /**
     * The rule engine to use. If not specified then the service does
     * not support rule engine invocations.
     */
    private IStatelessRuleEngineInvocation ruleEngine;

    /**
     * The entity interceptor that is used to intercept calls hibernate
     * calls
     */
    private EntityInterceptor entityInterceptor;

    /**
     * Construct an instance of this service using the specified archetpe class by loading and parsing all the
     * descripor cache
     * <p/>
     * The resource specified by afile must be loadable from the classpath. A
     * similar constraint applies to the resourcr specified by adir, it must be
     * a valid path in the classpath.
     *
     * @param cache the archetype descriptor cache
     */
    public ArchetypeService(IArchetypeDescriptorCache cache) {
        dCache = cache;
    }

    /**
     * Returns the DAO.
     *
     * @return the dao.
     */
    public IMObjectDAO getDao() {
        return dao;
    }

    /**
     * Sets the DAO.
     *
     * @param dao the dao to set.
     */
    public void setDao(IMObjectDAO dao) {
        this.dao = dao;
    }

    /**
     * Returns the entity interceptor.
     *
     * @return the entity interceptor
     */
    public EntityInterceptor getEntityInterceptor() {
        return entityInterceptor;
    }

    /**
     * Sets the entity interceptor.
     *
     * @param entityInterceptor the entity interceptor
     */
    public void setEntityInterceptor(EntityInterceptor entityInterceptor) {
        this.entityInterceptor = entityInterceptor;
        entityInterceptor.setDescriptorCache(dCache);
    }

    /**
     * Returns the rule engine.
     *
     * @return the rule engine
     */
    public IStatelessRuleEngineInvocation getRuleEngine() {
        return ruleEngine;
    }

    /**
     * Sets the rule engine.
     *
     * @param ruleEngine the rule engine
     */
    public void setRuleEngine(IStatelessRuleEngineInvocation ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(java.lang.String)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        return dCache.getArchetypeDescriptor(shortName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return dCache.getArchetypeDescriptor(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecord(java.lang.String)
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return dCache.getAssertionTypeDescriptor(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecords()
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return dCache.getAssertionTypeDescriptors();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public IMObject create(ArchetypeId id) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.create: Creating object of type "
                    + id.getShortName());
        }

        ArchetypeDescriptor desc = dCache.getArchetypeDescriptor(id);
        if (desc != null) {
            return create(desc);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(java.lang.String)
     */
    public IMObject create(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.create: Creating object of type "
                    + name);
        }

        ArchetypeDescriptor desc = dCache.getArchetypeDescriptor(name);
        if (desc != null) {
            return create(desc);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#validateObject(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void validateObject(IMObject object) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "ArchetypeService.validateObject: Validating object of type "
                            + object.getArchetypeId().getShortName()
                            + " with uid " + object.getUid()
                            + " and version " + object.getVersion());
        }

        List<ValidationError> errors = new ArrayList<ValidationError>();

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object
                .getArchetypeId());
        if (descriptor == null) {
            errors.add(new ValidationError(null, new StringBuffer(
                    "No archetype definition for ").append(
                    object.getArchetypeId()).toString()));
            logger.error(new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                    object.getArchetypeId().toString()));
        } else {
            // if there are nodes attached to the archetype then validate the
            // associated assertions
            if (descriptor.getNodeDescriptors().size() > 0) {
                JXPathContext context = JXPathHelper.newContext(object);
                validateObject(context, descriptor.getNodeDescriptors(),
                               errors);
            }
        }

        /**
         * if we have accumulated any errors then throw an exception
         */
        if (errors.size() > 0) {
            throw new ValidationException(
                    errors,
                    ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype,
                    new Object[]{object.getArchetypeId()});

        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#deriveValues(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void deriveValues(IMObject object) {
        // check for a non-null object
        if (object == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "ArchetypeService.deriveValues: Deriving values for type"
                            + object.getArchetypeId().getShortName()
                            + " with uid " + object.getUid()
                            + " and version " + object.getVersion());
        }

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(
                object.getArchetypeId());
        if (descriptor == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                    object.getArchetypeId().toString());
        }

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        if (descriptor.getNodeDescriptors().size() > 0) {
            JXPathContext context = JXPathHelper.newContext(object);
            deriveValues(context, descriptor.getNodeDescriptors());
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#deriveValue(org.openvpms.component.business.domain.im.common.IMObject, java.lang.String)
     */
    public void deriveValue(IMObject object, String node) {
        if (object == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NonNullObjectRequired);
        }

        if (StringUtils.isEmpty(node)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NonNullNodeNameRequired);
        }

        // check that the node name is valid for the specified object
        ArchetypeDescriptor adesc = getArchetypeDescriptor(
                object.getArchetypeId());
        if (adesc == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidArchetypeDescriptor,
                    object.getArchetypeId());
        }

        NodeDescriptor ndesc = adesc.getNodeDescriptor(node);
        if (ndesc == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidNodeDescriptor,
                    node, object.getArchetypeId());
        }

        // derive the value
        ndesc.deriveValue(object);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecords()
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return dCache.getArchetypeDescriptors();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptors(java.lang.String)
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        return dCache.getArchetypeDescriptors(shortName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptorsByRmName(java.lang.String)
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(
            String rmName) {
        return dCache.getArchetypeDescriptorsByRmName(rmName);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(org.openvpms.component.system.common.query.ArchetypeQuery)
     */
    public IPage<IMObject> get(IArchetypeQuery query) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.get: query " + query);
        }
        try {
            return getQueryDelegator(query).get(query);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteQuery,
                    exception);
        }
    }

    /**
     * Retrieves partially populated objects that match the specified query
     * criteria.<p/>
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <code>nodes</code> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     */
    public IPage<IMObject> get(IArchetypeQuery query,
                               Collection<String> nodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.get: query=" + query
                    + ", nodes=" + nodes);
        }
        try {
            return getQueryDelegator(query).get(query, nodes);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteQuery,
                    exception);
        }
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.getObjects: query=" + query);
        }
        try {
            return getQueryDelegator(query).getObjects(query);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteQuery,
                    exception);
        }
    }

    /**
     * Retrieves the nodes from the objects that match the query criteria.
     *
     * @param query the archetype query
     * @param nodes the node names
     * @return the nodes for each object that matches the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<NodeSet> getNodes(IArchetypeQuery query,
                                   Collection<String> nodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.get: query " + query
                    + ", nodes=" + nodes);
        }

        try {
            return getQueryDelegator(query).getNodes(query, nodes);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteQuery,
                    exception, query.toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#remove(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void remove(IMObject entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.remove: Removing object of type "
                    + entity.getArchetypeId().getShortName()
                    + " with uid " + entity.getUid()
                    + " and version " + entity.getVersion());
        }

        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[]{});
        }

        validateObject(entity);
        try {
            dao.delete(entity);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToDeleteObject,
                    exception, entity.getUid());
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void save(IMObject entity) {
        save(entity, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    @Deprecated
    public void save(IMObject entity, boolean validate) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.save: Saving object of type "
                    + entity.getArchetypeId().getShortName()
                    + " with uid " + entity.getUid()
                    + " and version " + entity.getVersion());
        }

        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[]{});
        }

        if (validate)
            validateObject(entity);
        try {
            dao.save(entity);

            // a quick fix to the archetype descriptor update problem
            // couldn't get the hibernate interceptors working
            // TODO Review the interceptors
            if (entity instanceof ArchetypeDescriptor) {
                ArchetypeDescriptor adesc = (ArchetypeDescriptor) entity;
                if (dCache != null) {
                    dCache.addArchetypeDescriptor(adesc, true);
                }
            }
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToSaveObject,
                    exception, entity);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#save(java.util.Collection)
     */

    public void save(Collection<IMObject> entities, boolean validate) {
        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[]{});
        }

        // first validate each entity
        if (validate) {
            for (Object entity : entities) {
                validateObject((IMObject) entity);
            }
        }

        // now issue a call to save the entities
        try {
            dao.save(entities);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToSaveCollectionOfObjects,
                    exception, entities.size());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames(java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    public List<String> getArchetypeShortNames(String rmName,
                                               String entityName,
                                               String conceptName,
                                               boolean primaryOnly) {
        return dCache.getArchetypeShortNames(rmName, entityName, conceptName,
                                             primaryOnly);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames(java.lang.String, boolean)
     */
    public List<String> getArchetypeShortNames(String shortName,
                                               boolean primaryOnly) {
        return dCache.getArchetypeShortNames(shortName, primaryOnly);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames()
     */
    public List<String> getArchetypeShortNames() {
        return dCache.getArchetypeShortNames();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#executeRule(java.lang.String, java.util.Map, java.util.List)
     */
    public List<Object> executeRule(String ruleUri, Map<String, Object> props,
                                    List<Object> facts) {
        if (ruleEngine == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.RuleEngineNotSupported);
        }
        try {
            return ruleEngine.executeRule(ruleUri, props, facts);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteRule,
                    exception, ruleUri);
        }
    }

    /**
     * Returns a new query delegator for the specified query.
     *
     * @param query the query
     * @return a new query delegator for the query
     * @throws ArchetypeServiceException if the query is unsupported
     */
    private QueryDelegator getQueryDelegator(IArchetypeQuery query) {
        if (query instanceof ArchetypeQuery) {
            return new DefaultQueryDelegator();
        } else if (query instanceof NamedQuery) {
            return new NamedQueryDelegator();
        }
        throw new ArchetypeServiceException(
                ArchetypeServiceException.ErrorCode.UnsupportedQuery,
                query.getClass().getName());
    }

    /**
     * Iterate through all the nodes and ensure that the object meets all the
     * specified assertions. The assertions are defined in the node and can be
     * hierarchical, which means that this method is re-entrant.
     *
     * @param context holds the object to be validated
     * @param nodes   assertions are managed by the nodes object
     * @param errors  the errors are collected in this object
     */
    private void validateObject(JXPathContext context, Map nodes,
                                List<ValidationError> errors) {
        for (Object o : nodes.values()) {
            NodeDescriptor node = (NodeDescriptor) o;
            Object value = null;
            try {
                value = node.getValue(context);
            } catch (Exception ignore) {
                // ignore since context.setLenient doesn't
                // seem to be working.
                // TODO Need to sort out a better way since this
                // can also cause problems
            }

            // first check whether the value for this node is derived and if it
            // is then set the derived value
            if (node.isDerived()) {
                try {
                    context.getPointer(node.getPath()).setValue(value);
                } catch (Exception exception) {
                    value = null;
                    errors.add(new ValidationError(node.getName(),
                                                   "Cannot derive value"));
                    logger.error("Failed to derive value for " +
                            node.getName(), exception);
                }
            }

            // check the cardinality
            int minCardinality = node.getMinCardinality();
            int maxCardinality = node.getMaxCardinality();
            if ((minCardinality == 1) &&
                    ((value == null) ||
                            ((value instanceof String) && (StringUtils.isEmpty(
                                    (String) value))))) {
                errors.add(new ValidationError(node.getName(),
                                               "value is required"));

                if (logger.isDebugEnabled()) {
                    logger.debug("Validation failed for Node: "
                            + node.getName() + " min cardinality violated");
                }
            }

            // do collection related processing
            if (node.isCollection()) {
                Collection collection = node.toCollection(value);
                // check the min cardinality if specified
                if ((minCardinality > 0) &&
                        (collection == null || collection.size() < minCardinality))
                {
                    errors.add(new ValidationError(node.getName(),
                                                   " must supply at least " + minCardinality + " "
                                                           + node.getBaseName()));

                }

                // check the max cardinality if specified
                if ((maxCardinality > 0) &&
                        (maxCardinality != NodeDescriptor.UNBOUNDED) &&
                        (collection != null && collection.size() > maxCardinality))
                {
                    errors.add(new ValidationError(node.getName(),
                                                   " cannot supply more than " + maxCardinality + " "
                                                           + node.getBaseName()));
                }

                // if it's a parent-child relationship then validate the
                // children. This is the recursive validation
                if (node.isParentChild()) {
                    for (Object obj : collection) {
                        if ((obj == null) ||
                                (!(obj instanceof IMObject))) {
                            continue;
                        }

                        // cast to an imobject and ensure that we can retrieve
                        // the archetypeId. If we can them attempt to retrieve
                        // the associated descriptor. The the IMObject does not
                        // contain and archetypeId then it was incorrectly
                        // created.
                        IMObject imobj = (IMObject) obj;
                        if (imobj.getArchetypeId() == null) {
                            errors.add(
                                    new ValidationError(null, new StringBuffer(
                                            "No archetype Id was set for object of type ")
                                            .append(imobj.getClass().getName()).toString()));
                            continue;
                        }

                        ArchetypeDescriptor adesc = getArchetypeDescriptor(
                                imobj.getArchetypeId());
                        if (adesc == null) {
                            errors.add(
                                    new ValidationError(null, new StringBuffer(
                                            "No archetype definition for ").append(
                                            imobj.getArchetypeId()).toString()));
                            logger.error(new ArchetypeServiceException(
                                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                                    imobj.getArchetypeId().toString()));
                            continue;
                        }

                        // if there are nodes attached to the archetype then validate the
                        // associated assertions
                        if ((adesc.getNodeDescriptors() != null) &&
                                (adesc.getNodeDescriptors().size() > 0)) {
                            JXPathContext childContext = JXPathHelper.newContext(
                                    imobj);
                            childContext.setLenient(true);
                            validateObject(childContext,
                                           adesc.getNodeDescriptors(), errors);
                        }
                    }
                }
            }

            if ((value != null) &&
                    (node.getAssertionDescriptorsAsArray().length > 0)) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType =
                            dCache.getAssertionTypeDescriptor(
                                    assertion.getName());
                    if (assertionType == null) {
                        throw new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.AssertionTypeNotSpecified,
                                assertion.getName());
                    }

                    try {
                        if (!assertionType.validate(value, node, assertion)) {
                            errors.add(new ValidationError(node.getName(),
                                                           assertion.getErrorMessage()));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Assertion failed for Node: "
                                        + node.getName() + " and Assertion "
                                        + assertion.getName());
                            }
                        }
                    } catch (Exception exception) {
                        // log the error
                        logger.error("Error in validateObject for node "
                                + node.getName(), exception);
                        errors.add(new ValidationError(node.getName(),
                                                       assertion.getErrorMessage()));
                    }
                }
            }

            // if this node has other nodes then re-enter this method
            if (node.getNodeDescriptors().size() > 0) {
                validateObject(context, node.getNodeDescriptors(), errors);
            }
        }
    }


    /**
     * Iterate through the {@link NodeDescriptor}s and set all derived values.
     * Do it recursively.
     *
     * @param context the context object
     * @param nodes   a list of node descriptors
     * @throws ArchetypeServiceException
     */
    private void deriveValues(JXPathContext context,
                              Map<String, NodeDescriptor> nodes) {
        for (NodeDescriptor node : nodes.values()) {
            if (node.isDerived()) {
                try {
                    Object value = context.getValue(node.getDerivedValue());
                    context.getPointer(node.getPath()).setValue(value);
                } catch (Exception exception) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToDeriveValue,
                            exception, node.getName(), node.getPath());
                }
            }

            // if this node contains other nodes then make a recursive call
            if (node.getNodeDescriptors().size() > 0) {
                deriveValues(context, node.getNodeDescriptors());
            }
        }
    }

    /**
     * This method will create a default object using the specified archetype
     * descriptor. Fundamentally, it will set the default value when specified
     * and it will also create an object through a default constructur if a
     * cardinality constraint is specified.
     *
     * @param descriptor the archetype descriptor
     * @return IMObject
     * @throws ArchetypeServiceException if it failed to create the object
     */
    private IMObject create(ArchetypeDescriptor descriptor) {
        IMObject imobj;
        try {
            Class domainClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(descriptor.getClassName());
            if (!IMObject.class.isAssignableFrom(domainClass)) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidDomainClass,
                        descriptor.getClassName());
            }

            imobj = (IMObject) domainClass.newInstance();
            imobj.setArchetypeId(descriptor.getType());

            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathHelper.newContext(imobj);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            create(context, descriptor.getNodeDescriptors());
        } catch (Exception exception) {
            // rethrow as a runtime exception
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                    exception, descriptor.getType().getShortName());
        }

        return imobj;
    }

    /**
     * Iterate through all the nodes in the archetype definition and create the
     * default object.
     *
     * @param context the JXPath
     * @param nodes   the node descriptors for the archetype
     */
    @SuppressWarnings("unchecked")
    private void create(JXPathContext context, Map nodes) {
        for (Object o : nodes.values()) {
            NodeDescriptor node = (NodeDescriptor) o;

            // only ceate a node if it is a collection of if it contains
            // children nodes or if it has a default vaules specified
            if ((node.isCollection()) ||
                    (node.getNodeDescriptorCount() > 0) ||
                    (!StringUtils.isEmpty(node.getDefaultValue()))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to create path " + node.getPath()
                            + " for node " + node.getName());
                }

                // if we have a value to set then do a create and set
                // otherwise do only a create
                String value = node.getDefaultValue();
                context.getVariables().declareVariable("node", node);
                if (StringUtils.isEmpty(value)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("calling createPath for node "
                                + node.getName() + " and path "
                                + node.getPath());
                    }
                    context.createPath(node.getPath());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("calling createPathAndSetValue for node "
                                + node.getName() + " path " + node.getPath()
                                + " and default value "
                                + node.getDefaultValue());
                    }
                    context.createPath(node.getPath());

                    // evaluate the default value
                    Object defValue = context.getValue(value);
                    if (!defValue.getClass().isAssignableFrom(node.getClazz()))
                    {
                        try {
                            defValue = convertValue(defValue, node.getClazz());
                        } catch (Exception exception) {
                            throw new ArchetypeServiceException(
                                    ArchetypeServiceException.ErrorCode.InvalidDefaultValue,
                                    exception, node.getDefaultValue(),
                                    node.getName()
                            );
                        }
                    }
                    context.setValue(node.getPath(), defValue);
                }
            }

            // determine whether any of the node's associated assertions
            // want to hook in to the creation phase
            if (node.getAssertionDescriptorsAsArray().length > 0) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType =
                            dCache.getAssertionTypeDescriptor(
                                    assertion.getName());
                    if (assertionType == null) {
                        throw new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.AssertionTypeNotSpecified,
                                assertion.getName());
                    }

                    try {
                        assertionType.create(context.getContextBean(), node,
                                             assertion);
                    } catch (Exception exception) {
                        throw new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.FailedToExecuteCreateFunction,
                                exception, assertion.getName());
                    }
                }
            }

            // if this node has children then process them
            // recursively
            if (node.getNodeDescriptors().size() > 0) {
                create(context, node.getNodeDescriptors());
            }
        }
    }

    /**
     * Convert value to the specified type
     *
     * @param value the value to convert
     * @param clazz the class that the value should be converted too
     * @return Object
     *         the converted value
     * @throws Exception let the client handle the exception
     */
    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class clazz)
            throws Exception {
        return ConvertUtils.convert(ConvertUtils.convert(value), clazz);
    }

    abstract class QueryDelegator {

        public IPage<IMObject> get(IArchetypeQuery query) {
            ResultCollectorFactory factory = dao.getResultCollectorFactory();
            ResultCollector<IMObject> collector
                    = factory.createIMObjectCollector();
            get(query, collector);
            return collector.getPage();
        }

        public IPage<IMObject> get(IArchetypeQuery query,
                                   Collection<String> nodes) {
            ResultCollectorFactory factory = dao.getResultCollectorFactory();
            ResultCollector<IMObject> collector
                    = factory.createIMObjectCollector(nodes);
            get(query, collector);
            return collector.getPage();
        }

        public IPage<NodeSet> getNodes(IArchetypeQuery query,
                                       Collection<String> nodes) {
            ResultCollectorFactory factory = dao.getResultCollectorFactory();
            ResultCollector<NodeSet> collector
                    = factory.createNodeSetCollector(nodes);
            get(query, collector);
            return collector.getPage();
        }

        public abstract IPage<ObjectSet> getObjects(IArchetypeQuery query);

        protected abstract void get(IArchetypeQuery query,
                                    ResultCollector collector);
    }

    class DefaultQueryDelegator extends QueryDelegator {


        public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
            ResultCollectorFactory factory = dao.getResultCollectorFactory();
            QueryBuilder builder = new QueryBuilder();
            QueryContext context = builder.build((ArchetypeQuery) query);
            ResultCollector<ObjectSet> collector
                    = factory.createObjectSetCollector(context.getSelectNames(),
                                                       context.getSelectTypes());
            get(context, query, collector);
            return collector.getPage();
        }

        protected void get(IArchetypeQuery query, ResultCollector collector) {
            QueryBuilder builder = new QueryBuilder();
            QueryContext context = builder.build((ArchetypeQuery) query);
            get(context, query, collector);
        }

        private void get(QueryContext context, IArchetypeQuery query,
                         ResultCollector collector) {
            if (logger.isDebugEnabled()) {
                logger.debug("ArchetypeService.get: query "
                        + context.getQueryString());
            }

            dao.get(context.getQueryString(), context.getParameters(),
                    collector, query.getFirstResult(), query.getMaxResults(),
                    query.countResults());
        }
    }

    class NamedQueryDelegator extends QueryDelegator {

        public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
            NamedQuery q = (NamedQuery) query;
            ResultCollectorFactory factory = dao.getResultCollectorFactory();
            ResultCollector<ObjectSet> collector
                    = factory.createObjectSetCollector(q.getNames(), null);
            get(query, collector);
            return collector.getPage();
        }

        protected void get(IArchetypeQuery query, ResultCollector collector) {
            NamedQuery q = (NamedQuery) query;
            dao.getByNamedQuery(q.getQuery(), q.getParameters(), collector,
                                q.getFirstResult(), q.getMaxResults(),
                                q.countResults());
        }
    }

}
