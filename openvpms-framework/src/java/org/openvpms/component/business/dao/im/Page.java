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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.dao.im;

import java.io.Serializable;
import java.util.List;

import org.openvpms.component.system.common.search.IPage;

/**
 * This object is used to support pagination, where a subset of the query 
 * result set is returned to the caller. The object contains the first row
 * and number of rows in the page. In addition it also contains the total number
 * of rows that the query would return if pagination was not used.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Page<T> implements Serializable, IPage<T> {
    /**
     * Default UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The first row of this page
     */
    private int firstRow;
    
    /**
     * The number of rows in this page
     */
    private int numOfRows;
    
    /**
     * The total number of rows, which is must be equal or greater than 
     * {@link #numOfRows}.
     */
    private int totalNumOfRows;
    
    /**
     * The list of rows
     */
    private List<T> rows;
    
    /**
     * Default constructor
     */
    public Page() {
        // do nothing
    }
    
    /**
     * Instantiate an instance of this object using the specified parameters
     * 
     * @param rows
     *            the rows that are part of this  page
     * @param firstRow
     *            the first row of the page
     * @param numOfRows
     *            the number of rows of the page
     * @param totalNumOfRows
     *            the total number of rows if no pagination was used                                     
     */
    public Page(List<T> rows, int firstRow, int numOfRows, int totalNumOfRows) {
        this.rows = rows;
        this.firstRow = firstRow;
        this.numOfRows = numOfRows;
        this.totalNumOfRows = totalNumOfRows;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.search.IPage#getFirstRow()
     */
    public int getFirstRow() {
        return firstRow;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.search.IPage#getNumOfRows()
     */
    public int getNumOfRows() {
        return numOfRows;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.search.IPage#getRows()
     */
    public List<T> getRows() {
        return rows;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.search.IPage#getTotalNumOfRows()
     */
    public int getTotalNumOfRows() {
        return totalNumOfRows;
    }

    /**
     * @param firstRow The firstRow to set.
     */
    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    /**
     * @param numOfRows The numOfRows to set.
     */
    public void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }

    /**
     * @param rows The rows to set.
     */
    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    /**
     * @param totalNumOfRows The totalNumOfRows to set.
     */
    public void setTotalNumOfRows(int totalNumOfRows) {
        this.totalNumOfRows = totalNumOfRows;
    }
}
