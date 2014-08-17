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

package org.openvpms.archetype.rules.stock.io;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports stock data from a CSV file.
 *
 * @author Tim Anderson
 * @see StockCSVReader
 */
public class StockDataImporter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The field separator.
     */
    private final char separator;

    /**
     * The data filter.
     */
    private StockDataFilter filter;

    /**
     * Constructs an {@link StockDataImporter}.
     *
     * @param service   the archetype service
     * @param handlers  the document handlers
     * @param separator the field separator
     */
    public StockDataImporter(IArchetypeService service, DocumentHandlers handlers, char separator) {
        this.service = service;
        this.handlers = handlers;
        this.separator = separator;
        filter = new StockDataFilter(service);
    }

    /**
     * Loads stock data.
     *
     * @param document the CSV document to load
     * @param author   the author to add to the adjustment
     * @param reason   populates the reason node of the adjustment. May be {@code null}
     * @return the stock data. This will contain an <em>act.stockAdjust</em> if the load was successful
     */
    public StockDataSet load(Document document, User author, String reason) {
        StockCSVReader reader = new StockCSVReader(handlers, separator);
        StockDataSet data = reader.read(document);
        if (data.getErrors().isEmpty() && !data.getData().isEmpty()) {
            data = filter.filter(data.getData());
            if (data.getErrors().isEmpty() && !data.getData().isEmpty()) {
                data = load(data.getData(), author, reason);
            }
        }
        return data;
    }

    /**
     * Loads stock data.
     *
     * @param data   the data to load
     * @param author the author to add to the adjustment
     * @param reason populates the reason node of the adjustment. May be {@code null}
     * @return the stock data
     */
    private StockDataSet load(List<StockData> data, User author, String reason) {
        Act act = (Act) service.create(StockArchetypes.STOCK_ADJUST);
        ActBean bean = new ActBean(act, service);
        bean.addNodeParticipation("stockLocation", data.get(0).getStockLocation());
        bean.addNodeParticipation("author", author);
        bean.setValue("reason", reason);
        List<Act> toSave = new ArrayList<Act>();
        toSave.add(act);
        for (StockData item : data) {
            Act child = (Act) service.create(StockArchetypes.STOCK_ADJUST_ITEM);
            BigDecimal quantity = item.getNewQuantity().subtract(item.getQuantity());
            ActBean childBean = new ActBean(child, service);
            childBean.addNodeParticipation("product", item.getProduct());
            childBean.setValue("quantity", quantity);
            bean.addNodeRelationship("items", child);
            toSave.add(child);
        }
        service.save(toSave);
        return new StockDataSet(data, act);
    }
}
