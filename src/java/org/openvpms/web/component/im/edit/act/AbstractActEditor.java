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
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;

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
     * The start time node name.
     */
    protected static final String START_TIME = "startTime";

    /**
     * The end time node name.
     */
    protected static final String END_TIME = "endTime";

    /**
     * Constructs an <tt>AbstractActEditor</tt>.
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
        initParticipant("author", context.getContext().getUser());
    }

    /**
     * Sets the author.
     *
     * @param author the author. May be <tt>null</tt>
     */
    public void setAuthor(User author) {
        setParticipant("author", author);
    }

    /**
     * Returns the author.
     *
     * @return the author, or <tt>null</tt> if there is no author
     */
    public User getAuthor() {
        return (User) getParticipant("author");
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
     * Sets the act start time.
     *
     * @param time the start time
     */
    public void setStartTime(Date time) {
        setStartTime(time, false);
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
     * Sets the act end time.
     *
     * @param time the end time
     */
    public void setEndTime(Date time) {
        setEndTime(time, false);
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the start time is less than the end time, if non-null.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean result = super.validate(validator);
        if (result) {
            result = validateStartEndTimes(validator);
        }
        return result;
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
     * @param name   the participation property name
     * @param entity the entity reference. May be <tt>null</tt>
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
     * @return <tt>true</tt> if the participant was modified, otherwise <tt>false</tt>
     */
    protected boolean setParticipant(String name, IMObject entity) {
        IMObjectReference ref = (entity != null) ? entity.getObjectReference() : null;
        return setParticipant(name, ref);
    }

    /**
     * Sets a participant.
     *
     * @param name   the participation property name
     * @param entity the participant. May be <tt>null</tt>
     * @return <tt>true</tt> if the participant was modified, otherwise <tt>false</tt>
     * @throws IllegalArgumentException if the name doesn't correspond to a valid node
     */
    protected boolean setParticipant(String name, IMObjectReference entity) {
        boolean modified = false;
        ParticipationEditor editor = getParticipationEditor(name, false);
        if (editor != null) {
            if (!ObjectUtils.equals(editor.getEntityRef(), entity)) {
                editor.setEntityRef(entity);
                modified = true;
            }
        } else {
            // no editor created yet. Set the participant via the corresponding
            // property
            Property property = getProperty(name);
            if (property == null) {
                throw new IllegalArgumentException("Invalid node: " + name);
            }
            Participation participant = getParticipation((IMObjectProperty) property);
            if (participant != null) {
                if (participant.getAct() == null) {
                    participant.setAct(getObject().getObjectReference());
                    modified = true;
                }
                if (!ObjectUtils.equals(participant.getEntity(), entity)) {
                    participant.setEntity(entity);
                    modified = true;
                }
                if (modified) {
                    property.refresh();   // flag as modified
                }
            }
        }
        return modified;
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
            if (bean.hasNode(name)) {
                result = bean.getNodeParticipantRef(name);
            } else {
                result = null;
            }
        }
        return result;
    }

    /**
     * Returns a participant.
     *
     * @param name the participation property name
     * @return the participant. May be <tt>null</tt>
     */
    protected IMObject getParticipant(String name) {
        IMObjectReference ref = getParticipantRef(name);
        return getObject(ref);
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
    protected <T extends Entity> ParticipationEditor<T> getParticipationEditor(String name, boolean create) {
        ParticipationEditor<T> result = null;
        Editor editor = getEditor(name, create);
        if (editor instanceof ParticipationEditor) {
            result = (ParticipationEditor<T>) editor;
        } else if (editor instanceof ParticipationCollectionEditor) {
            // handle the case of an optional participation
            ParticipationCollectionEditor collectionEditor
                    = ((ParticipationCollectionEditor) editor);
            if (collectionEditor.getEditor() instanceof SingleParticipationCollectionEditor) {
                IMObjectEditor current = collectionEditor.getCurrentEditor();
                if (current instanceof ParticipationEditor) {
                    result = (ParticipationEditor<T>) current;
                }
            }
        }
        return result;
    }

    /**
     * Sets the act start time.
     *
     * @param time    the start time
     * @param disable if <tt>true</tt> disable the {@link #onStartTimeChanged} callback
     */
    protected void setStartTime(Date time, boolean disable) {
        Property startTime = getProperty(START_TIME);
        if (disable) {
            removeStartEndTimeListeners();
        }
        startTime.setValue(time);
        if (disable) {
            addStartEndTimeListeners();
        }
    }

    /**
     * Sets the act end time.
     *
     * @param time    the end time
     * @param disable if <tt>true</tt> disable the {@link #onEndTimeChanged} callback
     */
    protected void setEndTime(Date time, boolean disable) {
        Property endTime = getProperty(END_TIME);
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
        Property startTime = getProperty(START_TIME);
        if (startTime != null) {
            startTime.addModifiableListener(startTimeListener);
        }

        Property endTime = getProperty(END_TIME);
        if (endTime != null) {
            endTime.addModifiableListener(endTimeListener);
        }
    }

    /**
     * Removes act start/end time modification callbacks.
     */
    protected void removeStartEndTimeListeners() {
        Property startTime = getProperty(START_TIME);
        if (startTime != null) {
            startTime.removeModifiableListener(startTimeListener);
        }

        Property endTime = getProperty(END_TIME);
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
                setStartTime(end, true);
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
                setEndTime(start, true);
            }
        }
    }

    /**
     * Validates that the start and end times are valid.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the start and end times are valid
     */
    protected boolean validateStartEndTimes(Validator validator) {
        boolean result = true;
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (start.getTime() > end.getTime()) {
                String startName = getDisplayName(START_TIME);
                String endName = getDisplayName(END_TIME);
                String message = Messages.get("act.validation.startGreaterThanEnd", startName, endName);
                validator.add(this, new ValidatorError(message));
                result = false;
            }
        }
        return result;
    }

    /**
     * Helper to return the display name of a property.
     *
     * @param name the property name
     * @return the property's display name, or <tt>name</tt> if the property doesn't exist
     */
    private String getDisplayName(String name) {
        Property property = getProperty(name);
        return (property != null) ? property.getDisplayName() : name;
    }
}
