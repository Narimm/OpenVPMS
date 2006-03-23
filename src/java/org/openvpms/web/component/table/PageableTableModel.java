package org.openvpms.web.component.table;

import nextapp.echo2.app.table.TableModel;


/**
 * Pageable table model.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
    int getRows();
}
