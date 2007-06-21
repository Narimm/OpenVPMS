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

package org.openvpms.web.system;

import nextapp.echo2.app.ApplicationInstance;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.util.MacroCache;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

import javax.sql.DataSource;


/**
 * Helper for accessing services managed by Spring.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class ServiceHelper {

    /**
     * Helper to get the archetype service.
     *
     * @return the archetype service
     */
    public static IArchetypeService getArchetypeService() {
        return ArchetypeServiceHelper.getArchetypeService();
    }

    /**
     * Helper to return the data source.
     *
     * @return the data source
     */
    public static DataSource getDataSource() {
        return (DataSource) getContext().getBean("dataSource");
    }

    /**
     * Helper to get the document handlers.
     *
     * @return the document handlers
     */
    public static DocumentHandlers getDocumentHandlers() {
        return (DocumentHandlers) getContext().getBean("documentHandlers");
    }

    /**
     * Helper to get the mail sender.
     *
     * @return the mail sender
     */
    public static JavaMailSender getMailSender() {
        return (JavaMailSender) getContext().getBean("mailSender");
    }

    /**
     * Helper to get the currency cache.
     *
     * @return the currency cache
     */
    public static Currencies getCurrencies() {
        return (Currencies) getContext().getBean("currencies");
    }

    /**
     * Helper to get the macro cache.
     *
     * @return the macro cache
     */
    public static MacroCache getMacroCache() {
        return (MacroCache) getContext().getBean("macroCache");
    }

    /**
     * Helper to return the application context associated with the current
     * thread.
     *
     * @return the application context associated with the current thread.
     */
    public static ApplicationContext getContext() {
        SpringApplicationInstance app
                = (SpringApplicationInstance) ApplicationInstance.getActive();
        return app.getApplicationContext();
    }

}
