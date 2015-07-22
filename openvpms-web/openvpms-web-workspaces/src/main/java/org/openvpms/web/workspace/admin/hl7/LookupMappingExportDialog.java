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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.LARGE_INSET;
import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;

/**
 * Exports a mapping between lookups.
 *
 * @author Tim Anderson
 */
public class LookupMappingExportDialog extends ModalDialog {

    private final SelectField mapFrom;
    private final SelectField mapTo;

    /**
     * Constructs an {@link LookupMappingExportDialog}.
     */
    public LookupMappingExportDialog(HelpContext help) {
        super(Messages.get("admin.hl7.mapping.export.title"), "MessageDialog", OK_CANCEL, help);

        List<String> shortNames = Collections.singletonList(PatientArchetypes.SPECIES);
        mapFrom = SelectFieldFactory.create(new ShortNameListModel(shortNames, false, true));
        mapFrom.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onFromChanged();
            }
        });
        mapTo = SelectFieldFactory.create(createModel(getMapFrom()));
        mapTo.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onToChanged();
            }
        });
        onChanged();
    }

    public String getMapFrom() {
        return (String) mapFrom.getSelectedItem();
    }

    public String getMapTo() {
        return (String) mapTo.getSelectedItem();
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        String from = getMapFrom();
        String to = getMapTo();
        if (from != null && to != null && !ObjectUtils.equals(from, to)) {
            super.onOK();
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create("admin.hl7.mapping.export.message", Styles.BOLD);
        Row from = RowFactory.create(CELL_SPACING, LabelFactory.create("admin.hl7.mapping.export.from"), mapFrom);
        Row to = RowFactory.create(CELL_SPACING, LabelFactory.create("admin.hl7.mapping.export.to"), mapTo);
        Row inset = RowFactory.create(LARGE_INSET,
                                      ColumnFactory.create(WIDE_CELL_SPACING, message,
                                                           RowFactory.create(WIDE_CELL_SPACING, from, to)));
        getLayout().add(inset);
    }

    private void onFromChanged() {
        String from = getMapFrom();
        mapTo.setModel(createModel(from));
        onChanged();
    }

    private ShortNameListModel createModel(String from) {
        ShortNameListModel model;
        if (from != null) {
            String[] mappings = DescriptorHelper.getNodeShortNames(from, "mapping");
            Set<String> archetypes = new HashSet<>();
            for (String shortName : mappings) {
                String[] target = DescriptorHelper.getNodeShortNames(shortName, "target");
                archetypes.addAll(Arrays.asList(target));
            }
            model = new ShortNameListModel(new ArrayList<>(archetypes), false, true);
        } else {
            model = new ShortNameListModel(new ArrayList<String>(), false, true);
        }
        return model;
    }

    private void onToChanged() {
        onChanged();
    }

    private void onChanged() {
        String from = getMapFrom();
        String to = getMapTo();
        boolean enabled = from != null && to != null && !ObjectUtils.equals(from, to);
        getButtons().setEnabled(OK_ID, enabled);
    }

}
