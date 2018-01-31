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

package org.openvpms.web.workspace.admin.job.scheduledreport;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.DocumentTemplatePrinter;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.report.ParameterType;
import org.openvpms.report.Report;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.PropertyComponentEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.SingleEntityLinkCollectionEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.mail.AddressSelector;
import org.openvpms.web.component.mail.EmailAddress;
import org.openvpms.web.component.mail.FromAddressSelector;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.ToAddressSelector;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.admin.job.AbstractJobConfigurationEditor;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Scheduled report job configuration editor.
 *
 * @author Tim Anderson
 */
public class ScheduledReportJobConfigurationEditor extends AbstractJobConfigurationEditor {

    /**
     * The report node name.
     */
    public static final String REPORT = "report";

    /**
     * The file node name.
     */
    public static final String FILE = "file";

    /**
     * The directory node name.
     */
    public static final String DIRECTORY = "directory";

    /**
     * The email node name.
     */
    public static final String EMAIL = "email";

    /**
     * The email from node name.
     */
    public static final String EMAIL_FROM = "emailFrom";

    /**
     * The email to node name.
     */
    public static final String EMAIL_TO = "emailTo";

    /**
     * The print node name.
     */
    public static final String PRINT = "print";

    /**
     * The printer node name.
     */
    public static final String PRINTER = "printer";

    // private final ToAddressSelector to;

    /**
     * The report factory.
     */
    private final ReportFactory reportFactory;

    /**
     * The report template link editor.
     */
    private final SingleEntityLinkCollectionEditor reportLink;

    /**
     * The printer selector.
     */
    private final SelectField printerSelector;

    /**
     * The printer editor.
     */
    private final PropertyComponentEditor printerEditor;

    /**
     * The maximum number of supported email addresses.
     */
    private int maxEmailTo;

    /**
     * The to-email addresses requested/in use.
     */
    private int emailTo;

    /**
     * The maximum number of supported parameters.
     */
    private int maxParameters;

    /**
     * The parameters requested/in use.
     */
    private int parameters;

    /**
     * The from-address selector.
     */
    private AddressSelector from;

    /**
     * The to-address selector.
     */
    private ToAddressSelector to;

    /**
     * The name node.
     */
    private static final String NAME = "name";

    /**
     * The description node.
     */
    private static final String DESCRIPTION = "description";


