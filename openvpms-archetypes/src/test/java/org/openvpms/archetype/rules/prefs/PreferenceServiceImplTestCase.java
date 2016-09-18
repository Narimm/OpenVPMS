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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link PreferenceServiceImpl}.
 *
 * @author Tim Anderson
 */
public class PreferenceServiceImplTestCase extends ArchetypeServiceTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * Tests the {@link PreferenceServiceImpl#reset(User)} method.
     */
    @Test
    public void testReset() {
        PreferenceService service = new PreferenceServiceImpl(getArchetypeService(), transactionManager);
        User user = TestHelper.createUser();
        service.reset(user);  // no-op

        Preferences prefs = service.getPreferences(user, true);

        prefs.getPreference(PreferenceArchetypes.SUMMARY, "showReferral", true); // will create group

        Entity entity = service.getEntity(user);
        assertNotNull(entity);
        IMObjectBean bean = new IMObjectBean(entity);
        List<IMObject> groups = bean.getValues("groups");
        assertEquals(1, groups.size());

        // reset preferences and verify the associated entities are removed
        service.reset(user);
        assertNull(get(entity));
        assertNull(get(groups.get(0)));
    }
}
