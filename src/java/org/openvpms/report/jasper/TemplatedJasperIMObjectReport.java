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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.ReportException;

import java.util.Iterator;


/**
 * A {@link JasperIMReport} that uses pre-defined templates, operating on collections of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class TemplatedJasperIMObjectReport extends AbstractTemplatedJasperIMReport<IMObject> {

    /**
     * Constructs a {@code TemplatedJasperIMObjectReport}.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @throws ReportException if the report cannot be created
     */
    public TemplatedJasperIMObjectReport(Document template, IArchetypeService service, DocumentHandlers handlers) {
        super(template, service, handlers);
    }

    /**
     * Constructs a {@code TemplatedJasperIMObjectReport}.
     *
     * @param design   the master report design
     * @param service  the archetype service
     * @param handlers the document handlers
     * @throws ReportException if the report cannot be created
     */
    public TemplatedJasperIMObjectReport(JasperDesign design, IArchetypeService service, DocumentHandlers handlers) {
        super(design, service, handlers);
    }

    /**
     * Creates a data source for a collection of objects.
     *
     * @param objects an iterator over the collection of objects
     * @return a new data source
     */
    protected JRDataSource createDataSource(Iterator<IMObject> objects) {
        return new IMObjectCollectionDataSource(objects, getArchetypeService(), getDocumentHandlers());
    }

}
