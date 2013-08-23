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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.io;

import org.openvpms.archetype.rules.product.io.ProductWriter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.system.ServiceHelper;

import java.util.Iterator;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ProductExportDialog extends BrowserDialog<Product> {

    private static final String EXPORT_ID = "button.export";
    private static final String[] BUTTONS = {EXPORT_ID, CLOSE_ID};

    /**
     * Constructs a {@code ProductExportDialog}.
     *
     * @param title the dialog title
     * @param help  the help context
     */
    public ProductExportDialog(String title, LayoutContext context, HelpContext help) {
        super(title, BUTTONS, new DefaultIMObjectTableBrowser<Product>(new ProductExportQuery(), context), help);
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (EXPORT_ID.equals(button)) {
            onExport();
        } else {
            super.onButton(button);
        }
    }

    private void onExport() {
        ProductExportQuery query = (ProductExportQuery) ((QueryBrowser<Product>) getBrowser()).getQuery();
        ProductWriter exporter = ServiceHelper.getBean(ProductWriter.class);
        Iterator<Product> iterator = new ResultSetIterator<Product>(query.query());
        Document document;
        switch (query.getPrices()) {
            case LATEST:
                document = exporter.write(iterator, true);
                break;
            case ALL:
                document = exporter.write(iterator, false);
                break;
            default:
                document = exporter.write(iterator, query.getFrom(), query.getTo());
        }
        DownloadServlet.startDownload(document);
    }

}
