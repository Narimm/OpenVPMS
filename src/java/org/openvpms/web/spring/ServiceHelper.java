package org.openvpms.web.spring;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.context.ApplicationContext;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * Helper for accessing services managed by Spring.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision$ $Date$
 */
public final class ServiceHelper {

    /**
     * Helper to get the archetype service.
     *
     * @return the archetype service
     */
    public static IArchetypeService getArchetypeService() {
        return (IArchetypeService) getContext().getBean("archetypeService");
    }

    /**
     * Helper to get the lookup service.
     *
     * @return the lookup service
     */
    public static ILookupService getLookupService() {
        return (ILookupService) getContext().getBean("lookupService");
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
