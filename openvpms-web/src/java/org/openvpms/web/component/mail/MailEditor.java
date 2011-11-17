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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.mail;

import echopointng.KeyStrokeListener;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.echo.TextField;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.table.AbstractTableCellRenderer;
import org.openvpms.web.component.table.DefaultTableCellRenderer;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.util.TableFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.resource.util.Styles;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An editor for mail messages.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailEditor extends AbstractModifiable {

    /**
     * The from address.
     */
    private Label fromAddress;

    /**
     * The email address if 0 or 1 address are provided.
     */
    private Label toAddress;

    /**
     * The address selector, if multiple addresses are provided.
     */
    private SelectField addressSelector;

    /**
     * The subject.
     */
    private TextField subject;

    /**
     * The text message.
     */
    private TextArea message;

    /**
     * The text property. Used to support macro expansion.
     */
    private Property property;

    /**
     * Determines if this has been modified.
     */
    private boolean modified;

    /**
     * The attachments table. May be <tt>null</tt>.
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

    private static final Extent EXTENT = new Extent(100, Extent.PERCENT);

    /**
     * The split pane holding the grid and attachments.
     */
    private SplitPane gridAttachmentsPane;

    /**
     * The grid containing the from, to, and subject.
     */
    private Grid grid;

    /**
     * The attachment table model.
     */
    private DefaultTableModel model;

    /**
     * The mail editor component.
     */
    private SplitPane component;

    /**
     * The column holding the grid, when no attachments are being displayed.
     */
    private Column gridColumn;


    /**
     * Constructs a <tt>MailEditor</tt>.
     */
    public MailEditor() {
        this((String) null);
    }

    /**
     * Constructs a <tt>MailEditor</tt>.
     *
     * @param address the address. May be <tt>null</tt>
     */
    public MailEditor(String address) {
        this(address != null ? new String[]{address} : null);
    }

    /**
     * Constructs a <tt>MailEditor</tt>.
     * <p/>
     * If no addresses are supplied, the address will be editable, otherwise it will be read-only.
     * If there are multiple addresses, they will be displayed in a dropdown, with the first no. as the default
     *
     * @param addresses the available addresses. May be <tt>null</tt>
     */
    public MailEditor(String[] addresses) {
        fromAddress = LabelFactory.create();
        int length = (addresses == null) ? 0 : addresses.length;
        if (length <= 1) {
            toAddress = LabelFactory.create();
            if (length == 1) {
                toAddress.setText(addresses[0]);
            }
        } else {
            addressSelector = createAddressSelector(addresses);
            addressSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onModified();
                }
            });
        }

        subject = TextComponentFactory.create(40);
        subject.setWidth(EXTENT);
        subject.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onModified();
            }
        });

        property = new SimpleProperty("property", String.class);
        property.setTransformer(new StringPropertyTransformer(property, new Object(), false));
        message = createMessageEditor();
        message.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onModified();
            }
        });
        focus = new FocusGroup("MailEditor");
        if (toAddress != null) {
            focus.add(toAddress);
        } else {
            focus.add(addressSelector);
        }
        focus.add(message);
        focus.setDefault(message);
    }

    /**
     * Declares a variable to be used in macro expansion.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void declareVariable(String name, Object value) {
        StringPropertyTransformer transformer = (StringPropertyTransformer) property.getTransformer();
        transformer.getMacroEvaluator().declareVariable(name, value);
    }

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    public void setFrom(String from) {
        fromAddress.setText(from);
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        return fromAddress.getText();
    }

    /**
     * Sets the address to send to.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        if (this.toAddress != null) {
            this.toAddress.setText(address);
        } else {
            addressSelector.setSelectedItem(address);
        }
    }

    /**
     * Returns the address.
     *
     * @return the address. May be <tt>null</tt>
     */
    public String getAddress() {
        String result = null;
        if (toAddress != null) {
            result = toAddress.getText();
        } else if (addressSelector.getSelectedItem() != null) {
            result = addressSelector.getSelectedItem().toString();
        }
        return result;
    }

    /**
     * Sets the message subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject.setText(subject);
    }

    /**
     * Returns the message subject.
     *
     * @return the message subject
     */
    public String getSubject() {
        return subject.getText();
    }

    /**
     * Sets the message to send.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        property.setValue(message);
    }

    /**
     * Returns the message to send.
     *
     * @return the message to send
     */
    public String getMessage() {
        Object result = property.getValue();
        return (result != null) ? result.toString() : null;
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
        if (document.isNew()) {
            ServiceHelper.getArchetypeService().save(document);
            delete = true;
        }
        DocRef ref = new DocRef(document, delete);
        documents.add(ref);
        Component viewer = new DocumentViewer(ref.getReference(), null, ref.getName(), true).getComponent();
        if (viewer instanceof Button) {
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
        Component to = (toAddress != null) ? toAddress : addressSelector;
        GridLayoutData align = new GridLayoutData();
        align.setAlignment(Alignment.ALIGN_RIGHT);
        align.setInsets(new Insets(0, 0, 10, 0));

        Label fromLabel = LabelFactory.create("mail.from");
        fromLabel.setLayoutData(align);

        Label toLabel = LabelFactory.create("mail.to");
        toLabel.setLayoutData(align);

        Label subjectLabel = LabelFactory.create("mail.subject");
        subjectLabel.setLayoutData(align);

        grid = GridFactory.create(2, fromLabel, fromAddress,
                                  toLabel, to,
                                  subjectLabel, subject);
        grid.setColumnWidth(0, new Extent(10, Extent.PERCENT));
        grid.setWidth(EXTENT);
        message.setWidth(EXTENT);

        gridColumn = ColumnFactory.create("Inset.Large", grid);
        component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "MailEditor", gridColumn,
                                            ColumnFactory.create("Inset.Large", message));
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
     * @return <tt>true</tt> if the object has been modified
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
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
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
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    protected boolean doValidation(Validator validator) {
        return !StringUtils.isEmpty(getFrom()) && !StringUtils.isEmpty(getAddress())
               && !StringUtils.isEmpty(getSubject()) && !StringUtils.isEmpty(getMessage());
    }

    /**
     * Creates an address selector for a list of addresses.
     *
     * @param addresses the addresses
     * @return an address editor
     */
    protected SelectField createAddressSelector(String[] addresses) {
        return SelectFieldFactory.create(addresses);
    }

    /**
     * Creates a message editor.
     *
     * @return a message editor
     */
    protected BoundTextArea createMessageEditor() {
        BoundTextArea result = new BoundTextArea(property, 132, 40);
        result.setStyleName(Styles.DEFAULT);
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
        component.remove(gridColumn);
        KeyStrokeListener listener = new KeyStrokeListener();
        listener.setCancelMode(true);
        listener.addKeyCombination(KeyStrokeListener.VK_DELETE);
        listener.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                int index = attachments.getSelectionModel().getMinSelectedIndex();
                if (index != -1) {
                    deleteAttachment(index);
                }
            }
        });

        gridAttachmentsPane = SplitPaneFactory.create(SplitPane.ORIENTATION_HORIZONTAL, "MailEditor.grid",
                                                      ColumnFactory.create("Inset.Large", grid),
                                                      ColumnFactory.create("Inset.Large", attachments, listener));
        component.add(gridAttachmentsPane, 0);
    }

    /**
     * Updates the attachment summary.
     */
    private void updateAttachments() {
        long size = 0;
        for (DocRef doc : documents) {
            size += doc.getSize();
        }
        model.setColumnName(0, Messages.get("mail.attachments", documents.size()));
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
            component.remove(gridAttachmentsPane);
            gridColumn.add(grid);
            component.add(gridColumn, 0);
            attachments = null;
        } else {
            updateAttachments();
        }
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
            result = Messages.get("mail.size.bytes", size);
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
        return Messages.get(key, result);
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
         * Constructs a <tt>DocRef</tt>.
         *
         * @param document the document
         * @param delete   <tt>true</tt> if the document needs to be deleted
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
         * @return <tt>true</tt> if the document should be deleted
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