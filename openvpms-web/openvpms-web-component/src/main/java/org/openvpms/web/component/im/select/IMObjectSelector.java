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
 */

package org.openvpms.web.component.im.select;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.DocumentListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.List;


/**
 * Selector that provides query support for partial/incorrect names.
 *
 * @author Tim Anderson
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
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The listener. May be {@code null}
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
     * Constructs an {@code IMObjectSelector}.
     *
     * @param property the property
     * @param context  the layout context
     */
    public IMObjectSelector(Property property, LayoutContext context) {
        this(property, false, context);
    }

    /**
     * Constructs an {@code IMObjectSelector}.
     *
     * @param property    the property
     * @param allowCreate determines if objects may be created
     * @param context     the layout context
     */
    public IMObjectSelector(Property property, boolean allowCreate, LayoutContext context) {
        this(property.getDisplayName(), allowCreate, context);
    }

    /**
     * Constructs an {@code IMObjectSelector}.
     *
     * @param type       display name for the types of objects this may select
     * @param context    the layout context
     * @param shortNames the archetype short names to query
     */
    public IMObjectSelector(String type, LayoutContext context, String... shortNames) {
        this(type, false, context, shortNames);
    }

    /**
     * Constructs an {@code IMObjectSelector}.
     *
     * @param type        display name for the types of objects this may select
     * @param allowCreate determines if objects may be created
     * @param context     the layout context
     * @param shortNames  the archetype short names to query
     */
    public IMObjectSelector(String type, boolean allowCreate, LayoutContext context, String... shortNames) {
        this(type, allowCreate, ButtonStyle.RIGHT, context, shortNames);
    }

    /**
     * Constructs an {@code IMObjectSelector}.
     *
     * @param type        display name for the types of objects this may select
     * @param allowCreate determines if objects may be created
     * @param style       the button style
     * @param context     the layout context
     * @param shortNames  the archetype short names to query
     */
    public IMObjectSelector(String type, boolean allowCreate, ButtonStyle style, LayoutContext context,
                            String... shortNames) {
        super(style, true);
        setFormat(Format.NAME);
        this.type = type;
        this.context = context;
        this.shortNames = shortNames;
        this.allowCreate = allowCreate;
        getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });

        TextField text = getTextField();
        textListener = new DocumentListener() {
            public void onUpdate(DocumentEvent event) {
                onTextChanged();
            }
        };

        text.getDocument().addDocumentListener(textListener);

        // Register an action listener for Enter
        text.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onTextAction();
            }
        });
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(T object) {
        this.object = object;
        TextField text = getTextField();
        text.getDocument().removeDocumentListener(textListener);
        super.setObject(object);
        text.getDocument().addDocumentListener(textListener);
        prevText = text.getText();
    }

    /**
     * Returns the current object.
     *
     * @return the current object. May be {@code null}
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the listener.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(IMObjectSelectorListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Determines if the selector is valid.
     * It is valid if no dialog is currently displayed and:
     * <ul>
     * <li>an object has been selected and the entered text is the same as its name
     * <li>no object is present and no text is input
     * </ul>
     *
     * @return {@code true} if the selector is valid, otherwise {@code false}
     */
    public boolean isValid() {
        boolean valid = !inSelect;
        if (valid) {
            String text = getText();
            if (object != null) {
                valid = ObjectUtils.equals(object.getName(), text);
            } else {
                valid = StringUtils.isEmpty(text);
            }
        }
        return valid;
    }

    /**
     * Determines if a selection dialog has been popped up.
     *
     * @return {@code true} if a selection dialog has been popped up
     *         otherwise {@code false}
     */
    public boolean inSelect() {
        return inSelect;
    }

    /**
     * Determines if objects may be created.
     *
     * @param create if {@code true}, objects may be created
     */
    public void setAllowCreate(boolean create) {
        allowCreate = create;
    }

    /**
     * Determines if objects may be created.
     *
     * @return {@code true} if objects may be created
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
     * <p/>
     * Only pops up a dialog if one isn't already visible.
     */
    protected void onSelect() {
        if (!inSelect) {
            onSelect(createQuery(), false);
        }
    }

    /**
     * Pop up a dialog to select an object.
     *
     * @param query    the query
     * @param runQuery if {@code true} run the query
     */
    protected void onSelect(Query<T> query, boolean runQuery) {
        if (runQuery) {
            query.setAuto(runQuery);
        }
        try {
            final FocusCommand focus = new FocusCommand();
            final Browser<T> browser = BrowserFactory.create(query, context);
            final BrowserDialog<T> popup = new BrowserDialog<T>(type, browser, allowCreate, context.getHelpContext());

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    focus.restore();
                    setInSelect(false);
                    if (popup.createNew()) {
                        onCreate();
                    } else {
                        T object = popup.getSelected();
                        if (object != null) {
                            onSelected(object, browser);
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
     * Invoked when an object is selected via a browser.
     *
     * @param object  the selected object
     * @param browser the browser
     */
    protected void onSelected(T object, Browser<T> browser) {
        T current = getObject();
        setObject(object);
        getFocusGroup().setFocus(); // set the focus back to the component
        if (listener != null && !ObjectUtils.equals(current, object)) {
            listener.selected(object, browser);
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
        String value = getText();
        return createQuery(value);
    }

    /**
     * Creates a query to select objects.
     *
     * @param value a value to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    protected Query<T> createQuery(String value) {
        Query<T> query = QueryFactory.create(shortNames, getContext().getContext());
        query.setValue(value);
        return query;
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a return a new browser
     */
    protected Browser<IMObject> createBrowser(Query<IMObject> query) {
        return BrowserFactory.create(query, context);
    }

    /**
     * Determines if a selection dialog has been popped up.
     *
     * @param select if {@code true} denotes that a selection dialog has
     *               been popped up
     */
    protected void setInSelect(boolean select) {
        this.inSelect = select;
    }

    /**
     * Creates the select button.
     *
     * @param buttonId the button identifier. Ignored for this implementation
     * @return the select button
     */
    protected Button createSelectButton(String buttonId) {
        return ButtonFactory.create(null, "select");
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return context;
    }

    /**
     * Invoked when the text field is updated.
     */
    private void onTextChanged() {
        String text = getText();
        if (!ObjectUtils.equals(text, prevText)) {
            if (StringUtils.isEmpty(text)) {
                setObject(null);
                notifySelected();
            } else {
                try {
                    Query<T> query = createQuery(text);
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
     * Invoked by the action listener associated with the text field.
     * <p/>
     * This is provided to handle Enter being pressed in the field when it is empty, to display a search dialog.
     * <p/>
     * Note that {@link #onTextChanged} will have been invoked just prior to this method if the text was updated.
     */
    private void onTextAction() {
        if (!isValid() || StringUtils.isEmpty(getText())) {
            onSelect();
        }
    }

    /**
     * Notifies the listener of selection via the text field.
     */
    private void notifySelected() {
        if (listener != null) {
            listener.selected(getObject());
        }
    }

}
