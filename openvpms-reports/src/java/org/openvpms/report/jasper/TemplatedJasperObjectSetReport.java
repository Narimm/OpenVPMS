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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.Parameters;

import java.util.Map;


/**
 * A {@link JasperIMReport} that uses pre-defined templates, operating on collections of {@link ObjectSet}s.
 *
 * @author Tim Anderson
 */
public class TemplatedJasperObjectSetReport extends AbstractTemplatedJasperIMReport<ObjectSet> {

    /**
     * Constructs an {@link AbstractTemplatedJasperIMReport}.
     *
     * @param template  the document template
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param handlers  the document handlers  @throws ReportException if the report cannot be created
     * @param functions the JXPath extension functions
     */
    public TemplatedJasperObjectSetReport(Document template, IArchetypeService service, ILookupService lookups,
                                          DocumentHandlers handlers, Functions functions) {
        super(template, service, lookups, handlers, functions);
    }

    /**
     * Creates a data source for a collection of objects.
     *
     * @param objects    an iterator over the collection of objects
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @return a new data source
     */
    @Override
    protected JRRewindableDataSource createDataSource(Iterable<ObjectSet> objects, Map<String, Object> parameters,
                                                      Map<String, Object> fields) {
        ILookupService lookups = getLookupService();
        IArchetypeService service = getArchetypeService();
        PropertySet f = (fields != null) ? new ResolvingPropertySet(fields, service, lookups) : null;
        Parameters p = (parameters) != null ? new Parameters(parameters) : null;
        return new ObjectSetDataSource(objects, p, f, service, lookups, getDocumentHandlers(), getFunctions());
    }

}
