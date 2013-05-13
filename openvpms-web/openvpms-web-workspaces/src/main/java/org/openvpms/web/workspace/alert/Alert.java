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
package org.openvpms.web.workspace.alert;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;


/**
 * Associates an alert type lookup <em>lookup.customerAlertType</em> with an optional alert act.
 * <p/>
 * Implements <tt>Comparable</tt> to order alerts on priority.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Alert implements Comparable<Alert> {

    /**
     * The alert priority. Corresponds to the <em>lookup.customerAlertType</em>/<em>lookup.patientAlertType</em>
     * priority.
     */
    public enum Priority {

        HIGH,
        MEDIUM,
        LOW
    }

    /**
     * The alert type.
     */
    private final Lookup lookup;

    /**
     * The alert. May be <tt>null</tt>.
     */
    private Act alert;

    /**
     * Constructs an <tt>Alerts</tt>.
     *
     * @param lookup the alert type
     */
    public Alert(Lookup lookup) {
        this(lookup, null);
    }

    /**
     * Constructs an <tt>Alerts</tt>.
     *
     * @param lookup the alert type
     * @param alert  the alert act. May be <tt>null</tt>
     */
    public Alert(Lookup lookup, Act alert) {
        this.lookup = lookup;
        this.alert = alert;
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type
     */
    public Lookup getAlertType() {
        return lookup;
    }

    /**
     * Returns the alert.
     *
     * @return the alert. May be <tt>null</tt>
     */
    public Act getAlert() {
        return alert;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param object the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    public int compareTo(Alert object) {
        int result = 0;
        Lookup alertType1 = getAlertType();
        IMObjectBean bean1 = new IMObjectBean(alertType1);
        Lookup alertType2 = object.getAlertType();
        if (!ObjectUtils.equals(alertType1, alertType2)) {
            IMObjectBean bean2 = new IMObjectBean(alertType2);
            Priority priority1 = Priority.valueOf(bean1.getString("priority"));
            Priority priority2 = Priority.valueOf(bean2.getString("priority"));
            result = priority1.compareTo(priority2);
            if (result == 0) {
                result = new Long(alertType1.getId()).compareTo(alertType2.getId());
            }
        }
        return result;
    }
}
