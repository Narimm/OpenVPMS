package org.openvpms.web.app.customer;

import java.util.Set;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.Context;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;


/**
 * Estimation workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EstimationWorkspace extends AbstractViewWorkspace {

    /**
     * The CRUD window.
     */
    private CRUDWindow _window;

    /**
     * The component representing this.
     */
    private SplitPane _component;

    /**
     * Button row style.
     */
    private static final String ROW_STYLE = "ControlRow";

    /**
     * Workspace layout style.
     */
    private static final String LAYOUT_STYLE = "CRUDWorkspace.Layout";


    /**
     * Construct a new <code>EstimationWorkspace</code>.
     */
    public EstimationWorkspace() {
        super("customer", "estimation", "party", "party", "customer*");
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        _window = new EstimationCRUDWindow("Estimations", "common", "act", "estimation*");
        Party customer = Context.getInstance().getCustomer();
        setObject(customer);
        container.add(_window.getComponent());

        _window.setListener(new CRUDWindowListener() {
            public void saved(IMObject object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(IMObject object) {
                onDeleted(object);
            }
        });
    }

    /**
     * Invoked when a customer is selected.
     *
     * @param customer the selected customer
     */
    @Override
    protected void onSelected(IMObject customer) {
        super.onSelected(customer);
        Context.getInstance().setCustomer((Party) customer);
        Set<Participation> participations
                = ((Party) customer).getParticipations();
        if (participations != null) {
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
    }


}
