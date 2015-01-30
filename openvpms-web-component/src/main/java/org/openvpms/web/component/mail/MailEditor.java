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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import echopointng.KeyStrokeListener;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.doc.DocumentHelper;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.doc.Downloader;
import org.openvpms.web.component.im.doc.DownloaderListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.StyleSheetHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.table.AbstractTableCellRenderer;
import org.openvpms.web.echo.table.DefaultTableCellRenderer;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.echo.util.DoubleClickMonitor;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.openvpms.web.echo.style.Styles.LARGE_INSET;


/**
 * An editor for mail messages.
 *
 * @author Tim Anderson
 */
public class MailEditor extends AbstractModifiable {

    /**
     * The from, to, and subject.
     */
    private final MailHeader header;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The message body property. Used to support macro expansion.
     */
    private SimpleProperty message;

    /**
     * Determines if this has been modified.
     */
    private boolean modified;

    /**
     * The attachments table. May be {@code null}.
     */
    private Table attachments;

    /**
     * The document attachment references.
     */
    private List<DocRef> documents = new ArrayList<DocRef>();

    /**
     * The listeners.
     */
    private ModifiableListeners listeners = new ModifiableListeners();

    /**
     * Focus group.
     */
    private FocusGroup focus;

    /**
     * The split pane holding the header and attachments.
     */
    private SplitPane headerAttachmentsPane;


    /**
     * The attachment table model.
     */
    private DefaultTableModel model;

    /**
     * The mail editor component.
     */
    private SplitPane component;

    /**
     * Monitors double clicks on attachments.
     */
    private DoubleClickMonitor monitor = new DoubleClickMonitor();

    /**
     * Constructs a {@link MailEditor}.
     * <p/>
     * If no 'to' addresses are supplied the address will be editable, otherwise it will be read-only.
     * If there are multiple addresses, they will be displayed in a dropdown, and the preferred contact selected
     *
     * @param mailContext the mail context
     * @param preferredTo the preferred 'to' address. May be {@code null}
     * @param context     the context
     */
    public MailEditor(MailContext mailContext, Contact preferredTo, LayoutContext context) {
        header = new MailHeader(mailContext, preferredTo, context);
        this.context = context.getContext();
        this.help = context.getHelpContext();
        Variables variables = mailContext.getVariables();
        Macros macros = ServiceHelper.getMacros();

        message = MailHelper.createProperty("message", "mail.message", false, macros, variables);
        message.setRequired(false);
        message.setMaxLength(-1);     // no maximum length
        // message.addModifiableListener(listener); TODO
    }

    /**
     * Sets the from address.
     *
     * @param from the from address. May be {@code null}
     */
    public void setFrom(Contact from) {
        header.setFrom(from);
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        return header.getFrom();
    }

    /**
     * Sets the 'to' address.
     *
     * @param toAddress the to address. May be {@code null}
     */
    public void setTo(Contact toAddress) {
        header.setTo(toAddress);
    }

    /**
     * Returns the from name.
     *
     * @return the from name
     */
    public String getFromName() {
        return header.getFromName();
    }

    /**
     * Returns the to addresses.
     *
     * @return the to addresses. May be {@code null}
     */
    public String[] getTo() {
        return header.getTo();
    }

    /**
     * Returns the Cc addresses.
     *
     * @return the Cc addresses. May be {@code null}
     */
    public String[] getCc() {
        return header.getCc();
    }

    /**
     * Returns the Bcc addresses.
     *
     * @return the Bcc addresses. May be {@code null}
     */
    public String[] getBcc() {
        return header.getBcc();
    }

    /**
     * Sets the message subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        header.setSubject(subject);
    }

    /**
     * Returns the message subject.
     *
     * @return the message subject
     */
    public String getSubject() {
        return header.getSubject();
    }

    /**
     * Returns the message to send.
     *
     * @return the message to send
     */
    public String getMessage() {
        return (String) message.getValue();
    }

