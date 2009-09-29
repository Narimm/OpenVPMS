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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.report.IMReport;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.archetype.rules.doc.DocumentRules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Generates a document from a template and updates the associated
 * {@link DocumentAct DocumentAct}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentGenerator {

    /**
     * Document generation listeners.
     */
    public interface Listener {

        /**
         * Invoked when generation completes.
         *
         * @param document the generated document
         */
        void generated(Document document);
    }

    /**
     * The document act.
     */
    private final DocumentAct act;

    /**
     * Reference to the document template.
     */
    private final IMObjectReference template;

    /**
     * The generated document.
     */
    private Document document;

    /**
     * The listener to notify when generation completes
     */
    private final Listener listener;


    /**
     * Creates a new <tt>DocumentGenerator</tt>.
     *
     * @param act      the document act
     * @param listener the listener to notify when generation completes
     */
    public DocumentGenerator(DocumentAct act, Listener listener) {
        this.act = act;
        this.listener = listener;
        ActBean bean = new ActBean(act);
        template = bean.getNodeParticipantRef("documentTemplate");
    }

    /**
     * Creates a new <tt>DocumentGenerator</tt>.
     *
     * @param template the document template reference
     * @param act      the document act
     * @param listener the listener to notify when generation completes
     */
    public DocumentGenerator(DocumentAct act, IMObjectReference template,
                             Listener listener) {
        this.act = act;
        this.template = template;
        this.listener = listener;
    }

    /**
     * Returns the generated document.
     *
     * @return the generated document, or <tt>null</tt> if generation hasn't
     *         been performed
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Generates the document, notifying the listener on completion.
     *
     * @param save if <tt>true</tt> save the act and generated document
     */
    public void generate(boolean save) {
        ReportGenerator gen = new ReportGenerator(template);
        IMReport<IMObject> report = gen.createReport();
        Set<ParameterType> parameters = report.getParameterTypes();
        boolean isLetter = TypeHelper.isA(act, "act.*Letter");
        if (parameters.isEmpty() || !isLetter) {
            generate(report, Collections.<String, Object>emptyMap(), save);
        } else {
            // only support parameter prompting for letters
            promptParameters(report, save);
        }
    }

    /**
     * Generates a document from a report.
     *
     * @param report     the report
     * @param parameters the report parameters
     * @param save       if <tt>true</tt>, save the document
     */
    private void generate(IMReport<IMObject> report,
                          Map<String, Object> parameters, boolean save) {
        List<IMObject> objects = Arrays.asList((IMObject) act);
        document = report.generate(objects.iterator(), parameters);

        if (save) {
            DocumentRules rules = new DocumentRules();
            List<IMObject> changes = rules.addDocument(act, document);
            if (SaveHelper.save(changes)) {
                listener.generated(document);
            }
        } else {
            listener.generated(document);
        }
    }

    /**
     * Pops up a dialog to prompt for report parameters.
     *
     * @param report the report
     * @param save   if <tt>true</tt>, save the document
     */
    private void promptParameters(final IMReport<IMObject> report,
                                  final boolean save) {
        Set<ParameterType> parameters = report.getParameterTypes();
        String title = Messages.get("document.input.parameters");
        final ParameterDialog dialog = new ParameterDialog(title, parameters);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                generate(report, dialog.getValues(), save);
            }
        });
        dialog.show();
    }
}
