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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.system.ServiceHelper;


/**
 * Helper to link medical records in a way suitable for {@link org.openvpms.web.component.retry.Retryer}.
 * <p/>
 * This is required as there may be a lot of contention for a given set of patient records.
 *
 * @author Tim Anderson
 */
public class PatientMedicalRecordLinker extends AbstractRetryable {

    /**
     * The original patient clinical event.
     */
    private Act event;

    /**
     * The original patient clinical problem.
     */
    private Act problem;

    /**
     * The original patient record item.
     */
    private Act item;

    /**
     * The current event.
     */
    private Act currentEvent;

    /**
     * The current problem;
     */
    private Act currentProblem;

    /**
     * The current item.
     */
    private Act currentItem;

    /**
     * The rules.
     */
    private final MedicalRecordRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PatientMedicalRecordLinker.class);


    /**
     * Constructs a {@link PatientMedicalRecordLinker}.
     *
     * @param event the patient clinical event
     * @param item  the patient record item
     */
    public PatientMedicalRecordLinker(Act event, Act item) {
        rules = ServiceHelper.getBean(MedicalRecordRules.class);
        boolean isProblem = TypeHelper.isA(item, PatientArchetypes.CLINICAL_PROBLEM);
        init(event, isProblem ? item : null, isProblem ? null : item);
    }

    /**
     * Constructs a {@link PatientMedicalRecordLinker}.
     *
     * @param event   the patient clinical event. May be {@code null}
     * @param problem the patient clinical problem. May be {@code null}
     * @param item    the patient record item. May be {@code null}
     */
    public PatientMedicalRecordLinker(Act event, Act problem, Act item) {
        rules = ServiceHelper.getBean(MedicalRecordRules.class);
        init(event, problem, item);
    }

    /**
     * Returns the current instance of the event.
     * <p/>
     * If the link was successful, and one was supplied at construction, this will be non-null.
     *
     * @return the current instance of the event. May be {@code null}
     */
    public Act getEvent() {
        return currentEvent;
    }

    /**
     * Returns the current instance of the problem.
     * <p/>
     * If the link was successful, and one was supplied at construction, this will be non-null.
     *
     * @return the current instance of og the problem. May be {@code null}
     */
    public Act getProblem() {
        return currentProblem;
    }

    /**
     * Returns the current instance of the item.
     * <p/>
     * If the link was successful, and one was supplied at construction, this will be non-null.
     *
     * @return the current instance of the item. May be {@code null}
     */
    public Act getItem() {
        return currentItem;
    }

    /**
     * Returns a string representation of this.
     *
     * @return a string representation of this.
     */
    public String toString() {
        return "PatientMedicalRecordLinker(" + getId(event) + ", " + getId(item) + ")";
    }

    /**
     * Runs the action for the first time.
     * <p/>
     * This implementation delegates to {@link #runAction()}.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     *         retried
     * @throws RuntimeException if the action fails and may be retried
     */
    @Override
    protected boolean runFirst() {
        return linkRecords(event, problem, item);
    }

    /**
     * Runs the action.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     *         retried
     * @throws RuntimeException if the action fails and may be retried
     */
    @Override
    protected boolean runAction() {
        return linkRecords(IMObjectHelper.reload(event), IMObjectHelper.reload(problem), IMObjectHelper.reload(item));
    }

    /**
     * Initialises this.
     *
     * @param event   the patient clinical event. May be {@code null}
     * @param problem the patient clinical problem. May be {@code null}
     * @param item    the patient record item. May be {@code null}
     */
    private void init(Act event, Act problem, Act item) {
        if (event != null) {
            if (!TypeHelper.isA(event, PatientArchetypes.CLINICAL_EVENT)) {
                throw new IllegalArgumentException("Argument 'event' is invalid: "
                                                   + event.getArchetypeId().getShortName());
            }
            if (event.isNew()) {
                throw new IllegalStateException("Argument 'event' must be saved");
            }
        }
        if (problem != null && problem.isNew()) {
            throw new IllegalStateException("Argument 'problem' must be saved");
        }
        if (item != null && item.isNew()) {
            throw new IllegalStateException("Argument 'item' must be saved: " + item.getArchetypeId().getShortName());
        }
        this.event = event;
        this.problem = problem;
        this.item = item;
    }

    /**
     * Links the records.
     *
     * @param currentEvent   the current instance of the event. May be {@code null}
     * @param currentProblem the current instance of the problem. May be {@code null}
     * @param currentItem    the current instance of the item. May be {@code null}
     * @return {@code true} if the records were linked, {@code false} if one or both of the events is missing.
     */
    private boolean linkRecords(Act currentEvent, Act currentProblem, Act currentItem) {
        boolean result = false;
        if (currentEvent == null && event != null) {
            logMissing(event);
        } else if (currentProblem == null && problem != null) {
            logMissing(problem);
        } else if (currentItem == null && item != null) {
            logMissing(item);
        } else {
            // link the records to the event
            rules.linkMedicalRecords(currentEvent, currentProblem, currentItem);
            this.currentEvent = currentEvent;
            this.currentProblem = currentProblem;
            this.currentItem = currentItem;
            result = true;
        }
        return result;
    }

    /**
     * Logs a failure to link due to missing act.
     *
     * @param source the source act
     */
    private void logMissing(Act source) {
        log.warn("Cannot link " + getId(source) + ": it no longer exists");
    }

    /**
     * Helper to return an id for an act.
     *
     * @param act the act
     * @return the id
     */
    private String getId(Act act) {
        if (act != null) {
            IMObjectReference ref = act.getObjectReference();
            return ref.getArchetypeId().getShortName() + "-" + ref.getId();
        }
        return null;
    }

}
