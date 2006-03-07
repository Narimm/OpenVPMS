package org.openvpms.web.component.im.query;

import java.util.EventListener;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Query browser event listener.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface QueryListener extends EventListener {

    /**
     * Invoked when a query is performed.
     */
    void query();

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    void selected(IMObject object);
}
