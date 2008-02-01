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

package org.openvpms.component.business.service.ruleengine;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Test rules for {@link DroolsRuleEngineTestCase}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActSimpleRules {

    /**
     * The transaction template.
     */
    private static TransactionTemplate template;

    /**
     * Sets the transaction manager.
     *
     * @param manager the transaction manager
     */
    public static void setTransactionManager(
            PlatformTransactionManager manager) {
        template = new TransactionTemplate(manager);
        template.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Creates and saves a new act in a new transaction, associating it
     * with the specified act as the source of the act relationship.
     *
     * @param act the act
     * @param service the archetype service
     * @see DroolsRuleEngineTestCase#testTransactionIsolation
     */
    public static void insertNewActInIsolation(Act act,
                                               final IArchetypeService service) {
        final Act related = (Act) service.create("act.simple");
        ActRelationship relationship
                = (ActRelationship) service.create("actRelationship.simple");
        relationship.setName("a simple relationship");
        relationship.setSource(related.getObjectReference());
        relationship.setTarget(act.getObjectReference());
        related.addActRelationship(relationship);
        act.addActRelationship(relationship);

        // save the new act in a new transaction, suspending any current
        // transaction
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                service.save(related);
                return null;
            }
        });
    }
}
