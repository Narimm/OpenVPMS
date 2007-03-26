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

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;


/**
 * Maintains the state of a loaded object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadState {

    /**
     * The reference to the object.
     */
    private final IMObjectReference reference;

    /**
     * The object. May be <tt>null</tt>.
     */
    private IMObject object;

    /**
     * The helper bean. May be <tt>null</tt>
     */
    private IMObjectBean bean;


    /**
     * Constructs a new <tt>LoadState</tt>.
     *
     * @param object  the object
     * @param service the archetype service
     */
    public LoadState(IMObject object, IArchetypeService service) {
        reference = object.getObjectReference();
        this.object = object;
        this.bean = new IMObjectBean(object, service);
    }

    /**
     * Returns a reference to the object.
     *
     * @return a reference to the object
     */
    public IMObjectReference getRef() {
        return reference;
    }

    /**
     * Returns the object.
     *
     * @return the object, or <tt>null</tt> if {@link #setNull()} has been
     *         invoked
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the helper bean.
     *
     * @return the helper bean or <tt>null</tt> if {@link #setNull()} has been
     *         invoked
     */
    public IMObjectBean getBean() {
        return bean;
    }

    /**
     * Removes the object and bean to enable them to be garbage collected.
     * The {@link #getRef()} still returns the reference to the object.
     */
    public void setNull() {
        object = null;
        bean = null;
    }
}
