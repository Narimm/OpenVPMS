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
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.TemplateHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.servlet.DownloadServlet;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link IMObjectPrinter} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectPrinter<T extends IMObject>
        implements IMObjectPrinter<T> {

    /**
     * The object to print.
     */
    private final T object;


    /**
     * Constructs a new <code>AbstractIMObjectPrinter</code>.
     *
     * @param object the object to print
     */
    public AbstractIMObjectPrinter(T object) {
        this.object = object;
    }

    /**
     * Returns the object being printed.
     *
     * @return the object being printed
     */
    public T getObject() {
        return object;
    }

    /**
     * Prints the object to the default printer.
     *
     * @throws OpenVPMSException for any error
     */
    public void print() {
        print(getDefaultPrinter());
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be <code>null</code>
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        IMObjectReport report = createReport();
        List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(object);
        report.print(objects, getProperties(object, printer));
    }

    /**
     * Creates a new report.
     *
     * @return a new report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected abstract IMObjectReport createReport();

    /**
     * Returns the print properties for an object.
     *
     * @param object  the object to print
     * @param printer the printer
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    protected PrintProperties getProperties(T object, String printer) {
        return new PrintProperties(printer);
    }

    /**
     * Generates a document and downloads it to the client.
     *
     * @throws OpenVPMSException for any error
     */
    protected void download() {
        Document document = getDocument();
        DownloadServlet.startDownload(document);
    }

    /**
     * Helper to return the media size for a document template.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @return the media size for the template, or <code>null</code> if none
     *         is defined
     * @throws OpenVPMSException for any error
     */
    protected MediaSizeName getMediaSize(Entity template) {
        IMObjectBean bean = new IMObjectBean(template);
        String size = bean.getString("paperSize");
        if (size != null) {
            BigDecimal width = bean.getBigDecimal("paperWidth");
            BigDecimal height = bean.getBigDecimal("paperHeight");
            String units = bean.getString("paperUnits");
            return MediaHelper.getMedia(size, width, height, units);
        }
        return null;
    }

    /**
     * Helper to return the media tray for a document template.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @param printer  the printer name
     * @return the media tray for the template, or <code>null</code> if none
     *         is defined
     */
    protected MediaTray getMediaTray(Entity template, String printer) {
        Party practice = GlobalContext.getInstance().getPractice();
        if (practice != null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            return TemplateHelper.getMediaTray(template, practice, printer,
                                               service);
        }
        return null;
    }

}
