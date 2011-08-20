package org.openvpms.web.app.customer.charge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests the {@link CustomerChargeActEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Tests creation and saving of an empty invoice.
     */
    @Test
    public void testEmptyInvoice() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Tests creation and saving of an empty invoice.
     */
    @Test
    public void testEmptyCredit() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Tests creation and saving of an empty counter sale.
     */
    @Test
    public void testEmptyCounterSale() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests invoicing.
     */
    @Test
    public void testInvoice() {
        checkCharge((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Tests counter sales.
     */
    @Test
    public void testCounterSale() {
        checkCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests credits.
     */
    @Test
    public void testCredit() {
        checkCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes an invoice and its item.
     */
    @Test
    public void testDeleteInvoice() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, patient, product,
                                                                           ActStatus.IN_PROGRESS);
        checkDeleteCharge(acts.get(0), acts.get(1));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a credit and its item.
     */
    @Test
    public void testDeleteCredit() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new Money(100), customer, patient, product,
                                                                          ActStatus.IN_PROGRESS);
        checkDeleteCharge(acts.get(0), acts.get(1));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a counter sale and its item.
     */
    @Test
    public void testDeleteCounterSale() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new Money(100), customer, product,
                                                                           ActStatus.IN_PROGRESS);
        checkDeleteCharge(acts.get(0), acts.get(1));
    }


    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes an invoice and its item.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkDeleteCharge(FinancialAct charge, FinancialAct item) {
        save(charge, item);
        final CustomerChargeActEditor editor = new CustomerChargeActEditor(charge, null, new DefaultLayoutContext());
        editor.getComponent();
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        Boolean result = template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return editor.delete();
            }
        });
        assertTrue(result);

        assertNull(get(charge));
        assertNull(get(item));
    }

    /**
     * Tests editing a charge with no items.
     *
     * @param charge the charge
     */
    private void checkEmptyCharge(FinancialAct charge) {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party location = TestHelper.createLocation();
        User author = TestHelper.createUser();
        context.getContext().setCustomer(customer);
        context.getContext().setLocation(location);
        context.getContext().setUser(author);

        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, context, new EditorManager());
        editor.getComponent();
        assertTrue(editor.isValid());
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);

        editor.setStatus(ActStatus.POSTED);
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Tests editing of a charge.
     *
     * @param charge the charge
     */
    private void checkCharge(FinancialAct charge) {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party location = TestHelper.createLocation();
        Party patient = TestHelper.createPatient(customer);
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();
        context.getContext().setCustomer(customer);
        context.getContext().setLocation(location);
        context.getContext().setUser(author);
        context.getContext().setClinician(clinician);

        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(11);
        BigDecimal tax = BigDecimal.valueOf(1);
        BigDecimal total = BigDecimal.valueOf(11);
        Product product = createProduct(ProductArchetypes.MEDICATION, unitCost, unitPrice);

        EditorManager mgr = new EditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, context, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = editor.addItem();
        itemEditor.getComponent();
        assertTrue(editor.isValid());
        assertFalse(itemEditor.isValid());

        if (itemEditor.getProperty("patient") != null) {
            itemEditor.setPatient(patient);
        }
        itemEditor.setProduct(product);
        if (TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE)
            && TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            // invoice items have a dispensing node
            assertFalse(itemEditor.isValid());  // not valid while popup is displayed
            assertFalse(editor.isValid());
            checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION);
            // save the popup editor - should be a medication
        }

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, BigDecimal.ZERO);
        editor.setStatus(ActStatus.POSTED);
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, balance);

        FinancialAct item = (FinancialAct) itemEditor.getObject();
        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal fixedCost = BigDecimal.ZERO;
        BigDecimal fixedPrice = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        checkItem(item, patient, product, author, clinician, quantity, unitCost, unitPrice, fixedCost, fixedPrice,
                  discount, tax, total);
    }

    /**
     * Checks the balance for a customer.
     *
     * @param customer the customer
     * @param unbilled the expected unbilled amount
     * @param balance  the expected balance
     */
    private void checkBalance(Party customer, BigDecimal unbilled, BigDecimal balance) {
        CustomerAccountRules rules = new CustomerAccountRules();
        checkEquals(unbilled, rules.getUnbilledAmount(customer));
        checkEquals(balance, rules.getBalance(customer));
    }

    /**
     * Creates a customer charge act editor.
     *
     * @param invoice the charge to edit
     * @param context the layout context
     * @param manager the popup editor manager
     * @return a new customer charge act editor
     */
    private CustomerChargeActEditor createCustomerChargeActEditor(final FinancialAct invoice,
                                                                  final LayoutContext context,
                                                                  final PopupEditorManager manager) {
        return new CustomerChargeActEditor(invoice, null, context, false) {
            @Override
            protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
                ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
                if (editor instanceof ChargeItemRelationshipCollectionEditor) {
                    // register a handler for act popups
                    ((ChargeItemRelationshipCollectionEditor) editor).setPopupEditorManager(manager);
                }
                return editor;
            }
        };
    }

}
