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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.deposit;

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;


/**
 * Deposit test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DepositTestHelper extends TestHelper {

    /**
     * Creates and saves a new deposit account.
     *
     * @return a new account
     */
    public static Party createDepositAccount() {
        Party account = (Party) create(DepositArchetypes.DEPOSIT_ACCOUNT);
        account.setName("XDepositAccount" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(account);
        bean.setValue("bank", "Westpac");
        bean.setValue("branch", "Eltham");
        bean.setValue("accountNumber", "123-456-789");
        bean.setValue("accountName", "Foo");
        bean.save();
        return account;
    }

}
