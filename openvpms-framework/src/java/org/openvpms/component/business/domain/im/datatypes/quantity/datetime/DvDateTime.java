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


package org.openvpms.component.business.domain.im.datatypes.quantity.datetime;

// openvpms-framework
import java.util.Calendar;

import org.openvpms.component.business.domain.im.datatypes.basic.ComparableDataValue;

/**
 * This class is used to represent a date and a time. It is also comparable.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class DvDateTime extends ComparableDataValue {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The date and time are stored as a calendar
     */
    private Calendar dateTime;
    
    
    /**
     * Create an instance using the current date and time
     */
    public DvDateTime() {
        this.dateTime = Calendar.getInstance();
    }
    
    /**
     * Create an instance using a {@link Calendar} instance
     * 
     * @param dateTime
     */
    public DvDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }
    

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(Object o) {
        // if they are the same object then they are equal
        if (this == o) {
            return 0;
        }
        
        // if the are different objects then always return -1
        if (!(o instanceof DvDateTime)) {
            return -1;
        }
        
        DvDateTime dt = (DvDateTime)o;
        return this.dateTime.compareTo(dt.dateTime);
    }

    /**
     * @return Returns the dateTime.
     */
    public Calendar getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime The dateTime to set.
     */
    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }
}
