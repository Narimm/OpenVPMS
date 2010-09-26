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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.mr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Helper to link medical records in a way suitable for {@link org.openvpms.web.component.util.Retryer}.
 * <p/>
 * This is required as there may be a lot of contention for a given set of patient records.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientMedicalRecordLinker implements Runnable {

    /**
     * The patient clinical event.
     */
    private final Act event;

    /**
     * The patient clinical problem. May be <tt>null</tt>
     */
    private final Act problem;

    /**
     * The patient record item.
     */
    private final Act item;

    /**
     * The rules.
     */
    private final MedicalRecordRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PatientMedicalRecordLinker.class);


    /**
     * Constructs a <tt>PatientMedicalRecordLinker</tt>.
     *
     * @param event   the patient clinical event
     * @param problem the patient clinical problem. May be <tt>null</tt>
     * @param item    the patient record item. May be <tt>null</tt>
     */
    public PatientMedicalRecordLinker(Act event, Act problem, Act item) {
        this.event = event;
        this.problem = problem;
        this.item = item;
        if (event.isNew()) {
            throw new IllegalStateException("Argument 'event' must be saved");
        }
        if (problem != null) {
            if (!TypeHelper.isA(problem, PatientArchetypes.CLINICAL_PROBLEM)) {
                throw new IllegalArgumentException("Argument 'problem' is invalid: "
                                                   + problem.getArchetypeId().getShortName());
            }
            if (problem.isNew()) {
                throw new IllegalStateException("Argument 'problem' must be saved");
            }
        }
        if (item != null && item.isNew()) {
            throw new IllegalStateException("Argument 'item' must be saved");
        }
        if (!TypeHelper.isA(event, PatientArchetypes.CLINICAL_EVENT)) {
            throw new IllegalArgumentException("Argument 'event' is invalid: " + event.getArchetypeId().getShortName());
        }
        rules = new MedicalRecordRules();
    }

    /**
     * Link the records.
     *
     * @throws RuntimeException for any error
     */
    public void run() {
        Act currentEvent = IMObjectHelper.reload(event);
        Act currentProblem = IMObjectHelper.reload(problem);
        Act currentItem = IMObjectHelper.reload(item);
        if (currentEvent == null) {
            logMissing(event, item, event);
        } else {
            if (problem != null && currentProblem == null) {
                logMissing(problem, item, problem);
            }
            if (item != null && currentItem == null) {
                logMissing(event, item, item);
            }
            // link the records to the event (those that exist...)
            rules.linkMedicalRecords(currentEvent, currentProblem, currentItem);
        }
    }

    /**
     * Returns a string representation of this.
     *
     * @return a string representation of this.
     */
    public String toString() {
        return "PatientMedicalRecordLinker(" + getId(event) + ", " + getId(problem) + ", " + getId(item) + ")";
    }

    /**
     * Logs a failure to link due to missing act.
     *
     * @param source  the source act
     * @param target  the target act
     * @param missing the missing act
     */
    private void logMissing(Act source, Act target, Act missing) {
        log.warn("Cannot link " + getId(source) + " with " + getId(target) + ": " + getId(missing)
                 + " no longer exists");
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
