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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
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
     * Constructs a new <tt>AbstractActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
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
     * Returns the act start time.
     *
     * @return the act start time. May be <tt>null</tt>
     */
    public Date getStartTime() {
        return ((Act) getObject()).getActivityStartTime();
    }

    /**
     * Sets the act start time, temporarily disabling callbacks.
     *
     * @param time the start time
     */
    public void setStartTime(Date time) {
        Property startTime = getProperty("startTime");
        removeStartEndTimeListeners();
        startTime.setValue(time);
        addStartEndTimeListeners();
    }

    /**
     * Returns the end time.
     *
     * @return the end time. May be <tt>null</tt>
     */
    public Date getEndTime() {
        return ((Act) getObject()).getActivityEndTime();
    }

    /**
     * Sets the act end time, temporarily disabling callbacks.
     *
     * @param time the end time
     */
    public void setEndTime(Date time) {
        setEndTime(time, true);
    }

    /**
     * Helper to initialises a participation, if it exists and is empty.
     *
     * @param name   the participation name
     * @param entity the participation entity. May be <tt>null</tt>
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
     * @param entity the participant. May be <tt>null</tt>
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
     * @param entity the participant. May be <tt>null</tt>
     */
    protected void setParticipant(String name, IMObjectReference entity) {
        ParticipationEditor editor = getParticipationEditor(name, false);
        if (editor != null) {
            editor.setEntityRef(entity);
        } else {
            // no editor created yet. Set the participant via the corresponding
            // property
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
    }

    /**
     * Returns a participant reference.
     *
     * @param name the participation property name
     * @return a reference to the participant. May be <tt>null</tt>
     */
    protected IMObjectReference getParticipantRef(String name) {
        IMObjectReference result;
        ParticipationEditor editor = getParticipationEditor(name, false);
        if (editor != null) {
            result = editor.getEntityRef();
        } else {
            ActBean bean = new ActBean((Act) getObject());
            result = bean.getNodeParticipantRef(name);
        }
        return result;
    }

    /**
     * Returns a prticipant.
     *
     * @param name the participation property name
     * @return the participant. May be <tt>null</tt>
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
     * Returns the participation editor for the named participation node.
     *
     * @param name   the participation property name
     * @param create if <tt>true</tt> force creation of the edit components if
     *               it hasn't already been done
     * @return the editor corresponding to <tt>name</tt> or </tt>null</tt> if
     *         none exists or hasn't been created
     */
    @SuppressWarnings("unchecked")
    protected <T extends Entity> ParticipationEditor<T>
            getParticipationEditor(String name, boolean create) {
        ParticipationEditor<T> result = null;
        Editor editor = getEditor(name, create);
        if (editor instanceof ParticipationEditor) {
            result = (ParticipationEditor<T>) editor;
        } else if (editor instanceof ParticipationCollectionEditor) {
            // handle the case of an optional participation
            ParticipationCollectionEditor collectionEditor
                    = ((ParticipationCollectionEditor) editor);
            if (collectionEditor.getEditor()
                    instanceof SingleParticipationCollectionEditor) {
            }
            IMObjectEditor current = collectionEditor.getCurrentEditor();
            if (current instanceof ParticipationEditor) {
                result = (ParticipationEditor<T>) current;
            }
        }
        return result;
    }

    /**
     * Sets the act end time.
     *
     * @param time    the end time
     * @param disable if <tt>true</tt> disable the {@link #onEndTimeChanged}
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
