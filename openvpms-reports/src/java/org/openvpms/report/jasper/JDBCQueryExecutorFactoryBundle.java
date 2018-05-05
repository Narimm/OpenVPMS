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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactoryBundle;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;

import static net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory.QUERY_LANGUAGE_SQL;

/**
 * A query executer factory bundle for JDBC queries.
 *
 * @author Tim Anderson
 */
public class JDBCQueryExecutorFactoryBundle implements JRQueryExecuterFactoryBundle {

    /**
     * The JDBC factory.
     */
    private final JDBCQueryExecuterFactory factory;

    /**
     * Constructs a {@link JDBCQueryExecutorFactoryBundle}.
     *
     * @param factory the JDBC factory
     */
    public JDBCQueryExecutorFactoryBundle(JDBCQueryExecuterFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the supported languages.
     * <p/>
     * the the supported languages
     */
    @Override
    public String[] getLanguages() {
        return new String[]{QUERY_LANGUAGE_SQL};
    }

    /**
     * Returns the query executer factory for a language.
     *
     * @param language the query language
     * @throws JRException for any error
     */
    @Override
    public QueryExecuterFactory getQueryExecuterFactory(String language) throws JRException {
        return QUERY_LANGUAGE_SQL.equalsIgnoreCase(language) ? factory : null;
    }
}
