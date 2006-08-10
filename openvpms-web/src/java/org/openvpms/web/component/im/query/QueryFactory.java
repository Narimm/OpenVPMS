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

package org.openvpms.web.component.im.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * A factory for {@link Query} instances. The factory is configured to return
 * specific {@link Query} implementations based on the supplied criteria, with
 * {@link DefaultQuery} returned if no implementation matches.
 * <p/>
 * The factory is configured using a <em>queryfactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding query implementations. Short names may be wildcarded
 * e.g:
 * <p/>
 * <table> <tr><td>classification.*</td><td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>lookup.*</td><td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>party.patient*</td><td>org.openvpms.web.component.im.query.PatientQuery</td></tr>
 * <tr><td>party.organisation*</td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>party.supplier*</td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * </table>
 * <p/>
 * Multiple <em>queryfactory.properties</em> may be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class QueryFactory {

    /**
     * Query implementations.
     */
    private static ArchetypeHandlers _queries;

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(QueryFactory.class);


    /**
     * Prevent construction.
     */
    private QueryFactory() {
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at
     * least constructor accepting the following arguments, invoked in the
     * order: <ul> <li>(String refModelName, String entityName, String
     * conceptName)</li> <li>(String[] shortNames)</li> <li>default
     * constructor</li> </ul>
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a new query
     */
    public static Query create(String refModelName, String entityName,
                               String conceptName) {
        Query result = null;
        String[] shortNames = DescriptorHelper.getShortNames(
                refModelName, entityName, conceptName);
        ArchetypeHandler handler = getQueries().getHandler(shortNames);
        if (handler != null) {
            try {
                try {
                    String[] args = {refModelName, entityName, conceptName};
                    Class[] types = {String.class, String.class, String.class};
                    result = (Query) handler.create(args, types);
                } catch (NoSuchMethodException exception) {
                    result = create(handler, shortNames);
                }
            } catch (Throwable throwable) {
                _log.error(throwable, throwable);
            }
        }
        if (result == null) {
            result = new DefaultQuery(refModelName, entityName, conceptName);
        }
        return result;
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at
     * least one constructor accepting the following arguments, invoked in the
     * order: <ul> <li>(String[] shortNames)</li> <li>default constructor</li>
     * </ul>
     *
     * @param shortNames the archetype short names to query on. May contain
     *                   wildcards
     * @return a new query
     */
    public static Query create(String[] shortNames) {
        shortNames = DescriptorHelper.getShortNames(shortNames);
        ArchetypeHandler handler = getQueries().getHandler(shortNames);
        if (handler == null) {
            return new DefaultQuery(shortNames);
        }
        return create(handler, shortNames);
    }

    /**
     * Constructs a query implementation, using the <em>(String[]
     * shortNames)</em> constructor; or the default constructor if it doesn't
     * exist.
     *
     * @param handler    the {@link Query} implementation
     * @param shortNames the archerype short names to query on
     * @return a new query implementation
     */
    private static Query create(ArchetypeHandler handler, String[] shortNames) {
        Query result;
        try {
            try {
                Object[] args = new Object[]{shortNames};
                result = (Query) handler.create(args);
            } catch (NoSuchMethodException exception) {
                result = (Query) handler.create();
            }
        } catch (Throwable throwable) {
            _log.error(throwable, throwable);
            result = new DefaultQuery(shortNames);
        }
        return result;
    }

    /**
     * Returns the query implementations.
     *
     * @return the editors
     */
    private static ArchetypeHandlers getQueries() {
        if (_queries == null) {
            _queries = new ArchetypeHandlers("QueryFactory.properties",
                                             Query.class);
        }
        return _queries;
    }


}
