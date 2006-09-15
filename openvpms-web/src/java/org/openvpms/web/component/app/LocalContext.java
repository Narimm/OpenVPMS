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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Local context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocalContext extends AbstractContext {

    /**
     * The parent context.
     */
    private final Context parent;

    /**
     * Constructs a new <code>LocalContext</code>, with the
     * {@link GlobalContext} as the immediate parent.
     */
    public LocalContext() {
        this(ContextApplicationInstance.getInstance().getContext());
    }

    /**
     * Constructs a new <code>LocalContext</code>, with the specified parent
     * context.
     *
     * @param parent the parent context
     */
    public LocalContext(Context parent) {
        this.parent = parent;
    }

    /**
     * The current schedule date.
     *
     * @return the current schedule date
     */
    @Override
    public Date getScheduleDate() {
        Date date = super.getScheduleDate();
        if (date == null) {
            date = parent.getScheduleDate();
        }
        return date;
    }

    /**
     * Returns the current work lsit date.
     *
     * @return the current work lsit date
     */
    @Override
    public Date getWorkListDate() {
        Date date = super.getWorkListDate();
        if (date == null) {
            date = parent.getWorkListDate();
        }
        return date;
    }

    /**
     * Returns an object for the specified key.
     *
     * @param key the context key
     * @return the object corresponding to <code>key</code> or
     *         <code>null</code> if none is found
     */
    @Override
    public IMObject getObject(String key) {
        IMObject result = super.getObject(key);
        if (result == null) {
            result = parent.getObject(key);
        }
        return result;
    }

    /**
     * Helper to return the context objects in an array.
     *
     * @return the a list of the context objects
     */
    @Override
    public IMObject[] getObjects() {
        Set<IMObject> objects
                = new HashSet<IMObject>(Arrays.asList(parent.getObjects()));
        objects.addAll(Arrays.asList(super.getObjects()));
        return objects.toArray(new IMObject[0]);
    }

}
