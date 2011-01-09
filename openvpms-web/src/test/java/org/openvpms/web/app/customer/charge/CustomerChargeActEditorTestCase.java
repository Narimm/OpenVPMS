package org.openvpms.web.app.customer.charge;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Tests the {@link CustomerChargeActEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes an invoice and its item.
     */
    @Test
    public void testDelete() {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, patient, product,
                ActStatus.POSTED);
        save(acts);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);

        final CustomerChargeActEditor editor = new CustomerChargeActEditor(invoice, null, context);
        editor.getComponent();
        TransactionTemplate template = new TransactionTemplate(
                ServiceHelper.getTransactionManager());
        Boolean result = template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return editor.delete();
            }
        });
        assertTrue(result);

        assertNull(get(invoice));
        assertNull(get(item));
    }
}