    /**
     * Adds an attachment.
     * <p/>
     * If the document is unsaved, it will be saved and deleted on {@link #dispose()}.
     *
     * @param document the document to add
     */
    public void addAttachment(Document document) {
        if (attachments == null) {
            createAttachments();
        }

        boolean delete = false;

        if (document.getMimeType() != null && !DocFormats.PDF_TYPE.equals(document.getMimeType()) &&
            Converter.canConvert(document.getName(), document.getMimeType(), DocFormats.PDF_TYPE)) {
            document = DocumentHelper.convert(document, DocFormats.PDF_TYPE);
        }

        if (document.isNew()) {
            ServiceHelper.getArchetypeService().save(document);
            delete = true;
        }
        final DocRef ref = new DocRef(document, delete);
        documents.add(ref);
        DocumentViewer documentViewer = new DocumentViewer(ref.getReference(), null, ref.getName(), true, false,
                                                           new DefaultLayoutContext(context, help));
        documentViewer.setNameLength(18);  // display up to 18 characters of the name to avoid scrollbars
        documentViewer.setDownloadListener(new DownloaderListener() {
            public void download(Downloader downloader, String mimeType) {
                onDownload(downloader, mimeType, ref.getReference());
            }
        });
        Component viewer = documentViewer.getComponent();
        if (viewer instanceof Button) {
            // TODO - hardcoded style not ideal
            Button button = (Button) viewer;
            button.setBorder(new Border(Border.STYLE_NONE, Color.WHITE, 1));
            button.setRolloverBorder(new Border(Border.STYLE_NONE, Color.WHITE, 1));
        }
        Label sizeLabel = getSizeLabel(ref.getSize());
        TableLayoutData layout = new TableLayoutData();
        layout.setAlignment(Alignment.ALIGN_RIGHT);
        sizeLabel.setLayoutData(layout);
        model.addRow(new Object[]{RowFactory.create(viewer), sizeLabel});

        updateAttachments();
    }

