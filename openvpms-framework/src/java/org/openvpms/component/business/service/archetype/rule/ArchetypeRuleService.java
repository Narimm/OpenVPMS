/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.DelegatingArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.ruleengine.IRuleEngine;
import org.openvpms.component.business.service.ruleengine.RuleSetUriHelper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Implementation of {@link IArchetypeRuleService} that uses Spring's
 * {@link PlatformTransactionManager} to provide transaction support.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ArchetypeRuleService extends DelegatingArchetypeService implements IArchetypeRuleService {

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
    private static final Log log = LogFactory.getLog(ArchetypeRuleService.class);


    /**
     * Creates an {@link ArchetypeRuleService}.
     *
     * @param service    the archetype service to delegate requests to
     * @param rules      the rule engine
     * @param txnManager the transaction manager
     */
    public ArchetypeRuleService(IArchetypeService service, IRuleEngine rules, PlatformTransactionManager txnManager) {
        super(service);
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
     * @param facts the rule facts. May be {@code null}
     */
    public void setFacts(List<Object> facts) {
        this.facts = facts;
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its
     * archetype.
     *
     * @param object   the object to save
     * @param validate if {@code true} validate the object prior to saving it
     * @throws ArchetypeServiceException if the service cannot save the
     *                                   specified object
     * @throws ValidationException       if the specified object cannot be
     *                                   validated
     */
    @Deprecated
    public void save(final IMObject object, final boolean validate) {
        execute("save", object, new Runnable() {
            public void run() {
                getService().save(object, validate);
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
                getService().save(objects, validate);
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
                getService().remove(object);
            }
        });
    }

    /**
     * Executes an operation in a transaction, as follows:
     * <ol>
     * <li>begin transaction
     * <li>execute <em>before</em> rules for {@code object}
     * <li>execute {@code operation}
     * <li>execute <em>after</em> rules for {@code object}
     * <li>commit on success/rollback on failure
     * </ol>
     *
     * @param name      the name of the operation
     * @param object    the object that will be supplied to before and after rules
     * @param operation the operation to execute
     */
    private void execute(final String name, final IMObject object, final Runnable operation) {
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
     * Executes an operation in a transaction, with before and after rules.
     * <ol>
     * <li>begin transaction
     * <li>execute <em>before</em> rules for each object
     * <li>execute {@code operation}
     * <li>execute <em>after</em> rules for each object
     * <li>commit on success/rollback on failure
     * </ol>
     *
     * @param name    the name of the operation
     * @param objects the object that will be supplied to before and after rules
     * @param op      the operation to execute
     */
    private void execute(final String name, final Collection<? extends IMObject> objects, final Runnable op) {
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
     * @param before if {@code true} execute <em>before</em> rules, otherwise execute <em>after</em> rules
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
            localFacts.add(getService());
            localFacts.add(txnManager);
            if (facts != null) {
                localFacts.addAll(facts);
            }
            rules.executeRules(uri, localFacts);
        }
    }

}
