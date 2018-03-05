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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.print;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.IMReport;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;


/**
 * Prints reports for {@link IMObject}s generated by {@link IMReport}.
 *
 * @author Tim Anderson
 */
public class IMObjectReportPrinter<T extends IMObject> extends TemplatedIMPrinter<T> {

    /**
     * Constructs an {@link IMObjectReportPrinter}.
     *
     * @param object  the object to print
     * @param locator the document template locator
     * @param context the context
     * @param factory the reporter factory
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(T object, DocumentTemplateLocator locator, Context context, ReporterFactory factory) {
        super(factory.<T, TemplatedReporter<T>>create(object, locator, TemplatedReporter.class), context,
              factory.getService());
    }

    /**
     * Constructs an {@link IMObjectReportPrinter}.
     *
     * @param object   the object to print
     * @param template the document template to use
     * @param context  the context
     * @param factory  the reporter factory
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(T object, DocumentTemplate template, Context context, ReporterFactory factory) {
        super(factory.<T, TemplatedReporter<T>>create(object, template, TemplatedReporter.class), context,
              factory.getService());
    }

    /**
     * Constructs an {@link IMObjectReportPrinter} to print a collection of objects.
     *
     * @param objects  the objects to print
     * @param template the document template to use
     * @param context  the context
     * @param factory  the reporter factory
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(Iterable<T> objects, DocumentTemplate template, Context context,
                                 ReporterFactory factory) {
        super(factory.<T, TemplatedReporter<T>>create(objects, template, TemplatedReporter.class), context,
              factory.getService());
    }

    /**
     * Constructs an {@link IMObjectReportPrinter} to print a collection of objects.
     *
     * @param objects the objects to print
     * @param locator the document template locator
     * @param context the context
     * @param factory the reporter factory
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportPrinter(Iterable<T> objects, DocumentTemplateLocator locator, Context context,
                                 ReporterFactory factory) {
        super(factory.<T, TemplatedReporter<T>>create(objects, locator, TemplatedReporter.class), context,
              factory.getService());
    }

}
