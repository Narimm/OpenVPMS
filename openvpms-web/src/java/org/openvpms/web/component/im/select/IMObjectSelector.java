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

package org.openvpms.web.component.im.select;

import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.DocumentListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Selector that provides query support for partial/incorrect names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectSelector extends Selector {

    /**
     * The selected object.
     */
    private IMObject object;

    /**
     * The archetype short names to query on.
     */
    private final String[] shortNames;

    /**
     * Display name for the types of object this may select.
     */
    private final String type;

    /**
     * Update listener for the text field.
     */
    private final DocumentListener textListener;

    /**
     * The listener. May be <code>null</code>
     */
    private QuerySelectorListener listener;

    /**
     * The previous selector text, to avoid spurious updates.
     */
    private String _prevText;


    /**
     * Construct a new <code>QuerySelector</code>.
     *
     * @param descriptor the node descriptor
     */
    public IMObjectSelector(NodeDescriptor descriptor) {
        this(descriptor.getDisplayName(), descriptor.getArchetypeRange());
    }

    /**
     * Construct a new <code>IMObjectSelector</code>.
     *
     * @param type       display name for the types of objects this may select
     * @param shortNames the archetype short names to query
     */
    public IMObjectSelector(String type, String[] shortNames) {
        super(ButtonStyle.RIGHT_NO_ACCEL, true);
        setFormat(Format.NAME);
        this.type = type;
        this.shortNames = shortNames;
        getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect();
            }
        });

        TextField text = getText();
        textListener = new DocumentListener() {
            public void documentUpdate(DocumentEvent event) {
                onTextChanged();
            }
        };

        text.getDocument().addDocumentListener(textListener);

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        text.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // no-op.
            }
        });
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        this.object = object;
        TextField text = getText();
        text.getDocument().removeDocumentListener(textListener);
        super.setObject(object);
        text.getDocument().addDocumentListener(textListener);
        _prevText = text.getText();
    }

    /**
     * Returns the current object.
     *
     * @return the current object. May be <code>null</code>
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Sets the listener.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(QuerySelectorListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the display name for the types of objects this may select.
     *
     * @return the type display name
     */
    protected String getType() {
        return type;
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
            final Browser<IMObject> browser = createBrowser(query);
            final BrowserDialog<IMObject> popup = createBrowserDialog(browser);
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
     * Creates a new browser dialog.
     *
     * @param browser the browser
     * @return a new dialog for the browser
     */
    protected BrowserDialog<IMObject> createBrowserDialog(
            Browser<IMObject> browser) {
        String title = Messages.get("imobject.select.title", type);
        return new BrowserDialog<IMObject>(title, browser);
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    protected void onSelected(IMObject object) {
        setObject(object);
        if (listener != null) {
            listener.selected(object);
        }
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query<IMObject> createQuery() {
        String name = getText().getText();
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
        Query<IMObject> query = QueryFactory.create(
                shortNames, GlobalContext.getInstance());
        query.setName(name);
        return query;
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a return a new browser
     */
    protected Browser<IMObject> createBrowser(Query<IMObject> query) {
        return new IMObjectTableBrowser<IMObject>(query);
    }

    /**
     * Invoked when the text field is updated.
     */
    private void onTextChanged() {
        TextField text = getText();
        String name = text.getText();
        if (!ObjectUtils.equals(name, _prevText)) {
            if (StringUtils.isEmpty(name)) {
                setObject(null);
            } else {
                try {
                    Query<IMObject> query = createQuery(name);
                    ResultSet<IMObject> set = query.query(null);
                    if (set != null && set.hasNext()) {
                        IPage<IMObject> page = set.next();
                        List<IMObject> rows = page.getResults();
                        int size = rows.size();
                        if (size == 0) {
                            setObject(null);
                        } else if (size == 1) {
                            IMObject object = rows.get(0);
                            setObject(object);
                            listener.selected(object);
                        } else {
                            onSelect(query, true);
                        }
                    }
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
                    listener.selected(null);
                }
            }
        }
    }

}
