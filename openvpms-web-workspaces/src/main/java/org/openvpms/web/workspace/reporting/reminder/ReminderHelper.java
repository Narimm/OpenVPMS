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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Reminder helper.
 *
 * @author Tim Anderson
 */
class ReminderHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderHelper.class);


    /**
     * Attempts to update a reminder.
     * <p/>
     * If the reminder has subsequently changed, it will be updated if the only change has been to the <em>error</em>
     * node.
     *
     * @param reminder the reminder to update
     * @param lastSent the last-sent time to use
     * @return <tt>true</tt> if the reminder was updated
     */
    public static boolean update(Act reminder, Date lastSent) {
        boolean result = false;
        Act current = IMObjectHelper.reload(reminder);
        if (current != null && !ActStatus.CANCELLED.equals(current.getStatus())) {
            try {
                if (compare(reminder, current)) {
                    ReminderRules rules = new ReminderRules(ServiceHelper.getArchetypeService(),
                                                            new PatientRules(ServiceHelper.getArchetypeService(),
                                                                             ServiceHelper.getLookupService()));
                    rules.updateReminder(current, lastSent);
                    result = true;
                }
            } catch (Throwable exception) {
                setError(reminder, exception);
            }
        }
        return result;
    }

    /**
     * Sets the <tt>error</tt> node of a reminder.
     *
     * @param reminder the reminder
     * @param error    the error
     */
    public static void setError(Act reminder, Throwable error) {
        try {
            reminder = IMObjectHelper.reload(reminder);
            if (reminder != null) {
                IMObjectBean bean = new IMObjectBean(reminder);
                bean.setValue("error", ErrorFormatter.format(error));
                bean.save();
            }
        } catch (Throwable exception) {
            log.warn(exception, exception);
        }
    }

    /**
     * Compares two objects for equality.
     *
     * @param oldVersion the old version of the object
     * @param newVersion the new version of the object
     * @return <tt>true</tt> if the objects are the same, otherwise <tt>false</tt>
     */
    private static boolean compare(IMObject oldVersion, IMObject newVersion) {
        if (oldVersion.getVersion() != newVersion.getVersion()) {
            ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(oldVersion);
            return (archetype != null) && compare(oldVersion, newVersion, archetype);
        }
        return true;
    }

    /**
     * Compares two objects for equality.
     * <p/>
     * This excludes the <em>act.patientReminder</em> <em>error</em> node from the comparison.
     *
     * @param oldVersion the old version of the object
     * @param newVersion the new version of the object
     * @param archetype  the object's archetype descriptor
     * @return <tt>true</tt> if the objects are the same, otherwise <tt>false</tt>
     */
    private static boolean compare(IMObject oldVersion, IMObject newVersion, ArchetypeDescriptor archetype) {
        IMObjectBean oldBean = new IMObjectBean(oldVersion);
        IMObjectBean newBean = new IMObjectBean(newVersion);
        boolean isReminder = oldBean.isA(ReminderArchetypes.REMINDER);
        for (NodeDescriptor descriptor : archetype.getSimpleNodeDescriptors()) {
            String name = descriptor.getName();
            if (!(isReminder && "error".equals(name))) {
                if (!compare(oldBean, newBean, descriptor)) {
                    return false;
                }
            }
        }
        for (NodeDescriptor descriptor : archetype.getComplexNodeDescriptors()) {
            if (!compare(oldBean, newBean, descriptor)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two nodes values for equality.
     *
     * @param oldVersion the old version of the object
     * @param newVersion the new version of the object
     * @param descriptor the descriptor of the node to compare
     * @return <tt>true</tt> if the node values are the same, otherwise <tt>false</tt>
     */
    private static boolean compare(IMObjectBean oldVersion, IMObjectBean newVersion, NodeDescriptor descriptor) {
        String name = descriptor.getName();
        boolean result;
        if (descriptor.isCollection()) {
            List<IMObject> oldValues = oldVersion.getValues(name);
            List<IMObject> newValues = oldVersion.getValues(name);
            result = compare(oldValues, newValues);
        } else {
            Object oldValue = oldVersion.getValue(name);
            Object newValue = newVersion.getValue(name);
            result = ObjectUtils.equals(oldValue, newValue);
        }
        return result;
    }

    /**
     * Compares two list of objects for equality
     *
     * @param oldValues the old values
     * @param newValues the new values
     * @return <tt>true</tt> if the objects are the same, otherwise <tt>false</tt>
     */
    private static boolean compare(List<IMObject> oldValues, List<IMObject> newValues) {
        if (oldValues.size() == newValues.size()) {
            Map<IMObjectReference, IMObject> newMap = new HashMap<IMObjectReference, IMObject>();
            for (IMObject newValue : newValues) {
                newMap.put(newValue.getObjectReference(), newValue);
            }
            for (IMObject oldValue : oldValues) {
                IMObject newValue = newMap.get(oldValue.getObjectReference());
                if (newValue == null || !compare(oldValue, newValue)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
