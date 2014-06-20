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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.archetype.rules.doc;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.InvalidOrientation;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.InvalidPaperSize;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.InvalidUnits;


/**
 * Wrapper for <em>entity.documentTemplate</em>.
 *
 * @author Tim Anderson
 */
public class DocumentTemplate {

    public enum PrintMode {
        IMMEDIATE,
        MANUAL,
        CHECK_OUT
    }

    /**
     * A4 paper size.
     */
    public static final String A4 = "A4";

    /**
     * A5 paper size.
     */
    public static final String A5 = "A5";

    /**
     * Letter paper size.
     */
    public static final String LETTER = "LETTER";

    /**
     * Custom paper size.
     */
    public static final String CUSTOM = "CUSTOM";

    /**
     * Portrait print orientation.
     */
    public static final String PORTRAIT = "PORTRAIT";

    /**
     * Landscape print orientation.
     */
    public static final String LANDSCAPE = "LANDSCAPE";

    /**
     * Millimetres unit for paper size.
     */
    public static final String MM = "MM";

    /**
     * Inchces unit for paper size.
     */
    public static final String INCH = "INCH";

    /**
     * The bean to access the template's properties.
     */
    private final EntityBean bean;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a <tt>DocumentTemplate</tt>.
     *
     * @param template the template
     * @param service  the archetype service
     */
    public DocumentTemplate(Entity template, IArchetypeService service) {
        bean = new EntityBean(template, service);
        this.service = service;
    }

    /**
     * Returns the document template name.
     *
     * @return the document template name. May be <tt>null</tt>
     */
    public String getName() {
        return bean.getString("name");
    }

    /**
     * Sets the document template name.
     *
     * @param name the name
     */
    public void setName(String name) {
        bean.setValue("name", name);
    }

    /**
     * Returns the document template description.
     *
     * @return the document template description. May be <tt>null</tt>
     */
    public String getDescription() {
        return bean.getString("description");
    }

    /**
     * Sets the document template description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        bean.setValue("description", description);
    }

    /**
     * Determines if the template is active.
     *
     * @return <tt>true</tt> if the template is active; otherwise it is inactive
     */
    public boolean isActive() {
        return bean.getBoolean("active");
    }

    /**
     * Determines if the template is active.
     *
     * @param active if <tt>true<tt> the template is active, otherwise it is inactive
     */
    public void setActive(boolean active) {
        bean.setValue("active", active);
    }

    /**
     * Returns the archetype that the template applies to.
     * <p/>
     * The archetype may be an archetype short name, or a 'special' archetype such as
     * <em>GROUPED_REMINDERS</em> or <em>REPORT</em>
     * <p/>
     * TODO: this node should be changed to better reflect its usage
     *
     * @return the archetype. May be <tt>null</tt>
     */
    public String getArchetype() {
        return bean.getString("archetype");
    }

    /**
     * Sets the archetype that the template applies to.
     *
     * @param archetype the archetype
     */
    public void setArchetype(String archetype) {
        bean.setValue("archetype", archetype);
    }

    /**
     * Returns the user level that the template applies to.
     * <p/>
     * TODO - need a better facility for user authorisation
     *
     * @return the user level that the template applies to. May be <tt>null</tt>
     */
    public String getUserLevel() {
        return bean.getString("userLevel");
    }

    /**
     * Sets the user level that the template applies to.
     *
     * @param level the user level
     */
    public void setUserLevel(String level) {
        bean.setValue("userLevel", level);
    }

    /**
     * Returns the report type.
     *
     * @return the report type. May be <tt>null</tt>
     */
    public String getReportType() {
        return bean.getString("reportType");
    }

    /**
     * Sets the report type.
     *
     * @param type the report type
     */
    public void setReportType(String type) {
        bean.setValue("reportType", type);
    }

    /**
     * Returns the print mode.
     *
     * @return the print mode. May be <tt>null</tt>
     */
    public PrintMode getPrintMode() {
        String mode = bean.getString("printMode");
        return (mode != null) ? PrintMode.valueOf(mode) : null;
    }

    /**
     * Sets the print mode.
     *
     * @param mode the print mode. May be <tt>null</tt>
     */
    public void setPrintMode(PrintMode mode) {
        bean.setValue("printMode", mode != null ? mode.name() : null);
    }

    /**
     * Returns the paper size.
     * <p/>
     * Current legal values are:
     * <ul>
     * <li>{@link #A4}
     * <li>{@link #A5}
     * <li>{@link #LETTER}
     * <li>{@link #CUSTOM}
     * </ul>
     *
     * @return the paper size. May be <tt>null</tt>
     */
    public String getPaperSize() {
        return bean.getString("paperSize");
    }

