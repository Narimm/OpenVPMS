/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.DefaultDescriptorTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Customer estimate viewer.
 *
 * @author benjamincharlton
 */
public class EstimateViewer extends PopupDialog {

    /**
     * The alerts to display.
     */
    private final List<Act> estimates;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The alerts table.
     */
    private PagedIMTable<Act> table;

    /**
     * The column containing the alerts table and optional viewer.
     */
    private Column column;

    /**
     * The estimate viewer.
     */
    private Component viewer;

    /**
     * Constructs an {@link EstimateViewer}.
     *
     * @param estimates the estimates
     * @param context   the context
     * @param help      the help context
     */
    public EstimateViewer(List<Act> estimates, Context context, HelpContext help) {
        super(Messages.get("estimates.title"), "EstimatesViewer", CLOSE, help);
        this.context = context;
        this.estimates = estimates;
        setModal(true);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        getLayout().add(getComponent());
    }

    /**
     * Renders the component.
     *
     * @return the component
     */
    private Component getComponent() {
        ResultSet<Act> set = new ListResultSet<Act>(estimates, 20);
        IMTableModel<Act> model = new DefaultDescriptorTableModel<Act>(
                EstimateArchetypes.ESTIMATE, createLayoutContext(context, getHelpContext()));
        table = new PagedIMTable<Act>(model, set);

        table.getTable().setStyleName("EstimatesTableViewer");
        // this style disables the selection blur style used in other tables, as it hides white text

        column = ColumnFactory.create(Styles.CELL_SPACING, ColumnFactory.create(Styles.INSET, table));

        if (estimates.size() == 1) {
            show(estimates.get(0));
        } else {
            table.getTable().addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    showSelected();
                }
            });
        }
        return column;
    }

    /**
     * Displays the selected estimate.
     */
    private void showSelected() {
        Act estimate = table.getSelected();
        show(estimate);
    }

    /**
     * Shows an estimate.
     *
     * @param estimate the alert to show. May be {@code null}
     */
    private void show(Act estimate) {
        if (estimate != null) {
            if (viewer != null) {
                column.remove(viewer);
            }
            DefaultLayoutContext layout = new DefaultLayoutContext(context, getHelpContext());
            viewer = new IMObjectViewer(estimate, layout).getComponent();
            column.add(viewer);
        }
    }

    private LayoutContext createLayoutContext(Context context, HelpContext help) {
        LayoutContext result = new DefaultLayoutContext(context, help);
        result.setEdit(true); // hack to disable hyperlinks
        TableComponentFactory factory = new TableComponentFactory(result);
        result.setComponentFactory(factory);
        return result;
    }

}
