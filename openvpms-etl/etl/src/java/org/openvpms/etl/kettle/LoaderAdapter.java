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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.sql.Timestamp;
import java.util.List;


/**
 * Wrapper around a {@link Loader}, that converts Kettle <tt>RowMetaInterface</tt> instances
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
     * @param metaData the row meta data
     * @param row      the row to load
     * @return the objects generated from the row, or an empty collection if
     *         the row has already been successfully processed
     * @throws KettleException for any error
     */
    public List<IMObject> load(RowMetaInterface metaData, Object[] row) throws KettleException {
        int idIndex = metaData.indexOfValue(mappings.getIdColumn());
        if (idIndex == -1) {
            String msg = Messages.get("LoaderAdapter.MissingColumn", mappings.getIdColumn());
            throw new KettleException(msg);
        }
        ValueMetaInterface valueMeta = metaData.getValueMeta(idIndex);
        String id = getLegacyId(valueMeta, row[idIndex]);
        if (StringUtils.isEmpty(id)) {
            String msg = Messages.get("LoaderAdapter.NullIdColumn", mappings.getIdColumn());
            throw new KettleException(msg);
        }
        ETLRow mapRow = new ETLRow(id);
        for (Mapping mapping : mappings.getMapping()) {
            int index = metaData.indexOfValue(mapping.getSource());
            if (index == -1) {
                String msg = Messages.get("LoaderAdapter.MissingColumn", mapping.getSource());
                throw new KettleException(msg);
            }
        }
        for (int i = 0; i < row.length; ++i) {
            valueMeta = metaData.getValueMeta(i);
            Object value = getValue(valueMeta, row[i]);
            mapRow.add(valueMeta.getName(), value);
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
     * Converts a kettle value to an object.
     *
     * @param valueMeta the value meta data
     * @param value     the value to convert
     * @return the converted value
     * @throws KettleException if the value cannot be converted
     */
    private Object getValue(ValueMetaInterface valueMeta, Object value) throws KettleException {
        Object result;
        int type = valueMeta.getType();
        if (type == ValueMetaInterface.TYPE_DATE) {
            result = valueMeta.getDate(value);
        } else if (type == ValueMetaInterface.TYPE_NUMBER) {
            if (valueMeta.getPrecision() == 0) {
                result = valueMeta.getBigNumber(value).toBigIntegerExact();
            } else {
                result = valueMeta.getBigNumber(value);
            }
        } else if (type == ValueMetaInterface.TYPE_INTEGER) {
            result = valueMeta.getInteger(value);
        } else if (type == ValueMetaInterface.TYPE_BIGNUMBER) {
            result = valueMeta.getBigNumber(value);
        } else {
            // handle everything else as a string
            String str = valueMeta.getString(value);
            result = StringUtils.trimToNull(str);
        }
        return result;
    }

    /**
     * Helper to get a stringified form of the legacy identifier.
     * This ensures that any date identifiers are formatted using
     * <em>java.sql.Timestamp.toString()</em> to avoid localisation issues.
     *
     * @param valueMeta the value meta data
     * @param id        the legacy id
     * @return the stringified form of the legacy identifier
     * @throws KettleException if the id cannot be coverted
     */
    private String getLegacyId(ValueMetaInterface valueMeta, Object id) throws KettleException {
        String result = null;
        if (!valueMeta.isNull(id)) {
            if (valueMeta.getType() == ValueMetaInterface.TYPE_DATE) {
                Timestamp datetime = new Timestamp(valueMeta.getDate(id).getTime());
                result = datetime.toString();
            } else {
                Object value = getValue(valueMeta, id);
                if (value != null) {
                    result = value.toString();
                }
            }
        }
        return result;
    }

}