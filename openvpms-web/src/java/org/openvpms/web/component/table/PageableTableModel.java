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
     * Determines if a page exists.
     *
     * @param page the page
     * @return <tt>true</tt> if the page exists, otherwise <tt>false</tt>
     */
    boolean hasPage(int page);

    /**
     * Attempts to set the current page.
     *
     * @param page the page to set
     * @return <tt>true</tt> if the page was set, or <tt>false</tt> if there
     *         is no such page
     */
    boolean setPage(int page);

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    int getPage();

    /**
     * Returns the total number of pages.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedPages()}.
     *
     * @return the total no. of pages.
     */
    int getPages();

    /**
     * Returns an estimation of the total no. of pages.
     *
     * @return an estimation of the total no. of pages
     */
    int getEstimatedPages();

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedPages()} would return the same as
     * {@link #getPages()}.
     *
     * @return <tt>true</tt> if the estimated pages equals the actual no.
     *         of pages
     */
    boolean isEstimatedActual();

    /**
     * Returns the number of rows per page.
     *
     * @return the number. of rows per page
     */
    int getRowsPerPage();

    /**
     * Returns the total number of rows.
     * This is the same as invoking <tt>getPages(true)</tt>.
     * <p><em>NOTE: </em> the {@link #getRowCount} method returns the number of
     * visible rows.
     *
     * @return the total number of rows
     */
    int getResults();

    /**
     * Returns the total number of results matching the query criteria.
     *
     * @param force if <tt>true</tt>, force a calculation of the total no. of
     *              results
     * @return the total no. of results, or <tt>-1</tt> if the no. isn't known
     */
    int getResults(boolean force);

}
