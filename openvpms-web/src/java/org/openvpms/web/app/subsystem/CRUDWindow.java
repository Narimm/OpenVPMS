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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface CRUDWindow<T extends IMObject> {

    /**
     * Sets the event listener.
     *
     * @param listener the event listener.
     */
    void setListener(CRUDWindowListener<T> listener);

    /**
     * Returns the event listener.
     *
     * @return the event listener
     */
    CRUDWindowListener<T> getListener();

    /**
     * Returns the component representing this.
     *
     * @return the component
     */
    Component getComponent();

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    void setObject(T object);

    /**
     * Returns the object.
     *
     * @return the object, or <code>null</code> if there is none set
     */
    T getObject();

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or <code>null</code> if there
     *         is no object set
     */
    ArchetypeDescriptor getArchetypeDescriptor();

    /**
     * Creates and edits a new object.
     */
    void create();

    /**
     * Edits the current object.
     */
    void edit();
}
