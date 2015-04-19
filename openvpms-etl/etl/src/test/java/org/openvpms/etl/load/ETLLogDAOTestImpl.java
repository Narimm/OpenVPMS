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

import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLLogDAOTestImpl implements ETLLogDAO {

    /**
     * The logs, keyed on log id.
     */
    private final Map<Long, ETLLog> logs = new HashMap<Long, ETLLog>();

    /**
     * Log identifier seed.
     */
    private long seed;

    /**
     * Saves a log.
     *
     * @param log the log to save
     */
    public void save(ETLLog log) {
        if (log.getLogId() == 0) {
            log.setLogId(++seed);
        }
        logs.put(log.getLogId(), log);
    }

    /**
     * Saves a collection of logs.
     *
     * @param logs the logs to save
     */
    public void save(Iterable<ETLLog> logs) {
        for (ETLLog log : logs) {
            save(log);
        }
    }

    /**
     * Returns an {@link ETLLog} given its identifier.
     *
     * @param logId the log identifier
     * @return the corresponding log, or <tt>null</tt> if none is found
     */
    public ETLLog get(long logId) {
        return logs.get(logId);
    }

    /**
     * Returns all {@link ETLLog}s associated with a loader, legacy row
     * identifier and archetype.
     *
     * @param loader    the loader name. May be <tt>null</tt> to indicate all
     *                  loaders
     * @param rowId     the legacy row identifier
     * @param archetype the archetype short name. May be <tt>null</tt> to
     *                  indicate all objects with the same legacy identifier.
     *                  May contain '*' wildcards.
     * @return all logs matching the criteria
     */
    public List<ETLLog> get(String loader, String rowId, String archetype) {
        List<ETLLog> result = new ArrayList<ETLLog>();
        for (ETLLog log : logs.values()) {
            if (loader != null && !log.getLoader().equals(loader)) {
                continue;
            }
            if (archetype != null
                    && !TypeHelper.matches(log.getArchetype(), archetype)) {
                continue;
            }
            if (log.getRowId().equals(rowId)) {
                result.add(log);
            }
        }
        return result;
    }

    /**
     * Determines if a legacy row has been successfully processed.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     * @return <tt>true> if the row has been sucessfully processed
     */
    public boolean processed(String loader, String rowId) {
        List<ETLLog> logs = get(loader, rowId, null);
        if (logs.isEmpty()) {
            return false;
        }
        for (ETLLog log : logs) {
            if (log.getErrors() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deletes all {@link ETLLog}s associated with a loader and legacy row
     * identifier.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     */
    public void remove(String loader, String rowId) {
        Iterator<ETLLog> iterator = logs.values().iterator();
        while (iterator.hasNext()) {
            ETLLog log = iterator.next();
            if (log.getRowId().equals(rowId)
                    && log.getLoader().equals(loader)) {
                iterator.remove();
            }
        }
    }
}
