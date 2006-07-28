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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperExportManager;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.IMObjectReportException;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of the {@link JasperIMObjectReport} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractJasperIMObjectReport
        implements JasperIMObjectReport {

    /**
     * The archetype service.
     */
    private final IArchetypeService _service;


    /**
     * Constructs a new <code>AbstractJasperIMObjectReport</code>.
     *
     * @param service the archetype service
     */
    public AbstractJasperIMObjectReport(IArchetypeService service) {
        _service = service;
    }

    /**
     * Generates a report for an object.
     *
     * @param object the object
     * @return a document containing the report
     * @throws IMObjectReportException for any error
     */
    public Document generate(IMObject object) {
        Document document = (Document) _service.create("document.other");
        try {
            JasperPrint print = report(object);
            byte[] report = JasperExportManager.exportReportToPdf(print);
            document.setName(print.getName());
            document.setContents(report);
            document.setMimeType("application/pdf");
            document.setDocSize(report.length);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception);
        }
        return document;
    }

    /**
     * Generates a report for an object.
     *
     * @param object the object
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(IMObject object) throws JRException {
        IMObjectDataSource source
                = new IMObjectDataSource(object, getArchetypeService());
        HashMap<String, Object> properties
                = new HashMap<String, Object>(getParameters(object));
        properties.put("dataSource", source);
        return JasperFillManager.fillReport(getReport(), properties, source);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return _service;
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @param object the object to report on
     * @return the report parameters
     */
    protected Map<String, Object> getParameters(IMObject object) {
        return new HashMap<String, Object>();
    }
}
