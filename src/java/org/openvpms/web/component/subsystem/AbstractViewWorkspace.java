/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.subsystem;

import java.util.List;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Workspace that provides a selector to select an object for viewing.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
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
     * The current object. May be <code>null</code>.
     */
    private IMObject _object;

    /**
     * The selector.
     */
    private Selector _selector;

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String _type;

    /**
     * The root component.
     */
    private SplitPane _root;


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

        _selector.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onSelect();
            }
        });
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    public boolean canHandle(String shortName) {
        boolean result = false;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        try {
            List<String> shortNames = service.getArchetypeShortNames(
                    _refModelName, _entityName, _conceptName, true);
            result = shortNames.contains(shortName);
        } catch (OpenVPMSException exception) {
            ErrorDialog.show(exception);
        }
        return result;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        _object = object;
        _selector.setObject(object);
    }

    /**
     * Returns the current object.
     *
     * @return the current object. May be <code>null</code>
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        _root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "AbstractViewWorkspace.Layout");
        Component heading = super.doLayout();
        Component selector = _selector.getComponent();

        Column top = ColumnFactory.create(heading, selector);
        _root.add(top);
        doLayout(_root);
        return _root;
    }

    /**
     * Returns the root split pane.
     *
     * @return the root split pane
     */
    protected SplitPane getRootComponent() {
        return _root;
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
        Query query = QueryFactory.create(refModelName, entityName,
                                          conceptName);
        SortConstraint[] sort = {new NodeSortConstraint("name", true)};
        return new Browser(query, sort);
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
