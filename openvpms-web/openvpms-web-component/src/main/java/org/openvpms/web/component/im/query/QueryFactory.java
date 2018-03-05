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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;


/**
 * A factory for {@link Query} instances. The factory is configured to return
 * specific {@link Query} implementations based on the supplied criteria, with
 * {@link DefaultQuery} returned if no implementation matches.
 * <p>
 * The factory is configured using a <em>QueryFactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding query implementations. Short names may be wildcarded
 * e.g:
 * <p>
 * <table> <tr><td>classification.*</td><td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>lookup.*</td><td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>party.patient*</td><td>org.openvpms.web.component.im.query.PatientQuery</td></tr>
 * <tr><td>party.organisation*</td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>party.supplier*</td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * </table>
 * <p>
 * Multiple <em>QueryFactory.properties</em> may be used.
 * <p>
 * Default implementations can be registered in a <em>DefaultQueryFactory.properties</em> file; these are overridden by
 * <em>QueryFactory.properties</em>.
 *
 * @author Tim Anderson
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
        return create(new String[]{shortName}, context, IMObject.class);
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
     * @param shortName the archetype short name to query on. May contain wildcards
     * @param context   the context
     * @param type      the type that the query returns
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public static <T> Query<T> create(String shortName, Context context, Class type) {
        return create(new String[]{shortName}, context, type);
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at least constructor one accepting the
     * following arguments, invoked in the
     * order:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param shortNames the archetype short names to query on. May contain wildcards
     * @param context    the current context
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public static <T extends IMObject> Query<T> create(String[] shortNames, Context context) {
        return create(shortNames, context, IMObject.class);
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at least constructor one accepting the
     * following arguments, invoked in the order:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param shortNames the archetype short names to query on. May contain wildcards
     * @param context    the current context
     * @param type       the type that the query returns
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     * @throws QueryException          if no query supports the supplied short names and type
     */
    public static <T> Query<T> create(String[] shortNames, Context context, Class type) {
        return create(shortNames, true, context, type);
    }


    /**
     * Construct a new {@link Query}. Query implementations must provide at least constructor one accepting the
     * following arguments, invoked in the order:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param shortNames the archetype short names to query on. May contain wildcards
     * @param exact      if {@code true}, all short names must have the same handler and configuration.
     *                   If {@code false}, all short names must match a common handler
     * @param context    the current context
     * @param type       the type that the query returns
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     * @throws QueryException          if no query supports the supplied short names and type
     */
    public static <T> Query<T> create(String[] shortNames, boolean exact, Context context, Class type) {
        Query<T> query;
        shortNames = DescriptorHelper.getShortNames(shortNames);
        ArchetypeHandler<Query> handler = getQueries().getHandler(shortNames, exact);
        if (handler == null) {
            query = createDefaultQuery(shortNames, type);
        } else {
            query = create(handler, shortNames, context, type);
        }
        if (query == null) {
            throw new QueryException(QueryException.ErrorCode.NoQuery, StringUtils.join(shortNames, ", "),
                                     type.getName());
        }
        return query;
    }

    /**
     * Initialise a query.
     *
     * @param query the query to initialise
     */
    @SuppressWarnings("unchecked")
    public static <T> void initialise(Query<T> query) {
        Class<Query> type = (Class<Query>) query.getClass();
        ArchetypeHandlers<Query> handlers = getQueries();
        ArchetypeHandler<Query> handler = handlers.getHandler(type);
        if (handler != null) {
            initialise(query, handler);
        } else {
            handler = handlers.getHandler(query.getShortNames());
            if (handler != null && handler.getType().isAssignableFrom(type)) {
                initialise(query, handler);
            }
        }
    }

    /**
     * Initialise a query.
     *
     * @param query   the query
     * @param handler the query handler
     */
    protected static <T> void initialise(Query<T> query, ArchetypeHandler<Query> handler) {
        try {
            handler.initialise(query);
        } catch (Throwable exception) {
            log.error(exception);
        }
    }

    /**
     * Attempts to create a new query, using one of the following constructors:
     * <ul>
     * <li>(String[] shortNames, Context context)</li>
     * <li>(String[] shortNames)</li>
     * <li>(Context)</li>
     * <li>default constructor</li>
     * </ul>
     *
     * @param handler    the {@link Query} implementation
     * @param shortNames the archetype short names to query on
     * @param context    the context
     * @param type       the type that the query returns
     * @return a new query, or {@code null} if no appropriate constructor can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private static <T> Query<T> create(ArchetypeHandler<Query> handler, String[] shortNames, Context context,
                                       Class type) {
        Query result = null;
        try {
            try {
                Object[] args = new Object[]{shortNames, context};
                result = handler.create(args);
            } catch (NoSuchMethodException exception) {
                try {
                    Object[] args = new Object[]{shortNames};
                    result = handler.create(args);
                } catch (NoSuchMethodException nested) {
                    try {
                        Object[] args = new Object[]{context};
                        result = handler.create(args);
                    } catch (NoSuchMethodException nested2) {
                        result = handler.create();
                    }
                }
            }
            if (!type.isAssignableFrom(result.getType())) {
                result = null;
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
        return (Query<T>) result;
    }

    /**
     * Creates a new default query for the supplied archetype short names and type.
     *
     * @param shortNames the archetype short names to query
     * @param type       the type that the query returns
     * @return a new query, or {@code null} if there is no default query for the specified type
     */
    @SuppressWarnings("unchecked")
    private static <T> Query<T> createDefaultQuery(String[] shortNames, Class type) {
        if (IMObject.class.isAssignableFrom(type)) {
            return new DefaultQuery(shortNames, type);
        }
        return null;
    }

    /**
     * Returns the query implementations.
     *
     * @return the queries
     */
    private static synchronized ArchetypeHandlers<Query> getQueries() {
        if (queries == null) {
            queries = new ArchetypeHandlers<>("QueryFactory.properties", "DefaultQueryFactory.properties", Query.class,
                                              "query.", ArchetypeServiceHelper.getArchetypeService());
        }
        return queries;
    }

}
