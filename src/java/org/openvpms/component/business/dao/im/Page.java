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

// java core
import java.io.Serializable;
import java.util.List;

// openvpms-framework
import org.openvpms.component.system.common.query.IPage;

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
     * The first row in the page
     */
    private int firstRow;
    
    /**
     * The number of rows that were requested
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
     *            the first row to return            
     * @param numOfRows
     *            the number of rows requested, which may not always be equal
     *            to the number of tows in the list.              
     * @param totalNumOfRows
     *            the total number of rows in the result set.                                     
     */
    public Page(List<T> rows, int firstRow, int numOfRows, int totalNumOfRows) {
        this.rows = rows;
        this.firstRow = firstRow;
        this.numOfRows = numOfRows;
        this.totalNumOfRows = totalNumOfRows;
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

    /**
     * @return Returns the firstRow.
     */
    public int getFirstRow() {
        return firstRow;
    }

    /**
     * @param firstRow The firstRow to set.
     */
    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    /**
     * @return Returns the numOfRows.
     */
    public int getNumOfRows() {
        return numOfRows;
    }

    /**
     * @param numOfRows The numOfRows to set.
     */
    public void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }
}
