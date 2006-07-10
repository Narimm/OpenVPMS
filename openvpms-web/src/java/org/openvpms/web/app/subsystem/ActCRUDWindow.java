/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.subsystem;

import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.report.jasper.IMObjectReport;
import org.openvpms.report.jasper.IMObjectReportFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import nextapp.echo2.app.filetransfer.Download;
import nextapp.echo2.app.filetransfer.DownloadProvider;

import java.io.IOException;
import java.io.OutputStream;


/**
 * CRUD Window for acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActCRUDWindow extends CRUDWindow {

    /**
     * Act in progress status type.
     */
    protected static final String INPROGRESS_STATUS = "In Progress";

    /**
     * Act completed status type.
     */
    protected static final String COMPLETED_STATUS = "Completed";

    /**
     * Act posted status type. Acts with this status can't be edited or
     * deleted.
     */
    protected static final String POSTED_STATUS = "Posted";

    /**
     * The print button.
     */
    private Button _print;

    /**
     * Print button identifier.
     */
    private static final String PRINT_ID = "print";


    /**
     * Create a new <code>ActCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create
     */
    public ActCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <code>true</code> if the act can be edited, otherwise
     *         <code>false</code>
     */
    protected boolean canEdit(Act act) {
        String status = act.getStatus();
        return INPROGRESS_STATUS.equals(status)
                || COMPLETED_STATUS.equals(status);
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act
     * @return <code>true</code> if the act can be deleted, otherwise
     *         <code>false</code>
     */
    protected boolean canDelete(Act act) {
        String status = act.getStatus();
        return !POSTED_STATUS.equals(status);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    @Override
    protected void onEdit() {
        IMObject object = getObject();
        if (object != null) {
            Act act = (Act) object;
            if (canEdit(act)) {
                super.onEdit();
            } else {
                showStatusError(act, "act.noedit.title", "act.noedit.message");
            }
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Act act = (Act) getObject();
        if (canDelete(act)) {
            super.onDelete();
        } else {
            showStatusError(act, "act.nodelete.title", "act.nodelete.message");
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        String name = getArchetypeDescriptor().getDisplayName();
        String title = Messages.get("act.print.title", name);
        String message = Messages.get("act.print.message", name);
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    doPrint();
                }
            }
        });
        dialog.show();
    }

    /**
     * Returns the print button.
     *
     * @return the print button
     */
    protected Button getPrintButton() {
        if (_print == null) {
            _print = ButtonFactory.create(PRINT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onPrint();
                }
            });
        }
        return _print;
    }

    /**
     * Sets the print status.
     *
     * @param act     the act
     * @param printed the print status
     */
    protected void setPrintStatus(Act act, boolean printed) {
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor descriptor = archetype.getNodeDescriptor("printed");
        if (descriptor != null) {
            descriptor.setValue(act, printed);
        }
    }

    /**
     * Helper to show a status error.
     *
     * @param act        the act
     * @param titleKey   the error dialog title key
     * @param messageKey the error messsage key
     */
    protected void showStatusError(Act act, String titleKey,
                                   String messageKey) {
        String name = getArchetypeDescriptor().getDisplayName();
        String status = act.getStatus();
        String title = Messages.get(titleKey, name);
        String message = Messages.get(messageKey, name, status);
        ErrorDialog.show(title, message);
    }

    /**
     * Print the act, updating its status if required.
     */
    private void doPrint() {
        Act act = (Act) getObject();
        String shortName = act.getArchetypeId().getShortName();
        try {
            IMObjectReport report = IMObjectReportFactory.create(
                    shortName, ServiceHelper.getArchetypeService());
            final JasperPrint print = report.generate(act);

            Download download = new Download();
            download.setProvider(new DownloadProvider() {

                public String getContentType() {
                    return "application/pdf";
                }

                public String getFileName() {
                    return print.getName() + ".pdf";
                }

                public int getSize() {
                    return -1;
                }

                public void writeFile(OutputStream stream) throws IOException {
                    try {
                        JasperExportManager.exportReportToPdfStream(print,
                                                                    stream);
                    } catch (JRException exception) {
                        throw new IOException(exception.getMessage());
                    }
                }
            });
            download.setActive(true);
            OpenVPMSApp.getInstance().enqueueCommand(download);
            String status = act.getStatus();
            if (!POSTED_STATUS.equals(status)) {
                act.setStatus(POSTED_STATUS);
                setPrintStatus(act, true);
                SaveHelper.save(act);
                setObject(act);
                CRUDWindowListener listener = getListener();
                if (listener != null) {
                    listener.saved(act, false);
                }
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

}
