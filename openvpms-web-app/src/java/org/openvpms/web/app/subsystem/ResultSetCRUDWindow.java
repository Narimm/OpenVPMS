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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.edit.EditResultSetDialog;
import org.openvpms.web.component.im.query.AbstractArchetypeQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.ViewResultSetDialog;
import org.openvpms.web.component.subsystem.AbstractCRUDWindow;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.i18n.Messages;


/**
 * A {@code CRUDWindow} that supports iteration over a {@link ResultSet}.
 *
 * @author Tim Anderson
 */
public class ResultSetCRUDWindow<T extends IMObject> extends AbstractCRUDWindow<T> {

    /**
     * The view button identifier.
     */
    protected static final String VIEW_ID = "view";

    /**
     * A result set to iterate over.
     */
    private ResultSet<T> set;

    /**
     * The query.
     */
    private Query<T> query;


    /**
     * Constructs a {@code ResultSetCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public ResultSetCRUDWindow(Archetypes<T> archetypes, Query<T> query, ResultSet<T> set, Context context,
                               HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<T>getInstance(), context, help);
        setResultSet(set);
        setQuery(query);
    }

    /**
     * Sets the query.
     * <p/>
     * This should only be used to access the query parameters. The result set will be passed via {@link #setResultSet}.
     *
     * @param query the query
     */
    public void setQuery(Query<T> query) {
        this.query = query;
    }

    /**
     * Sets the result set.
     *
     * @param set the result set
     */
    public void setResultSet(ResultSet<T> set) {
        this.set = set;
        boolean enable = getObject() != null && set != null;
        createViewButton().setEnabled(enable);
    }

    /**
     * Returns the result set.
     *
     * @return the result set. May be {@code null}
     */
    public ResultSet<T> getResultSet() {
        return set;
    }

    /**
     * Views the selected object.
     */
    public void view() {
        final T object = getObject();
        if (object != null) {
            final FocusCommand focus = new FocusCommand();
            String title = DescriptorHelper.getDisplayName(object);
            boolean edit = canEdit();
            final ViewResultSetDialog<T> dialog = new ViewResultSetDialog<T>(title, object, set, edit,
                                                                             getContext(), getHelpContext()) {
                @Override
                protected void view(T object) {
                    setTitle(DescriptorHelper.getDisplayName(object));
                    super.view(object);
                }
            };
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    focus.restore();
                    super.onAction(dialog);
                }

                @Override
                public void onAction(String action) {
                    if (ViewResultSetDialog.EDIT_ID.equals(action)) {
                        T selected = dialog.getSelected();
                        if (selected != null) {
                            edit(selected);
                        }
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Invoked when the 'new' button is pressed.
     * <p/>
     * This implementation specifies as the default archetype the one selected by the query, if present.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void onCreate(Archetypes<T> archetypes) {
        if (query != null && query instanceof AbstractArchetypeQuery) {
            String selected = ((AbstractArchetypeQuery) query).getShortName();
            if (selected != null) {
                archetypes = Archetypes.create(archetypes.getShortNames(), archetypes.getType(), selected,
                                               archetypes.getDisplayName());
            }
        }
        super.onCreate(archetypes);
    }

    /**
     * Edits an object.
     *
     * @param object the object to edit
     */
    @Override
    protected void edit(final T object) {
        if (object.isNew()) {
            super.edit(object);
        } else {
            final FocusCommand focus = new FocusCommand();
            String title = Messages.get("editor.edit.title", getArchetypes().getDisplayName());
            EditResultSetDialog<T> dialog = createEditResultSetDialog(object, title);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    focus.restore();
                    super.onAction(dialog);
                    onRefresh(object);
                }
            });
            dialog.show();
        }
    }

    /**
     * Creates a new result set dialog for editing.
     *
     * @param object the first object to edit
     * @param title  the dialog title
     * @return a new dialog
     */
    protected EditResultSetDialog<T> createEditResultSetDialog(T object, String title) {
        return new EditResultSetDialog<T>(title, object, set, getContext(), getHelpContext());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createViewButton(), 1); // add after new, before edit
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean enableViewEdit = enable && set != null;
        buttons.setEnabled(VIEW_ID, enableViewEdit);
        buttons.setEnabled(EDIT_ID, enableViewEdit);
        buttons.setEnabled(DELETE_ID, enable);
    }

    /**
     * Helper to create a new button with id {@link #VIEW_ID} linked to {@link #view}.
     *
     * @return a new view button
     */
    protected Button createViewButton() {
        return ButtonFactory.create(VIEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                view();
            }
        });
    }
}
