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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.macro.Macros;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundRichTextArea;
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
import org.openvpms.web.component.property.StringPropertyTransformer;
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
import org.openvpms.web.echo.text.MacroExpander;
import org.openvpms.web.echo.text.RichTextArea;
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
    private List<DocRef> documents = new ArrayList<>();

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
     * The message editor.
     */
    private RichTextArea messageEditor;

    /**
     * The macro expanded.
     */
    private final MacroExpander macroExpander;

    /**
     * The object to evaluate templates against.
     */
    private Object object;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MailEditor.class);

    /**
     * Constructs a {@link MailEditor}.
     * <p>
     * If no 'to' addresses are supplied the address will be editable, otherwise it will be read-only.
     * If there are multiple addresses, they will be displayed in a dropdown, and the preferred contact selected
     *
     * @param mailContext the mail context
     * @param preferredTo the preferred 'to' address. May be {@code null}
     * @param context     the context
     */
    public MailEditor(final MailContext mailContext, Contact preferredTo, LayoutContext context) {
        header = createHeader(mailContext, preferredTo, context);
        this.context = context.getContext();
        this.help = context.getHelpContext();
        final Variables variables = mailContext.getVariables();
        final Macros macros = ServiceHelper.getMacros();

        message = new SimpleProperty("message", null, String.class, Messages.get("mail.message"));
        message.setTransformer(new StringPropertyTransformer(message, false) {
            protected void checkCharacters(String string) {
                // no-op. OpenOffice doesn't use HTML entities for ASCII characters used by Word (e.g 226 for emdash)
                // They seem to be translated correctly in the browser however.
            }
        });
        message.setRequired(false);
        message.setMaxLength(-1);     // no maximum length
        message.setValue(" ");        // hack so that Firefox displays the correct font size etc
        // message.addModifiableListener(listener); TODO
        macroExpander = new MacroExpander() {
            @Override
            public String expand(String macro) {
                String result = null;
                try {
                    result = macros.run(macro, mailContext.getMacroContext(), variables);
                } catch (Throwable exception) {
                    log.error("Failed to expand macro: " + macro, exception);
                }
                return result;
            }
        };
        messageEditor = createMessageEditor(message);
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
     * @return the message to send. May be {@code null}
     */
    public String getMessage() {
        String result = null;
        String value = message.getString();
        if (!StringUtils.isBlank(value)) {
            // make sure the result is well formed html.
            result = "<html><body>" + value + "</body></html>";
        }
        return result;
    }

    /**
     * Sets the message to send.
     *
     * @param message the message to send. May be {@code null}
     */
    public void setMessage(String message) {
        if (message != null) {
            message = filter(message);
        }
        this.message.setValue(message);
    }

    /**
     * Sets the mail subject and message from a template.
     * <p>
     * The template will be evaluated against the object set via {@link #setObject(Object)}.
     *
     * @param template the template
     */
    public void setContent(Entity template) {
        setContent(template, false);
    }

    /**
     * Sets the mail subject and message from a template.
     * <p>
     * The template will be evaluated against the object set via {@link #setObject(Object)}.
     *
     * @param template the template
     * @param prompt   if {@code true}, prompt for parameters
     */
    public void setContent(Entity template, boolean prompt) {
        ParameterEmailTemplateEvaluator evaluator = new ParameterEmailTemplateEvaluator(template, context, help);
        evaluator.evaluate(object, prompt, new ParameterEmailTemplateEvaluator.Listener() {
            @Override
            public void generated(String subject, String message) {
                setSubject(subject);
                setMessage(message);
            }
        });
    }

    /**
     * Registers the object used to evaluate templates against.
     *
     * @param object the object. May be {@code null}
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Returns the object used to evaluate templates against.
     *
     * @return the object used to evaluate templates against. May be {@code null}
     */
    public Object getObject() {
        return object;
    }

    /**
     * Adds an attachment.
     * <p>
     * If the document is unsaved, it will be saved and deleted on {@link #dispose()}.
     *
     * @param document the document to add
     */
    public void addAttachment(Document document) {
        if (attachments == null) {
            createAttachments();
        }

        boolean delete = false;

        Converter converter = ServiceHelper.getBean(Converter.class);
        if (document.getMimeType() != null && !DocFormats.PDF_TYPE.equals(document.getMimeType()) &&
            converter.canConvert(document.getName(), document.getMimeType(), DocFormats.PDF_TYPE)) {
            document = converter.convert(document, DocFormats.PDF_TYPE);
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
            result = new ArrayList<>();
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
     * Returns the mail context.
     *
     * @return the mail context
     */
    public MailContext getMailContext() {
        return header.getMailContext();
    }

    /**
     * Returns the listener for keyboard shortcuts.
     *
     * @return the listener
     */
    public KeyStrokeListener getKeyStrokeListener() {
        return messageEditor.getListener();
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
     * Returns the header.
     *
     * @return the header
     */
    protected MailHeader getHeader() {
        return header;
    }

    /**
     * Creates the mail header.
     *
     * @param mailContext the mail context
     * @param preferredTo the preferred to address
     * @param context     the layout context
     * @return a new header
     */
    protected MailHeader createHeader(MailContext mailContext, Contact preferredTo, LayoutContext context) {
        return new MailHeader(mailContext, preferredTo, context);
    }

    /**
     * Creates a message editor.
     *
     * @param message the message property
     * @return a message editor
     */
    protected RichTextArea createMessageEditor(Property message) {
        BoundRichTextArea result = new BoundRichTextArea(message);
        result.setMacroExpander(macroExpander);
        result.setStyleName("MailEditor.message");
        return result;
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
    protected SplitPane createComponent() {
        focus = new FocusGroup("MailEditor");

        int inset = StyleSheetHelper.getProperty("padding.large", 1);

        GridLayoutData rightInset = new GridLayoutData();
        rightInset.setInsets(new Insets(0, 0, inset, 0));

        SplitPane component = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL, "MailEditor", header.getComponent(),
                ColumnFactory.create(LARGE_INSET, messageEditor));
        focus.add(header.getFocusGroup());
        focus.add(messageEditor);
        focus.setDefault(header.getFocusGroup().getDefaultFocus());
        return component;
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
     * Filters html to extract the inner html of the body tag. This is required by the rich text area editor.
     *
     * @param html the html to filter
     * @return the filtered html
     */
    private String filter(String html) {
        return HtmlFilter.filter(html);
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