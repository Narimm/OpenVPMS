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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


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
     * The original addendum.
     */
    private Act addendum;

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
        init(event, isProblem ? item : null, isProblem ? null : item, null);
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
        init(event, problem, item, null);
    }

    /**
     * Constructs a {@link PatientMedicalRecordLinker}.
     *
     * @param event    the patient clinical event. May be {@code null}
     * @param problem  the patient clinical problem. May be {@code null}
     * @param item     a medication or clinical note. May be {@code null}
     * @param addendum the addendum. May be {@code null}
     */
    public PatientMedicalRecordLinker(Act event, Act problem, Act item, Act addendum) {
        rules = ServiceHelper.getBean(MedicalRecordRules.class);
        init(event, problem, item, addendum);
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
     * Links the records.
     * <p/>
     * This is invoked in a transaction.
     *
     * @param currentEvent    the current instance of the event. May be {@code null}
     * @param currentProblem  the current instance of the problem. May be {@code null}
     * @param currentItem     the current instance of the item. May be {@code null}
     * @param currentAddendum the current instance of the addendum. May be {@code null}
     */
    protected void link(Act currentEvent, Act currentProblem, Act currentItem, Act currentAddendum) {
        rules.linkMedicalRecords(currentEvent, currentProblem, currentItem, currentAddendum);
    }

    /**
     * Runs the action for the first time.
     * <p/>
     * This implementation delegates to {@link #runAction()}.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     * retried
     * @throws RuntimeException if the action fails and may be retried
     */
    @Override
    protected boolean runFirst() {
        return linkRecords(event, problem, item, addendum);
    }

    /**
     * Runs the action.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     * retried
     * @throws RuntimeException if the action fails and may be retried
     */
    @Override
    protected boolean runAction() {
        return linkRecords(IMObjectHelper.reload(event), IMObjectHelper.reload(problem), IMObjectHelper.reload(item),
                           IMObjectHelper.reload(addendum));
    }

    /**
     * Initialises this.
     *
     * @param event    the patient clinical event. May be {@code null}
     * @param problem  the patient clinical problem. May be {@code null}
     * @param item     the patient record item. May be {@code null}
     * @param addendum the addendum. May be {@code null}
     */
    private void init(Act event, Act problem, Act item, Act addendum) {
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
        if (addendum != null && addendum.isNew()) {
            throw new IllegalStateException("Argument 'addendum' must be saved: "
                                            + addendum.getArchetypeId().getShortName());
        }
        this.event = event;
        this.problem = problem;
        this.item = item;
        this.addendum = addendum;
    }

    /**
     * Links the records.
     *
     * @param currentEvent    the current instance of the event. May be {@code null}
     * @param currentProblem  the current instance of the problem. May be {@code null}
     * @param currentItem     the current instance of the item. May be {@code null}
     * @param currentAddendum the current instance of the addendum. May be {@code null}
     * @return {@code true} if the records were linked, {@code false} if an act is no longer available
     */
    private boolean linkRecords(final Act currentEvent, final Act currentProblem, final Act currentItem,
                                final Act currentAddendum) {
        boolean result = false;
        if (currentEvent == null && event != null) {
            logMissing(event);
        } else if (currentProblem == null && problem != null) {
            logMissing(problem);
        } else if (currentItem == null && item != null) {
            logMissing(item);
        } else if (currentAddendum == null && addendum != null) {
            logMissing(addendum);
        } else {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    link(currentEvent, currentProblem, currentItem, currentAddendum);
                }
            });
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
