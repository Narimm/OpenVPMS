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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient;

import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.DatePropertyTransformer;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;


/**
 * Editor for <em>party.patientpet</em> parties.
 * <p/>
 * Creates an <em>entityRelationship.patientOwner</em> with the current
 * customer, if the parent object isn't an entity relationship.
 *
 * @author Tim Anderson
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
     * The breed editor.
     */
    private BreedEditor breedEditor;

    /**
     * The layout strategy.
     */
    private PatientLayoutStrategy strategy;

    /**
     * Listener for deceased flag changes.
     */
    private final ModifiableListener deceasedListener;

    /**
     * Listener for deceased date changes.
     */
    private final ModifiableListener deceasedDateListener;

    /**
     * Species node.
     */
    private static final String SPECIES = "species";

    /**
     * Active node.
     */
    private static final String ACTIVE = "active";

    /**
     * Deceased date node.
     */
    private static final String DECEASED = "deceased";

    /**
     * Deceased date node.
     */
    private static final String DECEASED_DATE = "deceasedDate";

    /**
     * New breed node.
     */
    private static final String NEW_BREED = "newBreed";

    /**
     * Constructs a {@link PatientEditor}.
     *
     * @param patient the object to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}.
     */
    public PatientEditor(Party patient, IMObject parent, LayoutContext context) {
        super(patient, parent, context);
        if (patient.isNew()) {
            if (!(parent instanceof EntityRelationship)) {
                addOwnerRelationship(patient);
            }
        }
        getProperty(SPECIES).addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                speciesChanged();
            }
        });
        Property breed = getProperty("breed");
        Property newBreed = getProperty(NEW_BREED);
        breedEditor = new BreedEditor(breed, patient);

        if (StringUtils.isEmpty(breed.getString()) && !StringUtils.isEmpty(newBreed.getString())) {
            breedEditor.selectNewBreed();
        }

        // need to add a listener with the component as both 'None' and 'New Breed' are represented using null
        // so switching from 'None' to 'New Breed' or vice versa won't trigger any event on the property
        breedEditor.getComponent().addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                breedChanged();
            }
        });

        // restrict the date of birth entry
        Property dateOfBirth = getProperty("dateOfBirth");
        Date maxDate = DateRules.getTomorrow(); // the maximum date, exclusive
        dateOfBirth.setTransformer(new DatePropertyTransformer(dateOfBirth, MIN_DATE, maxDate));

        // restrict the deceased date entry
        Property deceasedDate = getProperty("deceasedDate");
        deceasedDate.setTransformer(new DatePropertyTransformer(dateOfBirth, MIN_DATE, maxDate));

        deceasedDateListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                deceasedDateChanged();
            }
        };
        deceasedDate.addModifiableListener(deceasedDateListener);

        deceasedListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                deceasedChanged();
            }
        };
        getProperty(DECEASED).addModifiableListener(deceasedListener);

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
            strategy.addComponent(new ComponentState(breedEditor));
        }
        return strategy;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = super.doValidation(validator);
        if (valid) {
            Date date = getDeceasedDate();
            boolean deceased = isDeceased();
            if (date != null && !deceased) {
                Property property = getProperty(DECEASED);
                validator.add(property, new ValidatorError(property, Messages.get("patient.deceased.required")));
                valid = false;
            } else if (deceased && isActive()) {
                Property property = getProperty(DECEASED);
                validator.add(property, new ValidatorError(property, Messages.get("patient.deceased.inactive")));
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Adds a patient owner relationship to the current context customer, if present.
     *
     * @param patient the patient
     */
    private void addOwnerRelationship(Party patient) {
        Party customer = getLayoutContext().getContext().getCustomer();
        if (customer != null) {
            PatientRules rules = ServiceHelper.getBean(PatientRules.class);
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
        getComponent(); // force render to ensure the strategy has a focus group set

        String species = (String) getProperty(SPECIES).getValue();
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
        // need to refresh explicitly as it is not hooked in to AbstractIMObjectEditors lookup refresh
        breedEditor.getComponent().refresh();
        breedChanged();
        updateCustomFields();
    }

    /**
     * Invoked when the breed changes. If it is 'New Breed', displays the 'newBreed' node.
     */
    private void breedChanged() {
        boolean newBreed = breedEditor.isNewBreed();
        if (!newBreed) {
            getProperty(NEW_BREED).setValue(null);
        }
        strategy.updateNewBreed(newBreed);
    }

    /**
     * Sets the patient's deceased state. This sets the deceased node to the value supplied, and the active node to
     * {@code !deceased}.
     *
     * @param deceased the deceased state
     */
    private void setDeceased(boolean deceased) {
        Property property = getProperty(DECEASED);
        try {
            property.removeModifiableListener(deceasedListener);
            property.setValue(deceased);
            setActive(!deceased);
        } finally {
            property.addModifiableListener(deceasedListener);
        }
    }

    /**
     * Determines if the patient is deceased.
     *
     * @return {@code true} if the patient is deceased, otherwise {@code false}
     */
    private boolean isDeceased() {
        return getProperty(DECEASED).getBoolean();
    }

    /**
     * Sets the patient's active flag.
     *
     * @param active if {@code true}, the patient is active, else it is inactive
     */
    private void setActive(boolean active) {
        getProperty(ACTIVE).setValue(active);
    }

    /**
     * Determines if the patient is active or not.
     *
     * @return {@code true} if the patient is active, {@code false if it is inactive
     */
    private boolean isActive() {
        return getProperty(ACTIVE).getBoolean();
    }

    /**
     * Sets the patient deceased date. This sets the active flag accordingly.
     *
     * @param date the patient deceased date. May be {@code null}
     */
    private void setDeceasedDate(Date date) {
        Property property = getProperty(DECEASED_DATE);
        try {
            property.removeModifiableListener(deceasedDateListener);
            property.setValue(date);
            setActive(date == null);
        } finally {
            property.addModifiableListener(deceasedDateListener);
        }
    }

    /**
     * Returns the patient deceased date.
     *
     * @return the deceased date. May be {@code null}
     */
    private Date getDeceasedDate() {
        Property deceasedDate = getProperty(DECEASED_DATE);
        return deceasedDate.getDate();
    }

    /**
     * Invoked when the deceased date changes. Toggles the deceased and active nodes accordingly.
     */
    private void deceasedDateChanged() {
        Date date = getDeceasedDate();
        setDeceased(date != null);
    }

    /**
     * Invoked when the deceased flag changes. Sets the deceased date and active nodes accordingly.
     */
    private void deceasedChanged() {
        boolean deceased = isDeceased();
        setDeceasedDate(deceased ? new Date() : null);
    }

    /**
     * Returns the archetype short name from customFields node of the specified
     * species lookup.
     *
     * @param species the <em>lookup.species</em> code
     * @return the archetype short name, or {@code null} if none is found
     */
    private String getCustomFieldsArchetype(String species) {
        String result = null;
        if (species != null) {
            Lookup lookup = ServiceHelper.getLookupService().getLookup("lookup.species", species);
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
     * @return the object, or {@code null} if none is found
     */
    private Entity getCustomFields() {
        List<IMObject> result = customFieldEditor.getObjects();
        return !result.isEmpty() ? (Entity) result.get(0) : null;
    }


}