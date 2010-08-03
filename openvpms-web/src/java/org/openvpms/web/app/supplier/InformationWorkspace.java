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

package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BasicCRUDWorkspace;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.edit.EditListBrowserDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.resource.util.Messages;


/**
 * Supplier information workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends BasicCRUDWorkspace<Party> {

    /**
     * Construct a new <tt>InformationWorkspace</tt>.
     */
    public InformationWorkspace() {
        super("supplier", "info",
              Archetypes.create("party.supplier*", Party.class,
                                Messages.get("supplier.info.type")));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        GlobalContext.getInstance().setSupplier(object);
    }


    /**
     * Creates a new dialog to select an object.
     *
     * @param browser the browser
     * @return a new dialog
     */
    @Override
    protected BrowserDialog<Party> createBrowserDialog(Browser<Party> browser) {
        String title = Messages.get("imobject.select.title", getArchetypes().getDisplayName());
        return new EditListBrowserDialog<Party>(title, browser);
    }

}
