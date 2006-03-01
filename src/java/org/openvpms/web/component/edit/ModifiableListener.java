package org.openvpms.web.component.edit;

import java.util.EventListener;


/**
 * Listener for {@link Modifiable} events.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface ModifiableListener extends EventListener {

    /**
     * Invoked when a {@link Modifiable} changes.
     *
     * @param modifiable the modifiable
     */
    void modified(Modifiable modifiable);
}
