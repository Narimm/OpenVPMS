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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.util;

import org.apache.commons.lang.ObjectUtils;

import java.util.Date;


/**
 * Helper to determine if there has been a double click.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DoubleClickMonitor {

    /**
     * The default click interval, i.e. 2 seconds.
     */
    public static final int DEFAULT_INTERVAL = 2000;

    /**
     * The last clicked on object.
     */
    private Object last;

    /**
     * The time of the last click.
     */
    private Date lastClick;

    /**
     * The click interval.
     */
    private final int interval;


    /**
     * Constructs a <tt>DoubleClickMonitor</tt> with the default interval.
     */
    public DoubleClickMonitor() {
        this(DEFAULT_INTERVAL);
    }

    /**
     * Constructs a <tt>DoubleClickMonitor</tt> with the specified interval.
     *
     * @param interval the maximum interval between clicks
     */
    public DoubleClickMonitor(int interval) {
        this.interval = interval;
    }

    /**
     * Determines if there has been a double click on an object.
     * <p/>
     * It is considered a double click if the method is called twice with the same (i.e using equals()) object,
     * within the specified interval.
     *
     * @param object the clicked on object. May be <tt>null</tt>
     * @return true if the object has been clicked twice within the interval
     */
    public boolean isDoubleClick(Object object) {
        boolean result;
        Date now = new Date();
        result = (lastClick != null && (lastClick.getTime() + interval) >= now.getTime());
        result = result && ObjectUtils.equals(last, object);
        lastClick = now;
        last = object;
        return result;

    }

    /**
     * Determines if there has been a double click.
     * <p/>
     * It is considered a double click if the method is called twice within the specified interval.
     *
     * @return <tt>true</tt> if there has been a double click, otherwise <tt>false</tt>
     */
    public boolean isDoubleClick() {
        return isDoubleClick(null);
    }

}
