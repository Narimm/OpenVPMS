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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Task to optionally print <em>act.patientDocumentForm</em> and <em>act.patientDocumentLetter</em> for a patient.
 *
 * @author Tim Anderson
 */
class PrintPatientDocumentsTask extends Tasks {

    /**
     * Constructs a {@link PrintPatientDocumentsTask}.
     */
    public PrintPatientDocumentsTask(HelpContext help) {
        super(help);
        setRequired(false);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    @Override
    public void start(final TaskContext context) {
        Entity schedule = CheckInHelper.getSchedule(context);
        Entity worklist = CheckInHelper.getWorkList(context);
        if (hasTemplates(schedule) || hasTemplates(worklist)) {
            Party patient = context.getPatient();
            if (patient == null) {
                throw new ContextException(ContextException.ErrorCode.NoPatient);
            }

            String title = Messages.get("workflow.print.title");
            Query<Entity> query;
            query = new ScheduleDocumentTemplateQuery(schedule, worklist);
            final DocumentTemplateBrowser browser = new DocumentTemplateBrowser(
                    query, new DefaultLayoutContext(context, context.getHelpContext()));
            final BrowserDialog<Entity> dialog = new BrowserDialog<Entity>(title, PopupDialog.OK_SKIP_CANCEL, browser,
                                                                           context.getHelpContext());
            dialog.setCloseOnSelection(false);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (PopupDialog.OK_ID.equals(action)) {
                        print(browser.getSelectedList(), context);
                    } else if (PopupDialog.SKIP_ID.equals(action)) {
                        notifySkipped();
                    } else {
                        notifyCancelled();
                    }
                }
            });
            dialog.show();
        } else {
            // no templates associated with the schedule or worklist
            notifyCompleted();
        }
    }

    private void print(List<Entity> templates, TaskContext context) {
        CustomerMailContext mailContext = new CustomerMailContext(context, context.getHelpContext());
        for (Entity template : templates) {
            IMObjectBean templateBean = new IMObjectBean(template);
            Act document = (Act) ServiceHelper.getArchetypeService().create(templateBean.getString("archetype"));
            ActBean bean = new ActBean(document);
            bean.addNodeParticipation("patient", context.getPatient());
            bean.addNodeParticipation("documentTemplate", template);
            addTask(new PrintPatientActTask(document, mailContext));
        }
        // now start the workflow to print the documents
        super.start(context);
    }

    private boolean hasTemplates(Entity schedule) {
        if (schedule == null || ScheduleDocumentTemplateQuery.useAllTemplates(schedule)) {
            return true;
        }
        IMObjectBean bean = new IMObjectBean(schedule);
        return !bean.getValues("templates").isEmpty();
    }

    private static class PrintPatientActTask extends PrintActTask {

        /**
         * Constructs a {@link PrintPatientActTask}.
         *
         * @param act     the act to print
         * @param context the mail context. May be {@code null}
         */
        public PrintPatientActTask(Act act, MailContext context) {
            super(act, context, false);
            setRequired(false);
        }


        /**
         * Starts the task.
         * <p/>
         * The registered {@link TaskListener} will be notified on completion or failure.
         *
         * @param context the task context
         */
        @Override
        public void start(final TaskContext context) {
            final DocumentAct object = (DocumentAct) getObject(context);
            if (TypeHelper.isA(object, PatientArchetypes.DOCUMENT_LETTER)) {
                // need to generate the document before printing it.
                DocumentGenerator.Listener listener = new DocumentGenerator.AbstractListener() {
                    @Override
                    public void generated(Document document) {
                        print(object, context);
                    }

                    @Override
                    public void cancelled() {
                        notifyCancelled();
                    }

                    @Override
                    public void skipped() {
                        notifySkipped();
                    }

                    @Override
                    public void error() {
                        notifySkipped();
                    }
                };
                DocumentGenerator generator = new DocumentGenerator(object, context, context.getHelpContext(),
                                                                    listener);
                generator.generate(true, false, true);
            } else {
                super.start(context);
            }
        }

        /**
         * Invoked when the object is successfully printed.
         * <p/>
         * This implementation links the document to the patient visit.
         *
         * @param object  the printed object
         * @param context the task context
         */
        @Override
        protected void onPrinted(final IMObject object, final TaskContext context) {
            if (setPrintStatus(object)) {
                ServiceHelper.getArchetypeService().save(object);
            }

            Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, (Act) object);
            if (Retryer.run(linker)) {
                context.setObject(PatientArchetypes.CLINICAL_EVENT, linker.getEvent());
                notifyCompleted();
            } else {
                notifyCancelled();
            }
        }
    }

    /**
     * A {@link IMObjectTableBrowser} that enables multiple document templates to be selected.
     */
    private static class DocumentTemplateBrowser extends IMObjectTableBrowser<Entity> {

        /**
         * The set of selected templates.
         */
        private Set<IMObjectReference> selections;

        /**
         * Constructs a {@link DocumentTemplateBrowser}.
         *
         * @param query   the document template query
         * @param context the context
         */
        public DocumentTemplateBrowser(Query<Entity> query, LayoutContext context) {
            super(query, context);
        }

        /**
         * Returns the list of selected templates.
         *
         * @return the selected templates
         */
        public List<Entity> getSelectedList() {
            List<Entity> result;
            if (selections.isEmpty()) {
                result = Collections.emptyList();
            } else {
                result = new ArrayList<Entity>();
                for (IMObjectReference reference : selections) {
                    Entity template = (Entity) IMObjectHelper.getObject(reference, null);
                    if (template != null) {
                        result.add(template);
                    }
                }
            }
            return result;
        }

        /**
         * Creates a new table model.
         *
         * @param context the layout context
         * @return a new table model
         */
        @Override
        protected IMTableModel<Entity> createTableModel(LayoutContext context) {
            selections = new HashSet<IMObjectReference>();
            return new PrintTableModel(selections);
        }

        /**
         * Notifies listeners when an object is selected.
         *
         * @param selected the selected object
         */
        @Override
        protected void notifySelected(Entity selected) {
            super.notifySelected(selected);
            ((PrintTableModel) getTableModel()).toggleSelection(selected);
        }
    }

    private static class PrintTableModel extends BaseIMObjectTableModel<Entity> {

        /**
         * The print check boxes.
         */
        private List<CheckBox> print = new ArrayList<CheckBox>();

        /**
         * The print column.
         */
        private final int PRINT_INDEX = NEXT_INDEX;

        /**
         * Determines the selections.
         */
        private Set<IMObjectReference> selections;


        /**
         * Constructs a {@link PrintTableModel}.
         *
         * @param selections the selections
         */
        public PrintTableModel(Set<IMObjectReference> selections) {
            super(null);
            setTableColumnModel(createTableColumnModel(false));
            this.selections = selections;
        }

        /**
         * Sets the objects to display.
         *
         * @param objects the objects to display
         */
        @Override
        public void setObjects(List<Entity> objects) {
            super.setObjects(objects);
            print = new ArrayList<CheckBox>();
            for (final Entity object : objects) {
                boolean selected = selections.contains(object.getObjectReference());
                final CheckBox e = CheckBoxFactory.create(selected);
                e.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        setSelected(object, e.isSelected());
                    }
                });
                print.add(e);
            }
        }

        public void toggleSelection(Entity object) {
            int index = getObjects().indexOf(object);
            if (index != -1) {
                CheckBox checkBox = print.get(index);
                boolean selected = checkBox.isSelected();
                checkBox.setSelected(!selected);
                setSelected(object, !selected);
            }
        }

        private void setSelected(Entity object, boolean selected) {
            if (selected) {
                selections.add(object.getObjectReference());
            } else {
                selections.remove(object.getObjectReference());
            }
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Entity object, TableColumn column, int row) {
            Object result;
            if (column.getModelIndex() == PRINT_INDEX) {
                result = print.get(row);
            } else {
                result = super.getValue(object, column, row);
            }
            return result;
        }

        /**
         * Creates a new column model.
         *
         * @param showId        if {@code true}, show the ID
         * @param showArchetype if {@code true} show the archetype
         * @return a new column model
         */
        protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype) {
            TableColumnModel model = new DefaultTableColumnModel();
            TableColumn column = createTableColumn(PRINT_INDEX, "batchprintdialog.print");
            model.addColumn(column);
            return super.createTableColumnModel(showId, showArchetype, model);
        }

    }

}
