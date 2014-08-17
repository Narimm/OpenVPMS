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

package org.openvpms.web.workspace.product.stock;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.io.ProductWriter;
import org.openvpms.archetype.rules.stock.io.StockCSVWriter;
import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.archetype.rules.util.FileNameHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.product.io.ProductExportQuery;

/**
 * Stock export dialog.
 *
 * @author Tim Anderson
 */
public class StockExportDialog extends BrowserDialog<StockData> {

    /**
     * The export button identifier.
     */
    private static final String EXPORT_ID = "button.export";

    /**
     * The dialog buttons.
     */
    private static final String[] BUTTONS = {EXPORT_ID, CLOSE_ID};

    /**
     * The field separator.
     */
    private final char separator;


    /**
     * Constructs a {@link StockExportDialog}.
     *
     * @param help the help context
     */
    public StockExportDialog(LayoutContext context, HelpContext help) {
        super(Messages.get("product.stock.export.title"), BUTTONS, false, help);
        separator = StockIOHelper.getFieldSeparator(context.getContext().getPractice());
        init(new StockExportBrowser(context), null);
        setCloseOnSelection(false);
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate on*() method for the button if it is known,
     * else sets the action to the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (EXPORT_ID.equals(button)) {
            try {
                onExport();
            } catch (Throwable exception) {
                ErrorHandler.getInstance().error(exception);
            }
        } else {
            super.onButton(button);
        }
    }

    /**
     * Invoked when the "export" button is pressed.
     * <p/>
     * This runs the {@link ProductWriter} against the products returned by the {@link ProductExportQuery},
     * and starts a download of the resulting document.
     */
    private void onExport() {
        StockExportQuery query = ((StockExportBrowser) getBrowser()).getQuery();
        StockCSVWriter exporter = new StockCSVWriter(ServiceHelper.getBean(DocumentHandlers.class), separator);
        String name = "stock-" + FileNameHelper.clean(query.getStockLocation().getName()) + "-"
                      + new java.sql.Date(System.currentTimeMillis()).toString() + ".csv";

        Document document = exporter.write(name, new ResultSetIterator<StockData>(query.query()));
        DownloadServlet.startDownload(document);
    }


}
