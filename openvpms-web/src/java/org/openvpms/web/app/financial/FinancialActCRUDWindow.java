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

package org.openvpms.web.app.financial;

import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.util.ButtonFactory;

import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;


/**
 * CRUD window for the financial workspace. This provides selection and event
 * notification for child acts and print support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialActCRUDWindow extends CRUDWindow {

    /**
     * The summary button.
     */
    private Button _summary;

    /**
     * Summary button identifier.
     */
    private static final String SUMMARY_ID = "summary";


    /**
     * Create a new <code>FinancialActCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public FinancialActCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    @Override
    protected void onPrint() {
        print(getObject());
    }

    /**
     * Invoked when the 'summary' button is pressed.
     */
    protected void onSummary() {
        print(getObject());
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     */
    @Override
    protected IMObjectViewer createViewer(IMObject object) {
        IMObjectLayoutStrategy strategy = new ActLayoutStrategy() {

            /**
             * Creates a component to represent the item node.
             *
             * @param object     the parent object
             * @param items      the items node descriptor
             * @param properties the properties
             * @param context    the layout context
             * @return a component to represent the items node
             */
            protected Component createItems(IMObject object,
                                            NodeDescriptor items,
                                            PropertySet properties,
                                            LayoutContext context) {
                Component component = super.createItems(object, items,
                                                        properties,
                                                        context);
                final PagedIMObjectTable paged = (PagedIMObjectTable) component;
                final IMObjectTable table = paged.getTable();
                table.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        IMObject selected = table.getSelected();
                        FinancialAct child = null;
                        if (selected instanceof ActRelationship) {
                            ActRelationship relationship
                                    = (ActRelationship) selected;
                            child = (FinancialAct) IMObjectHelper.getObject(
                                    relationship.getTarget());
                        }
                        onChildActSelected(child);
                    }
                });
                return paged;
            }
        };
        return new IMObjectViewer(object, strategy, null);
    }

    /**
     * Returns the summary button.
     *
     * @return the summary button
     */
    protected Button getSummaryButton() {
        if (_summary == null) {
            _summary = ButtonFactory.create(SUMMARY_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSummary();
                }
            });
        }
        return _summary;
    }

    /**
     * Invoked when a child act is selected/deselected.
     *
     * @param child the child act. May be <code>null</code>
     */
    protected void onChildActSelected(FinancialAct child) {
    }
}
