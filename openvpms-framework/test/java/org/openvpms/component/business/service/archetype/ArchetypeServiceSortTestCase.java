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

import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import static org.openvpms.component.business.service.archetype.ArchetypeServiceException.ErrorCode.FailedToExecuteQuery;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

/**
 * Test that sorting part of the api works on the IArchetypeService
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServiceSortTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test sort on a non-sortable property.
     */
    @Test
    public void testSortOnNonExistentProperty() {
        try {
            ArchetypeQuery query = new ArchetypeQuery("act", null, false, false)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .add(new NodeSortConstraint("baby", true));

            get(query);
            fail("This request should have thrown an exception");
        } catch (ArchetypeServiceException exception) {
            if (exception.getErrorCode() != FailedToExecuteQuery) {
                fail(exception.getErrorCode() + " is not a valid exception");
            }
        }
    }

    /**
     * Test sort on name in ascending order
     */
    @Test
    public void testSortOnNameInAscendingOrder() {
        ArchetypeQuery query = new ArchetypeQuery("act", null, false, false);
        query.add(new NodeSortConstraint("name"));
        List<IMObject> objects = get(query);
        IMObject lhs = null;
        IMObject rhs;
        for (IMObject object : objects) {
            if (lhs == null) {
                lhs = object;
                continue;
            }
            rhs = object;
            if (lhs.getName() != null && rhs.getName() != null
                && lhs.getName().compareTo(rhs.getName()) == 1) {
                fail("The objects are not in ascending order lhs="
                     + lhs.getName() + " rhs=" + rhs.getName());
            }
            lhs = rhs;
            if (logger.isDebugEnabled()) {
                logger.debug("Name :" + object.getName());
            }
        }
    }

    /**
     * Test sort on name in ascending order
     */
    @Test
    public void testSortOnNameInDescendingOrder() {
        ArchetypeQuery query = new ArchetypeQuery("act", null, false, false)
                .setFirstResult(0)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(new NodeSortConstraint("name", false));
        List<IMObject> objects = get(query);
        IMObject lhs = null;
        IMObject rhs;
        for (IMObject object : objects) {
            if (lhs == null) {
                lhs = object;
                continue;
            }
            rhs = object;
            if (lhs.getName() != null && rhs.getName() != null
                && lhs.getName().compareTo(rhs.getName()) == -1) {
                fail("The objects are not in descending order lhs="
                     + lhs.getName() + " rhs=" + rhs.getName());
            }

            lhs = rhs;
            if (logger.isDebugEnabled()) {
                logger.debug("Name :" + object.getName());
            }
        }
    }

}
