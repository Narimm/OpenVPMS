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

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.EntityObjectSetResultSet;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Editor for the supplier {@link IMObjectReference}s on <em>entityRelationship.productStockLocation</em>.
 * <p/>
 * This constraints suppliers to those on the related product.
 *
 * @author Tim Anderson
 */
class PreferredSupplierEditor extends AbstractIMObjectReferenceEditor<Party> {

    /**
     * The parent editor.
     */
    private final ProductStockLocationEditor editor;


    /**
     * Constructs a {@link PreferredSupplierEditor}.
     *
     * @param editor   the parent editor
     * @param property the product reference property
     * @param context  the layout context
     */
    public PreferredSupplierEditor(ProductStockLocationEditor editor, Property property, LayoutContext context) {
        super(property, editor.getParent(), new DefaultLayoutContext(context,
                                                                     context.getHelpContext().topic("product")));
        this.editor = editor;
    }

    /**
     * Determines if the reference is valid, logging a validation error if not.
     *
     * @param validator the validator
     * @return {@code true} if the reference is valid, otherwise {@code false}
     */
    @Override
    protected boolean isValidReference(IMObjectReference reference, Validator validator) {
        boolean result = isValidReference(reference);
        if (!result) {
            String product = editor.getProduct() != null ? editor.getProduct().getName() : "";
            String supplier = IMObjectHelper.getName(reference);
            String message = Messages.format("product.supplier.invalid", supplier, product);
            validator.add(this, new ValidatorError(getProperty(), message));
        }
        return result;
    }

    /**
     * Determines if a reference is valid.
     * <p/>
     * This implementation determines if the query returned by {#link #createQuery} selects the reference.
     *
     * @param reference the reference to check
     * @return {@code true} if the query selects the reference
     */
    @Override
    protected boolean isValidReference(IMObjectReference reference) {
        // restrict suppliers to those linked to the product
        Query<Party> query = createQuery(null, false);
        return query.selects(reference);
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Party> createQuery(String name) {
        return createQuery(name, true);
    }

    /**
     * Creates a query for suppliers constraining them to those linked to the product.
     *
     * @param name     the name to filter on. May be {@code null}
     * @param allowAll if {@code true}, and there are no product-suppliers, allow all product suppliers
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    private Query<Party> createQuery(String name, boolean allowAll) {
        Query<Party> result;
        final List<Long> suppliers = getSuppliers();
        String[] archetypeRange = getProperty().getArchetypeRange();
        if (!suppliers.isEmpty()) {
            EntityObjectSetQuery set = new EntityObjectSetQuery(archetypeRange) {
                @Override
                protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
                    return new EntityObjectSetResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), sort,
                                                        getMaxResults(), isDistinct()) {
                        @Override
                        protected ArchetypeQuery createQuery() {
                            ArchetypeQuery query = super.createQuery();
                            query.add(Constraints.in("id", suppliers.toArray(new Long[suppliers.size()])));
                            return query;
                        }
                    };
                }
            };
            // TODO - EntityQuery should be parameterised
            Query entities = new EntityQuery(set, getContext());
            entities.setAuto(true);
            result = entities;
        } else if (allowAll) {
            result = super.createQuery(name);
        } else {
            result = new ListQuery<Party>(Collections.<Party>emptyList(), archetypeRange, Party.class);
        }
        return result;
    }

    /**
     * Returns identifiers for the suppliers linked to the product.
     * <p/>
     * This uses the associated {@link ProductEditor}, if registered, as that may have uncommitted supplier
     * relationships.
     *
     * @return the suppliers
     */
    private List<Long> getSuppliers() {
        final List<Long> result = new ArrayList<Long>();
        List<IMObjectReference> suppliers = null;
        if (editor.getProductEditor() != null) {
            suppliers = editor.getProductEditor().getSuppliers();
        } else {
            Product product = editor.getProduct();
            if (product != null) {
                EntityBean bean = new EntityBean(product);
                suppliers = bean.getNodeTargetEntityRefs("suppliers");
            }
        }
        if (suppliers != null) {
            for (IMObjectReference supplier : suppliers) {
                result.add(supplier.getId());
            }
        }
        return result;
    }


}
