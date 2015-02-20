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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.apache.commons.jxpath.Functions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.ObjectSetExpressionEvaluator;

import java.util.Iterator;


/**
 * A {@code JRDataSource} for {@link ObjectSet}s.
 *
 * @author Tim Anderson
 */
class ObjectSetDataSource implements JRRewindableDataSource {

    /**
     * The current object.
     */
    private ObjectSetExpressionEvaluator current;

    /**
     * The collection.
     */
    private final Iterable<ObjectSet> collection;

    /**
     * The collection iterator.
     */
    private Iterator<ObjectSet> iterator;

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
     * The JXPath extension functions.
     */
    private final Functions functions;


    /**
     * Constructs a {@link ObjectSetDataSource}.
     *
     * @param collection the iterator
     * @param fields     additional report fields. These override any in the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param functions  the JXPath extension functions
     */
    public ObjectSetDataSource(Iterable<ObjectSet> collection, PropertySet fields, IArchetypeService service,
                               ILookupService lookups, Functions functions) {
        this.collection = collection;
        this.iterator = collection.iterator();
        this.fields = fields;
        this.service = service;
        this.lookups = lookups;
        this.functions = functions;
    }

    /**
     * Tries to position the cursor on the next element in the data source.
     *
     * @return true if there is a next record, false otherwise
     * @throws JRException if any error occurs while trying to move to the next element
     */
    public boolean next() throws JRException {
        try {
            if (iterator.hasNext()) {
                current = new ObjectSetExpressionEvaluator(iterator.next(), fields, service, lookups, functions);
                return true;
            }
            return false;
        } catch (Throwable exception) {
            throw new JRException(exception);
        }
    }

    /**
     * Gets the field value for the current position.
     *
     * @return an object containing the field value. The object type must be the field object type.
     */
    public Object getFieldValue(JRField field) throws JRException {
        return current.getValue(field.getName());
    }

    /**
     * Moves back to the first element in the data source.
     */
    @Override
    public void moveFirst() {
        iterator = collection.iterator();
    }
}
