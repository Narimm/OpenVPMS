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

package org.openvpms.web.component.table;

import nextapp.echo2.app.table.TableModel;


/**
 * Pageable table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface PageableTableModel extends TableModel {

    /**
     * Sets the current page.
     *
     * @param page the page to set
     */
    void setPage(int page);

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    int getPage();

    /**
     * Returns the total number of pages.
     *
     * @return the total number of pages
     */
    int getPages();

    /**
     * Returns the number of rows per page.
     *
     * @return the number. of rows per page
     */
    int getRowsPerPage();

    /**
     * Returns the total number of rows.
     * <em>NOTE: </em> the {@link #getRowCount} method returns the number of
     * visible rows.
     *
     * @return the total number of rows
     */
    int getResults();
}
