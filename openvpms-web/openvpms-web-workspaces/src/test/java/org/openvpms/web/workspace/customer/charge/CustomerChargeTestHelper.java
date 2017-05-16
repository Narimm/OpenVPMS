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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.util.HL7Archetypes;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Helper routines for customer charge tests.
 *
 * @author Tim Anderson
 */
public class CustomerChargeTestHelper {

    /**
     * Adds a charge item.
     *
     * @param editor   the editor
     * @param patient  the patient
     * @param product  the product
     * @param quantity the quantity. If {@code null}, indicates the quantity won't be changed
     * @param queue    the popup editor manager
     * @return the editor for the new item
     */
    public static CustomerChargeActItemEditor addItem(CustomerChargeActEditor editor, Party patient,
                                                      Product product, BigDecimal quantity, EditorQueue queue) {
        CustomerChargeActItemEditor itemEditor = editor.addItem();
        itemEditor.getComponent();
        assertTrue(editor.isValid());
        assertFalse(itemEditor.isValid());

        setItem(editor, itemEditor, patient, product, quantity, queue);
        return itemEditor;
    }

    /**
     * Sets the values of a charge item.
     *
     * @param editor     the charge editor
     * @param itemEditor the charge item editor
     * @param patient    the patient
     * @param product    the product
     * @param quantity   the quantity. If {@code null}, indicates the quantity won't be changed
     * @param queue      the popup editor manager
     */
    public static void setItem(CustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor,
                               Party patient, Product product, BigDecimal quantity, EditorQueue queue) {
        if (itemEditor.getProperty("patient") != null) {
            itemEditor.setPatient(patient);
        }
        itemEditor.setProduct(product);
        if (quantity != null) {
            itemEditor.setQuantity(quantity);
        }
        if (TypeHelper.isA(editor.getObject(), CustomerAccountArchetypes.INVOICE)) {
            if (!TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                checkSavePopups(editor, itemEditor, product, queue);
            } else {
                EntityBean bean = new EntityBean(product);
                List<Entity> includes = bean.getNodeTargetEntities("includes");
                for (Entity include : includes) {
                    checkSavePopups(editor, itemEditor, (Product) include, queue);
                }
            }
        }
        Validator validator = new DefaultValidator();
        boolean valid = itemEditor.validate(validator);
        if (!valid) {
            ValidationHelper.showError(validator);
        }
        assertTrue(itemEditor.isValid());
    }

    public static void checkSavePopups(CustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor,
                                       Product product, EditorQueue queue) {
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            // invoice items have a dispensing node
            IMObjectBean bean = new IMObjectBean(product);
            if (bean.getBoolean("label")) {
                // dispensing label should be displayed
                assertFalse("Editor should invalid after setting " + product.getName(), itemEditor.isValid());
                // not valid while popup is displayed

                checkSavePopup(queue, PatientArchetypes.PATIENT_MEDICATION, true);
                // save the popup editor - should be a medication
            }
        }

