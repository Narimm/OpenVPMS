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

package org.openvpms.web.component.im.edit.act;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.ProductSupplier;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import static org.openvpms.component.business.service.archetype.helper.EntityBean.RefEquals;
import org.openvpms.component.business.service.archetype.helper.NodePredicate;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.app.product.ProductSupplierTableModel;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.ProductQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.CollectionHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Participation editor for products.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ProductParticipationEditor
        extends AbstractParticipationEditor<Product> {

    /**
     * The patient, used to constrain searches to a particular species. May be
     * <tt>null</tt>.
     */
    private Property patient;

    /**
     * The current supplier.
     */
    private Party supplier;

    /**
     * The product supplier relationship.
     */
    private ProductSupplier productSupplier;

    /**
     * The stock location, used to constrain searches to a particular location.
     * May be <tt>null</tt>.
     */
    private Party location;


    /**
     * Constructs a new <tt>ProductParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <tt>null</tt>
     */
    public ProductParticipationEditor(Participation participation,
                                      Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.product")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the patient, used to constrain product searches to a set of
     * species.
     *
     * @param patient the patient. May be <tt>null</tt>
     */
    public void setPatient(Property patient) {
        this.patient = patient;
    }

    /**
     * Sets the product supplier. This is used to determine if the selected
     * product's supplier differs to
     */
    public void setSupplier(Party supplier) {
        this.supplier = supplier;
        productSupplier = null;
    }

    /**
     * Returns the product supplier.
     *
     * @return the product supplier. May be <tt>null</tt>
     */
    public Party getSupplier() {
        return supplier;
    }

    /**
     * Sets the stock location. If set, only those products that have
     * an relationship with the location, or no stock relationships at all
     * will be returned.
     *
     * @param location the stock location. May be <tt>null</tt>
     */
    public void setStockLocation(Party location) {
        this.location = location;
    }

    /**
     * The <em>entityRelationship.productSupplier</em> relationship
     * associated with the product. Only populated when the user selects
     * the product.
     *
     * @return the product supplier relationship. May be <tt>null</tt>
     */
    public ProductSupplier getProductSupplier() {
        return productSupplier;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Product> createObjectReferenceEditor(
            Property property) {
        return new ProductReferenceEditor(property);
    }

    /**
     * Editor for product {@link IMObjectReference}s.
     */
    private class ProductReferenceEditor
            extends AbstractIMObjectReferenceEditor<Product> {

        public ProductReferenceEditor(Property property) {
            super(property, getParent(), getLayoutContext());
        }

        /**
         * Invoked when an object is selected.
         *
         * @param product the selected object. May be <tt>null</tt>
         */
        @Override
        protected void onSelected(Product product) {
            if (product != null && supplier != null) {
                checkSupplier(product);
            } else {
                setProduct(product, null);
            }
        }

        /**
         * Creates a query to select objects.
         *
         * @param name a name to filter on. May be <tt>null</tt>
         * @param name the name to filter on. May be <tt>null</tt>
         * @return a new query
         * @throws ArchetypeQueryException if the short names don't match any
         *                                 archetypes
         */
        @Override
        protected Query<Product> createQuery(String name) {
            Query<Product> query = super.createQuery(name);
            return getQuery(query);
        }

        /**
         * Creates a query to select objects.
         *
         * @return a new query
         */
        protected Query<Product> getQuery(Query<Product> query) {
            if (query instanceof ProductQuery) {
                ProductQuery productQuery = ((ProductQuery) query);
                if (patient != null) {
                    IMObjectReference ref
                            = (IMObjectReference) patient.getValue();
                    if (ref != null) {
                        IMObject patient = IMObjectHelper.getObject(ref);
                        if (patient != null) {
                            String species = (String) IMObjectHelper.getValue(
                                    patient, "species");
                            if (species != null) {
                                productQuery.setSpecies(species);
                            }
                        }
                    }
                }
                if (location != null) {
                    productQuery.setStockLocation(location);
                }
            }
            return query;
        }

        private void setProduct(Product product,
                                EntityRelationship relationship) {
            if (relationship != null) {
                productSupplier = new ProductSupplier(relationship);
            } else {
                productSupplier = null;
            }
            setObject(product);
        }

        private void checkSupplier(final Product product) {
            OrderRules rules = new OrderRules();
            Entity otherSupplier;

            if (!rules.isSuppliedBy(supplier, product)
                    && (otherSupplier = getSupplier(product)) != null) {
                String title = Messages.get("product.othersupplier.title");
                String message = Messages.get("product.othersupplier.message",
                                              product.getName(),
                                              otherSupplier.getName());
                final ConfirmationDialog dialog
                        = new ConfirmationDialog(title, message);
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent event) {
                        if (ConfirmationDialog.OK_ID.equals(
                                dialog.getAction())) {
                            checkProductSupplierRelationships(product);
                        } else {
                            // cancel the update
                            setProduct(null, null);
                        }
                    }
                });
                dialog.show();
            } else {
                checkProductSupplierRelationships(product);
            }
        }

        /**
         * Returns the first active supplier for a product.
         *
         * @param product the product
         * @return the supplier, or <tt>null</tt> if none is found
         */
        private Entity getSupplier(Product product) {
            EntityBean bean = new EntityBean(product);
            return bean.getNodeTargetEntity("suppliers", EntityBean.ACTIVE);
        }

        private void checkProductSupplierRelationships(final Product product) {
            EntityBean bean = new EntityBean(product);
            Predicate predicate = new AndPredicate(
                    EntityBean.ACTIVE, RefEquals.getTargetEquals(supplier));
            List<EntityRelationship> relationships
                    = bean.getNodeRelationships("suppliers", predicate);
            if (relationships.isEmpty()) {
                setProduct(product, null);
            } else if (relationships.size() == 1) {
                setProduct(product, relationships.get(0));
            } else {
                Predicate preferred = new NodePredicate("preferred", true);
                EntityRelationship defaultRel = CollectionHelper.find(
                        relationships, preferred);
                Query<EntityRelationship> query
                        = new ListQuery<EntityRelationship>(
                        relationships, "entityRelationship.productSupplier",
                        EntityRelationship.class);
                String title = Messages.get("product.supplier.type");
                final Browser<EntityRelationship> browser
                        = new ProductSupplierBrowser(query);
                final BrowserDialog<EntityRelationship> dialog
                        = new BrowserDialog<EntityRelationship>(title, browser);
                browser.setSelected(defaultRel);

                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent event) {
                        EntityRelationship selected = browser.getSelected();
                        if (selected != null) {
                            setProduct(product, selected);
                        } else {
                            // cancel the update
                            setProduct(null, null);
                        }
                    }
                });
                browser.query();
                dialog.show();
            }
        }
    }

    private static class ProductSupplierBrowser
            extends TableBrowser<EntityRelationship> {

        /**
         * Construct a new <code>TableBrowser</code> that queries objects using
         * the specified query, displaying them in the table.
         *
         * @param query the query
         */
        public ProductSupplierBrowser(Query<EntityRelationship> query) {
            super(query, null,
                  new ProductSupplierTableModel(query.getShortNames(), null,
                                                true));
        }

        /**
         * Lay out this component.
         */
        @Override
        protected void doLayout() {
            setComponent(ColumnFactory.create());
        }

    }
}