    /**
     * Constructs a {@link ScheduledReportJobConfigurationEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ScheduledReportJobConfigurationEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        reportFactory = ServiceHelper.getBean(ReportFactory.class);
        reportLink = new SingleEntityLinkCollectionEditor(getCollectionProperty(REPORT), object, layoutContext);
        reportLink.getComponent();

        // determine the number of report parameters in use
        int paramIndex = 0;
        parameters = 0;
        Property property;
        while ((property = getParamName(paramIndex)) != null) {
            paramIndex++;
            if (!StringUtils.isEmpty(property.getName())) {
                ++parameters;
            }
        }
        maxParameters = paramIndex;

        // initialise email
        MailContext mailContext = layoutContext.getMailContext();
        if (mailContext == null) {
            mailContext = new UserMailContext(layoutContext.getContext());
        }

        // initialise the email-from address selector
        List<Contact> fromContacts = new ArrayList<>(mailContext.getFromAddresses());
        String fromAddress = getProperty(EMAIL_FROM).getString();
        Contact fromContact = initFromContacts(fromAddress, fromContacts);
        from = new FromAddressSelector(fromContacts, mailContext.getFromAddressFormatter());
        from.setSelected(fromContact);
        from.addModifiableListener(modifiable -> getProperty(EMAIL_FROM).setValue(from.getAddress()));

        // collect the email-to addresses
        List<String> toAddresses = new ArrayList<>();
        int emailIndex = 0;
        Property toEmail;
        while ((toEmail = getEmailTo(emailIndex)) != null) {
            emailIndex++;
            String address = toEmail.getString();
            if (!StringUtils.isEmpty(address)) {
                toAddresses.add(address);
            }
        }
        emailTo = toAddresses.size();
        maxEmailTo = emailIndex;

        // initialise the email-to address selector
        List<Contact> toContacts = mailContext.getToAddresses();
        List<Contact> selected = new ArrayList<>();
        for (String toAddress : toAddresses) {
            Contact contact = AddressSelector.getContact(toContacts, toAddress);
            if (contact == null) {
                contact = AddressSelector.createContact(toAddress);
                if (contact != null) {
                    selected.add(contact);
                }
            }
        }
        to = new ToAddressSelector(toContacts, mailContext.getToAddressFormatter(), layoutContext, "mail.to");
        to.setSelected(selected);
        to.addModifiableListener(modifiable -> onEmailToSelected());

        // initialise the printer dropdown
        Property printerName = getProperty(PRINTER);
        DocumentTemplate template = getTemplate();
        ListModel model = (template != null) ? getPrinters(template) : new DefaultListModel();
        printerSelector = BoundSelectFieldFactory.create(printerName, model);
        printerEditor = new PropertyComponentEditor(printerName, printerSelector);
        getEditors().add(printerEditor);

        reportLink.addModifiableListener(modifiable -> onReportChanged());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        strategy.addComponent(new ComponentState(reportLink));
        strategy.addComponent(new ComponentState(printerEditor));
        strategy.addComponent(new ComponentState(from.getComponent(), getProperty(EMAIL_FROM)));
        strategy.addComponent(new ComponentState(to.getComponent(), getEmailTo(0)));
        return strategy;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateSelection(validator) && validateParameters(validator)
               && validateFile(validator) && validateEmail(validator) && validatePrint(validator);
    }

    /**
     * Determines if an editor should be disposed on layout change.
     * This implementation always returns true.
     *
     * @param editor the editor
     * @return {@code true}
     */
    @Override
    protected boolean disposeOnChangeLayout(Editor editor) {
        return editor != reportLink && editor != printerEditor;
    }

    /**
     * Invoked when the email-to addresses update.
     * <p>
     * Copies the selected addresses to the emailTo0,emailTo1,...emailToN properties.
     */
    private void onEmailToSelected() {
        String[] addresses = to.getAddresses();
        if (addresses == null) {
            addresses = new String[0];
        }
        emailTo = addresses.length;
        int i = 0;
        for (; i < addresses.length && i < maxEmailTo; ++i) {
            getEmailTo(i).setValue(addresses[i]);
        }
        // clear out the remaining addresses
        for (; i < maxEmailTo; ++i) {
            getEmailTo(i).setValue(null);
        }
    }

    /**
     * Initialises the email-from contacts.
     * <p>
     * This adds any selected contact to the list, if it isn't present.
     *
     * @param selected the selected address. May be {@code null}
     * @param contacts the contacts
     * @return the selected contact. May be {@code null}
     */
    private Contact initFromContacts(String selected, List<Contact> contacts) {
        Contact result = null;
        if (!StringUtils.isEmpty(selected)) {
            result = AddressSelector.getContact(contacts, selected);
            if (result == null) {
                EmailAddress address = EmailAddress.parse(selected);
                if (address != null) {
                    result = AddressSelector.createContact(address);
                    contacts.add(result);
                }
            }
        }
        return result;
    }

    /**
     * Validates that one of 'file', 'email', or 'print' is selected.
     *
     * @param validator the validator
     * @return {@code true} if one is selected, otherwise {@code false}
     */
    private boolean validateSelection(Validator validator) {
        boolean result = false;
        if (isFileSelected() || isEmailSelected() || isPrintSelected()) {
            result = true;
        } else {
            validator.add(this, new ValidatorError(Messages.get("scheduledreport.nothingselected")));
        }
        return result;
    }

