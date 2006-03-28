package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.resource.util.Messages;


/**
 * Generic CRUD workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CRUDWorkspace extends AbstractViewWorkspace {

    /**
     * The CRUD window.
     */
    private CRUDWindow _window;

    /**
     * Construct a new <code>CRUDWorkspace</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public CRUDWorkspace(String subsystemId, String workspaceId,
                         String refModelName, String entityName,
                         String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
        _window = new CRUDWindow(getType(), refModelName, entityName,
                                 conceptName);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        _window.setObject(object);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
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
     * Invoked when the 'select' button is pressed. This pops up an {@link
     * Browser} to select an object.
     */
    @Override
    protected void onSelect() {
        final Browser browser = createBrowser(getRefModelName(),
                                              getEntityName(), getConceptName());

        String title = Messages.get("imobject.select.title", getType());
        final BrowserDialog popup = new BrowserDialog(title, browser, true);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (popup.createNew()) {
                    _window.onCreate();
                } else {
                    IMObject object = popup.getSelected();
                    if (object != null) {
                        onSelected(object);
                    }
                }
            }

        });

        popup.show();
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        setObject(object);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        setObject(null);
    }

}
