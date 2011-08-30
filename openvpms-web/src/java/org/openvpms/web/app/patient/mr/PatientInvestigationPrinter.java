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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.system.ServiceHelper;

/**
 * A printer for <em>act.patientInvestigation</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PatientInvestigationPrinter extends IMObjectReportPrinter<Act> {

    /**
     * Constructs a <tt>PatientInvestigationPrinter</tt>.
     *
     * @param investigation the investigation to print
     * @throws OpenVPMSException for any error
     */
    public PatientInvestigationPrinter(Act investigation) {
        super(investigation, getTemplateLocator(investigation));
    }

    /**
     * Returns a document template locator.
     * <p/>
     * TODO - this should not be dependent on the global context
     *
     * @param investigation the investigation
     * @return a new document template locator
     */
    private static DocumentTemplateLocator getTemplateLocator(Act investigation) {
        DocumentTemplate template = null;
        ActBean act = new ActBean(investigation);
        Entity investigationType = act.getParticipant(InvestigationArchetypes.INVESTIGATION_TYPE_PARTICIPATION);
        if (investigationType != null) {
            EntityBean bean = new EntityBean(investigationType);
            Entity entity = bean.getNodeTargetEntity("template");
            if (entity != null) {
                template = new DocumentTemplate(entity, ServiceHelper.getArchetypeService());
            }
        }
        return new ContextDocumentTemplateLocator(template, investigation, GlobalContext.getInstance());
    }

}
