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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.etl.load.ETLLogDAO;
import org.openvpms.etl.load.ETLRow;
import org.openvpms.etl.load.ErrorListener;
import org.openvpms.etl.load.Loader;
import org.openvpms.etl.load.LoaderException;
import org.openvpms.etl.load.Mapping;
import org.openvpms.etl.load.Mappings;

import java.sql.Timestamp;
import java.util.List;


/**
 * Wrapper around a {@link Loader}, that converts Kettle <tt>Row</tt> instances
 * to {@link ETLRow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderAdapter {

    /**
     * The mappings.
     */
    private final Mappings mappings;

    /**
     * The loader.
     */
    private final Loader loader;


    /**
     * Constructs a new <tt>LoaderAdapter</tt>.
     *
     * @param name     the loader name
     * @param mappings the mappings
     * @param dao      the DAO
     * @param service  the archetype service
     */
    public LoaderAdapter(String name, Mappings mappings, ETLLogDAO dao,
                         IArchetypeService service) {
        this.mappings = mappings;
        loader = new Loader(name, mappings, dao, service);
    }

    /**
     * Loads a row.
     * If the row has already been successfully processed, no objects will
     * be returned.
     *
     * @param row the row to load
     * @return the objects generated from the row, or an empty collection if
     *         the row has already been successfully processed
     * @throws KettleException for any error
     */
    public List<IMObject> load(Row row) throws KettleException {
        Value idValue = row.searchValue(mappings.getIdColumn());
        if (idValue == null) {
            String msg = Messages.get("LoaderAdapter.MissingColumn",
                                      mappings.getIdColumn());
            throw new KettleException(msg);
        }
        String id = getLegacyId(idValue);
        if (StringUtils.isEmpty(id)) {
            String msg = Messages.get("LoaderAdapter.NullIdColumn",
                                      mappings.getIdColumn());
            throw new KettleException(msg);
        }
        ETLRow mapRow = new ETLRow(id);
        for (Mapping mapping : mappings.getMapping()) {
            Value value = row.searchValue(mapping.getSource());
            if (value == null) {
                String msg = Messages.get("LoaderAdapter.MissingColumn",
                                          mapping.getSource());
                throw new KettleException(msg);
            }
        }
        for (int i = 0; i < row.size(); ++i) {
            Value value = row.getValue(i);
            mapRow.add(value.getName(), getValue(value));
        }
        return loader.load(mapRow);
    }

    /**
     * Sets the error listener.
     *
     * @param listener the listener
     */
    public void setErrorListener(ErrorListener listener) {
        loader.setErrorListener(listener);
    }

    /**
     * Closes the loader.
     *
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    public void close() {
        loader.close();
    }

    /**
     * Converts a kettle <tt>Value</tt> to an object.
     *
     * @param value the value to convert
     * @return the converted value
     */
    private Object getValue(Value value) {
        Object result = null;
        if (value.getType() == Value.VALUE_TYPE_STRING) {
            // need to trim whitespace
            if (!value.isNull()) {
                String str = value.toString(false);
                result = StringUtils.trimToNull(str);
            }
        } else if (!value.isNull()) {
            if (value.getType() == Value.VALUE_TYPE_DATE) {
                result = value.getDate();
            } else if (value.getType() == Value.VALUE_TYPE_NUMBER) {
                if (value.getPrecision() == 0) {
                    result = value.getBigNumber().toBigIntegerExact();
                } else {
                    result = value.getBigNumber();
                }
            } else if (value.getType() == Value.VALUE_TYPE_INTEGER) {
                result = value.getInteger();
            } else if (value.getType() == Value.VALUE_TYPE_BIGNUMBER) {
                result = value.getBigNumber();
            } else {
                result = value.toString(false); // no padding
            }
        }
        return result;
    }

    /**
     * Helper to get a stringified form of the legacy identifier.
     * This ensures that any date identifiers are formatted using
     * <em>java.sql.Timestamp.toString()</em> to avoid localisation issues.
     *
     * @param id the legacy id
     * @return the stringified form of the legacy identifier
     */
    private String getLegacyId(Value id) {
        String result = null;
        if (!id.isNull()) {
            if (id.getType() == Value.VALUE_TYPE_DATE) {
                Timestamp datetime = new Timestamp(id.getDate().getTime());
                result = datetime.toString();
            } else {
                Object value = getValue(id);
                if (value != null) {
                    result = value.toString();
                }
            }
        }
        return result;
    }

}