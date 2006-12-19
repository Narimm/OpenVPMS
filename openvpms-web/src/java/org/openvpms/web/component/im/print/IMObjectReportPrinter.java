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

package org.openvpms.web.component.im.print;

import org.openvpms.archetype.rules.doc.MediaHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.IMObjectReportFactory;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.TemplateHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.spring.ServiceHelper;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Prints reports for {@link IMObject}s generated by {@link IMObjectReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportPrinter<T extends IMObject>
        extends AbstractIMObjectPrinter<T> {

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document getDocument(T object) {
        List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(object);
        IMObjectReport report = createReport(object);
        String[] mimeTypes = {DocFormats.PDF_TYPE};
        return report.generate(objects, mimeTypes);
    }

    /**
     * Creates a new report.
     *
     * @param object the object to report on
     * @return a new report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObjectReport createReport(T object) {
        String shortName = object.getArchetypeId().getShortName();
        return IMObjectReportFactory.create(
                shortName, ArchetypeServiceHelper.getArchetypeService(),
                ServiceHelper.getDocumentHandlers());
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or <code>null</code> if there is
     *         none defined
     */
    protected String getDefaultPrinter(IMObject object) {
        String printer = null;
        String shortName = object.getArchetypeId().getShortName();
        Entity template = TemplateHelper.getTemplateForArchetype(
                shortName, ArchetypeServiceHelper.getArchetypeService());
        Party practice = GlobalContext.getInstance().getPractice();
        if (template != null && practice != null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            printer = TemplateHelper.getPrinter(template, practice, service);
        }
        return printer;
    }

    /**
     * Returns the print properties for an object.
     *
     * @param object  the object to print
     * @param printer the printer
     * @return the print properties
     */
    @Override
    protected PrintProperties getProperties(T object, String printer) {
        PrintProperties properties = super.getProperties(object, printer);
        String shortName = object.getArchetypeId().getShortName();
        Entity template = TemplateHelper.getTemplateForArchetype(
                shortName, ArchetypeServiceHelper.getArchetypeService());
        if (template != null) {
            IMObjectBean bean = new IMObjectBean(template);
            String size = bean.getString("paperSize");
            BigDecimal width = bean.getBigDecimal("paperWidth");
            BigDecimal height = bean.getBigDecimal("paperHeight");
            String units = bean.getString("paperUnits");
            if (size != null) {
                MediaSizeName mediaSize
                        = MediaHelper.getMedia(size, width, height, units);
                properties.setMediaSize(mediaSize);
            }
            Party practice = GlobalContext.getInstance().getPractice();
            if (practice != null) {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                MediaTray tray = TemplateHelper.getMediaTray(template, practice,
                                                             printer,
                                                             service);
                properties.setMediaTray(tray);
            }
        }

        return properties;
    }
}
