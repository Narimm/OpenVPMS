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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.test;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Abstract base class for tests using the archetype service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-24 00:49:15Z $
 */
public abstract class ArchetypeServiceTest
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService _service;

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return _service;
    }

    /**
     * Helper to create a new object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    protected IMObject create(String shortName) {
        IMObject object = _service.create(shortName);
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
        _service.save(object);
    }

    /**
     * Helper to retrieve an object from the archetype service.
     *
     * @param ref the object reference
     * @return the corresponding object or <code>null</code> if no object
     *         is found
     */
    protected IMObject get(IMObjectReference ref) {
        return ArchetypeQueryHelper.getByObjectReference(_service, ref);
    }

    /**
     * Helper to remove an object.
     *
     * @param object the object to remove
     * @throws ArchetypeServiceException if the service cannot remove the object
     */
    protected void remove(IMObject object) {
        _service.remove(object);
    }

    /**
     * Helper to set a value on an object.
     *
     * @param object the object
     * @param node   the node name
     * @param value  the value to set
     */
    protected void setValue(IMObject object, String node, Object value) {
        ArchetypeDescriptor archetype = _service.getArchetypeDescriptor(
                object.getArchetypeId());
        assertNotNull(archetype);
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        assertNotNull(descriptor);
        descriptor.setValue(object, value);
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        _service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
        assertNotNull(_service);
    }

}
