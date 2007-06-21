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

package org.openvpms.web.component.im.edit.act;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.util.Date;


/**
 * Editor for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractActEditor extends AbstractIMObjectEditor {

    /**
     * Listener for <em>startTime</em> changes.
     */
    private final ModifiableListener startTimeListener;

    /**
     * Listener for <em>endTime</em> changes.
     */
    private final ModifiableListener endTimeListener;


    /**
     * Constructs a new <code>AbstractActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public AbstractActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStartTimeChanged();
            }
        };
        endTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onEndTimeChanged();
            }
        };
    }

    /**
     * Helper to initialises a participation, if it exists and is empty.
     *
     * @param name   the participation name
     * @param entity the participation entity. May be <code>null</code>
     */
    protected void initParticipant(String name, IMObject entity) {
        IMObjectReference ref
                = (entity != null) ? entity.getObjectReference() : null;
        initParticipant(name, ref);
    }

    /**
     * Helper to initialises a participant, if it exists and is empty.
     *
     * @param name the participation property name
     */
    protected void initParticipant(String name, IMObjectReference entity) {
        Property property = getProperty(name);
        if (property != null) {
            Participation participant
                    = getParticipation((IMObjectProperty) property);
            if (participant != null) {
                if (participant.getAct() == null) {
                    participant.setAct(getObject().getObjectReference());
                }
                if (entity != null && participant.getEntity() == null) {
                    participant.setEntity(entity);
                }
            }
        }
    }

    /**
     * Sets a participant.
     *
     * @param name   the participation property name
     * @param entity the participant. May be <code>null</code>
     */
    protected void setParticipant(String name, IMObject entity) {
        IMObjectReference ref
                = (entity != null) ? entity.getObjectReference() : null;
        setParticipant(name, ref);
    }

    /**
     * Sets a participant.
     *
     * @param name   the participation property name
     * @param entity the participant. May be <code>null</code>
     */
    protected void setParticipant(String name, IMObjectReference entity) {
        Property property = getProperty(name);
        Participation participant
                = getParticipation((IMObjectProperty) property);
        if (participant != null) {
            boolean modified = false;
            if (participant.getAct() == null) {
                participant.setAct(getObject().getObjectReference());
                modified = true;
            }
            if (!ObjectUtils.equals(participant.getEntity(), entity)) {
                participant.setEntity(entity);
            }
            if (modified) {
                property.refresh();   // flag as modified
            }
        }
    }

    /**
     * Returns a participant reference.
     *
     * @param name the participation property name
     * @return a reference to the participant. May be <code>null</code>
     */
    protected IMObjectReference getParticipantRef(String name) {
        Property property = getProperty(name);
        if (property != null) {
            Participation participant
                    = getParticipation((IMObjectProperty) property);
            if (participant != null) {
                return participant.getEntity();
            }
        }
        return null;
    }

    /**
     * Returns a prticipant.
     *
     * @param name the participation property name
     * @return the participant. May be <code>null</code>
     */
    protected IMObject getParticipant(String name) {
        return IMObjectHelper.getObject(getParticipantRef(name));
    }

    /**
     * Helper to return a participation.
     *
     * @param property the participation property
     * @return the participation
     */
    protected Participation getParticipation(IMObjectProperty property) {
        Object value = null;
        if (property.isCollection()) {
            Object[] values = property.getValues().toArray();
            if (values.length > 0) {
                value = values[0];
            } else {
                String[] shortNames = DescriptorHelper.getShortNames(
                        property.getDescriptor());
                if (shortNames.length == 1) {
                    value = IMObjectCreator.create(shortNames[0]);
                    if (value != null) {
                        property.add(value);
                    }
                }
            }
        } else {
            value = property.getValue();
        }
        return (value instanceof Participation) ? (Participation) value : null;
    }

    /**
     * Returns the act start time.
     *
     * @return the act start time. May be <code>null</code>
     */
    protected Date getStartTime() {
        return ((Act) getObject()).getActivityStartTime();
    }

    /**
     * Sets the act start time, temporarily disabling callbacks.
     *
     * @param time the start time
     */
    protected void setStartTime(Date time) {
        Property startTime = getProperty("startTime");
        removeStartEndTimeListeners();
        startTime.setValue(time);
        addStartEndTimeListeners();
    }

    /**
     * Returns the end time.
     *
     * @return the end time. May be <code>null</code>
     */
    protected Date getEndTime() {
        return ((Act) getObject()).getActivityEndTime();
    }

    /**
     * Sets the act end time, temporarily disabling callbacks.
     *
     * @param time the end time
     */
    protected void setEndTime(Date time) {
        setEndTime(time, true);
    }

    /**
     * Sets the act end time.
     *
     * @param time    the end time
     * @param disable if <code>true</code> disable the {@link #onEndTimeChanged}
     *                callback
     */
    protected void setEndTime(Date time, boolean disable) {
        Property endTime = getProperty("endTime");
        if (disable) {
            removeStartEndTimeListeners();
        }
        endTime.setValue(time);
        if (disable) {
            addStartEndTimeListeners();
        }
    }

    /**
     * Adds act start/end time modification callbacks.
     * When enabled, changes to the <em>startTime</em> property trigger
     * {@link #onStartTimeChanged()} and changes to <em>endTime</em> trigger
     * {@link #onEndTimeChanged()}.
     */
    protected void addStartEndTimeListeners() {
        Property startTime = getProperty("startTime");
        if (startTime != null) {
            startTime.addModifiableListener(startTimeListener);
        }

        Property endTime = getProperty("endTime");
        if (endTime != null) {
            endTime.addModifiableListener(endTimeListener);
        }
    }

    /**
     * Removes act start/end time modification callbacks.
     */
    protected void removeStartEndTimeListeners() {
        Property startTime = getProperty("startTime");
        if (startTime != null) {
            startTime.removeModifiableListener(startTimeListener);
        }

        Property endTime = getProperty("endTime");
        if (endTime != null) {
            endTime.removeModifiableListener(endTimeListener);
        }
    }

    /**
     * Invoked when the start time changes. Sets the value to end time if
     * start time > end time.
     */
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (start.compareTo(end) > 0) {
                setStartTime(end);
            }
        }
    }

    /**
     * Invoked when the end time changes. Sets the value to start time if
     * end time < start time.
     */
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                setEndTime(start);
            }
        }
    }

}