        EntityBean bean = new EntityBean(product);
        for (int i = 0; i < bean.getNodeTargetEntityRefs("investigationTypes").size(); ++i) {
            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(queue, InvestigationArchetypes.PATIENT_INVESTIGATION, false);
        }
        for (int i = 0; i < bean.getNodeTargetEntityRefs("reminders").size(); ++i) {
            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(queue, ReminderArchetypes.REMINDER, false);
        }
        for (int i = 0; i < bean.getNodeTargetEntityRefs("alerts").size(); ++i) {
            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(queue, PatientArchetypes.ALERT, false);
        }
    }

    /**
     * Saves the current popup editor.
     *
     * @param queue        the popup editor manager
     * @param shortName    the expected archetype short name of the object being edited
     * @param prescription if {@code true} process prescription prompts
     */
    public static void checkSavePopup(EditorQueue queue, String shortName, boolean prescription) {
        if (prescription) {
            PopupDialog dialog = queue.getCurrent();
            if (dialog instanceof ConfirmationDialog) {
                fireDialogButton(dialog, PopupDialog.OK_ID);
            }
        }
        PopupDialog dialog = queue.getCurrent();
        assertTrue(dialog instanceof EditDialog);
        IMObjectEditor editor = ((EditDialog) dialog).getEditor();
        assertTrue(TypeHelper.isA(editor.getObject(), shortName));
        assertTrue(editor.isValid());
        fireDialogButton(dialog, PopupDialog.OK_ID);
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedPrice the fixed price
     * @return a new product
     */
    public static Product createProduct(String shortName, BigDecimal fixedPrice) {
        Product product = createProduct(shortName);
        product.addProductPrice(createFixedPrice(BigDecimal.ZERO, fixedPrice));
        TestHelper.save(product);
        return product;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedCost  the fixed cost
     * @param fixedPrice the fixed price, tax-exclusive
     * @param unitCost   the unit cost
     * @param unitPrice  the unit price, tax-exclusive
     * @return a new product
     */
    public static Product createProduct(String shortName, BigDecimal fixedCost, BigDecimal fixedPrice,
                                        BigDecimal unitCost, BigDecimal unitPrice) {
        Product product = createProduct(shortName);
        product.addProductPrice(createFixedPrice(fixedCost, fixedPrice));
        product.addProductPrice(createUnitPrice(unitCost, unitPrice));
        TestHelper.save(product);
        return product;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName the product archetype short name
     * @return a new product
     */
    public static Product createProduct(String shortName) {
        return TestHelper.createProduct(shortName, null, true);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param cost  the cost price
     * @param price the price after markup
     * @return a new unit price
     */
    public static ProductPrice createFixedPrice(BigDecimal cost, BigDecimal price) {
        return createPrice(ProductArchetypes.FIXED_PRICE, cost, price);
    }

    /**
     * Helper to create a new product price.
     *
     * @param shortName the product price archetype short name
     * @param cost      the cost price
     * @param price     the price after markup
     * @return a new unit price
     */
    public static ProductPrice createPrice(String shortName, BigDecimal cost, BigDecimal price) {
        ProductPrice result = (ProductPrice) TestHelper.create(shortName);
        ProductPriceRules rules = new ProductPriceRules(ArchetypeServiceHelper.getArchetypeService());
        BigDecimal markup = rules.getMarkup(cost, price);
        result.setName("XPrice");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("price", price);
        return result;
    }

    /**
     * Helper to create a new unit price.
     *
     * @param cost  the cost price
     * @param price the price after markup
     * @return a new unit price
     */
    public static ProductPrice createUnitPrice(BigDecimal cost, BigDecimal price) {
        return createPrice(ProductArchetypes.UNIT_PRICE, cost, price);
    }

    /**
     * Creates a new <em>entity.HL7ServicePharmacy</em>.
     *
     * @param location the practice location
     * @return a new pharmacy
     */
    public static Entity createPharmacy(Party location) {
        Entity pharmacy = (Entity) TestHelper.create(HL7Archetypes.PHARMACY);
        pharmacy.setName("ZPharmacy");
        EntityBean bean = new EntityBean(pharmacy);
        bean.addNodeTarget("location", location);
        bean.addNodeTarget("user", TestHelper.createUser());
        TestHelper.save(pharmacy);
        return pharmacy;
    }

    /**
     * Creates a new <em>entity.HL7ServiceLaboratory</em>.
     *
     * @param location the practice location
     * @return a new laboratory
     */
    public static Entity createLaboratory(Party location) {
        Entity laboratory = (Entity) TestHelper.create(HL7Archetypes.LABORATORY);
        laboratory.setName("ZLaboratory");
        EntityBean bean = new EntityBean(laboratory);
        bean.addNodeTarget("location", location);
        bean.addNodeTarget("user", TestHelper.createUser());
        TestHelper.save(laboratory);
        return laboratory;
    }

    /**
     * Verifies an order matches that expected.
     *
     * @param order             the order
     * @param type              the expected type
     * @param patient           the expected patient
     * @param product           the expected product
     * @param quantity          the expected quantity
     * @param placerOrderNumber the expected placer order number
     * @param date              the expected date
     * @param clinician         the expected clinician
     * @param pharmacy          the expected pharmacy
     */
    public static void checkOrder(TestPharmacyOrderService.Order order, TestPharmacyOrderService.Order.Type type,
                                  Party patient, Product product, BigDecimal quantity,
                                  long placerOrderNumber, Date date, User clinician, Entity pharmacy) {
        assertEquals(type, order.getType());
        assertEquals(patient, order.getPatient());
        assertEquals(product, order.getProduct());
        assertTrue(quantity.compareTo(order.getQuantity()) == 0);
        assertEquals(placerOrderNumber, order.getPlacerOrderNumber());
        assertEquals(date, order.getDate());
        assertEquals(clinician, order.getClinician());
        assertEquals(pharmacy, order.getPharmacy());
    }

    /**
     * Verifies a laboratory order matches that expected.
     *
     * @param order             the order
     * @param type              the expected type
     * @param patient           the expected patient
     * @param placerOrderNumber the expected placer order number
     * @param date              the expected date
     * @param clinician         the expected clinician
     * @param laboratory        the expected laboratory
     */
    public static void checkOrder(TestLaboratoryOrderService.LabOrder order,
                                  TestLaboratoryOrderService.LabOrder.Type type, Party patient, long placerOrderNumber,
                                  Date date, User clinician, Entity laboratory) {
        assertEquals(type, order.getType());
        assertEquals(patient, order.getPatient());
        assertEquals(placerOrderNumber, order.getPlacerOrderNumber());
        assertEquals(0, DateRules.compareTo(date, order.getDate(), true));
        assertEquals(clinician, order.getClinician());
        assertEquals(laboratory, order.getLaboratory());
    }

    /**
     * Creates a product dispensed via a pharmacy.
     *
     * @param pharmacy the pharmacy
     * @return a new product
     */
    public static Product createProduct(Entity pharmacy) {
        Product product = TestHelper.createProduct();
        ProductTestHelper.addPharmacy(product, pharmacy);
        return product;
    }
}
