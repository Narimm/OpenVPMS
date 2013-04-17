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
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;
import java.util.Set;


/**
 * Generates a document from a document act.
 * <p/>
 * For document acts that have an existing document and no document template, the existing document will be returned.
 *
 * @author Tim Anderson
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
     * The context.
     */
    private final Context context;

    /**
     * The generated document.
     */
    private Document document;

    /**
     * The listener to notify when generation completes
     */
    private final Listener listener;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@code DocumentGenerator}.
     *
     * @param act      the document act
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify when generation completes
     */
    public DocumentGenerator(DocumentAct act, Context context, HelpContext help, Listener listener) {
        this.act = act;
        this.context = context;
        this.help = help;
        this.listener = listener;
    }

    /**
     * Returns the generated document.
     *
     * @return the generated document, or {@code null} if generation hasn't been performed
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Generates the document.
     * <p/>
     * The document will not be saved.
     */
    public void generate() {
        generate(false, false);
    }

    /**
     * Generates the document, notifying the listener on completion.
     *
     * @param save    if {@code true} save the act and generated document
     * @param version if {@code true}  and saving the document, version any old document if the act supports it
     */
    public void generate(boolean save, boolean version) {
        if (DocumentActReporter.hasTemplate(act)) {
            DocumentActReporter reporter = new DocumentActReporter(act);
            Set<ParameterType> parameters = reporter.getParameterTypes();
            boolean isLetter = TypeHelper.isA(act, "act.*Letter");
            if (parameters.isEmpty() || !isLetter) {
                generate(reporter, save, version);
            } else {
                // only support parameter prompting for letters
                promptParameters(reporter, save, version);
            }
        } else if (act.getDocument() != null) {
            Document existing = (Document) IMObjectHelper.getObject(act.getDocument(), context);
            if (existing == null) {
                throw new DocumentException(DocumentException.ErrorCode.NotFound);
            }
            listener.generated(existing);
        } else {
            throw new DocumentException(DocumentException.ErrorCode.NotFound);
        }
    }

    /**
     * Generates a document from a report.
     *
     * @param reporter the reporter
     * @param save     if {@code true}, save the document
     * @param version  if {@code true}  and saving the document, version any old document if the act supports it
     */
    private void generate(DocumentActReporter reporter, boolean save, boolean version) {
        document = reporter.getDocument();

        if (save) {
            DocumentRules rules = new DocumentRules();
            List<IMObject> changes = rules.addDocument(act, document, version);
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
     * @param reporter the report er
     * @param save     if {@code true}, save the document
     * @param version  if {@code true}  and saving the document, version any old document if the act supports it
     */
    private void promptParameters(final DocumentActReporter reporter, final boolean save, final boolean version) {
        Set<ParameterType> parameters = reporter.getParameterTypes();
        String title = Messages.get("document.input.parameters");
        final ParameterDialog dialog = new ParameterDialog(title, parameters, act, context, help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                reporter.setParameters(dialog.getValues());
                generate(reporter, save, version);
            }
        });
        dialog.show();
    }
}
