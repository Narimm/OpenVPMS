package org.openvpms.web.workspace.reporting.insurance;

import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.insurance.InsuranceCRUDWindow;
import org.openvpms.web.workspace.reporting.report.SQLReportPrinter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * A CRUD window for insurance claims.
 * <p>
 * This prevents creation of claims and policies.
 *
 * @author Tim Anderson
 */
public class ClaimCRUDWindow extends InsuranceCRUDWindow {

    /**
     * The claim query.
     */
    private final ClaimQuery query;

    /**
     * Report button identifier.
     */
    private static final String REPORT_ID = "button.report";

    /**
     * The report type.
     */
    private static final String REPORT_TYPE = "INSURANCE_CLAIMS_REPORT";

    /**
     * Constructs an {@link ClaimCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     * @param query   the query
     */
    public ClaimCRUDWindow(Context context, HelpContext help, ClaimQuery query) {
        super(true, context, help);
        this.query = query;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(REPORT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onReport();
            }
        });
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(Act object, LayoutContext context) {
        updateContext(object, context);
        return super.createEditor(object, context);
    }

    /**
     * Prints a report.
     */
    protected void onReport() {
        Context context = getContext();
        ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(REPORT_TYPE, context);
        DocumentTemplate template = locator.getTemplate();
        LookupNameHelper.getLookupNames(DocumentArchetypes.DOCUMENT_TEMPLATE, "archetype");
        String name = ServiceHelper.getLookupService().getName(DocumentArchetypes.DOCUMENT_TEMPLATE, "archetype",
                                                               REPORT_TYPE);
        if (name == null) {
            name = REPORT_TYPE;
        }

        if (template == null) {
            throw new ReportException(ReportException.ErrorCode.NoTemplateForArchetype, name);
        }
        FileNameFormatter formatter = ServiceHelper.getBean(FileNameFormatter.class);
        DataSource dataSource = ServiceHelper.getBean("reportingDataSource", DataSource.class);
        ReportFactory reportFactory = ServiceHelper.getBean(ReportFactory.class);
        SQLReportPrinter printer = new SQLReportPrinter(template, context, reportFactory, formatter, dataSource,
                                                        ServiceHelper.getArchetypeService());
        Map<String, Object> parameters = getParameters();
        printer.setParameters(parameters);
        String title = Messages.format("imobject.print.title", name);
        HelpContext help = getHelpContext().subtopic("report");
        InteractivePrinter iPrinter = new InteractivePrinter(title, printer, context, help);
        iPrinter.setMailContext(getMailContext());
        iPrinter.print();
    }

    /**
     * Returns the report parameters.
     *
     * @return report parameters
     */
    private Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        String id = StringUtils.chomp(query.getValue(), "*");
        parameters.put("id", id);
        parameters.put("from", query.getFrom());
        parameters.put("to", query.getTo());
        Party location = query.getLocation();
        if (location != null) {
            parameters.put("locationId", location.getId());
            parameters.put("locationName", location.getName());
        }
        parameters.put("status", query.getStatus());
        parameters.put("gapStatus", query.getGapStatus());
        Party insurer = query.getInsurer();
        if (insurer != null) {
            parameters.put("insurerId", insurer.getId());
            parameters.put("insurerName", insurer.getName());
        }
        User clinician = query.getClinician();
        if (clinician != null) {
            parameters.put("clinicianId", clinician.getId());
            parameters.put("clinicianName", clinician.getName());
        }
        return parameters;
    }

    /**
     * Updates the context with the claim customer and patient.
     *
     * @param object  the claim
     * @param context the context to update
     */
    private void updateContext(Act object, LayoutContext context) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        IMObjectBean claimBean = service.getBean(object);
        Party patient = claimBean.getTarget("patient", Party.class);
        if (patient == null) {
            throw new IllegalStateException("Cannot determine patient associated with claim");
        }
        Act policy = claimBean.getTarget("policy", Act.class);
        if (policy == null) {
            throw new IllegalStateException("Cannot determine policy associated with claim");
        }
        IMObjectBean policyBean = service.getBean(policy);
        Party customer = policyBean.getTarget("customer", Party.class);
        if (customer == null) {
            throw new IllegalStateException("Cannot determine customer associated with claim");
        }
        context.getContext().setCustomer(customer);
        context.getContext().setPatient(patient);
    }
}
