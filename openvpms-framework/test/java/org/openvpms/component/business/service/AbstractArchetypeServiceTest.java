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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service;

import static org.junit.Assert.assertNotNull;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Abstract base class for tests using the archetype service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractArchetypeServiceTest extends AbstractJUnit4SpringContextTests {

    /**
     * The archetype service.
     */
    @Autowired
    @Qualifier("archetypeService")
    private IArchetypeService service;

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Helper to create a new object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    protected IMObject create(String shortName) {
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Helper to create an object and wrap it in bean.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <tt>shortName</tt>.
     */
    protected IMObjectBean createBean(String shortName) {
        IMObject object = create(shortName);
        return new IMObjectBean(object);
    }

    /**
     * Helper to save an object.
     *
     * @param object the object to save
     */
    protected void save(IMObject object) {
        service.save(object);
    }

    /**
     * Helper to save a collection of objects.
     *
     * @param objects the object to save
     */
    protected <T extends IMObject> void save(T... objects) {
        save(Arrays.asList(objects));
    }

    /**
     * Helper to save a collection of objects.
     *
     * @param objects the object to save
     */
    protected <T extends IMObject> void save(Collection<T> objects) {
        service.save(objects);
    }

    /**
     * Helper to reload an object from the archetype service.
     *
     * @param object the object to reload
     * @return the corresponding object or <tt>null</tt> if no object is found
     */
    @SuppressWarnings("unchecked")
    protected <T extends IMObject> T get(T object) {
        return (T) get(object.getObjectReference());
    }

    /**
     * Helper to retrieve an object from the archetype service.
     *
     * @param ref the object reference
     * @return the corresponding object or <tt>null</tt> if no object is found
     */
    protected IMObject get(IMObjectReference ref) {
        return service.get(ref);
    }

    /**
     * Helper to get the results from a query.
     *
     * @param query the query
     * @return the query results
     */
    protected List<IMObject> get(ArchetypeQuery query) {
        return service.get(query).getResults();
    }

    /**
     * Helper to remove an object.
     *
     * @param object the object to remove
     */
    protected void remove(IMObject object) {
        service.remove(object);
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @throws org.openvpms.component.business.service.archetype.ValidationException
     *          if there are validation errors
     */
    protected void validateObject(IMObject object) {
        service.validateObject(object);
    }

    /**
     * Returns the {@link ArchetypeDescriptor} with the specified short name.
     *
     * @param shortName the short name
     * @return the descriptor corresponding to the short name, or <tt>null</tt> if none is found
     */
    protected ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        return service.getArchetypeDescriptor(shortName);
    }

}
