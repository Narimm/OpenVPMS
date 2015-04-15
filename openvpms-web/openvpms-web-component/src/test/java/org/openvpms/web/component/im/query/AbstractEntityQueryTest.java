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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Tests the {@link AbstractEntityQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractEntityQueryTest<T extends Entity> extends AbstractQueryTest<T> {

    /**
     * Tests querying entities by id.
     */
    @Test
    public void testQueryById() {
        T entity = createObject(true);
        Query<T> query = createQuery();
        query.setValue(Long.toString(entity.getId()));

        List<IMObjectReference> matches = checkExists(entity, query, true);
        assertEquals(1, matches.size());
        remove(entity);
        matches = checkExists(entity, query, false);
        assertEquals(0, matches.size());
    }

    /**
     * Tests querying by identity, for those entities that support them.
     */
    @Test
    public void testQueryByIdentity() {
        T entity = createObject(true);
        EntityBean bean = new EntityBean(entity);
        if (bean.hasNode("identities")) {
            String[] range = bean.getArchetypeRange("identities");
            if (range.length != 0) {
                String shortName = range[0];
                EntityIdentity identity = (EntityIdentity) create(shortName);
                String value = "" + System.currentTimeMillis() + System.nanoTime();
                identity.setIdentity(value);
                entity.addIdentity(identity);

                Query<T> query = createQuery();
                query.setValue(value);  // all numeric values trigger an identity search by default.

                List<IMObjectReference> matches = checkExists(entity, query, false);
                assertEquals(0, matches.size());

                save(entity);
                matches = checkExists(entity, query, true);
                assertEquals(1, matches.size());

                remove(entity);
                matches = checkExists(entity, query, false);
                assertEquals(0, matches.size());
            }
        }
    }
}
