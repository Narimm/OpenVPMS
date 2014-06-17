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

package org.openvpms.archetype.rules.product.io;

import au.com.bytecode.opencsv.CSVWriter;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.DateFunctions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.math.MathRules.ONE_HUNDRED;
import static org.openvpms.archetype.rules.product.ProductArchetypes.PRICING_GROUP;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createFixedPrice;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createUnitPrice;
import static org.openvpms.archetype.test.TestHelper.createTaxType;

/**
 * Tests the {@link ProductCSVWriter} and {@link ProductCSVReader} classes.
 *
 * @author Tim Anderson
 */
public class ProductCSVWriterReaderTestCase extends AbstractProductIOTest {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * The product price rules.
     */
    private ProductPriceRules rules;

    /**
     * The tax rules.
     */
    private TaxRules taxRules;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The first fixed price, pricing group A.
     */
    private ProductPrice fixed1A;

    /**
     * The first fixed price, pricing group B.
     */
    private ProductPrice fixed1B;

    /**
     * The first fixed price, no pricing group.
     */
    private ProductPrice fixed1C;

    /**
     * The second fixed price, pricing group A.
     */
    private ProductPrice fixed2A;

    /**
     * The second fixed price, pricing group B.
     */
    private ProductPrice fixed2B;

    /**
     * The second fixed price, no pricing group.
     */
    private ProductPrice fixed2C;

    /**
     * The first unit price, pricing group A.
     */
    private ProductPrice unit1A;

    /**
     * The first unit price, pricing group B.
     */
    private ProductPrice unit1B;

    /**
     * The first unit price, no pricing group.
     */
    private ProductPrice unit1C;

    /**
     * The second unit price.
     */
    private ProductPrice unit2A;

    /**
     * The second unit price.
     */
    private ProductPrice unit2B;

    /**
     * The second unit price.
     */
    private ProductPrice unit2C;

    /**
     * The product.
     */
    private Product product;

    /**
     * Pricing group A.
     */
    private Lookup groupA;

    /**
     * Pricing group B.
     */
    private Lookup groupB;

    /**
     * Current date - 2 months and 1 day.
     */
    private Date monthsMinus21;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new ProductPriceRules(getArchetypeService(), lookups);
        Party practice = TestHelper.getPractice();
        taxRules = new TaxRules(practice, getArchetypeService(), lookups);
        handlers = new DocumentHandlers();

        Date today = DateRules.getToday();
        Date tomorrow = DateRules.getTomorrow();
        Date yesterday = DateRules.getYesterday();
        Date months2 = DateRules.getDate(today, 2, DateUnits.MONTHS);
        Date months21 = DateRules.getDate(months2, 1, DateUnits.DAYS);
        Date monthsMinus2 = DateRules.getDate(today, -2, DateUnits.MONTHS);
        monthsMinus21 = DateRules.getDate(monthsMinus2, -1, DateUnits.DAYS);

        groupA = TestHelper.getLookup(PRICING_GROUP, "A");
        groupB = TestHelper.getLookup(PRICING_GROUP, "B");

        product = createProduct("Product A", "A");
        product.addClassification(createTaxType(new BigDecimal("5.0")));
        fixed1A = createFixedPrice("1.0", "0.5", "100", "10", monthsMinus21, yesterday, false);
        fixed1A.addClassification(groupA);

        fixed1B = createFixedPrice("1.0", "0.5", "100", "10", monthsMinus21, yesterday, false);
        fixed1B.addClassification(groupB);

        fixed1C = createFixedPrice("1.0", "0.5", "100", "10", monthsMinus21, yesterday, false);

        fixed2A = createFixedPrice("1.08", "0.6", "80", "10", today, months2, true);
        fixed2A.addClassification(groupA);

        fixed2B = createFixedPrice("1.08", "0.6", "80", "10", today, months2, true);
        fixed2B.addClassification(groupB);

        fixed2C = createFixedPrice("1.08", "0.6", "80", "10", today, months2, true);

        unit1A = createUnitPrice("1.92", "1.2", "60", "10", monthsMinus2, today);
        unit1A.addClassification(groupA);

        unit1B = createUnitPrice("1.92", "1.2", "60", "10", monthsMinus2, today);
        unit1B.addClassification(groupB);

        unit1C = createUnitPrice("1.92", "1.2", "60", "10", monthsMinus2, today);

