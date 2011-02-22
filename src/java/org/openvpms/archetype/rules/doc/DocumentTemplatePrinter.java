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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.doc;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import javax.print.attribute.standard.MediaTray;


/**
 * Wrapper for <em>entityRelationship.documentTemplatePrinter</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplatePrinter {

    /**
     * The bean to access the relationship's properties.
     */
    private final IMObjectBean bean;


    /**
     * Constructs a <tt>DocumentTemplatePrinter</tt>.
     *
     * @param relationship the <em>entityRelationship.documentTemplatePrinter</em>
     * @param service      the archetype service
     */
    public DocumentTemplatePrinter(EntityRelationship relationship, IArchetypeService service) {
        bean = new IMObjectBean(relationship, service);
    }

    /**
     * Returns the printer name.
     *
     * @return the printer name. May be <tt>null</tt>
     */
    public String getPrinterName() {
        return bean.getString("printerName");
    }

    /**
     * Sets the printer name.
     *
     * @param name the printer name
     */
    public void setPrinterName(String name) {
        bean.setValue("printerName", name);
    }

    /**
     * Returns the paper tray.
     * Current legal values are:
     * <ul>
     * <li>TOP
     * <li>MIDDLE
     * <li>BOTTOM
     * <li>ENVELOPE
     * <li>MANUAL
     * <li>LARGE_CAPACITY
     * <li>MAIN
     * <li>SIDE
     * </ul>
     *
     * @return the paper tray. May be <tt>null</tt>
     */
    public String getPaperTray() {
        return bean.getString("paperTray");
    }

    /**
     * Sets the paper tray.
     *
     * @param tray the paper tray
     */
    public void setPaperTray(String tray) {
        bean.setValue("paperTray", tray);
    }

    /**
     * Determines if the template should be printed interactively.
     *
     * @return <tt>true</tt> if the template should be printed interactively; <tt>false</tt> if it should be printed in
     *         the background
     */
    public boolean getInteractive() {
        return bean.getBoolean("interactive");
    }

    /**
     * Determines if the template should be printed interactively.
     *
     * @param interactive if <tt>true</tt>, the template should be printed interactively; if <tt>false</tt> it should
     *                    be printed in the background
     */
    public void setInteractive(boolean interactive) {
        bean.setValue("interactive", interactive);
    }

    /**
     * Returns the media tray.
     *
     * @return the media tray, or <tt>null</tt> if none is defined
     */
    public MediaTray getMediaTray() {
        String tray = getPaperTray();
        return (tray != null) ? Tray.getTray(tray) : null;
    }

    /**
     * Returns the associated template.
     *
     * @return the template. May be <tt>null</tt>
     */
    public Entity getTemplate() {
        return (Entity) bean.getObject("source");
    }

    /**
     * Returns the template reference.
     *
     * @return the template reference. May be <tt>null</tt>
     */
    public IMObjectReference getTemplateRef() {
        return bean.getReference("source");
    }

    /**
     * Returns the printer location.
     * <p/>
     * This may be an <em>party.organisationLocation</em> or <em>party.organisationPractice</em>
     *
     * @return the practice organisation. May be <tt>null</tt>
     */
    public Entity getLocation() {
        return (Entity) bean.getObject("target");
    }

    /**
     * Returns the printer location reference.
     * <p/>
     * This may be an <em>party.organisationLocation</em> or <em>party.organisationPractice</em>
     *
     * @return the practice organisation reference. May be <tt>null</tt>
     */
    public IMObjectReference getLocationRef() {
        return bean.getReference("target");
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param other the reference object with which to compare
     * @return <tt>true</tt> if other is a <tt>DocumentTemplatePrinter</tt> whose
     *         underlying {@link EntityRelationship} equals this one.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof DocumentTemplatePrinter) {
            DocumentTemplatePrinter p = ((DocumentTemplatePrinter) other);
            return p.bean.getObject().equals(bean.getObject());
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getRelationship().hashCode();
    }

    /**
     * Returns the underlying relationship
     *
     * @return the relationship
     */
    public EntityRelationship getRelationship() {
        return (EntityRelationship) bean.getObject();
    }

    /**
     * Provides a mapping between supported media trays and values defined in @{link MediaTray}.
     */
    private enum Tray {

        TOP(MediaTray.TOP),
        MIDDLE(MediaTray.MIDDLE),
        BOTTOM(MediaTray.BOTTOM),
        ENVELOPE(MediaTray.ENVELOPE),
        MANUAL(MediaTray.MANUAL),
        LARGE_CAPACITY(MediaTray.LARGE_CAPACITY),
        MAIN(MediaTray.MAIN),
        SIDE(MediaTray.SIDE);

        private Tray(MediaTray tray) {
            this.tray = tray;
        }

        public MediaTray getTray() {
            return tray;
        }

        public static MediaTray getTray(String name) {
            for (Tray tray : Tray.values()) {
                if (tray.name().equals(name)) {
                    return tray.getTray();
                }
            }
            throw new DocumentException(DocumentException.ErrorCode.InvalidMediaTray, name);
        }

        private final MediaTray tray;
    }

}
