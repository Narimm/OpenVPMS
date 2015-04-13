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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;


/**
 * Handler for {@link Context} events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ContextHandler {

    /**
     * Invoked prior to commit.
     *
     * @param context the assembly context
     */
    void preCommit(Context context);

    /**
     * Invoked after commit.
     *
     * @param context the assembly context
     */
    void commit(Context context);

    /**
     * Invoked after rollback.
     *
     * @param context the assembly context
     */
    void rollback(Context context);

}
