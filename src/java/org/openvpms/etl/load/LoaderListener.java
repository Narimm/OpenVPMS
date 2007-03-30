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


/**
 * Listener for loader events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface LoaderListener {

    /**
     * Indicates start of a load.
     */
    void start();

    /**
     * Adds a new object.
     *
     * @param object  the object
     * @param context the object's context. May be <tt>null</tt>
     */
    void add(IMObject object, Context context);

    /**
     * Indicates a load error.
     *
     * @param object    the object. May be <tt>null</tt>
     * @param exception the exception
     * @param context   the object's context. May be <tt>null</tt>
     * @return <tt>true</tt> if loading should proceed, or <tt>false</tt>
     *         if it should be aborted
     */
    boolean error(IMObject object, Throwable exception, Context context);

    /**
     * Flush any unsaved objects.
     */
    void flush();

    /**
     * Indicates end of a load.
     */
    void end();

    /**
     * Returns the no. of processed objects.
     *
     * @return the no. of processed objects
     */
    int getCount();
}
