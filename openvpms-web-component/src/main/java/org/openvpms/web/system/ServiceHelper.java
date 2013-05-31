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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.system;

import nextapp.echo2.app.ApplicationInstance;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.ScheduleService;
import org.openvpms.archetype.rules.workflow.TaskService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.esci.adapter.client.OrderServiceAdapter;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.macro.Macros;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.echo.spring.SpringApplicationInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


/**
 * Helper for accessing services managed by Spring.
 *
 * @author Tim Anderson
 */
public final class ServiceHelper {

    /**
     * Helper to get the archetype service.
     *
     * @return the archetype service
     */
    public static IArchetypeService getArchetypeService() {
        return (ApplicationInstance.getActive() instanceof SpringApplicationInstance) ?
               getBean(IArchetypeRuleService.class) : ArchetypeServiceHelper.getArchetypeService();
    }

    /**
     * Helper to get the archetype service.
     *
     * @param rules if <tt>true</tt> return an instance with business rules
     *              enabled
     * @return the archetype service
     */
    public static IArchetypeService getArchetypeService(boolean rules) {
        if (rules) {
            return getArchetypeService();
        }
        return getContext().getBean("archetypeService", IArchetypeService.class);
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    public static ILookupService getLookupService() {
        return getBean(ILookupService.class);
    }

    /**
     * Helper to return the data source.
     *
     * @return the data source
     */
    public static DataSource getDataSource() {
        return getBean(DataSource.class);
    }

    /**
     * Helper to get the document handlers.
     *
     * @return the document handlers
     */
    public static DocumentHandlers getDocumentHandlers() {
        return getBean(DocumentHandlers.class);
    }

    /**
     * Helper to get the mail sender.
     *
     * @return the mail sender
     */
    public static JavaMailSender getMailSender() {
        return getBean(JavaMailSender.class);
    }

    /**
     * Helper to get the currency cache.
     *
     * @return the currency cache
     */
    public static Currencies getCurrencies() {
        return getBean(Currencies.class);
    }

    /**
     * Helper to get the macros.
     *
     * @return the macros
     */
    public static Macros getMacros() {
        return getBean(Macros.class);
    }

    /**
     * Helper to get the transaction manager.
     *
     * @return the transaction manager
     */
    public static PlatformTransactionManager getTransactionManager() {
        return getBean(PlatformTransactionManager.class);
    }

    /**
     * Helper to get the appointment service.
     *
     * @return the appointment service
     */
    public static ScheduleService getAppointmentService() {
        return getBean(AppointmentService.class);
    }

    /**
     * Helper to return the task service.
     *
     * @return the task service
     */
    public static ScheduleService getTaskService() {
        return getBean(TaskService.class);
    }

    /**
     * Helper to return the order service.
     *
     * @return the order service
     */
    public static OrderServiceAdapter getOrderService() {
        return getBean(OrderServiceAdapter.class);
    }

    /**
     * Helper to return the supplier service locator.
     *
     * @return the supplier service locator
     */
    public static SupplierServiceLocator getSupplierServiceLocator() {
        return getBean(SupplierServiceLocator.class);
    }

    /**
     * Returns the SMS connection factory.
     *
     * @return the SMS connection factory
     */
    public static ConnectionFactory getSMSConnectionFactory() {
        return getBean(ConnectionFactory.class);
    }

    /**
     * Return the bean instance that uniquely matches the given type, if any.
     *
     * @param type the bean type
     * @return the matching bean
     */
    public static <T> T getBean(Class<T> type) {
        return getContext().getBean(type);
    }

    /**
     * Helper to return the application context associated with the current
     * thread.
     *
     * @return the application context associated with the current thread.
     */
    public static ApplicationContext getContext() {
        SpringApplicationInstance app = (SpringApplicationInstance) ApplicationInstance.getActive();
        return app.getApplicationContext();
    }

}
