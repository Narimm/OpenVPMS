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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.ReportException;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.component.im.print.AbstractIMPrinter;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * {@link DocumentAct} printer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActPrinter extends AbstractIMPrinter<IMObject> {

    /**
     * The report generator.
     */
    private final ReportGenerator generator;


    /**
     * Constructs a new <code>DocumentActPrinter</code>.
     *
     * @param object the object to print
     * @throws DocumentException if the object doesn't have any
     *                           <em>participation.documentTemplate</em>
     *                           participation
     */
    public DocumentActPrinter(DocumentAct object) {
        super(object);
        generator = new ReportGenerator(object);
        setInteractive(getInteractive(generator.getTemplate(),
                                      getDefaultPrinter()));
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        return DescriptorHelper.getDisplayName(getObject());
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be <code>null</code>
     * @throws OpenVPMSException for any error
     */
    @Override
    public void print(String printer) {
        DocumentAct act = (DocumentAct) getObject();
        Document doc = (Document) IMObjectHelper.getObject(
                act.getDocReference());
        if (doc == null) {
            IMReport<IMObject> report = createReport();
            List<IMObject> objects = new ArrayList<IMObject>();
            objects.add(act);
            report.print(objects.iterator(), getProperties(printer));
        } else if (DocFormats.ODT_TYPE.equals(doc.getMimeType())) {
            OpenOfficeHelper.getPrintService().print(doc, printer);
        } else {
            download();
        }
    }

    /**
     * Returns a document for an object.
     *
     * @return a document
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocument() {
        DocumentAct object = (DocumentAct) getObject();
        Document doc = (Document) IMObjectHelper.getObject(
                object.getDocReference());
        if (doc == null) {
            ReportGenerator gen = new ReportGenerator(object);
            doc = gen.generate(object, DocFormats.PDF_TYPE);
        }
        return doc;
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or <code>null</code> if there is
     *         none defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        return getDefaultPrinter(generator.getTemplate());
    }

    /**
     * Creates a new report.
     *
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMReport<IMObject> createReport() {
        return generator.createReport();
    }

    /**
     * Returns the print properties for an object.
     *
     * @param printer the printer
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    @Override
    protected PrintProperties getProperties(String printer) {
        return getProperties(printer, generator.getTemplate());
    }

}
