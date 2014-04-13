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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.test;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Abstract base class for tests using the archetype service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-24 00:49:15Z $
 */
@ContextConfiguration("/applicationContext.xml")
public abstract class ArchetypeServiceTest extends AbstractJUnit4SpringContextTests {

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
     * Helper to save an object.
     *
     * @param object the object to save
     * @throws ArchetypeServiceException if the service cannot save the object
     * @throws ValidationException       if the object cannot be validated
     */
    protected void save(IMObject object) {
        service.save(object);
    }

    /**
     * Helper to save a collection of objects.
     *
     * @param objects the object to save
     * @throws ArchetypeServiceException if the service cannot save the objects
     * @throws ValidationException       if the object cannot be validated
     */
    protected <T extends IMObject> void save(T... objects) {
        save(Arrays.asList(objects));
    }

    /**
     * Helper to save a collection of objects.
     *
     * @param objects the object to save
     * @throws ArchetypeServiceException if the service cannot save the objects
     * @throws ValidationException       if the object cannot be validated
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
     * Helper to reload an act.
     *
     * @param bean the act bean
     * @return the reloaded act, or <tt>null</tt> if none is found
     */
    protected ActBean get(ActBean bean) {
        Act act = (Act) get(bean.getAct().getObjectReference());
        return (act != null) ? new ActBean(act) : null;
    }

    /**
     * Helper to remove an object.
     *
     * @param object the object to remove
     * @throws ArchetypeServiceException if the service cannot remove the object
     */
    protected void remove(IMObject object) {
        service.remove(object);
    }

    /**
     * Verifies two <tt>BigDecimals</tt> are equal.
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    protected void checkEquals(BigDecimal expected, BigDecimal actual) {
        if (expected.compareTo(actual) != 0) {
            fail("Expected " + expected + ", but got " + actual);
        }
    }

}
