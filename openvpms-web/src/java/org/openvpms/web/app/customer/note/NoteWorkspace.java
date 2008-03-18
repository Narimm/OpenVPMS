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

package org.openvpms.web.app.customer.note;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.customer.CustomerActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;


/**
 * Customer note workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NoteWorkspace extends CustomerActWorkspace<Act> {

    /**
     * Short name supported by the workspace.
     */
    private static final String SHORT_NAME = "act.customerNote";


    /**
     * Constructs a new <tt>NoteWorkspace</tt>.
     */
    public NoteWorkspace() {
        super("customer", "note");
        setChildArchetypes(Act.class, SHORT_NAME);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new NoteCRUDWindow(getChildArchetypes());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<Act> createQuery() {
        return new NoteQuery(getObject());
    }

}
