package org.openvpms.pos.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.pos.api.POSException;
import org.openvpms.pos.api.POSService;
import org.openvpms.pos.api.Terminal;
import org.openvpms.pos.i18n.POSMessages;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Default implementation of {@link POSService}.
 *
 * @author Tim Anderson
 */
public class POSServiceImpl implements POSService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(POSServiceImpl.class);

    /**
     * Constructs a {@link POSService}.
     *
     * @param service the archetype service
     */
    public POSServiceImpl(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns a POS terminal given its configuration.
     *
     * @param terminal the terminal configuration.
     * @return the POS terminal
     */
    @Override
    public Terminal getTerminal(Entity terminal) {
        IMObjectBean bean = new IMObjectBean(terminal, service);
        String className = bean.getString("class");
        Object object;
        Class type;
        try {
            type = Class.forName(className);
        } catch (Exception exception) {
            throw new POSException(POSMessages.terminalNotFound(terminal), exception);
        }
        if (Terminal.class.isAssignableFrom(type)) {
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerSingleton("archetypeService", service);
            factory.registerSingleton("terminal", terminal);
            object = factory.createBean(type, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
        } else {
            log.error("Terminal, id=" + terminal.getId() + ", name=" + terminal.getName()
                    + ", class=" + type.getName() + " does not implement " + Terminal.class.getName());
            throw new POSException(POSMessages.terminalNotFound(terminal));
        }
        return (Terminal) object;
    }

}
