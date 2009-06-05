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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.business.service.ruleengine.IRuleEngine;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Default implementation of the {@link IArchetypeService} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeService implements IArchetypeService {

    /**
     * Define a logger for this class
     */
    private static final Log log = LogFactory.getLog(ArchetypeService.class);

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
    private IRuleEngine ruleEngine;

    /**
     * The listeners, keyed on archetype short name.
     */
    private final Map<String, List<IArchetypeServiceListener>> listeners
            = new HashMap<String, List<IArchetypeServiceListener>>();

    /**
     * Control characters, excluding <em>'\n', '\r', '\t'</em>.
     */
    private static final Pattern CNTRL_CHARS
            = Pattern.compile(".*[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F].*");


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
     * Returns the rule engine.
     *
     * @return the rule engine
     */
    public IRuleEngine getRuleEngine() {
        return ruleEngine;
    }

    /**
     * Sets the rule engine.
     *
     * @param ruleEngine the rule engine
     */
    public void setRuleEngine(IRuleEngine ruleEngine) {
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
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.create: Creating object of type "
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
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.create: Creating object of type "
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
        ArchetypeId id = object.getArchetypeId();
        if (log.isDebugEnabled()) {
            log.debug(
                    "ArchetypeService.validateObject: Validating object of type "
                            + id.getShortName()
                            + " with id " + object.getId()
                            + " and version " + object.getVersion());
        }

        List<ValidationError> errors = new ArrayList<ValidationError>();

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(id);
        if (descriptor == null) {
            errors.add(new ValidationError(
                    id.getShortName(), null,
                    "No archetype definition for " + id));
            log.error("No archetype definition for " + id);
        } else {
            // if there are nodes attached to the archetype then validate the
            // associated assertions
            if (descriptor.getNodeDescriptors().size() > 0) {
                JXPathContext context = JXPathHelper.newContext(object);
                validateObject(object, context, descriptor,
                               descriptor.getNodeDescriptors(), errors);
            }
        }

        /**
         * if we have accumulated any errors then throw an exception
         */
        if (errors.size() > 0) {
            throw new ValidationException(
                    errors,
                    ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype,
                    new Object[]{id});

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

        if (log.isDebugEnabled()) {
            log.debug(
                    "ArchetypeService.deriveValues: Deriving values for type"
                            + object.getArchetypeId().getShortName()
                            + " with id " + object.getId()
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
     * @deprecated
     */
    @Deprecated
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(
            String rmName) {
        return Collections.emptyList();
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException if the query fails
     */
    public IMObject get(IMObjectReference reference) {
        try {
            return dao.get(reference);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteQuery,
                    exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(org.openvpms.component.system.common.query.ArchetypeQuery)
     */
    public IPage<IMObject> get(IArchetypeQuery query) {
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.get: query " + query);
        }
        try {
            return dao.get(query);
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
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.get: query=" + query
                    + ", nodes=" + nodes);
        }
        try {
            return dao.get(query, nodes);
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
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.getObjects: query=" + query);
        }
        try {
            return dao.getObjects(query);
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
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.get: query " + query
                    + ", nodes=" + nodes);
        }

        try {
            return dao.getNodes(query, nodes);
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
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.remove: Removing object of type "
                    + entity.getArchetypeId().getShortName()
                    + " with id " + entity.getId()
                    + " and version " + entity.getVersion());
        }

        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[]{});
        }

        notifyRemove(entity, true);

        try {
            dao.delete(entity);
            notifyRemove(entity, false);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToDeleteObject,
                    exception, entity.getObjectReference());
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
    public void save(IMObject object, boolean validate) {
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.save: Saving object of type "
                    + object.getArchetypeId().getShortName()
                    + " with id " + object.getId()
                    + " and version " + object.getVersion());
        }

        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[]{});
        }

        notifySave(object, true);
        if (validate)
            validateObject(object);
        try {
            dao.save(object);
            if (object instanceof ArchetypeDescriptor
                    || object instanceof AssertionTypeDescriptor) {
                updateCache(object);
            }
            notifySave(object, false);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToSaveObject,
                    exception, object);
        }
    }

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    public void save(Collection<? extends IMObject> objects) {
        save(objects, true);
    }

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects  the objects to insert or update
     * @param validate whether to validate or not
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    public void save(Collection<? extends IMObject> objects, boolean validate) {
        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[]{});
        }

        notifySave(objects, true);

        // first validate each object
        if (validate) {
            for (IMObject object : objects) {
                validateObject(object);
            }
        }

        // now issue a call to save the objects
        try {
            dao.save(objects);
            notifySave(objects, false);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToSaveCollectionOfObjects,
                    exception, objects.size());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames(java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    @Deprecated
    public List<String> getArchetypeShortNames(String rmName,
                                               String entityName,
                                               String conceptName,
                                               boolean primaryOnly) {
        return dCache.getArchetypeShortNames(entityName, conceptName,
                                             primaryOnly);
    }

    /**
     * Return a list of archtype short names given the specified criteria.
     *
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public List<String> getArchetypeShortNames(String entityName,
                                               String conceptName,
                                               boolean primaryOnly) {
        return dCache.getArchetypeShortNames(entityName, conceptName,
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
            return ruleEngine.executeRules(ruleUri, props, facts);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToExecuteRule,
                    exception, ruleUri);
        }
    }

    /**
     * Adds a listener to receive notification of changes.
     * <p/>
     * In a transaction, notifications occur on successful commit.
     *
     * @param shortName the archetype short to receive events for. May contain
     *                  wildcards.
     * @param listener  the listener to add
     */
    public void addListener(String shortName,
                            IArchetypeServiceListener listener) {
        synchronized (listeners) {
            for (String name : getArchetypeShortNames(shortName, false)) {
                List<IArchetypeServiceListener> list = listeners.get(name);
                if (list == null) {
                    list = new ArrayList<IArchetypeServiceListener>();
                    listeners.put(name, list);
                }
                list.add(listener);
            }
        }
    }

    /**
     * Removes a listener.
     *
     * @param shortName the archetype short to remove the listener for. May
     *                  contain wildcards.
     * @param listener  the listener to remove
     */
    public void removeListener(String shortName,
                               IArchetypeServiceListener listener) {
        synchronized (listeners) {
            for (String name : getArchetypeShortNames(shortName, false)) {
                List<IArchetypeServiceListener> list = listeners.get(name);
                if (list != null) {
                    if (list.remove(listener)) {
                        if (list.isEmpty()) {
                            listeners.remove(name);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    protected Map<String, List<IArchetypeServiceListener>> getListeners() {
        return listeners;
    }

    /**
     * Iterate through all the nodes and ensure that the object meets all the
     * specified assertions. The assertions are defined in the node and can be
     * hierarchical, which means that this method is re-entrant.
     *
     * @param parent     the parent object
     * @param context    holds the object to be validated
     * @param descriptor the archetype descriptor
     * @param nodes      the node to validate
     * @param errors     the errors are collected in this object
     */
    private void validateObject(IMObject parent, JXPathContext context,
                                ArchetypeDescriptor descriptor,
                                Map<String, NodeDescriptor> nodes,
                                List<ValidationError> errors) {
        String shortName = descriptor.getType().getShortName();

        for (NodeDescriptor node : nodes.values()) {
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
                    errors.add(new ValidationError(shortName, node.getName(),
                                                   "Cannot derive value"));
                    log.error("Failed to derive value for " +
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
                errors.add(new ValidationError(shortName, node.getName(),
                                               "value is required"));

                if (log.isDebugEnabled()) {
                    log.debug("Validation failed for Node: "
                            + node.getName() + " min cardinality violated");
                }
            }

            if (value instanceof String) {
                if (CNTRL_CHARS.matcher((String) value).matches()) {
                    errors.add(new ValidationError(
                            shortName, node.getName(),
                            " contains invalid characters"));
                }
            }

            // do collection related processing
            if (node.isCollection()) {
                Collection collection = node.toCollection(value);
                // check the min cardinality if specified
                if ((minCardinality > 0) &&
                        (collection == null || collection.size() < minCardinality))
                {
                    errors.add(new ValidationError(shortName, node.getName(),
                                                   " must supply at least " + minCardinality + " "
                                                           + node.getBaseName()));
                }

                // check the max cardinality if specified
                if ((maxCardinality > 0) &&
                        (maxCardinality != NodeDescriptor.UNBOUNDED) &&
                        (collection != null && collection.size() > maxCardinality))
                {
                    errors.add(new ValidationError(shortName, node.getName(),
                                                   " cannot supply more than " + maxCardinality + " "
                                                           + node.getBaseName()));
                }

                // if it's a parent-child relationship then validate the
                // children. This is the recursive validation
                if (node.isParentChild()) {
                    for (Object obj : collection) {
                        if (obj == null || !(obj instanceof IMObject)) {
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
                                    new ValidationError(null, null,
                                                        new StringBuffer(
                                                                "No archetype Id was set for object of type ")
                                                                .append(imobj.getClass().getName()).toString()));
                            continue;
                        }

                        ArchetypeDescriptor adesc = getArchetypeDescriptor(
                                imobj.getArchetypeId());
                        if (adesc == null) {
                            errors.add(
                                    new ValidationError(null, null,
                                                        new StringBuffer(
                                                                "No archetype definition for ").append(
                                                                imobj.getArchetypeId()).toString()));
                            log.error("No archetype definition for " + imobj.getArchetypeId());
                            continue;
                        }

                        // if there are nodes attached to the archetype then validate the
                        // associated assertions
                        if ((adesc.getNodeDescriptors() != null) &&
                                (adesc.getNodeDescriptors().size() > 0)) {
                            JXPathContext childContext = JXPathHelper.newContext(
                                    imobj);
                            childContext.setLenient(true);
                            validateObject(imobj, childContext, adesc,
                                           adesc.getNodeDescriptors(), errors);
                        }
                    }
                }
            }

            if (value != null && node.getAssertionDescriptorsAsArray().length > 0) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node.getAssertionDescriptorsAsArray()) {
                    try {
                        if (!assertion.validate(value, parent, node)) {
                            errors.add(new ValidationError(shortName,
                                                           node.getName(),
                                                           assertion.getErrorMessage()));
                            if (log.isDebugEnabled()) {
                                log.debug("Assertion failed for Node: "
                                        + node.getName() + " and Assertion "
                                        + assertion.getName());
                            }
                        }
                    } catch (Exception exception) {
                        // log the error
                        log.error("Error in validateObject for node "
                                + node.getName(), exception);
                        errors.add(
                                new ValidationError(shortName, node.getName(),
                                                    assertion.getErrorMessage()));
                    }
                }
            }

            // if this node has other nodes then re-enter this method
            if (node.getNodeDescriptors().size() > 0) {
                validateObject(parent, context, descriptor,
                               node.getNodeDescriptors(), errors);
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
     * @throws ArchetypeServiceException if the create fails
     */
    private void create(JXPathContext context,
                        Map<String, NodeDescriptor> nodes) {
        for (NodeDescriptor node : nodes.values()) {

            // only create a node if it is a collection, or it has child nodes,
            // or it has a default value
            if (node.isCollection() || node.getNodeDescriptorCount() > 0
                    || !StringUtils.isEmpty(node.getDefaultValue())) {
                create(context, node);
            }

            for (AssertionDescriptor assertion : node.getAssertionDescriptorsAsArray()) {
                try {
                    assertion.create(context.getContextBean(), node);
                } catch (Exception exception) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToExecuteCreateFunction,
                            exception, assertion.getName());
                }
            }

            // if this node has children then process them recursively
            if (node.getNodeDescriptors().size() > 0) {
                create(context, node.getNodeDescriptors());
            }
        }
    }

    /**
     * Creates a node in the context, populating any default value.
     *
     * @param context the jxpath context
     * @param node    the node to create
     * @throws ArchetypeServiceException if the create fails
     */
    private void create(JXPathContext context, NodeDescriptor node) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to create path " + node.getPath()
                    + " for node " + node.getName());
        }

        context.getVariables().declareVariable("node", node);
        context.createPath(node.getPath());

        String expression = node.getDefaultValue();
        if (!StringUtils.isEmpty(expression)) {
            if (log.isDebugEnabled()) {
                log.debug("evaluating default value expression for node "
                        + node.getName() + " path " + node.getPath()
                        + " and expression " + expression);
            }
            Object value = context.getValue(expression);
            IMObject object = (IMObject) context.getContextBean();
            if (node.isCollection()) {
                if (value != null) {
                    if (Collection.class.isAssignableFrom(value.getClass())) {
                        for (Object v : (Collection) value) {
                            node.addChildToCollection(object, v);
                        }
                    } else {
                        node.addChildToCollection(object, value);
                    }
                }
            } else {
                node.setValue(object, value);
            }
        }
    }

    /**
     * Updates the descriptor cache. If a transaction is in progress, the
     * cache will only be updated on transaction commit. This means that the
     * descriptor will only be available via the <em>get*Descriptor</em> methods
     * on successful commit.
     *
     * @param object the object to add to the cache
     */
    private void updateCache(final IMObject object) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // update the cache when the transaction commits
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_COMMITTED) {
                                addToCache(object);
                            }
                        }
                    });
        } else {
            addToCache(object);
        }
    }

    /**
     * Adds a descriptor to the cache.
     *
     * @param object the object to add to the cache
     */
    private void addToCache(IMObject object) {
        if (object instanceof ArchetypeDescriptor) {
            ArchetypeDescriptor descriptor
                    = (ArchetypeDescriptor) object;
            dCache.addArchetypeDescriptor(descriptor, true);
        } else if (object instanceof AssertionTypeDescriptor) {
            AssertionTypeDescriptor descriptor
                    = (AssertionTypeDescriptor) object;
            dCache.addAssertionTypeDescriptor(descriptor, true);
        }
    }

    /**
     * Notifies any listeners when an object is saved.
     *
     * @param object  the object
     * @param preSave if <tt>true</tt> the object is about to be saved,
     *                otherwise it has been saved
     */
    private void notifySave(IMObject object, boolean preSave) {
        synchronized (listeners) {
            notifySave(object, null, preSave);
        }
    }

    /**
     * Notifies any listeners when a collection of objects is saved.
     *
     * @param objects the objects
     * @param preSave if <tt>true</tt> the objects are about to be saved,
     *                otherwise they have been saved
     */
    private void notifySave(Collection<? extends IMObject> objects,
                            boolean preSave) {
        synchronized (listeners) {
            Notifier notifier = null;
            for (IMObject object : objects) {
                notifier = notifySave(object, notifier, preSave);
            }
        }
    }

    /**
     * Notifies any listeners when an object is saved.
     *
     * @param object   the saved object
     * @param notifier the notifier to use. If <tt>null</tt> indicates to create
     *                 a new notifier
     * @param preSave  if <tt>true</tt> the object is about to be saved,
     *                 otherwise it has been saved
     * @return the notifier
     */
    private Notifier notifySave(IMObject object, Notifier notifier,
                                boolean preSave) {
        ArchetypeId id = object.getArchetypeId();
        String shortName = id.getShortName();
        List<IArchetypeServiceListener> list = listeners.get(shortName);
        if (list != null) {
            if (notifier == null) {
                notifier = Notifier.getNotifier(this);
            }
            if (preSave) {
                notifier.notifySaving(object, list);
            } else {
                notifier.notifySaved(object, list);
            }
        }
        return notifier;
    }

    /**
     * Notifies any listeners when an object is removed.
     *
     * @param object    removed saved object
     * @param preRemove if <tt>true</tt> the object is about to be removed,
     *                  otherwise it has been removed
     */
    private void notifyRemove(IMObject object, boolean preRemove) {
        synchronized (listeners) {
            ArchetypeId id = object.getArchetypeId();
            String shortName = id.getShortName();
            List<IArchetypeServiceListener> list = listeners.get(shortName);
            if (list != null) {
                Notifier notifier = Notifier.getNotifier(this);
                if (preRemove) {
                    notifier.notifyRemoving(object, list);
                } else {
                    notifier.notifyRemoved(object, list);
                }
            }
        }
    }

}
