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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.cache;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Dialog to display and manipulate application caches.
 *
 * @author Tim Anderson
 */
public class CacheDialog extends ModalDialog {

    /**
     * The cache browser.
     */
    private final CacheBrowser browser;

    /**
     * The cache manager.
     */
    private final EhcacheManager manager;

    /**
     * Edit button identifier.
     */
    private static final String EDIT_ID = "button.edit";

    /**
     * Clear cache button identifier.
     */
    private static final String CLEAR_CACHE_ID = "button.clearcache";

    /**
     * Constructs a {@link CacheDialog}.
     *
     * @param help the help context
     */
    public CacheDialog(HelpContext help) {
        super(Messages.get("admin.system.cache.title"), "MessageDialog", new String[0], help);
        browser = new CacheBrowser();
        manager = ServiceHelper.getBean(EhcacheManager.class);
        addButton(EDIT_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                edit();
            }
        });
        addButton("button.refresh", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                refreshStatistics();
            }
        });
        addButton("button.resetstatistics", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                resetStatistics();
            }
        });
        addButton(CLEAR_CACHE_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                clearCache();
            }
        });
        addButton(CLOSE_ID);
        enableButtons();
        browser.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                enableButtons();
            }
        });
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, browser.getComponent()));
        getFocusGroup().add(browser.getFocusGroup());
    }

    /**
     * Refreshes statistics.
     */
    private void refreshStatistics() {
        browser.refresh();
        enableButtons();
    }

    /**
     * Resets statistics.
     */
    private void resetStatistics() {
        browser.resetStatistics();
        enableButtons();
    }

    /**
     * Clears the selected cache.
     */
    private void clearCache() {
        browser.clear();
        enableButtons();
    }

    /**
     * Edits the selected cache.
     */
    private void edit() {
        CacheState cache = browser.getSelected();
        if (cache != null) {
            long suggestedSize = browser.getSuggestedSize(cache);
            EditCacheDialog dialog = new EditCacheDialog(cache.getDisplayName(), cache.getMaxCount(), suggestedSize,
                                                         getHelpContext().subtopic("edit"));
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    long count = dialog.getCount();
                    if (count > 0) {
                        manager.setMaxElements(cache.getCache(), cache.getName(), count);
                    }
                    refreshStatistics();
                }
            });
            dialog.show();
        }
    }

    /**
     * Enables the Clear Cache button if a cache is selected, otherwise disables it.
     */
    private void enableButtons() {
        ButtonSet buttons = getButtons();
        boolean enabled = browser.getSelected() != null;
        buttons.setEnabled(EDIT_ID, enabled);
        buttons.setEnabled(CLEAR_CACHE_ID, enabled);
    }
}
