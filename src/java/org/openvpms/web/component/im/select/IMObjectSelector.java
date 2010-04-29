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
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.DocumentListener;
import org.openvpms.web.component.event.WindowPaneListener;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Selector that provides query support for partial/incorrect names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectSelector<T extends IMObject> extends Selector<T> {

    /**
     * The selected object.
     */
    private T object;

    /**
     * The archetype short names to query on.
     */
    private final String[] shortNames;

    /**
     * Display name for the types of object this may select.
     */
    private final String type;

    /**
     * Determines if objects may be created.
     */
    private boolean allowCreate;

    /**
     * Update listener for the text field.
     */
    private final DocumentListener textListener;

    /**
     * The listener. May be <tt>null</tt>
     */
    private IMObjectSelectorListener<T> listener;

    /**
     * The previous selector text, to avoid spurious updates.
     */
    private String prevText;

    /**
     * Determines if a selection dialog is currently been popped up.
     */
    private boolean inSelect;


    /**
     * Constructs a new <tt>IMObjectSelector</tt>.
     *
     * @param property the property
     */
    public IMObjectSelector(Property property) {
        this(property, false);
    }

    /**
     * Constructs a new <tt>IMObjectSelector</tt>.
     *
     * @param property    the property
     * @param allowCreate determines if objects may be created
     */
    public IMObjectSelector(Property property, boolean allowCreate) {
        this(property.getDisplayName(), allowCreate);
    }

    /**
     * Constructs a new <tt>IMObjectSelector</tt>.
     *
     * @param type       display name for the types of objects this may select
     * @param shortNames the archetype short names to query
     */
    public IMObjectSelector(String type, String ... shortNames) {
        this(type, false, shortNames);
    }

    /**
     * Constructs a new <tt>IMObjectSelector</tt>.
     *
     * @param type        display name for the types of objects this may select
     * @param allowCreate determines if objects may be created
     * @param shortNames  the archetype short names to query
     */
    public IMObjectSelector(String type, boolean allowCreate,
                            String ... shortNames) {
        super(ButtonStyle.RIGHT_NO_ACCEL, true);
        setFormat(Format.NAME);
        this.type = type;
        this.shortNames = shortNames;
        this.allowCreate = allowCreate;
        getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });

        TextField text = getText();
        textListener = new DocumentListener() {
            public void onUpdate(DocumentEvent event) {
                onTextChanged();
            }
        };

        text.getDocument().addDocumentListener(textListener);

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        text.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                // no-op.
            }
        });
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(T object) {
        this.object = object;
        TextField text = getText();
        text.getDocument().removeDocumentListener(textListener);
        super.setObject(object);
        text.getDocument().addDocumentListener(textListener);
        prevText = text.getText();
    }

    /**
     * Returns the current object.
     *
     * @return the current object. May be <tt>null</tt>
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the listener.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(IMObjectSelectorListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Determines if the selector is valid.
     * It is valid if an object has been selected, or no object is present
     * and no text is input.
     *
     * @return <tt>true</tt> if the selector is valid, otherwise
     *         <tt>false</tt>
     */
    public boolean isValid() {
        return (object != null) || StringUtils.isEmpty(getText().getText());
    }

    /**
     * Determines if a selection dialog has been popped up.
     *
     * @return <tt>true</tt> if a selection dialog has been popped up
     *         otherwise <tt>false</tt>
     */
    public boolean inSelect() {
        return inSelect;
    }

    /**
     * Determines if objects may be created.
     *
     * @param create if <tt>true</tt>, objects may be created
     */
    public void setAllowCreate(boolean create) {
        allowCreate = create;
    }

    /**
     * Determines if objects may be created.
     *
     * @return <tt>true</tt> if objects may be created
     */
    public boolean allowCreate() {
        return allowCreate;
    }

    /**
     * Returns the display name for the types of objects this may select.
     *
     * @return the type display name
     */
    public String getType() {
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
     * @param runQuery if <tt>true</tt> run the query
     */
    protected void onSelect(Query<T> query, boolean runQuery) {
        if (runQuery) {
            query.setAuto(runQuery);
        }
        try {
            final Browser<T> browser = BrowserFactory.create(query);
            final BrowserDialog<T> popup = new BrowserDialog<T>(
                    type, browser, allowCreate);

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    setInSelect(false);
                    if (popup.createNew()) {
                        onCreate();
                    } else {
                        T object = popup.getSelected();
                        if (object != null) {
                            onSelected(object);
                        }
                    }
                }
            });

            setInSelect(true);
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
    protected BrowserDialog<T> createBrowserDialog(Browser<T> browser) {
        String title = Messages.get("imobject.select.title", type);
        return new BrowserDialog<T>(title, browser);
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        setObject(object);
        if (listener != null) {
            listener.selected(object);
        }
    }

    /**
     * Invoked to create a new object. Notifies the listener.
     */
    protected void onCreate() {
        if (listener != null) {
            listener.create();
        }
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query<T> createQuery() {
        String name = getText().getText();
        return createQuery(name);
    }

    /**
     * Creates a query to select objects.
     *
     * @param name a name to filter on. May be <tt>null</tt>
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Query<T> createQuery(String name) {
        Query<T> query = QueryFactory.create(
                shortNames, GlobalContext.getInstance());
        query.setValue(name);
        return query;
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a return a new browser
     */
    protected Browser<IMObject> createBrowser(Query<IMObject> query) {
        return BrowserFactory.create(query);
    }

    /**
     * Determines if a selection dialog has been popped up.
     *
     * @param select if <tt>true</tt> denotes that a selection dialog has
     *               been popped up
     */
    protected void setInSelect(boolean select) {
        this.inSelect = select;
    }

    /**
     * Invoked when the text field is updated.
     */
    private void onTextChanged() {
        TextField text = getText();
        String name = text.getText();
        if (!ObjectUtils.equals(name, prevText)) {
            if (StringUtils.isEmpty(name)) {
                setObject(null);
                notifySelected();
            } else {
                try {
                    Query<T> query = createQuery(name);
                    ResultSet<T> set = query.query(null);
                    if (set != null && set.hasNext()) {
                        IPage<T> page = set.next();
                        List<T> rows = page.getResults();
                        int size = rows.size();
                        if (size == 0) {
                            setObject(null);
                            notifySelected();
                        } else if (size == 1) {
                            T object = rows.get(0);
                            setObject(object);
                            notifySelected();
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

    /**
     * Notifies the listener of selection.
     */
    private void notifySelected() {
        if (listener != null) {
            listener.selected(getObject());
        }
    }

}
