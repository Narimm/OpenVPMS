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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.i18n.time.CompositeDurationFormatter;
import org.openvpms.archetype.i18n.time.DateDurationFormatter;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import static org.openvpms.web.echo.style.Styles.BOLD;
import static org.openvpms.web.echo.style.Styles.CELL_SPACING;

/**
 * Patient weight panel.
 *
 * @author Tim Anderson
 */
class WeightPanel {

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The component
     */
    private final IMObjectComponentFactory factory;

    /**
     * Wraps the patient weight. May be {@code null}
     */
    private final IMObjectBean weightBean;

    /**
     * Displays the units for the current weight.
     */
    private final Label currentWeightUnits = LabelFactory.create();

    /**
     * The current weight. May be {@code null}
     */
    private final SimpleProperty currentWeight;

    /**
     * The date when the current weight was recorded. May be {@code null}
     */
    private final SimpleProperty currentWeightDate;

    /**
     * Displays hold old the recorded weight is.
     */
    private final Label currentWeightAge = LabelFactory.create();

    /**
     * Weight unit codes to their corresponding display names.
     */
    private final Map<String, String> weightUnits;

    /**
     * The patient weight editor. May be {@code null}
     */
    private final WeightEditor editor;

    /**
     * The date format for formatting how long ago a weight was entered.
     */
    private final DateFormat dateFormat;

    /**
     * The patient weight. An <em>act.patientWeight</em>
     */
    private Act weight;

    /**
     * The weight age formatter.
     */
    private static final CompositeDurationFormatter WEIGHT_AGE_FORMATTER;

    static {
        WEIGHT_AGE_FORMATTER = new CompositeDurationFormatter();
        WEIGHT_AGE_FORMATTER.add(14, DateUnits.DAYS, DateDurationFormatter.DAY);
        WEIGHT_AGE_FORMATTER.add(90, DateUnits.DAYS, DateDurationFormatter.WEEK);
        WEIGHT_AGE_FORMATTER.add(23, DateUnits.MONTHS, DateDurationFormatter.MONTH);
        WEIGHT_AGE_FORMATTER.add(2, DateUnits.YEARS, DateDurationFormatter.create(true, true, false, false));
    }


    /**
     * Constructs a {@link WeightPanel}.
     *
     * @param layoutContext the layout context
     * @param service       the archetype service
     * @param rules         the patient rules
     */
    WeightPanel(LayoutContext layoutContext, IArchetypeService service, PatientRules rules) {
        this.rules = rules;
        factory = layoutContext.getComponentFactory();
        weightUnits = LookupNameHelper.getLookupNames(PatientArchetypes.PATIENT_WEIGHT, "units");
        weight = (Act) service.create(PatientArchetypes.PATIENT_WEIGHT);
        weightBean = service.getBean(weight);
        editor = new WeightEditor(weight, layoutContext);
        dateFormat = DateFormatter.getDateFormat(false);
        currentWeight = new SimpleProperty("currentWeight", null, BigDecimal.class,
                                           Messages.get("workflow.checkin.weight.current"), true);
        currentWeightDate = new SimpleProperty("currentWeightDate", null, String.class,
                                               Messages.get("workflow.checkin.weight.date"), true);
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        editor.setPatient(patient);
        Weight weight = (patient != null) ? rules.getWeight(patient) : null;
        Date date = null;
        if (weight == null || weight.isZero()) {
            currentWeight.setValue(null);
            currentWeightUnits.setText(null);
        } else {
            currentWeight.setValue(weight.getWeight());
            currentWeightUnits.setText(weightUnits.get(weight.getUnits().toString()));
            date = weight.getDate();
        }
        String age = null;
        if (date != null) {
            Date today = DateRules.getToday();
            if (today.equals(DateRules.getDate(date))) {
                currentWeightDate.setValue(DateFormatter.formatTime(date, false));
            } else {
                currentWeightDate.setValue(dateFormat.format(date));
                age = Messages.format("workflow.checkin.weight.age",
                                      WEIGHT_AGE_FORMATTER.format(date, today).toLowerCase());
            }
        } else {
            currentWeightDate.setValue(null);
        }
        currentWeightAge.setText(age);
        weightBean.setTarget("patient", patient);
        editor.setWeight(null); // so users don't have to clear the existing value before entering a new one
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        editor.setClinician(clinician);
    }

