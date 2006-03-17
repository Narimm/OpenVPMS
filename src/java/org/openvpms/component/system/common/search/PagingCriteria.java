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


package org.openvpms.component.system.common.search;

import java.io.Serializable;

/**
 * Used to specify the paging criteria for a search. A paging criteria
 * includes a firstRow and a numOfRows
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PagingCriteria implements Serializable {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates that all the rows should be returned
     */
    public final static int ALL_ROWS = -1;

    /**
     * The first row in the page
     */
    private int firstRow;
    
    /**
     * The number of rows in the page
     */
    private int numOfRows;

    /**
     * Construct a paging criteria using the following
     * 
     * @param firstRow
     *            the first row in the page
     * @param numOfRows
     *            the number of rows in the page
     */
    public PagingCriteria(int firstRow, int numOfRows) {
        this.firstRow = firstRow;
        this.numOfRows = numOfRows;
    }

    /**
     * @return Returns the firstRow.
     */
    public int getFirstRow() {
        return firstRow;
    }

    /**
     * @return Returns the numOfRows.
     */
    public int getNumOfRows() {
        return numOfRows;
    }
}
