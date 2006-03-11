package org.openvpms.web.component.im.query;

import java.util.EventListener;


/**
 * {@link Query} event listener.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface QueryListener extends EventListener {

    /**
     * Invoked when a query is performed.
     */
    void query();
}
