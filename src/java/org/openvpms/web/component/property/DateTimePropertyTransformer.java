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

package org.openvpms.web.component.property;

import java.util.Date;


/**
 * Handler for date/time nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class DateTimePropertyTransformer extends AbstractDateTimePropertyTransformer {

    /**
     * Constructs a <tt>DateTimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public DateTimePropertyTransformer(Property property) {
        super(property);
    }

    /**
     * Returns the supplied value as a date/time.
     *
     * @param value a date, time, or date/time
     * @return <tt>value</tt>
     */
    protected Date getDateTime(Date value) {
        return value;
    }

    /**
     * Returns the date.
     *
     * @return the date, or <tt>null</tt> if there is no date
     */
    protected Date getDate() {
        return (Date) getProperty().getValue();
    }
}