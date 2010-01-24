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
     * The lookup handler.
     */
    private final LookupHandler lookupHandler;

    /**
     * Determines if already processed rows should be skipped.
     */
    private boolean skipProcessed;


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
             new DefaultObjectHandler(name, mappings, dao, service));
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
        lookupHandler = new LookupHandler(mappings, service);
        mapper = new RowMapper(mappings, handler, lookupHandler, service);
        skipProcessed = mappings.getSkipProcessed();
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
        if (!skipProcessed || !dao.processed(name, row.getRowId())) {
            try {
                objects = mapper.map(row);
                if (lookupHandler != null) {
                    // commit lookups to make the available to the
                    // openvpms:lookup() jxpath extension function
                    lookupHandler.commit();
                }
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
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void close() {
        if (lookupHandler != null) {
            lookupHandler.commit();
            lookupHandler.close();
        }
        handler.end();
        handler.close();
    }

}
