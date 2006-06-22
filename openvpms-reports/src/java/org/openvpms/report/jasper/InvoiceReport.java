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
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Map;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceReport extends AbstractIMObjectReport {

    /**
     * The compiled report.
     */
    private final JasperReport _invoice;

    /**
     * The compiled sub-report.
     */
    private final JasperReport _invoiceItems;


    /**
     * Constructs a new <code>InvoiceReport</code>.
     *
     * @param service the archetype service
     * @throws JRException for any error
     */
    public InvoiceReport(IArchetypeService service) throws JRException {
        super(service);
        JasperDesign invoice
                = JRXmlLoader.load("src/reports/invoice.jrxml");
        JasperDesign invoiceItems
                = JRXmlLoader.load("src/reports/invoiceItems.jrxml");
        if (!invoice.getParametersMap().containsKey("object")) {
            JRDesignParameter object = new JRDesignParameter();
            object.setName("object");
            object.setValueClass(IMObject.class);
            invoice.addParameter(object);
        }
        _invoice = JasperCompileManager.compileReport(invoice);
        _invoiceItems = JasperCompileManager.compileReport(invoiceItems);

    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return _invoice;
    }

    /**
     * Returns the sub-reports.
     *
     * @return the sub-reports.
     */
    public JasperReport[] getSubreports() {
        return new JasperReport[] {_invoiceItems};
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @param object the object to report on
     * @return the report parameters
     */
    protected Map<String, Object> getParameters(IMObject object) {
        Map<String, Object> result = super.getParameters(object);
        result.put("object", object);
        result.put("items_subreport", _invoiceItems);
        return result;
    }
}
