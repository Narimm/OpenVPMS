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

package org.openvpms.component.business.service.archetype.helper;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link SingletonIMObjectCache}.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class SingletonIMObjectCacheTestCase extends AbstractArchetypeServiceTest {

    /**
     * Verifies that the {@link SingletonIMObjectCache} monitors singletons.
     */
    @Test
    public void testSingletonCache() {
        IArchetypeService service = Mockito.spy(getArchetypeService());

        // remove the existing singleton(s)
        ArchetypeQuery query = new ArchetypeQuery("party.singleton");
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        IMObjectQueryIterator<Party> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            remove(iterator.next());
        }

        // create a singleton
        Party first = createSingleton("first");

        // create the cache, and verify the singleton is returned
        SingletonIMObjectCache<Party> cache = new SingletonIMObjectCache<>(service, "party.singleton", Party.class);
        assertEquals(first, cache.getObject());

        // create a second object. This should be ignored.
        Party second = createSingleton("second");
        assertEquals(first, cache.getObject());

        // mark the first object inactive. The second should now be returned.
        first.setActive(false);
        save(first);
        assertEquals(second, cache.getObject());

        // verify the cache sees updates
        second.setName("updated");
        save(second);
        assertEquals("updated", cache.getObject().getName());

        // re-activate the first object. This will be picked up as it has a lower id
        first.setActive(true);
        save(first);
        assertEquals(first, cache.getObject());

        // de-activate the first again. The second should be returned.
        first.setActive(false);
        save(first);
        assertEquals(second, cache.getObject());

        // now remove the second. The should be no active objects to return
        remove(second);
        assertNull(cache.getObject());

        // re-activate the first object.
        first.setActive(true);
        save(first);
        assertEquals(first, cache.getObject());

        // verify that the listener is removed from the archetype service, when the cache is destroyed.
        Mockito.verify(service, Mockito.times(0)).removeListener(Mockito.eq("party.singleton"), Mockito.any());
        cache.destroy();
        Mockito.verify(service).removeListener(Mockito.eq("party.singleton"), Mockito.any());
    }

    /**
     * Creates and saves a <em>party.singleton</em>.
     *
     * @param name the name
     * @return a new object
     */
    private Party createSingleton(String name) {
        IMObject object = create("party.singleton");
        object.setName(name);
        save(object);
        return (Party) object;
    }

}
