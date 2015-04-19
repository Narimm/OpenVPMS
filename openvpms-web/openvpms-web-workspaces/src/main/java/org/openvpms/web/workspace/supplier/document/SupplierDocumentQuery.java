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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.workspace.supplier.document;

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * A query for
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SupplierDocumentQuery<T extends Act> extends DateRangeActQuery<T> {

    /**
     * Supplier document archetypes.
     */
    public static final String[] SHORT_NAMES = {SupplierArchetypes.DOCUMENT_FORM, SupplierArchetypes.DOCUMENT_LETTER,
        SupplierArchetypes.DOCUMENT_ATTACHMENT};

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES = new ActStatuses(SupplierArchetypes.DOCUMENT_LETTER);


    /**
     * Constructs a <tt>SupplierDocumentQuery</tt>.
     *
     * @param supplier the supplier to search for
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public SupplierDocumentQuery(Entity supplier) {
        super(supplier, "supplier", SupplierArchetypes.SUPPLIER_PARTICIPATION, SHORT_NAMES, STATUSES, Act.class);
    }
}
