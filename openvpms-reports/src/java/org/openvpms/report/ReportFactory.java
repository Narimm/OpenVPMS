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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report;

import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.jasper.TemplatedJasperObjectSetReport;
import org.openvpms.report.msword.MsWordIMReport;
import org.openvpms.report.openoffice.OpenOfficeIMReport;

import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;
import static org.openvpms.report.ReportException.ErrorCode.UnsupportedTemplate;


/**
 * A factory for {@link Report} instances.
 *
 * @author Tim Anderson
 */
public class ReportFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The JXPath extension function factory.
     */
    private final ArchetypeFunctionsFactory factory;

    /**
     * Constructs an {@link ReportFactory}.
     *
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param handlers the document handlers
     * @param factory  the factory for JXPath extension functions
     */
    public ReportFactory(IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                         ArchetypeFunctionsFactory factory) {
        this.service = new ReadOnlyArchetypeService(service);
        this.lookups = lookups;
        this.handlers = handlers;
        this.factory = factory;
    }

    /**
     * Creates a new report.
     *
     * @param template the document template
     * @return a new report
     * @throws ReportException for any report error
     */
    public Report createReport(Document template) {
        String name = template.getName();
        Report report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            IArchetypeService serviceProxy = proxy(service);
            Functions functions = factory.create(serviceProxy);
            report = new TemplatedJasperIMObjectReport(template, serviceProxy, lookups, handlers, functions);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
        }
        return report;
    }

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param template the document template
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<IMObject> createIMObjectReport(Document template) {
        String name = template.getName();
        IMReport<IMObject> report;
        IArchetypeService serviceProxy = proxy(service);
        Functions functions = factory.create(serviceProxy);
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperIMObjectReport(template, serviceProxy, lookups, handlers, functions);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMReport<IMObject>(template, serviceProxy, lookups, handlers, functions);
        } else if (name.endsWith(DocFormats.DOC_EXT)) {
            report = new MsWordIMReport<IMObject>(template, serviceProxy, lookups, handlers, functions);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
        }
        return report;
    }

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param shortName the archetype short name
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<IMObject> createIMObjectReport(String shortName) {
        TemplateHelper helper = new TemplateHelper(service);
        Document doc = helper.getDocumentForArchetype(shortName);
        if (doc == null) {
            String displayName = DescriptorHelper.getDisplayName(shortName, service);
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        return createIMObjectReport(doc);
    }

    /**
     * Creates a new report for a collection of {@link ObjectSet}s.
     *
     * @param template the document template
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<ObjectSet> createObjectSetReport(Document template) {
        String name = template.getName();
        IMReport<ObjectSet> report;
        IArchetypeService serviceProxy = proxy(service);
        Functions functions = factory.create(serviceProxy);
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperObjectSetReport(template, serviceProxy, lookups, handlers, functions);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMReport<ObjectSet>(template, serviceProxy, lookups, handlers, functions);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
        }
        return report;
    }

    /**
     * Creates a proxy for the archetype service.
     *
     * @param service service
     * @return the proxied service
     */
    protected IArchetypeService proxy(IArchetypeService service) {
        return service;
    }

}
