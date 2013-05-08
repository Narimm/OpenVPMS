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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.select;

import nextapp.echo2.app.Component;
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
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.TextComponentFactory;

import java.util.List;


/**
 * Multiple IMObject selector that provides query support for partial/incorrect names.
 *
 * @author Tim Anderson
 */
public class MultiIMObjectSelector<T extends IMObject> {

    /**
     * The selected objects.
     */
    private final SelectedObjects<T> objects = new SelectedObjects<T>();

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
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Update listener for the text field.
     */
    private final DocumentListener textListener;

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
     * Selected object's text. Null if the selector is not editable.
     */
    private TextField field;

    /**
     * Flag to indicate if the text field was updated. If so, then the action listener won't trigger a browser
     * dialog. Note that this is not 100% reliable as its not possible to determine if an action listener was invoked
     * after an update listener within the same web request.
     */
    private boolean onTextChangedInvoked;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;


    /**
     * Constructs a {@code MultiIMObjectSelector}.
     *
     * @param type       display name for the types of objects this may select
     * @param context    the layout context
     * @param shortNames the archetype short names to query
     */
    public MultiIMObjectSelector(String type, LayoutContext context, String... shortNames) {
        this(type, false, context, shortNames);
    }

    /**
     * Constructs a {@code MultiIMObjectSelector}.
     *
     * @param type        display name for the types of objects this may select
     * @param allowCreate determines if objects may be created
     * @param context     the layout context
     * @param shortNames  the archetype short names to query
     */
    public MultiIMObjectSelector(String type, boolean allowCreate, LayoutContext context, String... shortNames) {
        this.type = type;
        this.context = context;
        this.shortNames = shortNames;
        this.allowCreate = allowCreate;
        focusGroup = new FocusGroup(getClass().getSimpleName());
        TextField text = getTextField();
        focusGroup.add(text);
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
     * Sets the current objects.
     *
     * @param objects the objects. May be empty
     */
    public void setObjects(List<T> objects) {
        this.objects.setObjects(objects);
        refresh();
        prevText = field.getText();
    }

    /**
     * Returns the objects.
     *
     * @return the objects
     */
    public List<T> getObjects() {
        return objects.getObjects();
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
     * Returns the display name for the types of objects this may select.
     *
     * @return the type display name
     */
    public String getType() {
        return type;
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
        objects.parseNames(getText());
        return !inSelect() && objects.isValid();
    }

    /**
     * Determines if a selection dialog has been popped up.
     *
     * @return {@code true} if a selection dialog has been popped up otherwise {@code false}
     */
    public boolean inSelect() {
        return inSelect;
    }

    /**
     * Returns the selector component.
     *
     * @return the selector component
     */
    public Component getComponent() {
        return getTextField();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Returns the text field.
     *
     * @return the text field
     */
    public TextField getTextField() {
        if (field == null) {
            field = TextComponentFactory.create();
        }
        return field;
    }

    /**
     * Returns the text from the editable text field.
     *
     * @return the text, or {@code null} if there is no text, or this is not an editable selector.
     */
    public String getText() {
        return getTextField().getText();
    }

    /**
     * Returns the first name for which there is no corresponding object.
     *
     * @return the first name, or {@code null} if none exists
     */
    public String getFirstNotFound() {
        return objects.getFirstNotFound();
    }

    /**
     * Refreshes the field.
     */
    protected void refresh() {
        field.getDocument().removeDocumentListener(textListener);
        field.setText(objects.getText());
        field.getDocument().addDocumentListener(textListener);

        prevText = field.getText();
    }

    /**
     * Pops up a dialog to select an object.
     * <p/>
     * Only pops up a dialog if one isn't already visible.
     */
    protected void onSelect() {
        if (!inSelect) {
            int index = objects.size();
            for (int i = objects.size() - 1; i >= 0; i--) {
                if (!objects.haveMatch(i)) {
                    index = i;
                    break;
                }
            }
            onSelect(createQuery(null), false, index);
        }

    }

    /**
     * Pop up a dialog to select an object.
     *
     * @param query    the query
     * @param runQuery if {@code true} run the query
     * @param index    the position to locate the selected object
     */
    protected void onSelect(Query<T> query, boolean runQuery, final int index) {
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
                            onSelected(object, browser, index);
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
     * @param index   the position of the selected object
     */
    protected void onSelected(T object, Browser<T> browser, int index) {
        setObject(index, object);
        getFocusGroup().setFocus(); // set the focus back to the component
        if (listener != null) {
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
     * @param value a value to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    protected Query<T> createQuery(String value) {
        Query<T> query = QueryFactory.create(shortNames, context.getContext());
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
     * Invoked when the text field is updated.
     */
    private void onTextChanged() {
        onTextChangedInvoked = true;
        String text = getText();
        if (!ObjectUtils.equals(text, prevText)) {
            if (StringUtils.isEmpty(text)) {
                clear();
            } else {
                objects.parseNames(getText());
                List<String> names = objects.getNames();
                for (int i = 0; i < names.size(); ++i) {
                    String name = names.get(i);
                    if (!objects.haveMatch(i)) {
                        if (!query(name, i)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked when the field is cleared.
     * <p/>
     * Clears the internal state and notifies any registered listener.
     */
    private void clear() {
        objects.clear();
        prevText = null;
        notifySelected(null);
    }

    /**
     * Queries the supplied text.
     * <p/>
     * If there is a single match, the selected object is updated at the specified index.
     * If there are no matches, or multiple matches, then a browser is displayed.
     *
     * @param text  the text to query
     * @param index the index to store the selection
     * @return {@code true} if querying is complete, {@code false} if a browser was displayed
     */
    private boolean query(String text, int index) {
        boolean result = true;
        try {
            Query<T> query = createQuery(text);
            ResultSet<T> set = query.query(null);
            if (set != null) {
                T selected = null;
                if (set.hasNext()) {
                    IPage<T> page = set.next();
                    List<T> rows = page.getResults();
                    if (rows.size() == 1) {
                        // exact match
                        selected = rows.get(0);
                    }
                }
                if (selected != null) {
                    setObject(index, selected);
                    notifySelected(selected);
                } else {
                    onSelect(query, true, index);
                    result = false;
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            listener.selected(null);
        }
        return result;
    }

    /**
     * Invoked by the action listener associated with the text field.
     * <p/>
     * This is provided to handle Enter being pressed in the field to display a search dialog.
     * <p/>
     * Note:
     * <ul>
     * <li>that {@link #onTextChanged} will have been invoked just prior to this method if the text was updated.</li>
     * <li>in {@link #onTextChanged()} was invoked without enter being pressed, then enter must be pressed
     * <strong>twice</<strong> in order for the dialog to be displayed</li>
     * </ul>
     */
    private void onTextAction() {
        if (!onTextChangedInvoked) {
            onSelect();
        } else {
            onTextChangedInvoked = false;
        }
    }

    /**
     * Notifies the listener of selection via the text field.
     *
     * @param object the selected object. May be {@code null}
     */
    private void notifySelected(T object) {
        if (listener != null) {
            listener.selected(object);
        }
    }

    /**
     * Sets the object at the specified index and refreshes the field.
     *
     * @param index  the index
     * @param object the object
     */
    private void setObject(int index, T object) {
        objects.setObject(index, object);
        refresh();
    }

}
