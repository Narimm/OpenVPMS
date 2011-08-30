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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.PrintProperties;
import org.openvpms.web.component.im.report.TemplatedReporter;


/**
 * Base class for printers that print reports using a template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class TemplatedIMPrinter<T> extends AbstractIMPrinter<T> {

    /**
     * Creates a new <tt>TemplatedIMReportPrinter</tt>.
     *
     * @param reporter the reporter
     * @throws ArchetypeServiceException for any archetype service error
     */
    public TemplatedIMPrinter(TemplatedReporter<T> reporter) {
        super(reporter);
        Entity template = getTemplate();
        if (template != null) {
            setInteractive(getInteractive(template, getDefaultPrinter()));
        }
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        return DescriptorHelper.getDisplayName(getReporter().getShortName());
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or <tt>null</tt> if there is none defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        Entity template = getTemplate();
        return (template != null) ? getDefaultPrinter(template) : null;
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
        return getProperties(printer, getTemplate());
    }

    /**
     * Returns the reporter.
     *
     * @return the reporter
     */
    @Override
    protected TemplatedReporter<T> getReporter() {
        return (TemplatedReporter<T>) super.getReporter();
    }

    /**
     * Returns the document template entity.
     *
     * @return the document template, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Entity getTemplate() {
        return getReporter().getTemplate();
    }
}
