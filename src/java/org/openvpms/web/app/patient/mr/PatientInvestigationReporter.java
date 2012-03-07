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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.StaticDocumentTemplateLocator;
import org.openvpms.web.system.ServiceHelper;


/**
 * A {@link Reporter} for <em>act.patientInvestigation</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PatientInvestigationReporter extends DocumentActReporter {

    /**
     * Constructs a <tt>PatientInvestigationReporter</tt>.
     *
     * @param act     the act
     * @param locator the document template locator if the act doesn't have a template
     */
    public PatientInvestigationReporter(DocumentAct act, DocumentTemplateLocator locator) {
        super(act, getTemplateLocator(act, locator));
    }

    /**
     * Returns a document template locator.
     *
     * @param investigation the investigation
     * @param locator       the document template locator if the act doesn't have a template
     * @return the document template locator
     */
    private static DocumentTemplateLocator getTemplateLocator(Act investigation, DocumentTemplateLocator locator) {
        DocumentTemplateLocator result = locator;
        ActBean act = new ActBean(investigation);
        Entity investigationType = act.getParticipant(InvestigationArchetypes.INVESTIGATION_TYPE_PARTICIPATION);
        if (investigationType != null) {
            EntityBean bean = new EntityBean(investigationType);
            Entity entity = bean.getNodeTargetEntity("template");
            if (entity != null) {
                DocumentTemplate template = new DocumentTemplate(entity, ServiceHelper.getArchetypeService());
                result = new StaticDocumentTemplateLocator(template);
            }
        }
        return result;
    }
}
