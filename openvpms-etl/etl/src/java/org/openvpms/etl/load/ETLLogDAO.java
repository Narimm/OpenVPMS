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

import java.util.List;


/**
 * Data access object for {@link ETLLog} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ETLLogDAO {

    /**
     * Saves a log.
     *
     * @param log the log to save
     */
    void save(ETLLog log);

    /**
     * Saves a collection of logs.
     *
     * @param logs the logs to save
     */
    void save(Iterable<ETLLog> logs);

    /**
     * Returns an {@link ETLLog} given its identifier.
     *
     * @param logId the log identifier
     * @return the corresponding log, or <tt>null</tt> if none is found
     */
    ETLLog get(long logId);

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
    List<ETLLog> get(String loader, String rowId, String archetype);

    /**
     * Determines if a legacy row has been successfully processed.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     * @return <tt>true> if the row has been sucessfully processed
     */
    boolean processed(String loader, String rowId);

    /**
     * Deletes all {@link ETLLog}s associated with a loader and legacy row
     * identifier.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     */
    void remove(String loader, String rowId);

}
