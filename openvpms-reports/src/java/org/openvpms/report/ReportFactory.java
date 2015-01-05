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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.DelegatingArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.jasper.TemplatedJasperObjectSetReport;
import org.openvpms.report.msword.MsWordIMReport;
import org.openvpms.report.openoffice.OpenOfficeIMReport;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * Proxies the archetype service.
     *
     * @param service service
     * @return the proxied service
     */
    protected IArchetypeService proxy(IArchetypeService service) {
        return service;
    }

    protected static class ReadOnlyArchetypeService extends DelegatingArchetypeService {

        public ReadOnlyArchetypeService(IArchetypeService service) {
            super(service);
        }

        /**
         * Create a domain object given a short name. The short name is a reference to an {@link ArchetypeDescriptor}.
         *
         * @param shortName the short name
         * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
         * @throws UnsupportedOperationException
         */
        @Override
        public IMObject create(String shortName) {
            throw new UnsupportedOperationException();
        }

        /**
         * Create a domain object given an {@link ArchetypeId}.
         *
         * @param id the archetype id
         * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
         * @throws UnsupportedOperationException
         */
        @Override
        public IMObject create(ArchetypeId id) {
            throw new UnsupportedOperationException();
        }

        /**
         * Validate the specified {@link IMObject}. To validate the object it will retrieve the archetype and iterate
         * through the assertions.
         *
         * @param object the object to validate
         * @throws UnsupportedOperationException
         */
        @Override
        public void validateObject(IMObject object) {
            throw new UnsupportedOperationException();
        }

        /**
         * Derived values for the specified {@link IMObject}, based on its corresponding {@link ArchetypeDescriptor}.
         *
         * @param object the object to derived values for
         * @throws UnsupportedOperationException
         */
        @Override
        public void deriveValues(IMObject object) {
            throw new UnsupportedOperationException();
        }

        /**
         * Derive the value for the {@link NodeDescriptor} with the specified name.
         *
         * @param object the object to operate on.
         * @param node   the name of the {@link NodeDescriptor}, which will be used to derive the value
         * @throws UnsupportedOperationException
         */
        @Override
        public void deriveValue(IMObject object, String node) {
            throw new UnsupportedOperationException();
        }

        /**
         * Remove the specified object.
         *
         * @param object the object to remove
         * @throws UnsupportedOperationException
         */
        @Override
        public void remove(IMObject object) {
            throw new UnsupportedOperationException();
        }

        /**
         * Saves an object, executing any <em>save</em> rules associated with its archetype.
         *
         * @param object the object to save
         * @throws UnsupportedOperationException
         */
        @Override
        public void save(IMObject object) {
            throw new UnsupportedOperationException();
        }

        /**
         * Saves an object, executing any <em>save</em> rules associated with its archetype.
         *
         * @param object   the object to save
         * @param validate if {@code true} validate the object prior to saving it
         * @throws UnsupportedOperationException
         */
        @Override
        public void save(IMObject object, boolean validate) {
            throw new UnsupportedOperationException();
        }

        /**
         * Save a collection of {@link IMObject} instances. executing any  <em>save</em> rules associated with their
         * archetypes.
         * <p/>
         * Rules will be executed in the order that the objects are supplied.
         *
         * @param objects the objects to save
         * @throws UnsupportedOperationException
         */
        @Override
        public void save(Collection<? extends IMObject> objects) {
            throw new UnsupportedOperationException();
        }

        /**
         * Save a collection of {@link IMObject} instances.
         *
         * @param objects the objects to save
         * @throws UnsupportedOperationException
         */
        @Override
        public void save(Collection<? extends IMObject> objects, boolean validate) {
            throw new UnsupportedOperationException();
        }

        /**
         * Execute the rule specified by the uri and using the passed in properties and facts.
         *
         * @param ruleUri the rule uri
         * @param props   a set of properties that can be used by the rule engine
         * @param facts   a list of facts that are asserted in to the working memory
         * @return a list objects. May be an empty list.
         * @throws UnsupportedOperationException
         */
        @Override
        public List<Object> executeRule(String ruleUri, Map<String, Object> props, List<Object> facts) {
            throw new UnsupportedOperationException();
        }

        /**
         * Adds a listener to receive notification of changes.
         * <p/>
         * In a transaction, notifications occur on successful commit.
         *
         * @param shortName the archetype short to receive events for. May contain wildcards.
         * @param listener  the listener to add
         * @throws UnsupportedOperationException
         */
        @Override
        public void addListener(String shortName, IArchetypeServiceListener listener) {
            throw new UnsupportedOperationException();
        }

        /**
         * Removes a listener.
         *
         * @param shortName the archetype short to remove the listener for. May contain wildcards.
         * @param listener  the listener to remove
         * @throws UnsupportedOperationException
         */
        @Override
        public void removeListener(String shortName, IArchetypeServiceListener listener) {
            throw new UnsupportedOperationException();
        }
    }

}
