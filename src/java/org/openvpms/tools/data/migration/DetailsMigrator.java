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

package org.openvpms.tools.data.migration;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class DetailsMigrator {

    /**
     * The data source.
     */
    private final DataSource dataSource;

    private final XStream stream;


    /**
     * The default name of the application context file
     */
    private final static String APPLICATION_CONTEXT
            = "applicationContext.xml";

    /**
     * Constructs a new <tt>DetailsExporter</tt>.
     *
     * @param dataSource the datasource
     */
    public DetailsMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
        stream = new XStream();
    }

    public void export() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try {
            export(connection, "acts", "act_details", "act_id");
            export(connection, "act_relationships", "act_relationship_details",
                   "act_relationship_id");
            export(connection, "contacts", "contact_details", "contact_id");
            export(connection, "documents", "document_details", "document_id");
            export(connection, "entities", "entity_details", "entity_id");
            export(connection, "entity_identities", "entity_identity_details",
                   "entity_identity_id");
            export(connection, "entity_relationships",
                   "entity_relationship_details", "entity_relationship_id");
            export(connection, "lookups", "lookup_details", "lookup_id");
            export(connection, "lookup_relationships",
                   "lookup_relationship_details", "lookup_relationship_id");
            export(connection, "participations", "participation_details",
                   "participation_id");
            export(connection, "product_prices", "product_price_details",
                   "product_price_id");
        } finally {
            connection.close();
        }
    }

    private void export(Connection connection, String from, String to,
                        String key)
            throws SQLException {
        System.out.println("Migrating details from " + from + " to " + to);
        Date start = new Date();
        PreparedStatement select
                = connection.prepareStatement(
                "select " + key + ", details from " + from
                        + " where details is not null");
        PreparedStatement insert = connection.prepareStatement(
                "insert into " + to + " (" + key + ", type, name, value) "
                        + "values (?, ?, ?, ?)");
        ResultSet set = select.executeQuery();
        int input = 0;
        int output = 0;
        int batch = 0;
        while (set.next()) {
            ++input;
            long id = set.getLong(1);
            String details = set.getString(2);
            if (!StringUtils.isEmpty(details)) {
                DynamicAttributeMap map
                        = (DynamicAttributeMap) stream.fromXML(details);
                Map<String, Serializable> attributes = map.getAttributes();
                for (Map.Entry<String, Serializable> entry
                        : attributes.entrySet()) {
                    String name = entry.getKey();
                    TypedValue value = new TypedValue(entry.getValue());
                    insert.setLong(1, id);
                    insert.setString(2, value.getType());
                    insert.setString(3, name);
                    insert.setString(4, value.getValue());
                    insert.addBatch();
                    ++output;
                }
            }
            ++batch;
            if (batch >= 1000) {
                // commit every 1000 input rows
                insert.executeBatch();
                connection.commit();
                batch = 0;
            }
        }
        if (batch != 0) {
            insert.executeBatch();
            connection.commit();
        }
        Date end = new Date();
        double elapsed = (end.getTime() - start.getTime()) / 1000;
        System.out.printf("Processed %d rows, generating %d rows in %.2fs\n",
                          input, output, elapsed);
    }


    /**
     * Main line.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser);
            } else {
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                DataSource source = (DataSource) context.getBean("dataSource");
                DetailsMigrator migrator = new DetailsMigrator(source);
                migrator.export();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("Application context path"));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java " // NON-NLS
                + DetailsMigrator.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
