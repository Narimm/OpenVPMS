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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.DocumentListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Editor for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectReferenceEditor extends AbstractPropertyEditor {

    /**
     * The object name listener.
     */
    private final DocumentListener _nameListener;

    /**
     * The selector.
     */
    private Selector _selector;


    /**
     * Constructs a new <code>IMObjectReferenceEditor</code>.
     *
     * @param property the reference property
     * @param context  the layout context
     */
    public IMObjectReferenceEditor(Property property, LayoutContext context) {
        super(property);
        _selector = new Selector(Selector.ButtonStyle.RIGHT_NO_ACCEL, true);
        _selector.setFormat(Selector.Format.NAME);
        FocusSet set = new FocusSet("IMObjectReferenceEditor");
        set.add(_selector.getComponent());
        context.getFocusTree().add(set);
        _selector.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect();
            }
        });
        IMObjectReference reference = (IMObjectReference) property.getValue();
        if (reference != null) {
            NodeDescriptor descriptor = property.getDescriptor();
            _selector.setObject(
                    IMObjectHelper.getObject(reference, descriptor));
        }

        TextField text = _selector.getText();
        _nameListener = new DocumentListener() {
            public void documentUpdate(DocumentEvent event) {
                onNameChanged();
            }
        };
        text.getDocument().addDocumentListener(_nameListener);

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        text.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // no-op.
            }
        });
    }

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object. May  be <code>null</code>
     */
    public void setObject(IMObject object) {
        updateProperty(object);
        updateSelector(object);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return _selector.getComponent();
    }

    /**
     * Returns the object reference's descriptor.
     *
     * @return the object reference's descriptor
     */
    public NodeDescriptor getDescriptor() {
        return getProperty().getDescriptor();
    }

    /**
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return <code>true</code>  if the reference is null; otherwise
     *         <code>false</code>
     */
    public boolean isNull() {
        boolean result = false;
        if (getProperty().getValue() == null) {
            TextField text = _selector.getText();
            if (text == null || StringUtils.isEmpty(text.getText())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Pops up a dialog to select an object.
     */
    protected void onSelect() {
        onSelect(createQuery(), false);
    }

    /**
     * Pop up a dialog to select an object.
     *
     * @param query    the query
     * @param runQuery if <code>true</code> run the query
     */
    protected void onSelect(Query<IMObject> query, boolean runQuery) {
        try {
            final Browser<IMObject> browser = new TableBrowser<IMObject>(query);

            String title = Messages.get("imobject.select.title",
                                        getDescriptor().getDisplayName());
            final BrowserDialog<IMObject> popup = new BrowserDialog<IMObject>(
                    title, browser);
            if (runQuery && !query.isAuto()) {
                browser.query();
            }

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    IMObject object = popup.getSelected();
                    if (object != null) {
                        onSelected(object);
                    }
                }
            });

            popup.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    protected void onSelected(IMObject object) {
        setObject(object);
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query<IMObject> createQuery() {
        String name = _selector.getText().getText();
        return createQuery(name);
    }

    /**
     * Creates a query to select objects.
     *
     * @param name a name to filter on. May be <code>null</code>
     * @param name
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Query<IMObject> createQuery(String name) {
        String[] shortNames = getDescriptor().getArchetypeRange();
        Query<IMObject> query = QueryFactory.create(shortNames);
        query.setName(name);
        return query;
    }

    /**
     * Invoked when the name is updated.
     */
    private void onNameChanged() {
        TextField text = _selector.getText();
        String name = text.getText();
        if (StringUtils.isEmpty(name)) {
            setObject(null);
        } else {
            try {
                Query<IMObject> query = createQuery(name);
                ResultSet<IMObject> set = query.query(null);
                if (set != null && set.hasNext()) {
                    IPage<IMObject> page = set.next();
                    List<IMObject> rows = page.getRows();
                    int size = rows.size();
                    if (size == 0) {
                        setObject(null);
                    } else if (size == 1) {
                        IMObject object = rows.get(0);
                        setObject(object);
                    } else {
                        onSelect(query, true);
                    }
                }
            } catch (OpenVPMSException exception) {
                updateProperty(null);
                ErrorHelper.show(exception);
            }
        }
    }

    private void updateProperty(IMObject object) {
        Property property = getProperty();
        if (object != null) {
            property.setValue(object.getObjectReference());
        } else {
            property.setValue(null);
        }
    }

    private void updateSelector(IMObject object) {
        TextField text = _selector.getText();
        text.getDocument().removeDocumentListener(_nameListener);
        _selector.setObject(object);
        text.getDocument().addDocumentListener(_nameListener);
    }

}
