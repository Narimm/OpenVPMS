package org.openvpms.web.app;

import java.util.EventListener;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Listener for context changes.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface ContextChangeListener extends EventListener {

    /**
     * Change the context.
     *
     * @param context the context to change to
     */
    void changeContext(IMObject context);
}
