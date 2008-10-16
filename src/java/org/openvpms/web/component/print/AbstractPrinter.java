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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.print;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.doc.MediaHelper;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.PrintHelper;
import org.openvpms.web.servlet.DownloadServlet;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import java.math.BigDecimal;


/**
 * Abstract implementation of the {@link Printer} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractPrinter implements Printer {

    /**
     * Determines if printing should be interactive.
     */
    private boolean interactive = true;

    /**
     * The template helper.
     */
    private final TemplateHelper helper;


    /**
     * Constructs a new <tt>AbstractPrinter</tt>.
     */
    public AbstractPrinter() {
        helper = new TemplateHelper();
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
     * Determines if printing should occur interactively.
     *
     * @return <tt>true</tt> if printing should occur interactively,
     *         <tt>false</tt> if it can be performed non-interactively
     */
    public boolean getInteractive() {
        return interactive;
    }

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
     * Returns the print properties for an object.
     *
     * @param printer  the printer
     * @param template an <em>entity.documentTemplate</em>. May be <tt>null</tt>
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    protected PrintProperties getProperties(String printer, Entity template) {
        PrintProperties properties = new PrintProperties(printer);
        if (template != null) {
            properties.setMediaSize(getMediaSize(template));
            properties.setOrientation(getOrientation(template));
            properties.setMediaTray(getMediaTray(template, printer));
        }
        return properties;
    }

    /**
     * Prints a document, or downloads it to the client if printing is not
     * supported.
     *
     * @param document the document to print
     * @param printer  the printer
     */
    protected void print(Document document, String printer) {
        String mimeType = document.getMimeType();
        if (DocFormats.ODT_TYPE.equals(mimeType)
                || DocFormats.DOC_TYPE.equals(mimeType)) {
            OpenOfficeHelper.getPrintService().print(document, printer);
        } else {
            DownloadServlet.startDownload(document);
        }
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @param interactive if <tt>true</tt> print interactively
     */
    protected void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Helper to return the default printer for a template for the current
     * practice or location.
     *
     * @param template an <em>entity.documentTemplate</em>
     */
    protected String getDefaultPrinter(Entity template) {
        String result;
        Context context = GlobalContext.getInstance();
        EntityRelationship printer
                = getDocumentTemplatePrinter(context.getLocation(), template);
        if (printer == null) {
            printer = getDocumentTemplatePrinter(context.getPractice(),
                                                 template);
        }
        if (printer != null) {
            result = helper.getPrinter(printer);
        } else {
            result = getDefaultLocationPrinter(context.getLocation());
        }
        return result;
    }

    /**
     * Helper to return the media size for a document template.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @return the media size for the template, or <tt>null</tt> if none
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
     * @return the orientation for the template, or <tt>null</tt> if none
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
     * Helper to return the <em>entityRelationship.documentTemplatePrinter</em>
     * for a template and printer and current location/practice.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @param printer  the printer
     * @return the corresponding
     *         <em>entityRelationship.documentTemplatePrinter</em>
     *         or <tt>null</tt> if none is found
     */
    protected EntityRelationship getDocumentTemplatePrinter(Entity template,
                                                            String printer) {
        GlobalContext context = GlobalContext.getInstance();
        EntityRelationship result = getDocumentTemplatePrinter(
                context.getLocation(), template, printer);
        if (result == null) {
            result = getDocumentTemplatePrinter(context.getPractice(), template,
                                                printer);
        }
        return result;
    }

    /**
     * Helper to return the media tray for a document template for a particular
     * printer for the current practice.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @param printer  the printer name
     * @return the media tray for the template, or <tt>null</tt> if none
     *         is defined
     */
    protected MediaTray getMediaTray(Entity template, String printer) {
        EntityRelationship r = getDocumentTemplatePrinter(template, printer);
        return (r != null) ? helper.getMediaTray(r) : null;
    }

    /**
     * Helper to determine if printing should occur interactively for a
     * particular document template, printer and the current practice.
     * If no relationship is defined, defaults to <tt>true</tt>.
     *
     * @param template an <em>entity.documentTemplate</em>. May be <tt>null</tt>
     * @param printer  the printer name
     * @return <tt>true</tt> if printing should occur interactively
     */
    protected boolean getInteractive(Entity template, String printer) {
        boolean result = true;
        if (template != null) {
            EntityRelationship r = getDocumentTemplatePrinter(template,
                                                              printer);
            if (r != null) {
                result = helper.getInteractive(r);
            }
        }
        return result;
    }

    /**
     * Helper to return the <em>entityRelationship.documentTemplatePrinter</em>
     * for a template and printer for an organisation.
     *
     * @param organisation the organisation. May be <tt>null</tt>
     * @param template     an <em>entity.documentTemplate</em>
     * @param printer      the printer
     * @return the corresponding
     *         <em>entityRelationship.documentTemplatePrinter</em>
     *         or <tt>null</tt> if none is found
     */
    private EntityRelationship getDocumentTemplatePrinter(Party organisation,
                                                          Entity template,
                                                          String printer) {
        EntityRelationship relationship = getDocumentTemplatePrinter(
                organisation, template);
        if (relationship != null) {
            // make sure the relationship is for the same printer
            if (ObjectUtils.equals(printer, helper.getPrinter(relationship))) {
                return relationship;
            }
        }
        return null;
    }

    /**
     * Helper to return the <em>entityRelationship.documentTemplatePrinter</em>
     * for a template and organisation.
     *
     * @param organisation the organisation. May be <tt>null</tt>
     * @param template     an <em>entity.documentTemplate</em>
     * @return the corresponding
     *         <em>entityRelationship.documentTemplatePrinter</em>
     *         or <tt>null</tt> if none is found
     */
    private EntityRelationship getDocumentTemplatePrinter(Party organisation,
                                                          Entity template) {
        if (organisation != null) {
            return helper.getDocumentTemplatePrinter(template, organisation);
        }
        return null;
    }

    /**
     * Helper to return the default printer for a location.
     * If no default printer set than returns system default printer.
     *
     * @param location the location
     * @return the printer name
     */
    private String getDefaultLocationPrinter(Party location) {
        if (location != null) {
            IMObjectBean bean = new IMObjectBean(location);
            if (bean.hasNode("defaultPrinter")) {
                return bean.getString("defaultPrinter",
                                      PrintHelper.getDefaultPrinter());
            }
        }
        return PrintHelper.getDefaultPrinter();
    }
}