    /**
     * Validates that the report doesn't have too many parameters.
     *
     * @param validator the validator
     * @return {@code true} if the report doesn't have too many parameters, otherwise {@code false}
     */
    private boolean validateParameters(Validator validator) {
        boolean result = false;
        if (parameters <= maxParameters) {
            result = true;
        } else {
            String message = Messages.format("scheduledreport.toomanyparameters", parameters, maxParameters);
            validator.add(this, new ValidatorError(message));
        }
        return result;
    }

    /**
     * Invoked when the report changes. Updates the parameters and available printers.
     */
    private void onReportChanged() {
        Set<ParameterType> parameterTypes = Collections.emptySet();
        DocumentTemplate template = getTemplate();
        if (template != null) {
            getProperty(NAME).setValue(template.getName());
            getProperty(DESCRIPTION).setValue(template.getDescription());
            Document document = template.getDocument();
            if (document != null) {
                Report report = reportFactory.createReport(document);
                parameterTypes = report.getParameterTypes();
            }
            ListModel model = getPrinters(template);
            printerSelector.setModel(model);
        }
        updateParameters(parameterTypes);
        onLayout();  // need to redisplay to pick up changes to parameters
    }

    /**
     * Returns the available printers.
     *
     * @param template the report template
     * @return the printers
     */
    private ListModel getPrinters(DocumentTemplate template) {
        List<String> printerNames = new ArrayList<>();
        for (DocumentTemplatePrinter printer : template.getPrinters()) {
            printerNames.add(printer.getPrinterName());
        }
        DefaultListModel model;
        if (!printerNames.isEmpty()) {
            model = new DefaultListModel(printerNames.toArray(new String[printerNames.size()]));
        } else {
            model = new DefaultListModel(PrintHelper.getPrinters());
        }
        return model;
    }

    /**
     * Returns the report template.
     *
     * @return the template, or {@code null} if none is selected
     */
    private DocumentTemplate getTemplate() {
        ScheduledReportTemplateEditor currentEditor = (ScheduledReportTemplateEditor) reportLink.getCurrentEditor();
        return (currentEditor != null) ? currentEditor.getTemplate() : null;
    }

    /**
     * Updates the parameters.
     *
     * @param parameterTypes the report parameter types
     */
    private void updateParameters(Set<ParameterType> parameterTypes) {
        List<ParameterType> list = new ArrayList<>();
        for (ParameterType type : parameterTypes) {
            if (isSupportedParameter(type)) {
                list.add(type);
            }
        }
        parameters = list.size();
        int i = 0;
        for (; i < list.size() && i < maxParameters; ++i) {
            ParameterType type = list.get(i);
            setParameter(i, type);
        }
        for (; i < maxParameters; ++i) {
            setParameter(i, null, null, null, null);
        }
    }

    /**
     * Sets the parameter with the given index to that specified.
     *
     * @param index the parameter index
     * @param type  the parameter type
     */
    private void setParameter(int index, ParameterType type) {
        setParameter(index, type.getName(), type.getDescription(), type.getType().getName(), type.getDefaultValue());
    }

    /**
     * Sets the parameter with the given index to that specified.
     *
     * @param index       the parameter index
     * @param name        the parameter name. May be {@code null}
     * @param displayName the parameter display name. May be {@code null}
     * @param type        the parameter type. May be {@code null}
     * @param value       the parameter value. May be {@code null}
     */
    private void setParameter(int index, String name, String displayName, String type, Object value) {
        Property paramName = getParamName(index);
        Property paramDisplayName = getParamDisplayName(index);
        Property paramType = getParamType(index);
        Property paramValue = getParamValue(index);
        paramName.setValue(name);
        paramType.setValue(type);
        paramDisplayName.setValue(displayName);
        paramValue.setValue(value);
    }

    /**
     * Determines if a parameter is supported.
     *
     * @param parameterType the parameter type
     * @return {@code true} if the parameter type is supported
     */
    private boolean isSupportedParameter(ParameterType parameterType) {
        if (!parameterType.isSystem()) {
            SimpleProperty dummy = new SimpleProperty("dummy", parameterType.getType());
            return dummy.isString() || dummy.isBoolean() || dummy.isNumeric() || dummy.isDate();
        }
        return false;
    }

