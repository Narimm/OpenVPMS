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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.PropertyEditor;


/**
 * Editor for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-10-18 06:59:36Z $
 */
public interface IMObjectReferenceEditor<T extends IMObject>
        extends PropertyEditor {

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object. May  be <code>null</code>
     */
    void setObject(T object);

    /**
     * Returns the component.
     *
     * @return the component
     */
    Component getComponent();

    /**
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return <code>true</code>  if the reference is null; otherwise
     *         <code>false</code>
     */
    boolean isNull();

    /**
     * Determines if objects may be created.
     *
     * @param create if <code>true</code>, objects may be created
     */
    void setAllowCreate(boolean create);

    /**
     * Determines if objects may be created.
     *
     * @return <code>true</code> if objects may be created
     */
    boolean allowCreate();

}