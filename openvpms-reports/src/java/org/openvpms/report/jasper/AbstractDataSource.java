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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.Parameters;

import java.util.Collections;

/**
 * Base class for JasperReports data sources.
 *
 * @author Tim Anderson
 */
public abstract class AbstractDataSource implements DataSource {

    /**
     * Report parameters. May be {@code null}
     */
    private final Parameters parameters;

    /**
     * Additional fields. May be {@code null}
     */
    private final PropertySet fields;

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
     * The JXPath extension functions.
     */
    private final Functions functions;

    /**
     * Constructs an {@link AbstractDataSource}.
     *
     * @param parameters the report parameters. May be {@code null}
     * @param fields     additional report fields. These override any in the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param handlers   the document handlers
     * @param functions  the JXPath extension functions
     */
    public AbstractDataSource(Parameters parameters, PropertySet fields, IArchetypeService service,
                              ILookupService lookups, DocumentHandlers handlers, Functions functions) {
        this.parameters = parameters;
        this.fields = fields;
        this.functions = functions;
        this.service = service;
        this.handlers = handlers;
        this.lookups = lookups;
    }

    /**
     * Returns a data source for a collection node.
     *
     * @param name the collection node name
     * @return the data source
     * @throws JRException for any error
     */
    public JRRewindableDataSource getDataSource(String name) throws JRException {
        return getDataSource(name, new String[0]);
    }

    /**
     * Returns a data source for the given jxpath expression.
     *
     * @param object     the object
     * @param expression the expression. Must return a single {@link IMObject}, or an {@code Iterable} returning
     *                   {@link IMObject}s
     * @return the data source
     * @throws JRException for any error
     */
    protected JRRewindableDataSource getExpressionDataSource(Object object, String expression) throws JRException {
        Object value = evaluate(object, expression);
        return getCollectionDataSource(value, expression);
    }

    /**
     * Returns a collection data source for the supplied value.
     *
     * @param value      the value. Must be a single {@link IMObject} or an {@link Iterable} returning
     *                   {@link IMObject}s.
     * @param expression the expression that generated the value
     * @return the data source
     * @throws JRException if the value is not supported
     */
    @SuppressWarnings("unchecked")
    protected JRRewindableDataSource getCollectionDataSource(Object value, String expression) throws JRException {
        Iterable<IMObject> iterable;
        if (value instanceof Iterable) {
            iterable = (Iterable<IMObject>) value;
        } else if (value instanceof IMObject) {
            iterable = Collections.singletonList((IMObject) value);
        } else {
            throw new JRException("Unsupported value type=" + ((value != null) ? value.getClass() : null)
                                  + " returned by expression=" + expression);
        }
        return new IMObjectCollectionDataSource(iterable, parameters, fields, service, lookups, handlers, functions);
    }

    /**
     * Returns the parameters.
     *
     * @return the parameters. May be {@code null}
     */
    protected Parameters getParameters() {
        return parameters;
    }

    /**
     * Returns the fields.
     *
     * @return the fields. May be {@code null}
     */
    protected PropertySet getFields() {
        return fields;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the JXPath extension functions.
     *
     * @return the JXPath extension functions
     */
    protected Functions getFunctions() {
        return functions;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookupService() {
        return lookups;
    }

    /**
     * Returns the document handlers.
     *
     * @return the document handlers
     */
    protected DocumentHandlers getDocumentHandlers() {
        return handlers;
    }

}
