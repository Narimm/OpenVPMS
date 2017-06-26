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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.im.doc.FileNameFormatter;


/**
 * Generates {@link Document}s from one or more {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class IMObjectReporter<T extends IMObject> extends TemplatedReporter<T> {

    /**
     * The report factory.
     */
    private final ReportFactory factory;

    /**
     * Constructs an {@link IMObjectReporter} for a single object.
     *
     * @param object    the object
     * @param template  the document template to use
     * @param factory   the report factory
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public IMObjectReporter(T object, DocumentTemplate template, ReportFactory factory, FileNameFormatter formatter,
                            IArchetypeService service, ILookupService lookups) {
        super(object, template, formatter, service, lookups);
        this.factory = factory;
    }

    /**
     * Constructs an {@link IMObjectReporter} for a single object.
     *
     * @param object    the object
     * @param locator   the document template locator
     * @param factory   the report factory
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public IMObjectReporter(T object, DocumentTemplateLocator locator, ReportFactory factory,
                            FileNameFormatter formatter, IArchetypeService service, ILookupService lookups) {
        super(object, locator, formatter, service, lookups);
        this.factory = factory;
    }

    /**
     * Constructs an {@link IMObjectReporter} for a collection of objects.
     *
     * @param objects   the objects to print
     * @param template  the document template to use
     * @param factory   the report factory
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public IMObjectReporter(Iterable<T> objects, DocumentTemplate template, ReportFactory factory,
                            FileNameFormatter formatter, IArchetypeService service, ILookupService lookups) {
        super(objects, template, formatter, service, lookups);
        this.factory = factory;
    }

    /**
     * Constructs an {@link IMObjectReporter} for a collection of objects.
     *
     * @param objects   the objects to print
     * @param locator   the document template locator
     * @param factory   the report factory
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public IMObjectReporter(Iterable<T> objects, DocumentTemplateLocator locator, ReportFactory factory,
                            FileNameFormatter formatter, IArchetypeService service, ILookupService lookups) {
        super(objects, locator, formatter, service, lookups);
        this.factory = factory;
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     */
    @SuppressWarnings("unchecked")
    public IMReport<T> getReport() {
        Document doc = getTemplateDocument();
        IMReport<IMObject> report = factory.createIMObjectReport(doc);
        return (IMReport<T>) report;
    }

}
