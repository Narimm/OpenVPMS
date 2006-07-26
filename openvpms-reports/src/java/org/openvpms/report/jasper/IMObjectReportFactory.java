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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * A factory for {@link IMObjectReport} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportFactory {

    /**
     * Create a new report.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @return a new report
     * @throws JRException for any error
     */
    public static IMObjectReport create(String shortName,
                                        IArchetypeService service)
            throws JRException {
        JasperDesign report = TemplateHelper.getReportForArchetype(shortName,
                                                                   service);
        if (report != null) {
            return new TemplatedIMObjectReport(report, service);
        }
        return new DynamicIMObjectReport(
                service.getArchetypeDescriptor(shortName), service);
    }
}
