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

import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.Parameters;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the {@link IMObjectCollectionDataSource} class.
 *
 * @author Tim Anderson
 */
public class IMObjectCollectionDataSourceTestCase extends AbstractDataSourceTest<IMObject> {

    /**
     * Creates a new data source.
     *
     * @param objects    the objects
     * @param parameters the parameters
     * @param fields     the fields
     * @param handlers   the document handlers
     * @param functions  the functions
     * @return a new data source
     */
    @Override
    protected DataSource createDataSource(List<IMObject> objects, Parameters parameters, PropertySet fields,
                                          DocumentHandlers handlers, Functions functions) {
        return new IMObjectCollectionDataSource(objects, parameters, fields, getArchetypeService(),
                                                getLookupService(), handlers, functions);
    }

    /**
     * Creates a collection of customers to pass to the data source.
     *
     * @param customers the customers
     * @return a collection to pass to the data source
     */
    @Override
    protected List<IMObject> createCollection(Party... customers) {
        return Arrays.<IMObject>asList(customers);
    }

}
