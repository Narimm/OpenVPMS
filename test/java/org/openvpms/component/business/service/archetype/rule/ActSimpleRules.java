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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Test rules for {@link ArchetypeRuleServiceTestCase}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActSimpleRules {

    /**
     * Creates and saves a new act in a new transaction.
     * On return the supplied act will have a new unsaved relationship to the
     * new act.
     *
     * @param act     the act
     * @param service the archetype service
     * @param manager the transaction manager
     * @see ArchetypeRuleServiceTestCase#testTransactionIsolation
     */
    public static void insertNewActInIsolation(Act act,
                                               final IArchetypeService service,
                                               PlatformTransactionManager manager) {
        final Act related = (Act) service.create("act.simple");

        TransactionTemplate template = new TransactionTemplate(manager);
        template.setPropagationBehavior(
                TransactionTemplate.PROPAGATION_REQUIRES_NEW);

        // save the new act in a new transaction, suspending any current
        // transaction
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                service.save(related);
                return null;
            }
        });

        ActRelationship relationship
                = (ActRelationship) service.create("actRelationship.simple");
        relationship.setName("a simple relationship");
        relationship.setSource(related.getObjectReference());
        relationship.setTarget(act.getObjectReference());

        related.addActRelationship(relationship);
        act.addActRelationship(relationship);
    }
}