        unit2A = createUnitPrice("2.55", "1.5", "70", "10", tomorrow, months21);
        unit2A.addClassification(groupA);

        unit2B = createUnitPrice("2.55", "1.5", "70", "10", tomorrow, months21);
        unit2B.addClassification(groupB);

        unit2C = createUnitPrice("2.55", "1.5", "70", "10", tomorrow, months21);
        product.addProductPrice(fixed1A);
        product.addProductPrice(fixed1B);
        product.addProductPrice(fixed1C);
        product.addProductPrice(fixed2A);
        product.addProductPrice(fixed2B);
        product.addProductPrice(fixed2C);
        product.addProductPrice(unit1A);
        product.addProductPrice(unit1B);
        product.addProductPrice(unit1C);
        product.addProductPrice(unit2A);
        product.addProductPrice(unit2B);
        product.addProductPrice(unit2C);
        save(product);
    }

    /**
     * Tests writing the latest prices, and reading them back again.
     */
    @Test
    public void testWriteReadLatestPrices() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), true, true,
                                         new PricingGroup(groupA, false)); // exclude prices with no group

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);

        List<SimpleDateFormat> dateFormats = reader.getDateFormats(document);
        assertEquals(1, dateFormats.size());
        assertEquals("yy-MM-dd", dateFormats.get(0).toPattern());
        reader.setDateFormats(dateFormats);

        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        assertEquals(1, data.getFixedPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed2A);

        assertEquals(1, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit2A);
    }

    /**
     * Tests writing all prices for a pricing group, and reading them back again.
     */
    @Test
    public void testWriteReadAllPricesForGroup() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), false, true, new
                PricingGroup(groupA, false));

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        reader.setDateFormats(Arrays.asList(ProductCSVReader.YEAR_MONTH_DAY_FORMATS));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        assertEquals(2, data.getFixedPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed2A);
        checkPrice(data.getFixedPrices().get(1), fixed1A);

        assertEquals(2, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit2A);
        checkPrice(data.getUnitPrices().get(1), unit1A);
    }

    /**
     * Tests writing all prices, and reading them back again.
     */
    @Test
    public void testWriteReadAllPrices() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), false, true, PricingGroup.ALL);

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        reader.setDateFormats(Arrays.asList(ProductCSVReader.YEAR_MONTH_DAY_FORMATS));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        checkPrices(data.getFixedPrices(), fixed1A, fixed1B, fixed1C, fixed2A, fixed2B, fixed2C);
        checkPrices(data.getUnitPrices(), unit1A, unit1B, unit1C, unit2A, unit2B, unit2C);
    }

    /**
     * Tests writing prices matching a date range, and reading them back again.
     */
    @Test
    public void testWriteReadRangePrices() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Date from = monthsMinus21;
        Date to = DateRules.getDate(from, 1, DateUnits.MONTHS);
        Document document = writer.write(Arrays.asList(product).iterator(), from, to, true,
                                         new PricingGroup(groupB, false));

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        reader.setDateFormats(Arrays.asList(ProductCSVReader.YEAR_MONTH_DAY_FORMATS));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        assertEquals(1, data.getFixedPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed1B);

        assertEquals(1, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit1B);
    }

    /**
     * Tests writing a product that contains just unit prices.
     */
    @Test
    public void testWriteUnitPrices() {
        product.removeProductPrice(fixed1A);
        product.removeProductPrice(fixed1B);
        product.removeProductPrice(fixed1C);
        product.removeProductPrice(fixed2A);
        product.removeProductPrice(fixed2B);
        product.removeProductPrice(fixed2C);
        save(product);
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), false, true,
                                         new PricingGroup(groupA, false));

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        reader.setDateFormats(Arrays.asList(ProductCSVReader.YEAR_MONTH_DAY_FORMATS));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        assertEquals(0, data.getFixedPrices().size());
        assertEquals(2, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit2A);
        checkPrice(data.getUnitPrices().get(1), unit1A);
    }

    /**
     * Tests writing a product that contains just fixed  prices.
     */
    @Test
    public void testWriteFixedPrices() {
        product.removeProductPrice(unit1A);
        product.removeProductPrice(unit1B);
        product.removeProductPrice(unit1C);
        product.removeProductPrice(unit2A);
        product.removeProductPrice(unit2B);
        product.removeProductPrice(unit2C);
        save(product);
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), false, true,
                                         new PricingGroup(groupA, false));

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        reader.setDateFormats(Arrays.asList(ProductCSVReader.YEAR_MONTH_DAY_FORMATS));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        assertEquals(2, data.getFixedPrices().size());
        assertEquals(0, data.getUnitPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed2A);
        checkPrice(data.getFixedPrices().get(1), fixed1A);
    }

    /**
     * Verifies that the correct date formats are detected in the input document.
     */
    @Test
    public void testDateParsing() {
        ProductPrice fixed1 = createFixedPrice("1.0", "0.5", "100", "10", "2012-02-01", "2012-04-01", false);
        ProductPrice fixed2 = createFixedPrice("1.08", "0.6", "80", "10", "2012-04-02", "2012-06-01", true);
        ProductPrice unit1 = createUnitPrice("1.92", "1.2", "60", "10", "2012-02-02", "2012-04-02");
        ProductPrice unit2 = createUnitPrice("2.55", "1.5", "70", "10", "2012-04-03", "2012-06-02");
        Product product = createProduct("Product A", "A", fixed1, fixed2, unit1, unit2);
        product.addClassification(createTaxType(new BigDecimal("5.0")));
        save(product);

        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers) {
            @Override
            protected String getDate(Date date) {
                return DateFunctions.format(date, "dd/MM/yy");
            }
        };
        Document document = writer.write(Arrays.asList(product).iterator(), false, true, PricingGroup.ALL);
        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        List<SimpleDateFormat> dateFormats = reader.getDateFormats(document);
        assertEquals(3, dateFormats.size());
        assertEquals(ProductCSVReader.DAY_MONTH_YEAR_FORMATS[0], dateFormats.get(0));
        assertEquals(ProductCSVReader.YEAR_MONTH_DAY_FORMATS[0], dateFormats.get(1));
        assertEquals(ProductCSVReader.MONTH_DAY_YEAR_FORMATS[0], dateFormats.get(2));
        reader.setDateFormats(Arrays.asList(dateFormats.get(0)));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        checkPrices(data.getFixedPrices(), fixed1, fixed2);
        checkPrices(data.getUnitPrices(), unit1, unit2);
    }

    /**
     * Verifies that an error is raised if a fixed price is specified without a fixed cost.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingFixedCost() throws IOException {
        String[] data = {"1001", "Product A", "A", "-1", "1.08", "", "10", "02/04/12", "01/06/12", "true", "", "-1",
                         "2.55", "1.5", "10", "03/04/12", "02/06/12", "", "5.0"};
        ProductDataSet products = createProductDataSet(data);
        assertEquals(1, products.getErrors().size());
        assertEquals("A value for Fixed Cost is required", products.getErrors().get(0).getError());
    }

    /**
     * Verifies that an error is raised if a fixed price is specified without a fixed max discount.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingFixedMaxDiscount() throws IOException {
        String[] data = {"1001", "Product A", "A", "-1", "1.08", "0.6", "", "02/04/12", "01/06/12", "true", "", "-1",
                         "2.55", "1.5", "10", "03/04/12", "02/06/12", "", "5.0"};
        ProductDataSet products = createProductDataSet(data);

        assertEquals(1, products.getErrors().size());
        assertEquals("A value for Fixed Price Max Discount is required", products.getErrors().get(0).getError());
    }

    /**
     * Verifies that an error is raised if a unit price is specified without a unit cost.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingUnitCost() throws IOException {
        String[] data = {"1001", "Product A", "A", "-1", "1.08", "0.6", "10", "02/04/12", "01/06/12", "true", "", "-1",
                         "2.55", "", "10", "03/04/12", "02/06/12", "", "5.0"};
        ProductDataSet products = createProductDataSet(data);

        assertEquals(1, products.getErrors().size());
        assertEquals("A value for Unit Cost is required", products.getErrors().get(0).getError());
    }

    /**
     * Verifies that an error is raised if a unit price is specified without a unit price max discount.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingUnitPriceMaxDiscount() throws IOException {
        String[] data = {"1001", "Product A", "A", "-1", "1.08", "0.6", "10", "02/04/12", "01/06/12", "true", "", "-1",
                         "2.55", "1.5", "", "03/04/12", "02/06/12", "", "5.0"};
        ProductDataSet products = createProductDataSet(data);

        assertEquals(1, products.getErrors().size());
        assertEquals("A value for Unit Price Max Discount is required", products.getErrors().get(0).getError());
    }

    /**
     * Verifies that products can be written if they have prices with {@code null} costs and maxDiscounts.
     * <p/>
     * These will default to 0.0 and 100 respectively, as per the price archetypes.
     */
    @Test
    public void testWritePricesWithNullCostAndMaxDiscounts() {
        Product product = createProduct("Product A", "A");
        product.addClassification(createTaxType(new BigDecimal("5.0")));
        ProductPrice fixed1 = createFixedPrice("1.0", "0.5", "100", "10", "2012-02-01", "2012-04-01", false);
        ProductPrice unit1 = createUnitPrice("1.92", "1.2", "60", "10", "2012-02-02", "2012-04-02");

        IMObjectBean unit1Bean = new IMObjectBean(unit1);
        unit1Bean.setValue("cost", null);
        unit1Bean.setValue("maxDiscount", null);
        IMObjectBean fixed1Bean = new IMObjectBean(fixed1);
        fixed1Bean.setValue("cost", null);
        fixed1Bean.setValue("maxDiscount", null);
        product.addProductPrice(fixed1);
        product.addProductPrice(unit1);

        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, taxRules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), false, true, PricingGroup.ALL);

        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        reader.setDateFormats(Arrays.asList(ProductCSVReader.YEAR_MONTH_DAY_FORMATS));
        ProductDataSet products = reader.read(document);
        assertEquals(1, products.getData().size());
        assertEquals(0, products.getErrors().size());

        ProductData data = products.getData().get(0);
        checkProduct(data, product);
        assertEquals(1, data.getFixedPrices().size());
        checkEquals(ZERO, data.getFixedPrices().get(0).getCost());
        checkEquals(ONE_HUNDRED, data.getFixedPrices().get(0).getMaxDiscount());

        assertEquals(1, data.getUnitPrices().size());
        checkEquals(ZERO, data.getUnitPrices().get(0).getCost());
        checkEquals(ONE_HUNDRED, data.getUnitPrices().get(0).getMaxDiscount());
    }

    /**
     * Creates a CSV containing a single line from the supplied data, and reads it back into a {@link ProductDataSet}.
     *
     * @param data the data to write
     * @return the read data
     * @throws IOException for any I/O error
     */
    private ProductDataSet createProductDataSet(String[] data) throws IOException {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, ProductCSVWriter.SEPARATOR);
        csv.writeNext(ProductCSVWriter.HEADER);
        csv.writeNext(data);
        csv.close();

        DocumentHandler handler = handlers.get("Dummy.csv", ProductCSVWriter.MIME_TYPE);
        Document document = handler.create("Dummy.csv", new ByteArrayInputStream(writer.toString().getBytes("UTF-8")),
                                           ProductCSVWriter.MIME_TYPE, -1);
        ProductCSVReader reader = new ProductCSVReader(handlers, lookups);
        return reader.read(document);
    }

    /**
     * Verifies a product matches that expected.
     *
     * @param data     the product data
     * @param expected the expected product
     */
    private void checkProduct(ProductData data, Product expected) {
        IMObjectBean bean = new IMObjectBean(expected);
        assertEquals(expected.getId(), data.getId());
        assertEquals(expected.getName(), data.getName());
        assertEquals(bean.getString("printedName"), data.getPrintedName());
        checkEquals(taxRules.getTaxRate(expected), data.getTaxRate());
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param data     the price data
     * @param expected the expected price
     */
    private void checkPrice(PriceData data, ProductPrice expected) {
        IMObjectBean bean = new IMObjectBean(expected);
        assertEquals(expected.getPrice(), data.getPrice());
        assertEquals(bean.getBigDecimal("cost"), data.getCost());
        assertEquals(bean.getBigDecimal("maxDiscount"), data.getMaxDiscount());
        assertEquals(expected.getFromDate(), data.getFrom());
        assertEquals(expected.getToDate(), data.getTo());
        Set<Lookup> pricingGroups = ProductIOHelper.getPricingGroups(expected, getArchetypeService());
        assertEquals(pricingGroups, data.getPricingGroups());
    }

    private void checkPrices(List<PriceData> actual, ProductPrice... expected) {
        assertEquals(actual.size(), expected.length);
        for (ProductPrice price : expected) {
            for (PriceData other : actual) {
                if (other.getId() == price.getId()) {
                    checkPrice(other, price);
                    return;
                }
            }
            fail("PriceData not found for id=" + price.getId());
        }
    }

}
