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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * Abstract JRDataSource for {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectDataSource implements JRDataSource {

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
     * Constructs an {@link AbstractIMObjectDataSource}.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AbstractIMObjectDataSource(IArchetypeService service, ILookupService lookups, DocumentHandlers handlers) {
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
    }

    /**
     * Returns a data source for a collection node.
     *
     * @param name the collection node name
     * @return the data source
     * @throws JRException for any error
     */
    public JRDataSource getDataSource(String name) throws JRException {
        return getDataSource(name, new String[0]);
    }

    /**
     * Returns a data source for the given jxpath expression.
     *
     * @param expression the expression. Must return an {@code Iterable} or {@code Iterator} returning {@code IMObjects}
     * @return the data source
     * @throws JRException for any error
     */
    public abstract JRDataSource getExpressionDataSource(String expression) throws JRException;

    /**
     * Returns a data source for a collection node.
     *
     * @param name      the collection node name
     * @param sortNodes the list of nodes to sort on
     * @return the data source
     * @throws JRException for any error
     */
    public abstract JRDataSource getDataSource(String name, String[] sortNodes)
            throws JRException;

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
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
