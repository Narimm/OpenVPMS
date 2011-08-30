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

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * A factory for {@link Browser} instances. The factory is
 * configured to return specific {@link Browser} implementations
 * based on the supplied criteria, with {@link DefaultIMObjectTableBrowser}
 * returned if no implementation matches.
 * <p/>
 * The factory is configured using a
 * <em>BrowserFactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding browser implementations. Short names may be
 * wildcarded e.g:
 * <p/>
 * <table> <tr><td>party.patient*</td><td>org.openvpms.web.app.patient.PatientBrowser</td></tr>
 * </table>
 * <p/>
 * Multiple <em>BrowserFactory.properties</em> may be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-07 21:27:47Z $
 */
public final class BrowserFactory {

    /**
     * Browser implementations.
     */
    private static ArchetypeHandlers<Browser> browsers;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(BrowserFactory.class);


    /**
     * Prevent construction.
     */
    private BrowserFactory() {
    }

    /**
     * Creates a new {@link Browser}.
     * This is the same as invoking <tt>create(query, null)</tt>.
     *
     * @param query the query
     * @return a new browser
     */
    public static <T> Browser<T> create(Query<T> query) {
        return create(query, null);
    }

    /**
     * Creates a new {@link Browser}.
     * Implementations must provide at least one constructor accepting the
     * following arguments, invoked in the order:
     * <ul>
     * <li>(Query<T> query, SortConstraint[] sort)</li>
     * <li>(Query<T> query)</li>
     * </ul>
     *
     * @param query the query
     * @param sort  the sort criteria. May be <tt>null</tt>
     * @return a new browser
     */
    public static <T> Browser<T> create(
            Query<T> query, SortConstraint[] sort) {
        Browser<T> result = null;
        String[] shortNames = DescriptorHelper.getShortNames(
                query.getShortNames());
        ArchetypeHandler<Browser> handler
                = getBrowsers().getHandler(shortNames);
        if (handler != null) {
            result = create(handler, query, sort);
        }
        if (result == null) {
            result = createDefaultBrowser(query, sort, null, shortNames);
        }
        return result;
    }

    /**
     * Creates a new {@link Browser}.
     * Implementations must provide at least one constructor accepting the
     * following arguments, invoked in the order:
     * <ul>
     * <li>(Query<T> query, SortConstraint[] sort, IMTableModel<T> model)
     * <li>(Query<T> query, SortConstraint[] sort)</li>
     * <li>(Query<T> query)</li>
     * </ul>
     *
     * @param query the query
     * @param sort  the sort criteria. May be <tt>null</tt>
     * @return a new browser
     */
    public static <T> Browser<T> create(
            Query<T> query, SortConstraint[] sort, IMTableModel<T> model) {
        Browser<T> result = null;
        String[] shortNames = DescriptorHelper.getShortNames(
                query.getShortNames());
        ArchetypeHandler<Browser> handler
                = getBrowsers().getHandler(shortNames);
        if (handler != null) {
            result = create(handler, query, sort, model);
        }
        if (result == null) {
            result = createDefaultBrowser(query, sort, model, shortNames);
        }
        return result;
    }

    /**
     * Creates a default browser for the supplied query.
     *
     * @param query      the query
     * @param sort       the sort constraint. May be <tt>null</tt>
     * @param model      the model. May be <tt>null</tt>
     * @param shortNames the archetype short names
     * @return a new browser
     * @throws QueryException if there is no default browser for the query
     */
    @SuppressWarnings("unchecked")
    private static <T>Browser<T> createDefaultBrowser(Query<T> query,
                                                      SortConstraint[] sort,
                                                      IMTableModel<T> model,
                                                      String[] shortNames) {
        Browser<T> result;
        Class type = query.getType();
        if (IMObject.class.isAssignableFrom(type)) {
            if (model != null) {
                result = new DefaultIMObjectTableBrowser(query, sort, model);
            } else {
                result = new DefaultIMObjectTableBrowser(query, sort);
            }
        } else {
            throw new QueryException(QueryException.ErrorCode.NoBrowser,
                                     StringUtils.join(shortNames, ", "),
                                     type.getName());
        }
        return result;
    }

    /**
     * Attempts to create a new browser, using the BrowserImpl(Query<T>)
     * constructor.
     *
     * @param handler the {@link Browser} implementation
     * @param query   the query
     * @return a new browser, or <tt>null</tt> if no appropriate constructor
     *         can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private static <T> Browser<T> create(
            ArchetypeHandler<Browser> handler, Query<T> query) {
        Browser<T> result = null;
        try {
            Object[] args = {query};
            Class[] types = {query.getClass()};
            result = (Browser<T>) handler.create(args, types);
        } catch (Throwable exception) {
            log.error(exception, exception);
        }
        return result;
    }

    /**
     * Attempts to create a new browser. First tries the
     * BrowserImpl(Query<T>, SortConstraint[]) constructor. If that doesn't
     * exist, tries the BrowserImpl(Query<T>) constructor.
     *
     * @param handler the {@link Browser} implementation
     * @param query   the query
     * @param sort    the sort criteria. May be <tt>null</tt>
     * @return a new browser, or <tt>null</tt> if no appropriate constructor
     *         can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private static <T> Browser<T> create(
            ArchetypeHandler<Browser> handler, Query<T> query,
            SortConstraint[] sort) {
        Browser<T> result = null;
        try {
            Object[] args = {query, sort};
            Class[] types = {query.getClass(), SortConstraint[].class};
            result = (Browser<T>) handler.create(args, types);
        } catch (NoSuchMethodException ignore) {
            result = create(handler, query);
        } catch (Throwable exception) {
            log.error(exception, exception);
        }
        return result;
    }

    /**
     * Attempts to create a new browser. First tries the
     * BrowserImpl(Query<T>, SortConstraint[], IMTableModel<T>) constructor.
     * If that doesn't exist, tries the BrowserImpl(Query<T>, SortConstraint[]),
     * followed by the BrowserImpl(Query<T>) constructor.
     *
     * @param handler the {@link Browser} implementation
     * @param query   the query
     * @param sort    the sort criteria. May be <tt>null</tt>
     * @return a new browser, or <tt>null</tt> if no appropriate constructor
     *         can be found or construction fails
     */
    @SuppressWarnings("unchecked")
    private static <T> Browser<T> create(
            ArchetypeHandler<Browser> handler, Query<T> query,
            SortConstraint[] sort, IMTableModel<T> model) {
        Browser<T> result = null;
        try {
            Object[] args = {query, sort, model};
            Class[] types = {Query.class, SortConstraint[].class,
                             IMTableModel.class};
            result = (Browser<T>) handler.create(args, types);
        } catch (NoSuchMethodException ignore) {
            result = create(handler, query, sort);
        } catch (Throwable exception) {
            log.error(exception, exception);
        }
        return result;
    }

    /**
     * Returns the browser implementations.
     *
     * @return the browsers
     */
    private static synchronized ArchetypeHandlers<Browser> getBrowsers() {
        if (browsers == null) {
            browsers = new ArchetypeHandlers<Browser>(
                    "BrowserFactory.properties", Browser.class);
        }
        return browsers;
    }


}
