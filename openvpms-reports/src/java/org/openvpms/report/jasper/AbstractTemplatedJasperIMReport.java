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

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.IMReportException;

import java.util.Map;


/**
 * A {@link JasperIMReport} that uses pre-defined templates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractTemplatedJasperIMReport<T>
        extends AbstractJasperIMReport<T> {

    /**
     * The template loader.
     */
    private JasperTemplateLoader template;


    /**
     * Constructs a new <code>AbstractTemplatedJasperIMReport</code>.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @throws IMReportException if the report cannot be created
     */
    public AbstractTemplatedJasperIMReport(Document template,
                                           IArchetypeService service,
                                           DocumentHandlers handlers) {
        super(service, handlers);
        this.template = new JasperTemplateLoader(template, service, handlers);
    }

    /**
     * Constructs a new <code>AbstractTemplatedJasperIMReport</code>.
     *
     * @param design   the master report design
     * @param service  the archetype service
     * @param handlers the document handlers
     * @throws IMReportException if the report cannot be created
     */
    public AbstractTemplatedJasperIMReport(JasperDesign design,
                                           IArchetypeService service,
                                           DocumentHandlers handlers) {
        super(service, handlers);
        this.template = new JasperTemplateLoader(design, service, handlers);
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return template.getReport();
    }

    /**
     * Returns the sub-reports.
     *
     * @return the sub-reports.
     */
    public JasperReport[] getSubreports() {
        return template.getSubreports();
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();
        result.putAll(template.getParameters());
        return result;
    }

}