    /**
     * Validates that when file is selected, the path is valid and can be written to.
     *
     * @param validator the validator
     * @return {@code true} if the file information is valid, otherwise {@code false}
     */
    private boolean validateFile(Validator validator) {
        boolean result = false;
        if (isFileSelected()) {
            Property property = getProperty(DIRECTORY);
            String pathName = property.getString();
            if (StringUtils.isEmpty(pathName)) {
                reportRequired(property, validator);
            } else {
                try {
                    Path path = FileSystems.getDefault().getPath(pathName);
                    if (!Files.exists(path)) {
                        validator.add(property, new ValidatorError(property,
                                                                   Messages.format("dir.notfound", pathName)));
                    } else if (!Files.isDirectory(path)) {
                        validator.add(property, new ValidatorError(property, Messages.format("dir.notdir", pathName)));
                    } else if (!Files.isWritable(path)) {
                        validator.add(property, new ValidatorError(property,
                                                                   Messages.format("dir.notwritable", pathName)));
                    } else {
                        result = true;
                    }
                } catch (InvalidPathException exception) {
                    validator.add(property, new ValidatorError(property, Messages.format("dir.invalid", pathName)));
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Determines if the file option is selected.
     *
     * @return {@code true} if the file option is selected
     */
    private boolean isFileSelected() {
        return getProperty(FILE).getBoolean();
    }

    /**
     * Validates that when email is selected, the from and to addresses are valid.
     *
     * @param validator the validator
     * @return {@code true} if the email information is valid, otherwise {@code false}
     */
    private boolean validateEmail(Validator validator) {
        boolean result = false;
        if (isEmailSelected()) {
            Property from = getProperty(EMAIL_FROM);
            if (StringUtils.isEmpty(from.getString())) {
                reportRequired(from, validator);
            } else if (emailTo == 0) {
                reportRequired(getEmailTo(0), validator);
            } else if (emailTo > maxEmailTo) {
                String message = Messages.format("scheduledreport.toomanyemails", emailTo, maxEmailTo);
                validator.add(this, new ValidatorError(message));
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Determines if the email option is selected.
     *
     * @return {@code true} if the email option is selected
     */
    private boolean isEmailSelected() {
        return getProperty(EMAIL).getBoolean();
    }

    /**
     * Validates that when print is selected, the printer .
     *
     * @param validator the validator
     * @return {@code true} if the email information is valid, otherwise {@code false}
     */
    private boolean validatePrint(Validator validator) {
        boolean result = false;
        if (isPrintSelected()) {
            Property printer = getProperty(PRINTER);
            if (StringUtils.isEmpty(printer.getString())) {
                reportRequired(printer, validator);
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Determines if the print option is selected.
     *
     * @return {@code true} if the print option is selected
     */
    private boolean isPrintSelected() {
        return getProperty(PRINT).getBoolean();
    }

    /**
     * Returns an email-to property, given its index.
     *
     * @param index the email index
     * @return property, or {@code null} if none is found
     */
    private Property getEmailTo(int index) {
        return getProperty("emailTo" + index);
    }

    /**
     * Returns a parameter name property, given its index.
     *
     * @param index the index
     * @return property, or {@code null} if none is found
     */
    private Property getParamName(int index) {
        return getProperty("paramName" + index);
    }

    /**
     * Returns a parameter display name property, given its index.
     *
     * @param index the index
     * @return property, or {@code null} if none is found
     */
    private Property getParamDisplayName(int index) {
        return getProperty("paramDisplayName" + index);
    }

    /**
     * Returns a parameter type property, given its index.
     *
     * @param index the index
     * @return property, or {@code null} if none is found
     */
    private Property getParamType(int index) {
        return getProperty("paramType" + index);
    }

    /**
     * Returns a parameter value property, given its index.
     *
     * @param index the index
     * @return property, or {@code null} if none is found
     */
    private Property getParamValue(int index) {
        return getProperty("paramValue" + index);
    }
}
