package org.openvpms.web.app.customer;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.act.ActTableModel;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Estimation workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EstimationWorkspace extends AbstractViewWorkspace {

    /**
     * The workspace.
     */
    private SplitPane _workspace;

    /**
     * The query.
     */
    private ActQuery _query;

    /**
     * The act browser.
     */
    private Browser _acts;

    /**
     * The CRUD window.
     */
    private CRUDWindow _window;


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
        Party customer = Context.getInstance().getCustomer();
        setObject(customer);
        if (customer != null) {
            layoutWorkspace(customer, container);
        }
    }

    /**
     * Invoked when a customer is selected.
     *
     * @param customer the selected customer
     */
    @Override
    protected void onSelected(IMObject customer) {
        super.onSelected(customer);
        Party party = (Party) customer;
        Context.getInstance().setCustomer(party);
        if (_workspace == null) {
            layoutWorkspace(party, getComponent());
        }
        _query.setEntity(party);
        _acts.query();
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
        _acts.query();
        _window.setObject(null);
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    protected void actSelected(Act act) {
        _window.setObject(act);
    }

    protected void layoutWorkspace(Party customer, Component container) {
        String type = Messages.get("customer.estimation.createtype");

        _query = new ActQuery(customer, "act", "estimation");
        IMObjectTable table = new IMObjectTable(new ActTableModel());
        _acts = new Browser(_query, table);
        _acts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Act act = (Act) _acts.getSelected();
                actSelected(act);
            }
        });
        GroupBox actsBox = GroupBoxFactory.create(_acts.getComponent());

        _window = new EstimationCRUDWindow(type, "common", "act", "estimation");
        _workspace = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                "EstimationWorkspace.Layout", actsBox, _window.getComponent());
        container.add(_workspace);

        _window.setListener(new CRUDWindowListener() {
            public void saved(IMObject object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(IMObject object) {
                onDeleted(object);
            }
        });

    }
}