    /**
     * Sets the paper size.
     *
     * @param size the paper size
     */
    public void setPaperSize(String size) {
        bean.setValue("paperSize", size);
    }

    /**
     * Returns the print orientation.
     * <p/>
     * Current legal values are:
     * <ul>
     * <li>{@link #PORTRAIT}
     * <li>{@link #LANDSCAPE}
     * </ul>
     *
     * @return the print orientation. May be <tt>null</tt>
     */
    public String getOrientation() {
        return bean.getString("orientation");
    }

    /**
     * Sets the print orientation.
     *
     * @param orientation the print orientation
     */
    public void setOrientation(String orientation) {
        bean.setValue("orientation", orientation);
    }

    /**
     * Returns the default number of copies to print.
     *
     * @return the default number of copies
     */
    public int getCopies() {
        return bean.getInt("copies");
    }

    /**
     * Sets the default number of copies to print.
     *
     * @param copies the default number of copies
     */
    public void setCopies(int copies) {
        bean.setValue("copies", copies);
    }

    /**
     * Returns the paper height.
     * <p/>
     * Note that this is only applicable if {@link #getPaperSize()} is {@link #CUSTOM}.
     *
     * @return the paper height
     */
    public BigDecimal getPaperHeight() {
        return bean.getBigDecimal("paperHeight");
    }

    /**
     * Sets the paper height.
     *
     * @param height the paper height
     */
    public void setPaperHeight(BigDecimal height) {
        bean.setValue("paperHeight", height);

    }

    /**
     * Returns the paper width.
     * <p/>
     * Note that this is only applicable if {@link #getPaperSize()} is {@link #CUSTOM}.
     *
     * @return the paper width
     */
    public BigDecimal getPaperWidth() {
        return bean.getBigDecimal("paperWidth");
    }

    /**
     * Sets the paper width.
     *
     * @param width the paper width
     */
    public void setPaperWidth(BigDecimal width) {
        bean.setValue("paperWidth", width);
    }

    /**
     * Returns the paper units.
     * <p/>
     * Legal values are:
     * <ul>
     * <li>{@link #MM}
     * <li>{@link #INCH}
     * </ul>
     *
     * @return the paper units. May be <tt>null</tt>
     */
    public String getPaperUnits() {
        return bean.getString("paperUnits");
    }

    /**
     * Sets the paper units.
     *
     * @param units the paper units
     */
    public void setPaperUnits(String units) {
        bean.setValue("paperUnits", units);
    }

    /**
     * Returns the email subject to use when documents generated from the template are emailed.
     *
     * @return the email subject. May be <tt>null</tt>
     */
    public String getEmailSubject() {
        return bean.getString("emailSubject");
    }

    /**
     * Sets the email subject to use when documents generated from the template are emailed.
     *
     * @param subject the email subject
     */
    public void setEmailSubject(String subject) {
        bean.setValue("emailSubject", subject);
    }

    /**
     * Returns the email text to use when documents generated from the template are emailed.
     *
     * @return the email text. May be <tt>null</tt>
     */
    public String getEmailText() {
        return bean.getString("emailText");
    }

    /**
     * Sets the email text to use when documents generated from the template are emailed.
     *
     * @param text the text
     */
    public void setEmailText(String text) {
        bean.setValue("emailText", text);
    }

    /**
     * Returns the media size.
     *
     * @return the media size for the template, or <tt>null</tt> if none is defined
     */
    public MediaSizeName getMediaSize() {
        String size = getPaperSize();
        if (size != null) {
            BigDecimal width = getPaperWidth();
            BigDecimal height = getPaperHeight();
            String units = getPaperUnits();
            MediaSizeName media;
            if (PaperSize.CUSTOM.name().equals(size)) {
                media = getMedia(width, height, units);
            } else {
                media = PaperSize.getMediaSizeName(size);
            }
            return media;
        }
        return null;
    }

    /**
     * Returns the print orientation.
     *
     * @return the print orientation. May be <tt>null</tt>
     */
    public OrientationRequested getOrientationRequested() {
        return getOrientation() != null ? Orientation.getOrientation(getOrientation()) : null;
    }

    /**
     * Returns the printers.
     *
     * @return the printers
     */
    public List<DocumentTemplatePrinter> getPrinters() {
        List<DocumentTemplatePrinter> result = new ArrayList<DocumentTemplatePrinter>();
        List<EntityRelationship> printers = bean.getNodeRelationships("printers");
        for (EntityRelationship printer : printers) {
            result.add(new DocumentTemplatePrinter(printer, service));
        }
        return result;
    }

