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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.test.AbstractAppTest;


/**
 * Base class for {@link ResultSet} test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractResultSetTest extends AbstractAppTest {

    /**
     * Helper to create a new <code>act.customerEstimation</code>, and save it.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param product  the product
     * @return a new act
     */
    protected Act createEstimation(Party customer, Party patient,
                                   Product product) {
        Act act = createAct("act.customerEstimation");
        act.setStatus(FinancialActStatus.IN_PROGRESS);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);

        Act child = createAct("act.customerEstimationItem");
        child.setStatus(FinancialActStatus.IN_PROGRESS);

        ActBean childBean = new ActBean(child);
        childBean.addParticipation("participation.patient", patient);
        childBean.addParticipation("participation.product", product);
        bean.addRelationship("actRelationship.customerEstimationItem", child);

        save(act, child);
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected Act createAct(String shortName) {
        return (Act) create(shortName);
    }

}
