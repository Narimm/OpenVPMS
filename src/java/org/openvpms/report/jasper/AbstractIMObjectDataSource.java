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
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Abstract JRDataSource for <code>IMObject</code>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectDataSource implements JRDataSource {

    /**
     * The archetype service.
     */
    private final IArchetypeService _service;


    /**
     * Construct a new <code>AbstractIMObjectDataSource</code>.
     */
    public AbstractIMObjectDataSource(IArchetypeService service) {
        _service = service;
    }

    /**
     * Returns a data source for a collection node.
     *
     * @param name the collection node name
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
        return _service;
    }

}
