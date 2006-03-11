package org.openvpms.web.app.customer;

import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.act.ActTableModel;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Act workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
abstract class ActWorkspace extends AbstractViewWorkspace {

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
     * Construct a new <code>ActWorkspace</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public ActWorkspace(String subsystemId, String workspaceId,
                        String refModelName, String entityName,
                        String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current customer has changed.
     *
     * @return <code>true</code> if the workspace should be refreshed, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        Party customer = Context.getInstance().getCustomer();
        return (customer != getObject());
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
            initQuery(customer);
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
        initQuery(party);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        _acts.query();
        _acts.setSelected(object);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        _acts.query();
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    protected void actSelected(Act act) {
        _window.setObject(act);
    }

    /**
     * Lays out the workspace.
     *
     * @param customer  the customer
     * @param container the container
     */
    protected void layoutWorkspace(Party customer, Component container) {
        _query = createQuery(customer);
        IMObjectTable table = new IMObjectTable(new ActTableModel());
        _acts = new Browser(_query, table);
        _acts.addQueryListener(new QueryBrowserListener() {
            public void query() {
                selectFirst();
            }

            public void selected(IMObject object) {
                actSelected((Act) object);
            }
        });
        GroupBox actsBox = GroupBoxFactory.create(_acts.getComponent());

        _window = createCRUDWindow();
        _workspace = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                             "ActWorkspace.Layout", actsBox,
                                             _window.getComponent());
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

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected abstract CRUDWindow createCRUDWindow();

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected abstract ActQuery createQuery(Party customer);

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param customer the customer
     */
    private void initQuery(Party customer) {
        _query.setEntity(customer);
        _acts.query();
        selectFirst();
    }

    /**
     * Selects the first available act, if any.
     */
    private void selectFirst() {
        List<IMObject> objects = _acts.getObjects();
        if (!objects.isEmpty()) {
            IMObject current = objects.get(0);
            _acts.setSelected(current);
            _window.setObject(current);
        } else {
            _window.setObject(null);
        }
    }
}
