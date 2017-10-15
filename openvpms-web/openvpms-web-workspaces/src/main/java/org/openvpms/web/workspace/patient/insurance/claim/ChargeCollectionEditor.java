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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import net.sf.jasperreports.engine.util.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.ActCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMTableCollectionEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Editor for the collection of charges associated with a claim item.
 *
 * @author Tim Anderson
 * @see ClaimItemEditor
 */
public class ChargeCollectionEditor extends IMTableCollectionEditor<IMObject> {

    /**
     * Constructs an {@link IMTableCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param context  the layout context
     */
    public ChargeCollectionEditor(CollectionProperty property, Act object, LayoutContext context) {
        super(new ChargeRelationshipCollectionPropertyEditor(property, object), object, context);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        return ActCollectionResultSetFactory.INSTANCE.createTableModel(getCollectionPropertyEditor(),
                                                                       getObject(), getContext());
    }

    /**
     * Selects an object in the table.
     *
     * @param object the object to select
     */
    @Override
    protected void setSelected(IMObject object) {

    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be {@code null}
     */
    @Override
    protected IMObject getSelected() {
        return null;
    }

    /**
     * Selects the object prior to the selected object, if one is available.
     *
     * @return the prior object. May be {@code null}
     */
    @Override
    protected IMObject selectPrevious() {
        return null;
    }

    /**
     * Selects the object after the selected object, if one is available.
     *
     * @return the next object. May be {@code null}
     */
    @Override
    protected IMObject selectNext() {
        return null;
    }

    /**
     * Creates a new result set.
     *
     * @return a new result set
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ResultSet<IMObject> createResultSet() {
        List objects = getCollectionPropertyEditor().getObjects();
        return new IMObjectListResultSet<>(objects, ROWS);
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in
     * an editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    @Override
    protected IMObjectEditor onAdd() {
        LayoutContext layout = getContext();
        Context context = layout.getContext();
        String[] archetypes = {INVOICE};
        String[] statuses = {ActStatus.POSTED};
        Party customer = context.getCustomer();
        final Party patient = context.getPatient();
        DateRangeActQuery<Act> query = new DefaultActQuery<>(customer, "customer",
                                                             CustomerArchetypes.CUSTOMER_PARTICIPATION,
                                                             archetypes, statuses);
        query.setDistinct(true);
        Act parent = (Act) getObject();
        if (parent != null) {
            query.setFrom(parent.getActivityStartTime());
            query.setTo(parent.getActivityEndTime());
        }
        query.setConstraints(join("items").add(join("target").add(join("patient").add(eq("entity", patient)))));
        Browser<Act> browser = BrowserFactory.create(query, layout);
        String title = Messages.format("imobject.select.title", DescriptorHelper.getDisplayName(INVOICE));
        final BrowserDialog<Act> dialog = new BrowserDialog<>(title, browser, layout.getHelpContext());
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                Act invoice = dialog.getSelected();
                IMObjectReference patientRef = patient.getObjectReference();
                ActBean bean = new ActBean(invoice);
                for (Act item : bean.getNodeActs("items")) {
                    ActBean itemBean = new ActBean(item);
                    if (ObjectUtils.equals(patientRef, itemBean.getNodeParticipantRef("patient"))) {
                        add(item);
                    }
                }
                refresh();
            }
        });
        dialog.show();
        return null;
    }

    private static class ChargeRelationshipCollectionPropertyEditor extends ActRelationshipCollectionPropertyEditor {

        /**
         * Constructs an {@link ActRelationshipCollectionPropertyEditor}.
         *
         * @param property the property to edit
         * @param act      the parent act
         */
        public ChargeRelationshipCollectionPropertyEditor(CollectionProperty property, Act act) {
            super(property, act);
        }

        /**
         * Flags an object for removal when the collection is saved.
         *
         * @param object the object to remove
         * @return {@code true} if the object was removed
         */
        @Override
        protected boolean queueRemove(IMObject object) {
            return removeEdited(object);
        }
    }
}