    /**
     * Returns the attachment references.
     *
     * @return the attachment references
     */
    public List<IMObjectReference> getAttachments() {
        List<IMObjectReference> result;
        if (documents.isEmpty()) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<IMObjectReference>();
            for (DocRef doc : documents) {
                result.add(doc.getReference());
            }
        }
        return result;
    }

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        modified = false;
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        listeners.addListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        // no-op
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return null;
    }

    /**
     * Disposes of this editor, deleting any temporary documents.
     */
    public void dispose() {
        component = null;
        for (DocRef doc : documents) {
            if (doc.getDelete()) {
                delete(doc.getReference());
            }
        }
        documents.clear();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        return header.validate(validator) && validator.validate(message);
    }

    /**
     * Creates a message editor.
     *
     * @param message the message property
     * @return a message editor
     */
    protected TextArea createMessageEditor(Property message) {
        TextArea result = BoundTextComponentFactory.createTextArea(message);
        result.setStyleName("MailEditor.message");
        return result;
    }

    /**
     * Invoked when the address or message updates. Refreshes the display and notifies listeners.
     */
    protected void onModified() {
        modified = true;
        listeners.notifyListeners(this);
    }

    /**
     * Creates the table to display attachments.
     */
    private void createAttachments() {
        model = new DefaultTableModel(2, 0);
        attachments = TableFactory.create(model, "MailEditor.attachments");
        attachments.setDefaultRenderer(Object.class, DefaultTableCellRenderer.INSTANCE);
        attachments.setDefaultHeaderRenderer(new AbstractTableCellRenderer() {
            @Override
            protected Component getComponent(Table table, Object value, int column, int row) {
                Component result = super.getComponent(table, value, column, row);
                if (column == 1) {
                    TableLayoutData layout = new TableLayoutData();
                    layout.setAlignment(Alignment.ALIGN_RIGHT);
                    result.setLayoutData(layout);
                }
                return result;
            }
        });
        attachments.setHeaderVisible(true);
        component.remove(header.getComponent());
        KeyStrokeListener listener = new KeyStrokeListener();
        listener.setCancelMode(true);
        listener.addKeyCombination(KeyStrokeListener.VK_DELETE);
        listener.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                int index = attachments.getSelectionModel().getMinSelectedIndex();
                if (index != -1 && index < documents.size()) {
                    deleteAttachment(index);
                }
            }
        });

        headerAttachmentsPane = SplitPaneFactory.create(SplitPane.ORIENTATION_HORIZONTAL, "MailEditor.grid",
                                                        header.getComponent(),
                                                        ColumnFactory.create(LARGE_INSET, attachments, listener));
        component.add(headerAttachmentsPane, 0);
    }

    /**
     * Updates the attachment summary.
     */
    private void updateAttachments() {
        long size = 0;
        for (DocRef doc : documents) {
            size += doc.getSize();
        }
        model.setColumnName(0, Messages.format("mail.attachments", documents.size()));
        model.setColumnName(1, getSize(size));
    }

    /**
     * Deletes an attachment.
     *
     * @param index the attachment index
     */
    private void deleteAttachment(int index) {
        DocRef ref = documents.get(index);
        if (ref.getDelete()) {
            delete(ref.getReference());
        }
        documents.remove(index);
        model.deleteRow(index);
        if (documents.isEmpty()) {
            component.remove(headerAttachmentsPane);
            component.add(header.getComponent(), 0);
            attachments = null;
        } else {
            updateAttachments();
        }
    }

    /**
     * Creates the component.
     *
     * @return the component
     */
    private SplitPane createComponent() {
        focus = new FocusGroup("MailEditor");

        int inset = StyleSheetHelper.getProperty("padding.large", 1);

        GridLayoutData rightInset = new GridLayoutData();
        rightInset.setInsets(new Insets(0, 0, inset, 0));

        TextArea messageArea = createMessageEditor(message);

        focus.add(header.getFocusGroup());
        focus.add(messageArea);
        focus.setDefault(header.getFocusGroup().getDefaultFocus());

        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "MailEditor", header.getComponent(),
                                       ColumnFactory.create(LARGE_INSET, messageArea));
    }

    /**
     * Deletes a document, given its reference.
     *
     * @param reference the document reference
     */
    private void delete(IMObjectReference reference) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        IMObject object = service.get(reference);
        if (object != null) {
            service.remove(object);
        }
    }

    /**
     * Downloads an attachment, if it has been double clicked.
     *
     * @param downloader the downloader to use
     * @param mimeType   the mime type. May be {@code null}
     * @param reference  the document reference
     */
    private void onDownload(Downloader downloader, String mimeType, IMObjectReference reference) {
        int hash = System.identityHashCode(downloader); // avoid holding onto the downloader reference
        if (monitor.isDoubleClick(hash)) {
            downloader.download(mimeType);
        }
        for (int i = 0; i < documents.size(); ++i) {
            if (documents.get(i).getReference().equals(reference)) {
                attachments.getSelectionModel().setSelectedIndex(i, true);
                break;
            }
        }
    }

    /**
     * Returns a label for the specified size.
     *
     * @param size the size
     * @return a label for the size
     */
    private Label getSizeLabel(long size) {
        String displaySize = getSize(size);
        Label label = LabelFactory.create();
        label.setText(displaySize);
        return label;
    }

    /**
     * Helper to format a size.
     *
     * @param size the size, in bytes
     * @return the formatted size
     */
    private String getSize(long size) {
        String result;

        if (size / FileUtils.ONE_GB > 0) {
            result = getSize(size, FileUtils.ONE_GB, "mail.size.GB");
        } else if (size / FileUtils.ONE_MB > 0) {
            result = getSize(size, FileUtils.ONE_MB, "mail.size.MB");
        } else if (size / FileUtils.ONE_KB > 0) {
            result = getSize(size, FileUtils.ONE_KB, "mail.size.KB");
        } else {
            result = Messages.format("mail.size.bytes", size);
        }
        return result;
    }

    /**
     * Helper to return a formatted size, rounded.
     *
     * @param size    the size
     * @param divisor the divisor
     * @param key     the resource bundle key
     * @return the formatted size
     */
    private String getSize(long size, long divisor, String key) {
        BigDecimal result = new BigDecimal(size).divide(BigDecimal.valueOf(divisor), BigDecimal.ROUND_CEILING);
        return Messages.format(key, result);
    }

    /**
     * Helper to track the properties of a document so that it need not reside in memory.
     */
    private static class DocRef {

        /**
         * The document reference.
         */
        private IMObjectReference ref;

        /**
         * The document name.
         */
        private String name;

        /**
         * The mime type.
         */
        private String mimeType;

        /**
         * The document size.
         */
        private long size;

        /**
         * Determines if the document needs to be deleted.
         */
        private boolean delete;

        /**
         * Constructs a {@code DocRef}.
         *
         * @param document the document
         * @param delete   {@code true} if the document needs to be deleted
         */
        public DocRef(Document document, boolean delete) {
            ref = document.getObjectReference();
            name = document.getName();
            mimeType = document.getMimeType();
            size = document.getDocSize();
            this.delete = delete;
        }

        /**
         * Returns the document name.
         *
         * @return the document name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the mime type.
         *
         * @return the mime type
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         * Returns the document's uncompressed size.
         *
         * @return the document size
         */
        public long getSize() {
            return size;
        }

        /**
         * Determines if the document should be deleted.
         *
         * @return {@code true} if the document should be deleted
         */
        public boolean getDelete() {
            return delete;
        }

        /**
         * Returns the document reference.
         *
         * @return the document reference
         */
        public IMObjectReference getReference() {
            return ref;
        }
    }
}