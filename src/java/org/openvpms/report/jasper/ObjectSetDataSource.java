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
import net.sf.jasperreports.engine.JRField;
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
public class ObjectSetDataSource implements JRDataSource {

    /**
     * The current object.
     */
    private ObjectSetExpressionEvaluator current;

    /**
     * The iterator.
     */
    private final Iterator<ObjectSet> iterator;

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
     * Constructs a {@link ObjectSetDataSource}.
     *
     * @param iterator the iterator
     * @param fields   additional report fields. These override any in the report. May be {@code null}
     * @param service  the archetype service
     * @param lookups  the lookup service
     */
    public ObjectSetDataSource(Iterator<ObjectSet> iterator, PropertySet fields, IArchetypeService service,
                               ILookupService lookups) {
        this.iterator = iterator;
        this.fields = fields;
        this.service = service;
        this.lookups = lookups;
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
                current = new ObjectSetExpressionEvaluator(iterator.next(), fields, service, lookups);
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
}
