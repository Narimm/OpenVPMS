package org.openvpms.archetype.rules.supplier;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link OrderGenerator}.
 *
 * @author Tim Anderson
 */
public class OrderGeneratorTest extends AbstractSupplierTest {

    /**
     * The tax rules.
     */
    private TaxRules taxRules;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party practice = (Party) create("party.organisationPractice");
        taxRules = new TaxRules(practice);
    }

    /**
     * Tests the {@link OrderGenerator#getOrderableStock(Party, Party)}  method.
     */
    @Test
    public void testGetOrderableStock() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();

        addRelationships(product, stockLocation, supplier, true, 1, 10, 5);

        List<Stock> stock = generator.getOrderableStock(supplier, stockLocation);
        assertEquals(1, stock.size());
        checkStock(stock.get(0), product, supplier, stockLocation, 1, 0, 9);
    }

    /**
     * Checks that the on-hand, on-order and to-order quantities is calculated correctly when there are outstanding
     * orders.
     */
    @Test
    public void testGetOrderableStockForPendingOrders() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier1, true, 1, 10, 6);
        addRelationships(product2, stockLocation, supplier1, true, 2, 10, 5);
        addRelationships(product3, stockLocation, supplier2, true, 1, 10, 5);
        createOrder(product1, supplier1, stockLocation, 2, OrderStatus.IN_PROGRESS);
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.COMPLETED);
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.POSTED);
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.ACCEPTED);

        createOrder(product2, supplier1, stockLocation, 3, OrderStatus.ACCEPTED);

        // shouldn't impact totals
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.CANCELLED);
        createOrder(product2, supplier1, stockLocation, 1, OrderStatus.CANCELLED);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.IN_PROGRESS);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.COMPLETED);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.POSTED);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.ACCEPTED);

        supplier1 = get(supplier1);
        List<Stock> stock = generator.getOrderableStock(supplier1, stockLocation);
        assertEquals(2, stock.size());
        checkStock(stock.get(0), product1, supplier1, stockLocation, 1, 5, 4);
        checkStock(stock.get(1), product2, supplier1, stockLocation, 2, 3, 5);
    }

    /**
     * Tests creation of an order based on the amount of stock on hand.
     */
    @Test
    public void testCreateOrder() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier, true, 1, 10, 5);
        addRelationships(product2, stockLocation, supplier, true, 1, 10, 5);

        List<FinancialAct> order = generator.createOrder(supplier, stockLocation);
        assertEquals(3, order.size());
        FinancialAct act = order.get(0);
        FinancialAct item1 = order.get(1);
        FinancialAct item2 = order.get(2);
        assertTrue(TypeHelper.isA(act, SupplierArchetypes.ORDER));
        assertTrue(TypeHelper.isA(item1, SupplierArchetypes.ORDER_ITEM));
        assertTrue(TypeHelper.isA(item2, SupplierArchetypes.ORDER_ITEM));
        save(order);
    }


    /**
     * Verifies the values in a {@code Stock} match that expected.
     *
     * @param stock         the stock to check
     * @param product       the expected product
     * @param supplier      the expected supplier
     * @param stockLocation the expected stock location
     * @param quantity      the expected on-hand quantity
     * @param onOrder       the expected on-order quantity
     * @param toOrder       the expected to-order quantity
     */
    private void checkStock(Stock stock, Product product, Party supplier, Party stockLocation, int quantity,
                            int onOrder, int toOrder) {
        assertEquals(product, stock.getProduct());
        assertEquals(supplier, stock.getSupplier());
        assertEquals(stockLocation, stock.getStockLocation());
        checkEquals(BigDecimal.valueOf(quantity), stock.getQuantity());
        checkEquals(BigDecimal.valueOf(onOrder), stock.getOnOrder());
        checkEquals(BigDecimal.valueOf(toOrder), stock.getToOrder());
    }

    /**
     * Creates an order.
     *
     * @param product       the product to order
     * @param supplier      the supplier to order from
     * @param stockLocation the stock location for delivery to
     * @param quantity      the order quantity
     * @param status        the order status
     * @return a new order
     */
    private FinancialAct createOrder(Product product, Party supplier, Party stockLocation, int quantity,
                                     String status) {
        FinancialAct orderItem = createOrderItem(product, BigDecimal.valueOf(quantity), 1, BigDecimal.ONE);
        FinancialAct order = createOrder(supplier, stockLocation, orderItem);
        order.setStatus(status);
        save(order);
        return order;
    }

    /**
     * Creates relationships between a product and stock location and product and supplier.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param supplier      the supplier
     * @param preferred     indicates if the supplier is the preferred supplier
     * @param quantity      the quantity
     * @param idealQty      the ideal quantity
     * @param criticalQty   the critical quantity
     */
    private void addRelationships(Product product, Party stockLocation, Party supplier, boolean preferred,
                                  int quantity, int idealQty, int criticalQty) {
        EntityBean bean = new EntityBean(product);

        IMObjectBean productStockLocation = new IMObjectBean(bean.addNodeRelationship("stockLocations", stockLocation));
        productStockLocation.setValue("quantity", quantity);
        productStockLocation.setValue("idealQty", idealQty);
        productStockLocation.setValue("criticalQty", criticalQty);

        ProductSupplier ps = new ProductSupplier(bean.addNodeRelationship("suppliers", supplier));
        ps.setPreferred(preferred);
        ps.setPackageSize(1);

        save(product, stockLocation, supplier);
    }
}
