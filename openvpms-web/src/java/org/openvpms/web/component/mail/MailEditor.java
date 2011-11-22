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
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.echo.TextField;
import org.openvpms.web.component.echo.DropDown;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.list.AbstractListCellRenderer;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.Modifiable;
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
import org.openvpms.web.component.util.ListBoxFactory;
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
     * The from-address selector, if multiple addresses are provided.
     */
    private SelectField fromAddressSelector;

    /**
     * The 'from' addresses.
     */
    private List<Contact> fromAddresses;

    /**
     * The 'to' addresses.
     */
    private List<Contact> toAddresses;

    /**
     * The selected 'from' contact.
     */
    private Contact selectedFrom;

    /**
     * The selected 'to' contact.
     */
    private Contact selectedTo;

    /**
     * The from address.
     */
    private SimpleProperty from;

    /**
     * The 'to' listener.
     */
    private ModifiableListener toListener;

    /**
     * The 'to' address.
     */
    private SimpleProperty to;

    /**
     * The subject.
     */
    private SimpleProperty subject;

    /**
     * The message body property. Used to support macro expansion.
     */
    private SimpleProperty message;

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
     * Default extent - 100%.
     */
    private static final Extent EXTENT = new Extent(100, Extent.PERCENT);


    /**
     * Constructs a <tt>MailEditor</tt>.
     * <p/>
     * If no 'to' addresses are supplied the address will be editable, otherwise it will be read-only.
     * If there are multiple addresses, they will be displayed in a dropdown, with the first no. as the default
     *
     * @param fromAddresses the available 'from' addresses
     * @param toAddresses   the available 'to' addresses
     * @param preferredTo   the preferred 'to' address. May be <tt>null</tt>
     */
    public MailEditor(List<Contact> fromAddresses, List<Contact> toAddresses, Contact preferredTo) {
        this.fromAddresses = fromAddresses;
        this.toAddresses = toAddresses;
        from = createProperty("from", "mail.from");
        from.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
//                fromAddressChanged();
            }
        });

        to = createProperty("to", "mail.to");
        toListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                toAddressChanged();
            }
        };
        to.addModifiableListener(toListener);

        if (preferredTo != null) {
            setTo(preferredTo);
        } else if (!toAddresses.isEmpty()){
            setTo(toAddresses.get(0));
        }

        subject = createProperty("subject", "mail.subject");
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onModified();
            }
        };
        subject.addModifiableListener(listener);

        message = createProperty("message", "mail.message");
        message.setTransformer(new StringPropertyTransformer(message, new Object(), false));
        message.addModifiableListener(listener);
    }

    /**
     * Declares a variable to be used in macro expansion.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void declareVariable(String name, Object value) {
        StringPropertyTransformer transformer = (StringPropertyTransformer) message.getTransformer();
        transformer.getMacroEvaluator().declareVariable(name, value);
    }

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    public void setFrom(Contact from) {
        this.selectedFrom = from;
        this.from.setValue(getFormattedAddress(from));
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        return getEmailAddress(selectedFrom);
    }

    /**
     * Sets the 'to' address.
     *
     * @param toAddress the to address. May be <tt>null</tt>
     */
    public void setTo(Contact toAddress) {
        selectedTo = toAddress;
        to.removeModifiableListener(toListener);
        try {
            to.setValue(getFormattedAddress(toAddress));
        } finally {
            to.addModifiableListener(toListener);
        }
    }

    /**
     * Returns the from name.
     *
     * @return the from name
     */
    public String getFromName() {
        String name = null;
        if (selectedFrom != null && selectedFrom.getParty() != null) {
            name = selectedFrom.getParty().getName();
        }
        return name;
    }

    /**
     * Returns the to address.
     *
     * @return the to address. May be <tt>null</tt>
     */
    public String getTo() {
        return getEmailAddress(selectedTo);
    }

    /**
     * Sets the message subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject.setValue(subject);
    }

    /**
     * Returns the message subject.
     *
     * @return the message subject
     */
    public String getSubject() {
        return (String) subject.getValue();
    }

    /**
     * Sets the message to send.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message.setValue(message);
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
        return validator.validate(from) && validator.validate(to) && validator.validate(subject)
               && validator.validate(message);
    }

    /**
     * Creates an address selector for a list of addresses.
     *
     * @param addresses the addresses
     * @return an address editor
     */
    protected SelectField createAddressSelector(List<Contact> addresses) {
        SelectField result = SelectFieldFactory.create(addresses);
        result.setCellRenderer(EmailCellRenderer.INSTANCE);
        result.setWidth(EXTENT);
        return result;
    }

    /**
     * Creates a message editor.
     *
     * @param message the message property
     * @return a message editor
     */
    protected BoundTextArea createMessageEditor(Property message) {
        BoundTextArea result = new BoundTextArea(message, 132, 40);
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

    private void toAddressChanged() {
        String text = (String) to.getValue();
        Contact result = null;
        if (!StringUtils.isEmpty(text)) {
            int start = text.indexOf('<');
            int end = text.indexOf('>');
            if (start != -1 && end != -1) {
                text = text.substring(start + 1, end);
            }
            for (Contact contact : toAddresses) {
                String email = ContactHelper.getEmail(contact);
                if (StringUtils.equals(text, email)) {
                    result = contact;
                    break;
                }
            }
            if (result == null) {
                result = (Contact) ServiceHelper.getArchetypeService().create(ContactArchetypes.EMAIL);
                IMObjectBean bean = new IMObjectBean(result);
                bean.setValue("emailAddress", text);
            }
        }
        selectedTo = result;
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
     * Creates the component.
     *
     * @return the component
     */
    private SplitPane createComponent() {
        focus = new FocusGroup("MailEditor");

        Component fromAddress;
        if (fromAddresses.size() <= 1) {
            TextField fromText = TextComponentFactory.create(from, 40);
            fromText.setWidth(EXTENT);
            fromAddress = fromText;
            fromAddress.setEnabled(false);
            if (fromAddresses.size() == 1) {
                setFrom(fromAddresses.get(0));
            }
        } else {
            fromAddressSelector = createAddressSelector(fromAddresses);
            selectedFrom = (Contact) fromAddressSelector.getSelectedItem();
            fromAddressSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    selectedFrom = (Contact) fromAddressSelector.getSelectedItem();
                    onModified();
                }
            });
            fromAddress = fromAddressSelector;
            focus.add(fromAddressSelector);
        }

        TextField toText = TextComponentFactory.create(to, 40);
        toText.setWidth(EXTENT);
        Component toAddress = toText;
        if (!toAddresses.isEmpty()) {
            final ListBox toAddressSelector = ListBoxFactory.create(toAddresses);
            toAddressSelector.setWidth(EXTENT);
            toAddressSelector.setCellRenderer(EmailCellRenderer.INSTANCE);

            final DropDown toAddressDropDown = new DropDown();
            toAddressDropDown.setWidth(EXTENT);
            toAddress = toAddressDropDown;
            toAddressDropDown.setTarget(toText);
            toAddressDropDown.setPopUpAlwaysOnTop(true);
            toAddressDropDown.setFocusOnExpand(true);
            toAddressDropDown.setPopUp(toAddressSelector);
            toAddressDropDown.setFocusComponent(toText);
            toAddressSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    setTo((Contact) toAddressSelector.getSelectedValue());
                    toAddressDropDown.setExpanded(false);
                    onModified();
                }
            });
            focus.add(toAddressSelector);
        } else {
            focus.add(toText);
        }

        TextField subjectText = TextComponentFactory.create(subject, 40);
        subjectText.setWidth(EXTENT);

        TextArea messageArea = createMessageEditor(message);

        focus.add(subjectText);
        focus.add(messageArea);
        focus.setDefault(subjectText);

        GridLayoutData align = new GridLayoutData();
        align.setAlignment(Alignment.ALIGN_RIGHT);
        align.setInsets(new Insets(0, 0, 10, 0));

        grid = GridFactory.create(2, createLabel(from, align), fromAddress,
                                  createLabel(to, align), toAddress,
                                  createLabel(subject, align), subjectText);
        grid.setColumnWidth(0, new Extent(10, Extent.PERCENT));
        grid.setWidth(EXTENT);
        messageArea.setWidth(EXTENT);

        gridColumn = ColumnFactory.create("Inset.Large", grid);
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "MailEditor", gridColumn,
                                       ColumnFactory.create("Inset.Large", messageArea));
    }

    /**
     * Helper to create a mandatory property.
     *
     * @param name the property name
     * @param key  the message resource bundle key
     * @return a new property
     */
    private SimpleProperty createProperty(String name, String key) {
        SimpleProperty result = new SimpleProperty(name, String.class);
        result.setDisplayName(Messages.get(key));
        result.setRequired(true);
        return result;
    }

    /**
     * Helper to create a label for a property.
     *
     * @param property the property
     * @param layout   the layout
     * @return a new label
     */
    private Label createLabel(Property property, LayoutData layout) {
        Label label = LabelFactory.create();
        label.setText(property.getDisplayName());
        label.setLayoutData(layout);
        return label;
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
     * Helper to return a formatted email address for an email contact.
     *
     * @param contact the email contact
     * @return the formatted email address
     */
    private static String getFormattedAddress(Contact contact) {
        return Messages.get("mail.contact", contact.getParty().getName(), getEmailAddress(contact));
    }

    /**
     * Helper to return the email address for a contact.
     *
     * @param contact the contact. May be <tt>null</tt>
     * @return the email address. May be <tt>null</tt>
     */
    private static String getEmailAddress(Contact contact) {
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact);
            return bean.getString("emailAddress");
        }
        return null;
    }

    private static class EmailCellRenderer extends AbstractListCellRenderer<Contact> {

        /**
         * The singleton instance.
         */
        public static ListCellRenderer INSTANCE = new EmailCellRenderer();

        /**
         * Constructs an <tt>EmailCellRenderer</tt>.
         */
        private EmailCellRenderer() {
            super(Contact.class);
        }

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render. May be <tt>null</tt>
         * @param index  the object index
         * @return the rendered object
         */
        protected Object getComponent(Component list, Contact object, int index) {
            String result = null;
            if (object != null) {
                result = getFormattedAddress(object);
            }
            return result;
        }

        /**
         * Determines if an object represents 'All'.
         *
         * @param list   the list component
         * @param object the object. May be <tt>null</tt>
         * @param index  the object index
         * @return <tt>false</tt>
         */
        protected boolean isAll(Component list, Contact object, int index) {
            return false;
        }

        /**
         * Determines if an object represents 'None'.
         *
         * @param list   the list component
         * @param object the object. May be <tt>null</tt>
         * @param index  the object index
         * @return <tt>false</tt>
         */
        protected boolean isNone(Component list, Contact object, int index) {
            return false;
        }
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