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

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.util.Collections;
import java.util.List;


/**
 * Loads {@link IMObject}s from {@link ETLRow}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Loader {

    /**
     * The loader name.
     */
    private final String name;

    /**
     * The DAO.
     */
    private final ETLLogDAO dao;

    /**
     * The object handler.
     */
    private final ObjectHandler handler;

    /**
     * The row mapper.
     */
    private final RowMapper mapper;


    /**
     * Constructs a new <tt>Loader</tt>.
     *
     * @param name     the loader name
     * @param mappings the row/object mappings
     * @param dao      the DAO
     * @param service  the archetype service
     */
    public Loader(String name, Mappings mappings, ETLLogDAO dao,
                  IArchetypeService service) {
        this(name, mappings, dao, service,
             new DefaultObjectHandler(name, dao, service));
    }

    /**
     * Constructs a new <tt>Loader</tt>.
     *
     * @param name     the loader name
     * @param mappings the row/object mappings
     * @param dao      the DAO
     * @param service  the archetype service
     * @param handler  the object handler
     */
    protected Loader(String name, Mappings mappings, ETLLogDAO dao,
                     IArchetypeService service, ObjectHandler handler) {
        this.name = name;
        this.dao = dao;
        this.handler = handler;
        mapper = new RowMapper(mappings, this.handler, service);
    }

    /**
     * Loads a row.
     * If the row has already been successfully processed, no objects will
     * be returned.
     *
     * @param row the row to load
     * @return the objects generated from the row, or an empty collection if
     *         the row has already been successfully processed
     */
    public List<IMObject> load(ETLRow row) {
        List<IMObject> objects = Collections.emptyList();
        if (!processed(row)) {
            try {
                objects = mapper.map(row);
                handler.commit();
            } catch (OpenVPMSException exception) {
                handler.rollback();
                handler.error(row.getRowId(), exception);
            }
        }
        return objects;
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setErrorListener(ErrorListener listener) {
        handler.setErrorListener(listener);
    }

    /**
     * Closes the loader.
     *
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    public void close() {
        handler.end();
        handler.close();
    }

    /**
     * Determines if a row has already been successfully processed.
     *
     * @param row the row
     * @return <tt>true</tt> if the row has been successfully processed,
     *         otherwise <tt>false</tt>
     */
    private boolean processed(ETLRow row) {
        boolean result = true;
        List<ETLLog> logs = dao.get(name, row.getRowId(), null);
        if (logs.isEmpty()) {
            result = false;
        } else {
            for (ETLLog log : logs) {
                if (log.getErrors() != null) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

}
