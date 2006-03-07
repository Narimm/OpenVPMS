package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.im.query.DefaultQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.resource.util.Messages;


/**
 * Workspace that provides a selector to select an object for viewing.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractViewWorkspace extends AbstractWorkspace {

    /**
     * The archetype reference model name, used to query objects.
     */
    private final String _refModelName;

    /**
     * The archetype entity name, used to query objects. May be
     * <code>null</code>.
     */
    private final String _entityName;

    /**
     * The archetype concept name, used to query objects. May be
     * <code>null</code>.
     */
    private final String _conceptName;

    /**
     * The selector.
     */
    private Selector _selector;

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String _type;


    /**
     * Construct a new <code>AbstractViewWorkspace</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractViewWorkspace(String subsystemId, String workspaceId,
                                 String refModelName, String entityName,
                                 String conceptName) {
        super(subsystemId, workspaceId);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        _selector = new Selector();

        String id = getSubsystemId() + "." + getWorkspaceId();
        _type = Messages.get(id + ".type");
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane layout = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL);
        Component heading = super.doLayout();
        Component selector = _selector.getComponent();

        _selector.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onSelect();
            }
        });

        Column top = ColumnFactory.create(heading, selector);
        layout.add(top);
        doLayout(layout);
        return layout;
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected abstract void doLayout(Component container);

    /**
     * Returns the archetype reference model name.
     *
     * @return the archetype reference model name
     */
    protected String getRefModelName() {
        return _refModelName;
    }

    /**
     * Returns the archetype entity name.
     *
     * @return the archetype entity name
     */
    protected String getEntityName() {
        return _entityName;
    }

    /**
     * Returns the archetype concept name.
     */
    protected String getConceptName() {
        return _conceptName;
    }

    /**
     * Returns a localised type display name.
     *
     * @return a localised type display name
     */
    protected String getType() {
        return _type;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    protected void setObject(IMObject object) {
        _selector.setObject(object);
    }

    /**
     * Returns the selector.
     *
     * @return the selector
     */
    protected Selector getSelector() {
        return _selector;
    }

    /**
     * Create a new browser.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a new browser
     */
    protected Browser createBrowser(String refModelName, String entityName,
                                    String conceptName) {
        Query query = new DefaultQuery(refModelName, entityName, conceptName);
        return new Browser(query);
    }

    /**
     * Invoked when the 'select' button is pressed. This pops up an {@link
     * Browser} to select an object.
     */
    protected void onSelect() {
        final Browser browser = createBrowser(_refModelName, _entityName,
                _conceptName);

        String title = Messages.get("imobject.select.title", _type);
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject object = popup.getSelected();
                if (object != null) {
                    onSelected(object);
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    protected void onSelected(IMObject object) {
        setObject(object);
    }

}
