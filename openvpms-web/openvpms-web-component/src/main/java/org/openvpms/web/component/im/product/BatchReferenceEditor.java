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

package org.openvpms.web.component.im.product;

import echopointng.DropDown;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.bound.BoundTextField;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;
import java.util.List;


/**
 * Editor for product batch {@link IMObjectReference}s.
 *
 * @author Tim Anderson
 */
class BatchReferenceEditor extends AbstractPropertyEditor implements IMObjectReferenceEditor<Entity> {

    /**
     * If {@code true}, sets a default batch if none is present.
     */
    private final boolean setDefault;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * The product.
     */
    private Product product;

    /**
     * The current batch.
     */
    private Entity batch;

    /**
     * The earliest expiry date.
     */
    private Date expiryDate;

    /**
     * The text input property.
     */
    private final SimpleProperty input;

    /**
     * The batch drop down component.
     */
    private DropDown dropDown;

    /**
     * Input listener.
     */
    private final ModifiableListener listener;


    /**
     * Constructs a {@link BatchReferenceEditor}.
     *
     * @param property   the product reference property
     * @param setDefault if {@code true}, set a default batch if none is present
     * @param context    the layout context
     */
    public BatchReferenceEditor(Property property, boolean setDefault, LayoutContext context) {
        super(property);
        this.setDefault = setDefault;
        this.context = context;
        focusGroup = new FocusGroup(getClass().getSimpleName());
        input = new SimpleProperty(property.getName(), null, String.class, property.getDisplayName());
        input.setDescription(property.getDescription());
        BoundTextField inputField = new BoundTextField(input);
        inputField.setStyleName("Selector");
        dropDown = new DropDown();
        dropDown.setTarget(inputField);
        dropDown.setPopUpAlwaysOnTop(true);
        dropDown.setFocusOnExpand(true);
        batch = getObject();
        listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                updateBatches(true);
            }
        };
        updateText();
    }

    /**
     * Updates the field with the current batch.
     */
    private void updateText() {
        try {
            input.removeModifiableListener(listener);
            if (batch != null) {
                input.setValue(batch.getName());
            } else {
                input.setValue(null);
            }
        } finally {
            input.addModifiableListener(listener);
        }
    }

    /**
     * Sets the product, used to select the batch.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        if (batch != null && product != null && !hasRelationship(batch, product)) {
            setObject(null);
        }
        this.product = product;
        updateBatches(false);
    }

    /**
     * Sets the earliest batch expiry date.
     *
     * @param date The expiry date. May be {@code null}
     */
    public void setExpireAfter(Date date) {
        this.expiryDate = date;
        updateBatches(false);
    }


    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public Component getComponent() {
        return dropDown;
    }

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object. May be {@code null}
     * @return {@code true} if the value was set, {@code false} if it cannot be set due to error, or is the same as
     *         the existing value
     */
    @Override
    public boolean setObject(Entity object) {
        IMObjectReference ref = (object != null) ? object.getObjectReference() : null;
        batch = object;
        updateText();
        return getProperty().setValue(ref);
    }

    /**
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return {@code true} if the reference is null; otherwise {@code false}
     */
    @Override
    public boolean isNull() {
        return getProperty().getValue() == null && StringUtils.isEmpty(input.getString());
    }

    /**
     * Determines if objects may be created.
     *
     * @param create if {@code true}, objects may be created
     */
    @Override
    public void setAllowCreate(boolean create) {
        // no-op
    }

    /**
     * Determines if objects may be created.
     *
     * @return {@code false}
     */
    @Override
    public boolean allowCreate() {
        return false;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && isValidReference(validator);
    }

    /**
     * Determines if the reference is valid.
     *
     * @param validator the validator
     * @return {@code true} if the reference is valid, otherwise {@code false}
     */
    protected boolean isValidReference(Validator validator) {
        boolean result = true;
        if (batch != null && product != null) {
            if (!hasRelationship(batch, product)) {
                result = false;
                ArchetypeId archetypeId = batch.getArchetypeId();
                String displayName = DescriptorHelper.getDisplayName(archetypeId.getShortName());
                String message = Messages.format("imobject.invalidreference", displayName);
                validator.add(this, new ValidatorError(getProperty(), message));
            }
        }
        return result;
    }

    /**
     * Returns the object corresponding to the reference.
     *
     * @return the object, or {@code null} if the reference is {@code null} or the object no longer exists
     */
    protected Entity getObject() {
        Property property = getProperty();
        IMObjectReference reference = (IMObjectReference) property.getValue();
        Entity object = null;
        if (reference != null) {
            object = (Entity) IMObjectHelper.getObject(reference, property.getArchetypeRange(), context.getContext());
        }
        return object;
    }

    /**
     * Updates the product batches.
     * <p/>
     * If there are multiple batches associated with the product, renders a drop down containing them beside the text
     * field.
     *
     * @param showPopup if {@code true} display the popup if there is more than one match
     */
    private void updateBatches(boolean showPopup) {
        PagedIMTable<Entity> table = null;
        if (product != null && expiryDate != null) {
            boolean useDefault = showPopup || setDefault;
            String batchNumber = input.getString();
            if (batch != null && batch.getName().equals(batchNumber)) {
                // already have a batch, so list all batches
                batchNumber = null;
            } else if (batch != null && batchNumber == null) {
                // input text has been cleared. Clear the existing reference
                useDefault = false;
                setObject(null);
            } else if (batchNumber != null && !batchNumber.endsWith("*")) {
                batchNumber = batchNumber + "*";
            }
            ProductBatchResultSet set = new ProductBatchResultSet(batchNumber, product, expiryDate, 5);
            if (set.hasNext()) {
                IPage<Entity> next = set.next();
                List<Entity> results = next.getResults();
                if (results.size() >= 1) {
                    set.previous(); // reset() will shed caches
                    table = createTable(set);
                    if (useDefault && (batch == null || results.size() == 1)) {
                        setObject(results.get(0));
                        showPopup = false;
                    }
                }
            } else if (!StringUtils.isEmpty(batchNumber)) {
                // no match on the input batch number, so create a dropdown of all batches for the product and expiry
                set = new ProductBatchResultSet(null, product, expiryDate, 5);
                if (set.hasNext()) {
                    table = createTable(set);
                }
            }
        }
        if (table != null) {
            table.getTable().setSelected(batch);
            Column newValue = ColumnFactory.create(Styles.INSET, table);
            newValue.setBackground(Color.LIGHTGRAY);
            dropDown.setPopUp(newValue);
            dropDown.setFocusComponent(table);
        } else {
            Label component = LabelFactory.create("imobject.none");
            Column newValue = ColumnFactory.create(Styles.INSET, component);
            newValue.setBackground(Color.LIGHTGRAY);
            dropDown.setPopUp(newValue);
            dropDown.setFocusComponent(null);
        }
        if (showPopup) {
            dropDown.setExpanded(true);
        }
    }

    /**
     * Creates a table of batches.
     *
     * @param set the batch result set
     * @return a new table
     */
    private PagedIMTable<Entity> createTable(ProductBatchResultSet set) {
        ProductBatchTableModel model = new ProductBatchTableModel(context, false, false);
        final PagedIMTable<Entity> table = new PagedIMTable<Entity>(model, set);
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setObject(table.getTable().getSelected());
                dropDown.setExpanded(false);
            }
        });
        return table;
    }

    /**
     * Determines if a batch has a relationship to a product.
     *
     * @param batch   the batch
     * @param product the product
     * @return {@code true} if the batch has a relationship to the product
     */
    private boolean hasRelationship(Entity batch, Product product) {
        EntityBean bean = new EntityBean(batch);
        return ObjectUtils.equals(bean.getNodeTargetObjectRef("product"), product.getObjectReference());
    }

}
