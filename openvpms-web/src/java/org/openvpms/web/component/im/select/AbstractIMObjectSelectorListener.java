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
package org.openvpms.web.component.im.select;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.Browser;


/**
 * Abstract implementation of the {@link IMObjectSelectorListener} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectSelectorListener<T extends IMObject> implements IMObjectSelectorListener<T> {

    /**
     * Invoked when the selected object changes.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the object. May be <tt>null</tt>
     */
    public void selected(T object) {
    }

    /**
     * Invoked when the selected object changes, selected from a browser.
     * <p/>
     * This implementation delegates to {@link #selected(IMObject)}.
     *
     * @param object  the object. May be <tt>null</tt>
     * @param browser the browser
     */
    public void selected(T object, Browser<T> browser) {
        selected(object);
    }

    /**
     * Invoked to create a new object.
     * <p/>
     * This implementation is a no-op.
     */
    public void create() {
    }
}
