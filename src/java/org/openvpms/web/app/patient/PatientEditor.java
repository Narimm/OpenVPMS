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

package org.openvpms.web.app.patient;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.DatePropertyTransformer;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.util.Date;
import java.util.List;


/**
 * Editor for <em>party.patientpet</em> parties.
 * <p/>
 * Creates an <em>entityRelationship.patientOwner</em> with the current
 * customer, if the parent object isn't an entity relationship.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEditor extends AbstractIMObjectEditor {

    /**
     * A minimum for patient ages. This allows for parrots, turtles, while preventing some bad entry.
     */
    public static final Date MIN_DATE = java.sql.Date.valueOf("1900-01-01");

    /**
     * Editor for the "customFields" node.
     */
    private RelationshipCollectionTargetEditor customFieldEditor;

    /**
     * The layout strategy.
     */
    private PatientLayoutStrategy strategy;

    /**
     * Constructs a new <tt>PatientEditor</tt>.
     *
     * @param patient the object to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>.
     */
    public PatientEditor(Party patient, IMObject parent, LayoutContext context) {
        super(patient, parent, context);
        if (patient.isNew()) {
            if (!(parent instanceof EntityRelationship)) {
                addOwnerRelationship(patient);
            }
        }
        getProperty("species").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                speciesChanged();
            }
        });

        // restrict the date of birth entry
        Property dateOfBirth = getProperty("dateOfBirth");
        Date maxDate = new Date();
        dateOfBirth.setTransformer(new DatePropertyTransformer(dateOfBirth, MIN_DATE, maxDate));

        CollectionProperty customField = (CollectionProperty) getProperty("customFields");
        customFieldEditor = new EntityRelationshipCollectionTargetEditor(customField, patient, getLayoutContext());
        getEditors().add(customFieldEditor);
        createLayoutStrategy();
        updateCustomFields();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        if (strategy == null) {
            strategy = new PatientLayoutStrategy(customFieldEditor);
        }
        return strategy;
    }

    /**
     * Adds a patient owner relationship to the current context customer,
     * if present.
     *
     * @param patient the patient
     */
    private void addOwnerRelationship(Party patient) {
        Party customer = getLayoutContext().getContext().getCustomer();
        if (customer != null) {
            PatientRules rules = new PatientRules();
            if (!rules.isOwner(customer, patient)) {
                rules.addPatientOwnerRelationship(customer, patient);
            }
        }
    }

    /**
     * Updates the customFields node.
     * <p/>
     * If there is an existing <em>entity.customPatient*</em> that doesn't
     * match the customFields node of the associated <em>lookup.species</em>,
     * it will be removed.
     * <p/>
     * If there is an <em>entity.customPatient*</em> object and its archetype
     * matches that of the associated <em>lookup.species</em>, no update will occur.
     * <p/>
     * If there is no <em>entity.customPatient*</em> object, and the
     * <em>lookup.species</em> customFields node specifies an archetype, one
     * will be added.
     */
    private void updateCustomFields() {
        getComponent(); // force render to ensure the stategy has a focus group set

        String species = (String) getProperty("species").getValue();
        String shortName = getCustomFieldsArchetype(species);
        String currentShortName = null;
        Entity fields = getCustomFields();
        if (fields != null) {
            currentShortName = fields.getArchetypeId().getShortName();
        }
        if (currentShortName != null && !currentShortName.equals(shortName)) {
            customFieldEditor.remove(fields);
            strategy.removeCustomFields();
        }
        if (shortName != null && !shortName.equals(currentShortName)) {
            IMObject object = IMObjectCreator.create(shortName);
            if (object instanceof Entity) {
                customFieldEditor.add(object);
                strategy.addCustomFields();
            }
        }
    }

    /**
     * Invoked when the species changes. Updates the customFields node.
     */
    private void speciesChanged() {
        updateCustomFields();
    }

    /**
     * Returns the archetype short name from customFields node of the specified
     * species lookup.
     *
     * @param species the <em>lookup.species</em> code
     * @return the archetype short name, or <tt>null</tt> if none is found
     */
    private String getCustomFieldsArchetype(String species) {
        String result = null;
        if (species != null) {
            Lookup lookup = LookupServiceHelper.getLookupService().getLookup(
                    "lookup.species", species);
            if (lookup != null) {
                IMObjectBean bean = new IMObjectBean(lookup);
                result = bean.getString("customFields");
            }
        }
        return result;
    }

    /**
     * Returns the <em>entity.customFields</em> object from the customFields
     * node.
     *
     * @return the object, or <tt>null</tt> if none is found
     */
    private Entity getCustomFields() {
        List<IMObject> result = customFieldEditor.getObjects();
        return !result.isEmpty() ? (Entity) result.get(0) : null;
    }

}