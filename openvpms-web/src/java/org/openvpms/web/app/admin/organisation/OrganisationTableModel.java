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
package org.openvpms.web.app.admin.organisation;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;


/**
 * Table model for <em>party.organisation*</em> objects. Displays the archetype
 * name.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OrganisationTableModel extends BaseIMObjectTableModel<Party> {

    /**
     * Creates a new <tt>OrganisationModel</tt>.
     */
    public OrganisationTableModel() {
        super(null);
        setTableColumnModel(createTableColumnModel(true, true));
    }

}
