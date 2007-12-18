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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * A factory for {@link Query} instances. The factory is configured to return
 * specific {@link Query} implementations based on the supplied criteria, with
 * {@link DefaultQuery} returned if no implementation matches.
 * <p/>
 * The factory is configured using a <em>QueryFactory.properties</em> file,
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
 * Multiple <em>QueryFactory.properties</em> may be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class QueryFactory {

    /**
     * Query implementations.
     */
    private static ArchetypeHandlers<Query> queries;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(QueryFactory.class);


    /**
     * Prevent construction.
     */
    private QueryFactory() {
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at
     * least one constructor accepting the following arguments, invoked in the
     * order:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param shortName the archetype short name to query on. May contain
     *                  wildcards
     * @param context   the context
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public static <T extends IMObject> Query<T> create(String shortName,
                                                       Context context) {
        return create(new String[]{shortName}, context);
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at
     * least constructor one accepting the following arguments, invoked in the
     * order:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param shortNames the archetype short names to query on. May contain
     *                   wildcards
     * @param context    the current context
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public static <T extends IMObject> Query<T> create(String[] shortNames,
                                                       Context context) {
        shortNames = DescriptorHelper.getShortNames(shortNames);
        ArchetypeHandler<Query> handler = getQueries().getHandler(shortNames);
        if (handler == null) {
            return new DefaultQuery<T>(shortNames);
        }
        return create(handler, shortNames, context);
    }

    /**
     * Attempts to create a new query, using one of the following constructors:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param handler    the {@link Query} implementation
     * @param shortNames the archerype short names to query on
     * @param context    the context
     * @return a new query, or <code>null</code> if no appropriate constructor
     *         can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMObject> Query<T> create(
            ArchetypeHandler<Query> handler, String[] shortNames,
            Context context) {
        Query<T> result = null;
        try {
            try {
                Object[] args = new Object[]{shortNames, context};
                result = (Query<T>) handler.create(args);
            } catch (NoSuchMethodException exception) {
                try {
                    Object[] args = new Object[]{shortNames};
                    result = (Query<T>) handler.create(args);
                } catch (NoSuchMethodException nested) {
                    result = (Query<T>) handler.create();
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
        return result;
    }

    /**
     * Initialise a query.
     *
     * @param query the query to initialise
     */
    public static <T extends IMObject> void initialise(Query<T> query) {
        ArchetypeHandler<Query> handler = getQueries().getHandler(query.getShortNames());
        if (handler != null && handler.getType().isAssignableFrom(
                query.getClass())) {
            try {
                handler.initialise(query);
            } catch (Throwable exception) {
                log.error(exception);
            }
        }
    }

    /**
     * Returns the query implementations.
     *
     * @return the queries
     */
    private static synchronized ArchetypeHandlers<Query> getQueries() {
        if (queries == null) {
            queries = new ArchetypeHandlers<Query>("QueryFactory.properties",
                                                   Query.class);
        }
        return queries;
    }


}
