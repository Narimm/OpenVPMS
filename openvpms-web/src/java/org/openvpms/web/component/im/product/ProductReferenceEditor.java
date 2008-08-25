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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.NodeEquals;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.CollectionHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Editor for product {@link IMObjectReference}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-03-19 10:19:58 +1100 (Wed, 19 Mar 2008) $
 */
class ProductReferenceEditor
        extends AbstractIMObjectReferenceEditor<Product> {

    /**
     * The parent editor.
     */
    private final ProductParticipationEditor editor;


    /**
     * Creates a new <tt>ProductReferenceEditor</tt>.
     *
     * @param editor   the parent editor
     * @param property the
     */
    public ProductReferenceEditor(ProductParticipationEditor editor,
                                  Property property, LayoutContext context) {
        super(property, editor.getParent(), context);
        this.editor = editor;
    }

    /**
     * Invoked when an object is selected.
     *
     * @param product the selected object. May be <tt>null</tt>
     */
    @Override
    protected void onSelected(Product product) {
        if (product != null && editor.getSupplier() != null
                && hasSuppliers(product)) {
            checkSupplier(product);
        } else {
            setProduct(product, null);
        }
    }

    /**
     * Invoked when the underlying property updates.
     * This updates the product supplier relationship.
     *
     * @param product the updated object. May be <tt>null</tt>
     */
    @Override
    protected void onUpdated(Product product) {
        if (product != null && hasSuppliers(product)) {
            List<EntityRelationship> relationships
                    = getSupplierRelationships(product);
            if (relationships.isEmpty()) {
                setProductSupplier(null);
            } else if (relationships.size() == 1) {
                setProductSupplier(relationships.get(0));
            } else {
                setProductSupplier(getPreferred(relationships));
            }
        } else {
            setProductSupplier(null);
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
            Party patient = editor.getPatient();
            if (patient != null) {
                String species = (String) IMObjectHelper.getValue(
                        patient, "species");
                if (species != null) {
                    productQuery.setSpecies(species);
                }
            }
            Party location = editor.getStockLocation();
            if (location != null) {
                productQuery.setStockLocation(location);
            }
        }
        return query;
    }

    /**
     * Updates the product details.
     *
     * @param product      the product. May be <tt>null</tt>
     * @param relationship the product supplier relationship. May be
     *                     <tt>null</tt>
     */
    private void setProduct(Product product, EntityRelationship relationship) {
        setProductSupplier(relationship);
        setObject(product);
    }

    /**
     * Sets the product supplier relationship.
     *
     * @param relationship the relationship. May be <tt>null</tt>
     */
    private void setProductSupplier(EntityRelationship relationship) {
        if (relationship != null) {
            editor.setProductSupplier(new ProductSupplier(relationship));
        } else {
            editor.setProductSupplier(null);
        }
    }

    /**
     * Checks if a product is supplied by a different supplier. If so,
     * pops up a dialog to cancel the selection. If there are no other
     * suppliers, or the selection isn't cancelled, invokes
     * {@link #checkProductSupplierRelationships}.
     *
     * @param product the product
     */
    private void checkSupplier(final Product product) {
        SupplierRules rules = new SupplierRules();
        Entity otherSupplier;

        if (!rules.isSuppliedBy(editor.getSupplier(), product)
                && (otherSupplier = getSupplier(product)) != null) {
            String title = Messages.get("product.othersupplier.title");
            String message = Messages.get("product.othersupplier.message",
                                          product.getName(),
                                          otherSupplier.getName());
            final ConfirmationDialog dialog
                    = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
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
     * Determines if a product can have suppliers.
     *
     * @param product the product
     * @return <tt>true</tt> if the product can have suppliers, otherwise
     *         <tt>false</tt>
     */
    private boolean hasSuppliers(Product product) {
        EntityBean bean = new EntityBean(product);
        return bean.hasNode("suppliers");
    }

    /**
     * Returns the first active supplier for a product.
     *
     * @param product the product
     * @return the supplier, or <tt>null</tt> if none is found
     */
    private Entity getSupplier(Product product) {
        EntityBean bean = new EntityBean(product);
        return bean.getNodeTargetEntity("suppliers");
    }

    /**
     * Determines if there is product supplier relationships for the current
     * supplier and specified product.
     * <p/>
     * If there is more than one, pops up a dialog prompting to select one
     * of them.
     *
     * @param product the product
     */
    private void checkProductSupplierRelationships(final Product product) {
        // find all relationships for the product and supplier
        List<EntityRelationship> relationships
                = getSupplierRelationships(product);

        if (relationships.isEmpty()) {
            setProduct(product, null);
        } else if (relationships.size() == 1) {
            setProduct(product, relationships.get(0));
        } else {
            // pop up a browser displaying the relationships, with the
            // preferred one selected
            EntityRelationship preferred = getPreferred(relationships);
            Query<EntityRelationship> query
                    = new ListQuery<EntityRelationship>(
                    relationships, "entityRelationship.productSupplier",
                    EntityRelationship.class);
            String title = Messages.get("product.supplier.type");
            final Browser<EntityRelationship> browser
                    = new ProductSupplierBrowser(query);
            final BrowserDialog<EntityRelationship> dialog
                    = new BrowserDialog<EntityRelationship>(title, browser);

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
            browser.setSelected(preferred);
            dialog.show();
        }
    }

    /**
     * Returns the preferred supplier relationship.
     *
     * @param relationships the relationships
     * @return the preferred relationship, or the first if none is preferred,
     *         or <tt>null</tt> if there are no relationships
     */
    private EntityRelationship getPreferred(
            List<EntityRelationship> relationships) {
        EntityRelationship result = null;
        if (!relationships.isEmpty()) {
            Predicate preferred = new NodeEquals("preferred", true);
            result = CollectionHelper.find(relationships, preferred);
            if (result == null) {
                result = relationships.get(0);
            }
        }
        return result;
    }

    /**
     * Returns all active supplier relationships for the current supplier
     * and specified product.
     *
     * @param product the product
     * @return the active relationships
     */
    private List<EntityRelationship> getSupplierRelationships(Product product) {
        EntityBean bean = new EntityBean(product);
        Party supplier = editor.getSupplier();
        Predicate predicate = new AndPredicate(
                IsActiveRelationship.ACTIVE_NOW,
                RefEquals.getTargetEquals(supplier));
        return bean.getNodeRelationships("suppliers", predicate);
    }

    /**
     * Browser to display a product supplier relationships.
     */
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
         * Lays out this component.
         *
         * @param container the container
         */
        @Override
        protected void doLayout(Component container) {
            // no-op
        }
    }
}
