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
 */

package org.openvpms.web.app.workflow.messaging;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.CustomerParticipationEditor;
import org.openvpms.web.component.im.edit.act.ParticipationCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Set;


/**
 * Editor for <em>act.userMessage</em> acts.
 * <p/>
 * Note that acts may not be saved using this editor; it is intended to be used to generate a template message
 * which is then copied and sent to the selected users.
 * <p/>
 * This is required as <em>act.userMessage</em> may only have a single 'to' participation and status,
 * but there is a requirement to be able to send messages to multiple users, each of which may independently update the
 * act status.
 *
 * @author Tim Anderson
 */
public class UserMessageEditor extends ActEditor {

    /**
     * The 'to' address editor. This allows selection of multiple users and user groups.
     */
    private final ToAddressEditor toAddressEditor;

    /**
     * Constructs a {@code UserMessageActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}
     */
    public UserMessageEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);

        initParticipant("from", context.getContext().getUser());
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("patient", context.getContext().getPatient());

        toAddressEditor = new ToAddressEditor(act, getProperty("to"));
        getEditors().add(toAddressEditor);
    }

    /**
     * Returns the 'from' user.
     *
     * @return the 'from' user. May be {@code null}
     */
    public User getFrom() {
        return (User) getParticipant("from");
    }

    /**
     * Sets the 'from' user.
     *
     * @param from the 'from' user. May be {@code null}
     */
    public void setFrom(User from) {
        setParticipant("from", from);
    }

    /**
     * Returns the 'to' user.
     *
     * @return the 'to' user. May be {@code null}
     */
    public User getTo() {
        return (User) getParticipant("to");
    }

    /**
     * Sets the 'to' user.
     *
     * @param to the 'to' user. May be {@code null}
     */
    public void setTo(User to) {
        toAddressEditor.setTo(to);
    }

    /**
     * Returns the 'to' users.
     *
     * @return the 'to' users
     */
    public Set<User> getToUsers() {
        return toAddressEditor.getTo();
    }

    /**
     * Returns the message subject.
     *
     * @return the message subject. May be {@code null}
     */
    public String getSubject() {
        return getStringProperty("description");
    }

    /**
     * Sets the message subject.
     *
     * @param subject the subject. May be {@code null}
     */
    public void setSubject(String subject) {
        Property description = getProperty("description");
        description.setValue(subject);
    }

    /**
     * Returns the message.
     *
     * @return the message. May be {@code null}
     */
    public String getMessage() {
        return getStringProperty("message");
    }

    /**
     * Sets the message.
     *
     * @param message the message. May be {@code null}
     */
    public void setMessage(String message) {
        Property property = getProperty("message");
        property.setValue(message);
    }

    /**
     * Save any edits.
     * <p/>
     * This implementation always returns {@code false}
     *
     * @return {@code false}
     */
    @Override
    protected boolean doSave() {
        return false;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        UserMessageLayoutStrategy strategy = new UserMessageLayoutStrategy();
        strategy.addComponent(toAddressEditor.getComponentState());
        return strategy;
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        CustomerParticipationEditor customer = getCustomerEditor();
        PatientParticipationEditor patient = getPatientEditor();
        customer.setPatientParticipationEditor(patient);
        customer.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onCustomerChanged();
            }
        });
    }

    /**
     * Returns the customer editor.
     *
     * @return the customer editor
     */
    private CustomerParticipationEditor getCustomerEditor() {
        ParticipationCollectionEditor editor = (ParticipationCollectionEditor) getEditor("customer");
        return (CustomerParticipationEditor) editor.getCurrentEditor();
    }

    /**
     * Returns the patient editor.
     *
     * @return the patient editor
     */
    private PatientParticipationEditor getPatientEditor() {
        ParticipationCollectionEditor editor = (ParticipationCollectionEditor) getEditor("patient");
        return (PatientParticipationEditor) editor.getCurrentEditor();
    }

    /**
     * Invoked when the customer changes. Sets the patient to null if no relationship exists between the two.
     */
    private void onCustomerChanged() {
        try {
            Party customer = getCustomerEditor().getEntity();
            Party patient = getPatientEditor().getEntity();
            PatientRules rules = new PatientRules(ServiceHelper.getArchetypeService(),
                                                  ServiceHelper.getLookupService());
            if (customer != null && patient != null) {
                if (!rules.isOwner(customer, patient)) {
                    getPatientEditor().setEntity(null);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Helper to return the value of a string property.
     *
     * @param name the property name
     * @return the property value. May be {@code null}
     */
    private String getStringProperty(String name) {
        Property description = getProperty(name);
        Object value = description.getValue();
        return value != null ? value.toString() : null;
    }

}
