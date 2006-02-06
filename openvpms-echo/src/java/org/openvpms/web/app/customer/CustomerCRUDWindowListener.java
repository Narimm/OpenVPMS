package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.Context;
import org.openvpms.web.app.subsystem.CRUDWindowListener;


/**
 * Customer CRUD pane listener.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CustomerCRUDWindowListener implements CRUDWindowListener {

    /**
     * Invoked when a new object is selected.
     *
     * @param object the selcted object
     */
    public void selected(IMObject object) {
        Context.getInstance().setCustomer((Party) object);
    }

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     * @param isNew  determines if the object is a new instance
     */
    public void saved(IMObject object, boolean isNew) {
        Context.getInstance().setCustomer((Party) object);
    }

    /**
     * Invoked when an object is deleted
     *
     * @param object the deleted object
     */
    public void deleted(IMObject object) {
        Context.getInstance().setCustomer(null);
    }
}
