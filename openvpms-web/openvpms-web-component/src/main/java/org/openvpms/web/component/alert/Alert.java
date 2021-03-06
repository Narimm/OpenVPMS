/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.alert;

import nextapp.echo2.app.Color;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.web.echo.colour.ColourHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.HashMap;


/**
 * Associates an alert type with an optional alert act.
 * <p>
 * Implements {@code Comparable} to order alerts on priority.
 *
 * @author Tim Anderson
 */
public class Alert implements Comparable<Alert> {

    public enum Rank {
        HIGH,
        MEDIUM,
        LOW
    }

    /**
     * The alert priority.
     */
    public class Priority implements Comparable<Priority> {

        private final String code;

        private String displayName;

        private String name;

        public Priority(String code) {
            this.code = code;
        }

        public String getName() {
            if (name == null) {
                String value = ServiceHelper.getLookupService().getName(alertType, "priority");
                name = value != null ? value : code;

            }
            return name;
        }

        public Rank getRank() {
            return Rank.valueOf(code);
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(Priority o) {
            return getRank().compareTo(o.getRank());
        }

        public String getDisplayName() {
            if (displayName == null) {
                displayName = getBean().getDisplayName("priority");
            }
            return displayName;
        }
    }

    /**
     * The alert type. A <em>lookup.customerAlertType</em> or <em>entity.patientAlertType</em>
     */
    private final IMObject alertType;

    /**
     * The alert. May be {@code null}.
     */
    private Act alert;

    /**
     * The alert priority.
     */
    private Priority priority;

    /**
     * The alert priority code.
     */
    private String priorityCode;

    /**
     * The alert colour.
     */
    private Color colour;

    /**
     * The alert type bean.
     */
    private IMObjectBean bean;

    /**
     * Constructs an {@link Alert}.
     *
     * @param alertType the alert type. A <em>lookup.customerAlertType</em> or <em>entity.patientAlertType</em>
     */
    public Alert(IMObject alertType) {
        this(alertType, null);
    }

    /**
     * Constructs an {@link Alert}.
     *
     * @param alertType the alert type. A <em>lookup.customerAlertType</em> or <em>entity.patientAlertType</em>
     * @param alert     the alert act. May be {@code null}
     */
    public Alert(IMObject alertType, Act alert) {
        this.alertType = alertType;
        this.alert = alert;
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type
     */
    public IMObject getAlertType() {
        return alertType;
    }

    /**
     * Returns the alert.
     *
     * @return the alert. May be {@code null}
     */
    public Act getAlert() {
        return alert;
    }

    /**
     * Returns the name of the alert type.
     *
     * @return the alert type name
     */
    public String getName() {
        return alertType.getName();
    }

    /**
     * Returns the alert colour.
     *
     * @return the alert colour. May be {@code null}
     */
    public Color getColour() {
        if (colour == null) {
            colour = ColourHelper.getColor(getBean().getString("colour"));
        }
        return colour;
    }

    /**
     * Returns the alert text colour.
     *
     * @return the alert text colour. May be {@code null}
     */
    public Color getTextColour() {
        Color color = getColour();
        return (color != null) ? ColourHelper.getTextColour(color) : null;
    }

    /**
     * Returns the alert priority.
     *
     * @return the alert priority
     */
    public Priority getPriority() {
        if (priority == null) {
            priority = new Priority(getPriorityCode());
        }
        return priority;
    }

    /**
     * Returns the alert priority code.
     *
     * @return the alert priority code
     */
    public String getPriorityCode() {
        if (priorityCode == null) {
            priorityCode = getBean().getString("priority");
            if (priorityCode == null) {
                priorityCode = Rank.LOW.toString();
            }
        }
        return priorityCode;
    }

    /**
     * Determines if this is a mandatory alert.
     * <p>
     * Mandatory alerts must be acknowledged by users
     *
     * @return {@code true} if this is a mandatory alert, orth
     */
    public boolean isMandatory() {
        return getBean().getBoolean("mandatoryAlert");
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param object the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    public int compareTo(Alert object) {
        int result = 0;
        IMObject alertType1 = getAlertType();
        IMObject alertType2 = object.getAlertType();
        if (!ObjectUtils.equals(alertType1, alertType2)) {
            Priority priority1 = getPriority();
            Priority priority2 = object.getPriority();
            result = priority1.compareTo(priority2);
            if (result == 0) {
                result = new Long(alertType1.getId()).compareTo(alertType2.getId());
            }
        }
        return result;
    }

    /**
     * Returns the alert reason.
     *
     * @return the reason for the alert. May be {@code null}
     */
    public String getReason() {
        return (alert != null) ? alert.getReason() : null;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = super.equals(obj);
        if (!result && obj instanceof Alert) {
            Alert other = (Alert) obj;
            if (alert != null) {
                result = ObjectUtils.equals(alert, other.getAlert());
            } else {
                result = other.getAlert() == null && ObjectUtils.equals(alertType, other.getAlertType());
            }
        }
        return result;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return alert != null ? alert.hashCode() : alertType.hashCode();
    }

    /**
     * Creates an alert from an act.
     *
     * @param act the act. Must be an <em>act.patientAlert</em> or <em>act.customerAlert</em>
     * @return the alert, or {@code null} if the act is of the wrong type or the alert type cannot be determined
     */
    public static Alert create(Act act) {
        IMObjectBean bean = ServiceHelper.getArchetypeService().getBean(act);
        IMObject alertType = null;
        if (bean.isA(CustomerArchetypes.ALERT)) {
            alertType = bean.getLookup("alertType");
        } else if (bean.isA(PatientArchetypes.ALERT)) {
            alertType = bean.getTarget("alertType", IMObject.class);
        }
        return (alertType != null) ? new Alert(alertType, act) : null;
    }

    /**
     * Wraps the alert type in a bean.
     *
     * @return the bean
     */
    private IMObjectBean getBean() {
        if (bean == null) {
            bean = ServiceHelper.getArchetypeService().getBean(alertType);
        }
        return bean;
    }

}
