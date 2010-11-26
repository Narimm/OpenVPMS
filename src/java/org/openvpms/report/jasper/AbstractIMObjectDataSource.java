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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Abstract JRDataSource for <tt>IMObject</tt>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectDataSource implements JRDataSource {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Construct a new <tt>AbstractIMObjectDataSource</tt>.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AbstractIMObjectDataSource(IArchetypeService service,
                                      DocumentHandlers handlers) {
        this.service = service;
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
     * Returns the document handlers.
     *
     * @return the document handlers
     */
    protected DocumentHandlers getDocumentHandlers() {
        return handlers;
    }

}
