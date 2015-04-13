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

package org.openvpms.component.system.common.query;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;

import java.util.Date;


/**
 * Tests the {@link IterableQuery} classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-04 06:59:40Z $
 */
public class IterableQueryTestCase extends AbstractQueryTest {

    /**
     * Tests the {@link IterableIMObjectQuery}.
     */
    @Test
    public void testIterableIMObjectQuery() {
        ArchetypeQuery query = createQuery();

        Check<Act> check = new Check<Act>() {
            public void check(Act object) {
                // no-op
            }
        };
        IterableQuery<Act> iterator = new IterableIMObjectQuery<Act>(query);
        checkIterator(iterator.iterator(), check);

        // second iteration
        checkIterator(iterator.iterator(), check);
    }

    /**
     * Tests the {@link IterableObjectSetQuery}.
     */
    @Test
    public void testIterableObjectSetQuery() {
        ArchetypeQuery query = createQuery();
        query.add(new NodeSelectConstraint("act.startTime"));

        Check<ObjectSet> check = new Check<ObjectSet>() {
            public void check(ObjectSet set) {
                Date startTime = (Date) set.get("act.startTime");
                assertNotNull(startTime);
            }
        };
        IterableQuery<ObjectSet> iterator = new IterableObjectSetQuery(query);
        checkIterator(iterator.iterator(), check);

        // second iteration
        checkIterator(iterator.iterator(), check);
    }

}