    /**
     * Returns the printer for a given practice organisation.
     *
     * @param location an <em>party.organisationPractice</em> or <em>party.organisationLocation</em>
     * @return the corresponding printer, or <tt>null</tt> if none is defined
     */
    public DocumentTemplatePrinter getPrinter(Entity location) {
        Predicate predicate = AndPredicate.getInstance(
                IsActiveRelationship.isActiveNow(), RefEquals.getTargetEquals(location.getObjectReference()));
        EntityRelationship printer = bean.getNodeRelationship("printers", predicate);
        return (printer != null) ? new DocumentTemplatePrinter(printer, service) : null;
    }

    /**
     * Addsa a new printer relationship.
     *
     * @param location an <em>party.organisationPractice</em> or <em>party.organisationLocation</em>
     * @return the new printer relationship
     */
    public DocumentTemplatePrinter addPrinter(Entity location) {
        EntityRelationship result = bean.addNodeRelationship("printers", location);
        return new DocumentTemplatePrinter(result, service);
    }

    /**
     * Returns the document associated with the template.
     *
     * @return the corresponding document, or <tt>null</tt> if none is found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    public Document getDocument() {
        return new TemplateHelper(service).getDocumentFromTemplate(bean.getEntity());
    }

    /**
     * Returns the file name format expression.
     *
     * @return the file name format expression. May be {@code null}
     */
    public String getFileNameExpression() {
        List<IMObject> fileNameFormat = bean.getValues("fileNameFormat");
        if (!fileNameFormat.isEmpty()) {
            IMObjectBean format = new IMObjectBean(fileNameFormat.get(0));
            return format.getString("expression");
        }
        return null;
    }

    /**
     * Saves the template.
     *
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    public void save() {
        bean.save();
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return bean.getObject().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj argument; <tt>false</tt> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DocumentTemplate) && ((DocumentTemplate) obj).bean.getObject().equals(bean.getObject());
    }

    /**
     * Helper to convert a custom paper size to a {@link MediaSizeName}.
     *
     * @param width  the page width
     * @param height the page height
     * @param units  the units. One of 'MM' or 'INCH'.
     * @return the corresponding media size name.
     * @throws DocumentException if the paper size is invalid
     */
    private MediaSizeName getMedia(BigDecimal width, BigDecimal height, String units) {
        int unitCode = Units.getUnits(units);
        try {
            return MediaSize.findMedia(width.floatValue(), height.floatValue(), unitCode);
        } catch (IllegalArgumentException exception) {
            String size = width + "x" + height + " " + units;
            throw new DocumentException(InvalidPaperSize, size);
        }
    }

    /**
     * Provides a mapping between supported orientations and values defined in
     *
     * @{link OrientationRequested}.
     */
    private enum Orientation {

        PORTRAIT(OrientationRequested.PORTRAIT),
        LANDSCAPE(OrientationRequested.LANDSCAPE);

        private Orientation(OrientationRequested orientation) {
            this.orientation = orientation;
        }

        public OrientationRequested getOrientation() {
            return orientation;
        }

        public static OrientationRequested getOrientation(String orientation) {
            for (Orientation o : Orientation.values()) {
                if (o.name().equals(orientation)) {
                    return o.getOrientation();
                }
            }
            throw new DocumentException(InvalidOrientation, orientation);
        }

        private final OrientationRequested orientation;
    }

    /**
     * Provides a mapping between supported paper sizes and
     * {@link MediaSizeName}.
     */
    private enum PaperSize {

        A4(MediaSizeName.ISO_A4),
        A5(MediaSizeName.ISO_A5),
        LETTER(MediaSizeName.NA_LETTER),
        CUSTOM(null);

        private PaperSize(MediaSizeName name) {
            mediaName = name;
        }

        public MediaSizeName getMediaSizeName() {
            return mediaName;
        }

        public static MediaSizeName getMediaSizeName(String name) {
            for (PaperSize size : values()) {
                if (size.name().equals(name)) {
                    return size.getMediaSizeName();
                }
            }
            throw new DocumentException(InvalidPaperSize, name);
        }

        private final MediaSizeName mediaName;
    }

    /**
     * Provides a mapping between paper size units and corresponding
     * values defined in {@link javax.print.attribute.Size2DSyntax}.
     */
    private enum Units {

        MM(Size2DSyntax.MM),
        INCH(Size2DSyntax.INCH);

        private Units(int units) {
            this.units = units;
        }

        public int getUnits() {
            return units;
        }

        public static int getUnits(String units) {
            for (Units u : Units.values()) {
                if (u.name().equals(units)) {
                    return u.getUnits();
                }
            }
            throw new DocumentException(InvalidUnits, units);
        }

        private final int units;
    }


}
