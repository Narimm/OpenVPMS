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

package org.openvpms.web.workspace.customer.account;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.echo.text.TextField;

import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;


/**
 * Confirmation dialog for reversing customer account transactions.
 *
 * @author Tim Anderson
 */
public class ReverseConfirmationDialog extends ConfirmationDialog {

    /**
     * Determines if the transactions should be hidden in the customer statement.
     */
    private final CheckBox hide;

    /**
     * The notes.
     */
    private Property notesProperty;

    /**
     * The reference.
     */
    private Property referenceProperty;


    /**
     * Constructs a {@link ReverseConfirmationDialog}.
     *
     * @param title     the window title
     * @param message   the message
     * @param help      the help context. May be {@code null}
     * @param notes     the default notes
     * @param reference the default reference
     * @param canHide   if {@code true}, allow the user to hide the reversed/reversal transaction in the customer
     *                  statement
     */
    public ReverseConfirmationDialog(String title, String message, HelpContext help, String notes, String reference,
                                     boolean canHide) {
        super(title, message, help);
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(CustomerAccountArchetypes.INVOICE);

        notesProperty = createProperty("notes", notes, archetype, 300);
        referenceProperty = createProperty("reference", reference, archetype, 20);
        hide = (canHide) ? CheckBoxFactory.create("customer.account.reverse.hide", true) : null;
    }

    /**
     * Returns the notes to add to the reversal
     *
     * @return the notes
     */
    public String getNotes() {
        return notesProperty.getString();
    }

    /**
     * Returns the reference.
     *
     * @return the reference
     */
    public String getReference() {
        return referenceProperty.getString();
    }

    /**
     * Determines if the transactions should be hidden in the statement.
     *
     * @return {@code true} if the transactions should be hidden
     */
    public boolean getHide() {
        return hide != null && hide.isSelected();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        TextArea notesField = BoundTextComponentFactory.createTextArea(notesProperty, 80, 5);
        notesField.setWidth(Styles.FULL_WIDTH);
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
            }
        };
        notesField.addActionListener(listener);
        TextField refField = BoundTextComponentFactory.create(referenceProperty, 20);
        refField.addActionListener(listener);

        Label message = LabelFactory.create(null, Styles.BOLD);
        message.setText(getMessage());
        ComponentState noteState = new ComponentState(notesField, notesProperty);
        ComponentState refState = new ComponentState(refField, referenceProperty);
        Column column = ColumnFactory.create(WIDE_CELL_SPACING, message,
                                             ColumnFactory.create(CELL_SPACING, noteState.getLabel(), notesField),
                                             ColumnFactory.create(CELL_SPACING, refState.getLabel(), refField));
        if (hide != null) {
            column.add(RowFactory.create(Styles.CELL_SPACING, hide));
        }
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
        FocusGroup group = getFocusGroup();
        group.add(notesField);
        group.add(refField);
        if (hide != null) {
            group.add(hide);
        }
        group.setFocus();
    }

    /**
     * Creates a property, using the display name and maxLength from the corresponding archetype descriptor
     *
     * @param name             the property name
     * @param value            the property value. May be {@code null}
     * @param archetype        the archetype descriptor
     * @param defaultMaxLength the default max length
     * @return a new property
     */
    private Property createProperty(String name, String value, ArchetypeDescriptor archetype, int defaultMaxLength) {
        NodeDescriptor noteDesc = archetype.getNodeDescriptor(name);
        int maxLength = defaultMaxLength;
        String displayName = null;
        if (noteDesc != null) {
            displayName = noteDesc.getDisplayName();
            maxLength = noteDesc.getMaxLength();
        }
        SimpleProperty property = new SimpleProperty(name, value, String.class, displayName, false);
        property.setMaxLength(maxLength);
        return property;
    }

}
