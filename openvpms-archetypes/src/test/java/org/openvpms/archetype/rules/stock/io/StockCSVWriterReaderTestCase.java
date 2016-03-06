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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.stock.io;

import au.com.bytecode.opencsv.CSVWriter;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link StockCSVWriter} and {@link StockCSVReader}.
 *
 * @author Tim Anderson
 */
public class StockCSVWriterReaderTestCase extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        handlers = new DocumentHandlers(getArchetypeService());
    }

    /**
     * Writes a stock record and reads it back in again.
     */
    @Test
    public void testWriteRead() {
        Party stockLocation = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        StockData data = new StockData(stockLocation.getId(), stockLocation.getName(), product.getId(),
                                       product.getName(), "Mls", BigDecimal.ONE, BigDecimal.TEN);

        StockCSVWriter writer = new StockCSVWriter(handlers, ',');
        Document document = writer.write("test.csv", Arrays.asList(data).iterator());
        StockCSVReader reader = new StockCSVReader(handlers, ',');
        StockDataSet read = reader.read(document);
        assertEquals(1, read.getData().size());
        assertEquals(0, read.getErrors().size());
        assertNull(read.getAdjustment());

        StockData d = read.getData().get(0);
        assertEquals(stockLocation.getId(), d.getStockLocationId());
        assertEquals(stockLocation.getName(), d.getStockLocationName());
        assertEquals(product.getId(), d.getProductId());
        assertEquals(product.getName(), d.getProductName());
        assertEquals("Mls", d.getSellingUnits());
        checkEquals(BigDecimal.ONE, d.getQuantity());
        checkEquals(BigDecimal.TEN, d.getNewQuantity());
    }

    /**
     * Verifies that a line missing a stock location identifier is treated as an error.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingStockLocationId() throws IOException {
        String[] data = {"", "stock location", "10", "product", "Mls", "10", "0"};
        checkError(data, "A value for Stock Location Identifier is required");
    }

    /**
     * Verifies that a line missing a stock location name is treated as an error.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingStockLocationName() throws IOException {
        String[] data = {"104", "", "10", "product", "Mls", "10", "0"};
        checkError(data, "A value for Stock Location Name is required");
    }

    /**
     * Verifies that a line missing a product identifier is treated as an error.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingProductIdentifier() throws IOException {
        String[] data = {"104", "stock location", "", "product", "Mls", "10", "0"};
        checkError(data, "A value for Product Identifier is required");
    }

    /**
     * Verifies that a line missing a product name is treated as an error.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingProductName() throws IOException {
        String[] data = {"104", "stock location", "10", "", "Mls", "10", "0"};
        checkError(data, "A value for Product Name is required");
    }

    /**
     * Verifies that a line missing a quantity is treated as an error.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingQuantity() throws IOException {
        String[] data = {"104", "stock location", "10", "product", "Mls", "", "0"};
        checkError(data, "A value for Quantity is required");
    }

    /**
     * Verifies that a line missing a new quantity is treated as an error.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testMissingNewQuantity() throws IOException {
        String[] data = {"104", "stock location", "10", "product", "Mls", "10", ""};
        checkError(data, "A value for New Quantity is required");
    }

    /**
     * Creates a CSV file from a line of bad data, and reads it back in, verifying that the expected error message
     * is raised.
     *
     * @param line          the line to write
     * @param expectedError the expected error message
     * @throws IOException for any I/O error
     */
    private void checkError(String[] line, String expectedError) throws IOException {
        StockDataSet read = createStockDataSet(line);
        assertEquals(0, read.getData().size());
        assertEquals(1, read.getErrors().size());
        assertEquals(expectedError, read.getErrors().get(0).getError());
        assertNull(read.getAdjustment());
    }

    /**
     * Creates a CSV containing a single line from the supplied data, and reads it back into a {@link StockDataSet}.
     *
     * @param data the data to write
     * @return the read data
     * @throws IOException for any I/O error
     */
    private StockDataSet createStockDataSet(String[] data) throws IOException {
        Document document = createCSV(data, handlers);
        StockCSVReader reader = new StockCSVReader(handlers, ',');
        return reader.read(document);
    }

    /**
     * Creates a CSV document containing a single line from the supplied data
     *
     * @param data the data to write
     * @return the read data
     * @throws IOException for any I/O error
     */
    private Document createCSV(String[] data, DocumentHandlers handlers) throws IOException {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, ',');
        csv.writeNext(StockCSVWriter.HEADER);
        csv.writeNext(data);
        csv.close();

        DocumentHandler handler = handlers.get("Dummy.csv", StockCSVReader.MIME_TYPE);
        return handler.create("Dummy.csv", new ByteArrayInputStream(writer.toString().getBytes("UTF-8")),
                              StockCSVReader.MIME_TYPE, -1);
    }

}
