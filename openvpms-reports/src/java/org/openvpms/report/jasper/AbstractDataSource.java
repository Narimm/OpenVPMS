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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import org.apache.commons.jxpath.Functions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for JasperReports data sources.
 *
 * @author Tim Anderson
 */
public abstract class AbstractDataSource implements JRDataSource {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The JXPath extension functions.
     */
    private final Functions functions;

    /**
     * Constructs an {@link AbstractDataSource}.
     *
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param functions the JXPath extension functions
     */
    public AbstractDataSource(IArchetypeService service, ILookupService lookups, Functions functions) {
        this.functions = functions;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Evaluates an xpath expression.
     *
     * @param expression the expression
     * @return the result of the expression. May be {@code null}
     */
    public abstract Object evaluate(String expression);

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
     * Helper to prefix parameter names with P. to distinguish them from other variables.
     *
     * @param parameters the parameters
     * @return the prefixed parameters
     */
    protected Map<String, Object> getParameters(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            result.put("P." + entry.getKey(), entry.getValue());
        }
        return result;
    }

}
