package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.Context;
import org.openvpms.web.app.subsystem.CRUDWindowListener;


/**
 * Supplier CRUD pane listener.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
class SupplierCRUDWindowListener implements CRUDWindowListener {

    /**
     * Invoked when a new object is selected.
     *
     * @param object the selected object
     */
    public void selected(IMObject object) {
        Context.getInstance().setSupplier((Party) object);
    }

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     */
    public void saved(IMObject object) {
        Context.getInstance().setSupplier((Party) object);
    }

    /**
     * Invoked when an object is deleted
     *
     * @param object the deleted object
     */
    public void deleted(IMObject object) {
        Context.getInstance().setSupplier(null);
    }
}
