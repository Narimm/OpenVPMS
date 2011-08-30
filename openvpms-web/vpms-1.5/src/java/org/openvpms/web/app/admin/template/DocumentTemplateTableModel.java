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

package org.openvpms.web.app.admin.template;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.table.DescriptorTableModel;


/**
 * Table model for <em>entity.documentTemplate</em> objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */

public class DocumentTemplateTableModel extends DescriptorTableModel<Entity> {

    /**
     * Creates a <tt>DocumentTemplateTableModel</tt>.
     */
    public DocumentTemplateTableModel() {
        super(new String[]{"entity.documentTemplate"});
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"id", "name", "description", "reportType", "userLevel"};
    }
}
