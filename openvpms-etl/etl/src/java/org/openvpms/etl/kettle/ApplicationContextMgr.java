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

package org.openvpms.etl.kettle;

import org.apache.commons.lang.ObjectUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Manages the Spring <tt>ApplicationContext</tt>.
 * Helper to load an <tt>ApplicationContext</tt>, populating the
 * <em>dataSource</em> properties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ApplicationContextMgr {

    /**
     * The database info used to construct the context.
     */
    private static DatabaseMeta db;

    /**
     * The context.
     */
    private static ConfigurableApplicationContext context;

    /**
     * The logger.
     */
    private static final LogWriter log = LogWriter.getInstance();

    /**
     * The application context file path.
     */
    private static final String APPLICATION_CONTEXT
            = "applicationContext.xml"; // NON-NLS

    /**
     * Gets the application context, creating it if necessary.
     *
     * @param database the database properties
     * @return the application context
     * @throws KettleException for any kettle error
     * @throws BeansException  for any Spring error
     */
    public static synchronized ApplicationContext getContext(DatabaseMeta database) throws KettleException {
        if (context == null || !ObjectUtils.equals(db, database)) {
            if (context != null) {
                context.close();
            }
            log.println(LogWriter.LOG_LEVEL_BASIC,
                        Messages.get("ApplicationContextMgr.BeginInit",
                                     database.getURL()));
            Thread thread = Thread.currentThread();
            ClassLoader loader = thread.getContextClassLoader();
            Class<ApplicationContextMgr> clazz = ApplicationContextMgr.class;
            thread.setContextClassLoader(clazz.getClassLoader());
            try {
                context = load(database);
                log.println(LogWriter.LOG_LEVEL_BASIC,
                            Messages.get("ApplicationContextMgr.EndInit"));
            } finally {
                thread.setContextClassLoader(loader);
            }
            db = database;
        }
        return context;
    }

    /**
     * Loads the application context from the classpath,
     * populating <em>dataSource</em> bean properties.
     *
     * @param database the database properties
     * @return a new application context
     * @throws BeansException for any spring error
     * @throws KettleException for any kettle errror
     */
    private static ConfigurableApplicationContext load(DatabaseMeta database)
            throws KettleException {
        final String driver = database.getDriverClass();
        final String url = database.getURL();
        final String user = database.getUsername();
        final String password = database.getPassword();
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext(
                new String[]{APPLICATION_CONTEXT}, false);
        context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            @SuppressWarnings({"HardCodedStringLiteral"})
            public void postProcessBeanFactory(
                    ConfigurableListableBeanFactory beanFactory)
                    throws BeansException {
                BeanDefinition def = beanFactory.getBeanDefinition(
                        "dataSource");
                MutablePropertyValues properties = def.getPropertyValues();
                properties.addPropertyValue("driverClassName", driver);
                properties.addPropertyValue("url", url);
                properties.addPropertyValue("username", user);
                properties.addPropertyValue("password", password);
            }
        });
        context.refresh();
        return context;
    }

}
