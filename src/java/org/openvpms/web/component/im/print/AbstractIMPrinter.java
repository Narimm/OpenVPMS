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
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMReport;
import org.openvpms.report.IMReportException;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.TemplateHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.servlet.DownloadServlet;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Abstract implementation of the {@link IMPrinter} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMPrinter<T> implements IMPrinter<T> {

    /**
     * The objects to print.
     */
    private final List<T> objects;

    /**
     * The object to print.
     */
    private final T object;


    /**
     * Constructs a new <code>AbstractIMPrinter</code> to print a single object.
     *
     * @param object the object to print
     */
    public AbstractIMPrinter(T object) {
        objects = new ArrayList<T>();
        objects.add(object);
        this.object = object;
    }

    /**
     * Constructs a new <code>AbstractIMPrinter</code> to print a collection
     * of objects.
     *
     * @param objects the objects to print
     */
    public AbstractIMPrinter(Collection<T> objects) {
        this.objects = new ArrayList<T>(objects);
        object = null;
    }

    /**
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    public List<T> getObjects() {
        return objects;
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
        IMReport<T> report = createReport();
        report.print(getObjects().iterator(), getProperties(printer));
    }

    /**
     * Returns the object being printed.
     *
     * @return the object being printed, or <code>null</code> if a collection
     *         is being printed
     */
    protected T getObject() {
        return object;
    }

    /**
     * Creates a new report.
     *
     * @return a new report
     * @throws IMReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected abstract IMReport<T> createReport();

    /**
     * Returns the print properties for an object.
     *
     * @param printer the printer
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    protected PrintProperties getProperties(String printer) {
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
     * Helper to return the media orientation for a document template.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @return the orientation for the template, or <code>null</code> if none
     *         is defined
     * @throws OpenVPMSException for any error
     */
    protected OrientationRequested getOrientation(Entity template) {
        IMObjectBean bean = new IMObjectBean(template);
        String orientation = bean.getString("orientation");
        if (orientation != null) {
            return MediaHelper.getOrientation(orientation);
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