    /**
     * Returns the weight act created by Check-In.
     *
     * @return the weight, or {@code null} if none was saved
     */
    public Act getWeight() {
        Act act = editor.getObject();
        return act != null && !act.isNew() ? act : null;
    }

    /**
     * Sets the weight.
     *
     * @param weight the weight. May be {@code null}
     */
    public void setWeight(BigDecimal weight) {
        editor.getProperty("weight").setValue(weight);
    }

    /**
     * Validates the weight.
     *
     * @param validator the validator
     * @return {@code true} if the weight is valid, otherwise {@code false}
     */
    public boolean validate(Validator validator) {
        return editor.validate(validator);
    }

    /**
     * Lays out the component in the specified grid.
     *
     * @param grid the grid
     */
    public FocusGroup layout(ComponentGrid grid) {
        // add the current weight
        int maxColumns = DateFormatter.getLength(dateFormat);
        TextField date = BoundTextComponentFactory.create(currentWeightDate, maxColumns, dateFormat);
        date.setStyleName(Styles.EDIT);
        date.setEnabled(false);

        grid.add(TableHelper.createSpacer());
        grid.add(LabelFactory.text("Weight", BOLD));
        grid.add(LabelFactory.text(currentWeight.getDisplayName()),
                 RowFactory.create(CELL_SPACING, factory.create(currentWeight), currentWeightUnits),
                 LabelFactory.text(currentWeightDate.getDisplayName()),
                 RowFactory.create(CELL_SPACING, date, currentWeightAge));

        // add fields to enter the new weight
        Component weight = editor.getWeight().getComponent();
        Component units = editor.getUnits().getComponent();
        grid.add(LabelFactory.create("workflow.checkin.weight.new"), RowFactory.create(CELL_SPACING, weight, units));

        FocusGroup result = new FocusGroup("WeightPanel");
        result.add(weight);
        result.add(units);
        return result;
    }

    /**
     * Determines if no weight has been entered, or is zero.
     *
     * @return {@code true} if the weight is zero
     */
    public boolean isZero() {
        return editor.isZero();
    }

    /**
     * Saves the weight if required, linking it to the visit.
     *
     * @param visit the visit
     */
    public void save(Act visit) {
        if (!isZero() && editor.isModified()) {
            weightBean.save();
            PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(visit, weight);
            linker.run();
        }
    }

    /**
     * Determines if the weight is current.
     *
     * @return {@code true} if the user has entered a weight, or the weight was recorded today
     */
    public boolean isWeightCurrent() {
        boolean result = !isZero();
        if (!result && !MathRules.isZero(currentWeight.getBigDecimal(BigDecimal.ZERO))) {
            Date date = currentWeightDate.getDate();
            result = (date != null && DateRules.compareDateToToday(date) != 0);
        }
        return result;
    }

    /**
     * Editor for the patient weight.
     */
    private static class WeightEditor extends PatientActEditor {
        WeightEditor(Act act, LayoutContext context) {
            super(act, null, context);
        }

        public PropertyEditor getWeight() {
            return (PropertyEditor) getEditor("weight");
        }

        public PropertyEditor getUnits() {
            return (PropertyEditor) getEditor("units");
        }

        public void setWeight(BigDecimal value) {
            getProperty("weight").setValue(value);
        }

        public boolean isZero() {
            return MathRules.isZero(getProperty("weight").getBigDecimal(BigDecimal.ZERO));
        }


        /**
         * Invoked when layout has completed.
         * <p>
         * This can be used to perform processing that requires all editors to be created.
         */
        @Override
        protected void onLayoutCompleted() {
            super.onLayoutCompleted();
            // need to remove the parent component layout data as the components are being used outside of the editor
            getWeight().getComponent().setLayoutData(null);
            getUnits().getComponent().setLayoutData(null);
        }
    }
}

