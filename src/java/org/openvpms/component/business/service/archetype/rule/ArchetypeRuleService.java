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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.ruleengine.IRuleEngine;
import org.openvpms.component.business.service.ruleengine.RuleSetUriHelper;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Implementation of {@link IArchetypeRuleService} that uses Spring's
 * {@link PlatformTransactionManager} to provide transaction support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ArchetypeRuleService implements IArchetypeRuleService {

    /**
     * The archetype service to delegate to.
     */
    private final IArchetypeService service;

    /**
     * The rule service.
     */
    private final IRuleEngine rules;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager txnManager;

    /**
     * Transaction helper.
     */
    private final TransactionTemplate template;

    /**
     * The facts to supply to all rules.
     */
    private List<Object> facts;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            ArchetypeRuleService.class);


    /**
     * Creates a new <tt>ArchetypeRuleService</tt>.
     *
     * @param service    the archetype service to delegate requests to
     * @param rules      the rule engine
     * @param txnManager the transaction manager
     */
    public ArchetypeRuleService(IArchetypeService service, IRuleEngine rules,
                                PlatformTransactionManager txnManager) {
        this.service = service;
        this.rules = rules;
        this.txnManager = txnManager;
        this.template = new TransactionTemplate(txnManager);
    }

    /**
     * Sets a list of facts to pass to all rules.
     * <p/>
     * These are supplied in addition to the underlying archetype service
     * and transaction manager.
     * <p/>
     * NOTE: this method is not thread safe. It is not intended to be used
     * beyond the initialisation of the service
     *
     * @param facts the rule facts. May be <tt>null</tt>
     */
    public void setFacts(List<Object> facts) {
        this.facts = facts;
    }

    /**
     * Returns the {@link ArchetypeDescriptor} with the specified short name.
     *
     * @param shortName the short name
     * @return the descriptor corresponding to the short name, or
     *         <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        return service.getArchetypeDescriptor(shortName);
    }

    /**
     * Returns the {@link ArchetypeDescriptor} for the specified
     * {@link ArchetypeId}.
     *
     * @param id the archetype identifier
     * @return the archetype descriptor corresponding to <tt>id</tt> or
     *         <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return service.getArchetypeDescriptor(id);
    }

    /**
     * Create a domain object given a short name. The short name is a reference
     * to an {@link ArchetypeDescriptor}.
     *
     * @param shortName the short name
     * @return a new object, or <tt>null</tt> if there is no corresponding
     *         archetype descriptor for <tt>shortName</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(String shortName) {
        return service.create(shortName);
    }

    /**
     * Create a domain object given an {@link ArchetypeId}.
     *
     * @param id the archetype id
     * @return a new object, or <tt>null</tt> if there is no corresponding
     *         archetype descriptor for <tt>shortName</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(ArchetypeId id) {
        return service.create(id);
    }

    /**
     * Validate the specified {@link IMObject}. To validate the object it will
     * retrieve the archetype and iterate through the assertions.
     *
     * @param object the object to validate
     * @throws ValidationException if there are validation errors
     */
    public void validateObject(IMObject object) {
        service.validateObject(object);
    }

    /**
     * Derived values for the specified {@link IMObject}, based on its
     * corresponding {@link ArchetypeDescriptor}.
     *
     * @param object the object to derived values for
     * @throws ArchetypeServiceException if values cannot be derived
     */
    public void deriveValues(IMObject object) {
        service.deriveValues(object);
    }

    /**
     * Derive the value for the {@link NodeDescriptor} with the specified
     * name.
     *
     * @param object the object to operate on.
     * @param node   the name of the {@link NodeDescriptor}, which will be used
     *               to derive the value
     * @throws ArchetypeServiceException if the value cannot be derived
     */
    public void deriveValue(IMObject object, String node) {
        service.deriveValue(object, node);
    }

    /**
     * Returns all the {@link ArchetypeDescriptor} managed by this service.
     *
     * @return the archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return service.getArchetypeDescriptors();
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the
     * specified shortName.
     *
     * @param shortName the short name. May contain wildcards
     * @return a list of matching archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        return service.getArchetypeDescriptors(shortName);
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instance with the specified
     * reference model name.
     *
     * @param rmName the reference model name
     * @return a list of matching archetype descriptors
     * @throws ArchetypeServiceException for any error
     * @deprecated no replacement
     */
    @Deprecated
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(
            String rmName) {
        return service.getArchetypeDescriptors(rmName);
    }

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return the assertion type descriptor corresponding to <tt>name</tt>
     *         or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return service.getAssertionTypeDescriptor(name);
    }

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by
     * this service.
     *
     * @return the assertion type descriptors
     * @throws ArchetypeServiceException for any error
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return service.getAssertionTypeDescriptors();
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its
     * archetype.
     *
     * @param object the object to save
     * @throws ArchetypeServiceException if the service cannot save the
     *                                   specified object
     * @throws ValidationException       if the object cannot be validated
     */
    public void save(IMObject object) {
        save(object, true);
    }

    /**
     * Save a collection of {@link IMObject} instances. executing any
     * <em>save</em> rules associated with their archetypes.
     * <p/>
     * Rules will be executed in the order that the objects are supplied.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    public void save(Collection<? extends IMObject> objects) {
        save(objects, true);
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its
     * archetype.
     *
     * @param object   the object to save
     * @param validate if <tt>true</tt> validate the object prior to saving it
     * @throws ArchetypeServiceException if the service cannot save the
     *                                   specified object
     * @throws ValidationException       if the specified object cannot be
     *                                   validated
     */
    @Deprecated
    public void save(final IMObject object, final boolean validate) {
        execute("save", object, new Runnable() {
            public void run() {
                service.save(object, validate);
            }
        });
    }

    /**
     * Save a collection of {@link IMObject} instances, executing any
     * <em>save</em> rules associated with their archetypes.
     *
     * @param objects  the objects to insert or update
     * @param validate whether to validate or not
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    @Deprecated
    public void save(final Collection<? extends IMObject> objects,
                     final boolean validate) {
        execute("save", objects, new Runnable() {
            public void run() {
                service.save(objects, validate);
            }
        });
    }

    /**
     * Removes an object, executing any <em>remove</em> rules associated with
     * its archetype.
     *
     * @param object the object to remove
     * @throws ArchetypeServiceException if the object cannot be removed
     */
    public void remove(final IMObject object) {
        execute("remove", object, new Runnable() {
            public void run() {
                service.remove(object);
            }
        });
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException if the query fails
     */
    public IMObject get(IMObjectReference reference) {
        return service.get(reference);
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<IMObject> get(IArchetypeQuery query) {
        return service.get(query);
    }

    /**
     * Retrieves partially populated objects that match the query.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <tt>nodes</tt> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<IMObject> get(IArchetypeQuery query,
                               Collection<String> nodes) {
        return service.get(query, nodes);
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
        return service.getObjects(query);
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
        return service.getNodes(query, nodes);
    }

    /**
     * Return a list of archtype short names given the specified criteria.
     *
     * @param rmName      the reference model name
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     * @see #getArchetypeShortNames(String entityName, String conceptName,
     *      boolean primaryOnly)
     * @deprecated
     */
    @Deprecated
    public List<String> getArchetypeShortNames(String rmName, String entityName,
                                               String conceptName,
                                               boolean primaryOnly) {
        return service.getArchetypeShortNames(rmName, entityName, conceptName,
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
        return service.getArchetypeShortNames(entityName, conceptName,
                                              primaryOnly);
    }

    /**
     * Return all the archetype short names.
     *
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public List<String> getArchetypeShortNames() {
        return service.getArchetypeShortNames();
    }

    /**
     * Return all the archetypes which match the specified short name.
     *
     * @param shortName   the short name, which may contain a wildcard character
     * @param primaryOnly return only the primary archetypes
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public List<String> getArchetypeShortNames(String shortName,
                                               boolean primaryOnly) {
        return service.getArchetypeShortNames(shortName, primaryOnly);
    }

    /**
     * Execute the rule specified by the uri and using the passed in
     * properties and facts.
     *
     * @param ruleUri the rule uri
     * @param props   a set of properties that can be used by the rule engine
     * @param facts   a list of facts that are asserted in to the working memory
     * @return a list objects. May be an empty list.
     * @throws ArchetypeServiceException if it cannot execute the specified rule
     */
    public List<Object> executeRule(String ruleUri, Map<String, Object> props,
                                    List<Object> facts) {
        return service.executeRule(ruleUri, props, facts);
    }

    /**
     * Executes an operation in a transaction, as follows:
     * <ol>
     * <li>begin transaction
     * <li>execute <em>before</em> rules for <tt>object</tt>
     * <li>execute <tt>operation</tt>
     * <li>execute <em>after</em> rules for <tt>object</tt>
     * <li>commit on success/rollback on failure
     * </ol>
     *
     * @param name      the name of the operation
     * @param object    the object that will be supplied to before and after rules
     * @param operation the operation to execute
     */
    private void execute(final String name, final IMObject object,
                         final Runnable operation) {
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                executeRules(name, object, true);
                operation.run();
                executeRules(name, object, false);
                return null;
            }
        });
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
        service.addListener(shortName, listener);
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
        service.removeListener(shortName, listener);
    }

    /**
     * Executes an operation in a transaction, with before and after rules.
     * <ol>
     * <li>begin transaction
     * <li>execute <em>before</em> rules for each object
     * <li>execute <tt>operation</tt>
     * <li>execute <em>after</em> rules for each object
     * <li>commit on success/rollback on failure
     * </ol>
     *
     * @param name    the name of the operation
     * @param objects the object that will be supplied to before and after rules
     * @param op      the operation to execute
     */
    private void execute(final String name,
                         final Collection<? extends IMObject> objects,
                         final Runnable op) {
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                for (IMObject object : objects) {
                    executeRules(name, object, true);
                }
                op.run();
                for (IMObject object : objects) {
                    executeRules(name, object, false);
                }
                return null;
            }
        });
    }

    /**
     * Executes rules for an object.
     *
     * @param name   the operation name
     * @param object the object to  execute rules for
     * @param before if <tt>true</tt> execute <em>before</em> rules, otherwise
     *               execute <em>after</em> rules
     */
    private void executeRules(String name, IMObject object, boolean before) {
        String uri = RuleSetUriHelper.getRuleSetURI(
                "archetypeService", name, before,
                object.getArchetypeId().getShortName());
        if (rules.hasRules(uri)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing rules for uri=" + uri);
            }
            List<Object> localFacts = new ArrayList<Object>();
            localFacts.add(object);
            localFacts.add(service);
            localFacts.add(txnManager);
            if (facts != null) {
                localFacts.addAll(facts);
            }
            rules.executeRules(uri, localFacts);
        }
    }

}
