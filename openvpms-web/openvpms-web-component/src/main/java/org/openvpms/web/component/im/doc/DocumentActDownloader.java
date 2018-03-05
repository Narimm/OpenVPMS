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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.report.openoffice.OpenOfficeException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * Downloads a document from a {@link DocumentAct}.
 *
 * @author Tim Anderson
 */
public class DocumentActDownloader extends Downloader {

    /**
     * The document act.
     */
    private final DocumentAct act;

    /**
     * Determines if the document should be downloaded as a template.
     */
    private final boolean asTemplate;

    /**
     * Determines if the description should be displayed. If not, just the name is displayed.
     */
    private final boolean showDescription;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The template, when there is no document present.
     */
    private DocumentTemplate template;

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * PDF button style name.
     */
    private static final String PDF_STYLE_NAME = "download.pdf";


    /**
     * Constructs a {@link DocumentActDownloader}.
     *
     * @param act       the act
     * @param context   the context
     * @param formatter the file name formatter
     */
    public DocumentActDownloader(DocumentAct act, Context context, FileNameFormatter formatter) {
        this(act, false, context, formatter);
    }

    /**
     * Constructs a {@link DocumentActDownloader}.
     *
     * @param act        the act
     * @param asTemplate determines if the document should be downloaded as a template
     * @param context    the context
     * @param formatter  the file name formatter
     */
    public DocumentActDownloader(DocumentAct act, boolean asTemplate, Context context, FileNameFormatter formatter) {
        this(act, asTemplate, false, context, formatter);
    }

    /**
     * Constructs a {@link DocumentActDownloader}.
     *
     * @param act             the act
     * @param asTemplate      determines if the document should be downloaded as a template
     * @param showDescription determines if the description should be displayed. If not, just the name is displayed
     * @param context         the context
     * @param formatter       the file name formatter
     */
    public DocumentActDownloader(DocumentAct act, boolean asTemplate, boolean showDescription, Context context,
                                 FileNameFormatter formatter) {
        this.act = act;
        this.context = context;
        this.asTemplate = asTemplate;
        this.showDescription = showDescription;
        this.formatter = formatter;
    }

    /**
     * Returns a component representing the downloader.
     *
     * @return the component
     */
    public Component getComponent() {
        Component component;
        Button button;
        boolean generated = false;
        String name = act.getFileName();
        if (asTemplate) {
            DocumentTemplate template = getTemplate();
            if (template != null) {
                String docName = template.getDocumentName();
                if (!StringUtils.isEmpty(docName)) {
                    name = docName;
                }
            }
        } else if (act.getDocument() == null) {
            DocumentTemplate template = getTemplate();
            if (template != null) {
                name = template.getName();
                generated = true;
            }
        }

        String description = showDescription ? act.getDescription() : null;

        if (generated) {
            // if the document is generated, then its going to be a PDF, at least for the forseeable future.
            // Fairly expensive to determine the mime type otherwise. TODO
            button = ButtonFactory.create(new ActionListener() {
                public void onAction(ActionEvent event) {
                    selected(DocFormats.PDF_TYPE);
                }
            });
            button.setStyleName(PDF_STYLE_NAME);

            setButtonName(button, name, description);
        } else {
            button = ButtonFactory.create(new ActionListener() {
                public void onAction(ActionEvent event) {
                    selected(null);
                }
            });
            if (name != null) {
                setButtonNameAndStyle(button, name, description);
            } else {
                button.setStyleName(DEFAULT_BUTTON_STYLE);
            }
        }

        Converter converter = ServiceHelper.getBean(Converter.class);
        if (!generated && converter.canConvert(name, act.getMimeType(), DocFormats.PDF_TYPE)) {
            Button asPDF = ButtonFactory.create(new ActionListener() {
                public void onAction(ActionEvent event) {
                    selected(DocFormats.PDF_TYPE);
                }
            });
            asPDF.setStyleName(PDF_STYLE_NAME);
            asPDF.setProperty(Button.PROPERTY_TOOL_TIP_TEXT, Messages.get("file.download.asPDF.tooltip"));
            component = RowFactory.create(Styles.CELL_SPACING, button, asPDF);
        } else {
            component = RowFactory.create(Styles.CELL_SPACING, button);
        }
        return component;
    }

    /**
     * Returns the document for download.
     *
     * @param mimeType the expected mime type. If {@code null}, then no conversion is required.
     * @return the document for download
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     * @throws OpenOfficeException       if the document cannot be converted
     */
    protected Document getDocument(String mimeType) {
        Document document = null;
        if (!asTemplate) {
            IMObjectReference ref = act.getDocument();
            if (ref != null) {
                document = getDocumentByRef(ref, mimeType);
            } else {
                DocumentTemplate template = getTemplate();
                if (template != null) {
                    DocumentActReporter reporter = new DocumentActReporter(act, template, formatter,
                                                                           ServiceHelper.getArchetypeService(),
                                                                           ServiceHelper.getLookupService());
                    reporter.setFields(ReportContextFactory.create(context));
                    if (mimeType == null) {
                        document = reporter.getDocument();
                    } else {
                        document = reporter.getDocument(mimeType, true);
                    }
                }
            }
        } else {
            DocumentTemplate template = getTemplate();
            if (template != null) {
                document = template.getDocument();
                if (document == null) {
                    throw new DocumentException(DocumentException.ErrorCode.TemplateHasNoDocument, template.getName());
                }
                if (mimeType != null && !mimeType.equals(document.getMimeType())) {
                    Converter converter = ServiceHelper.getBean(Converter.class);
                    document = converter.convert(document, mimeType);
                }
            }
        }
        if (document == null) {
            throw new DocumentException(DocumentException.ErrorCode.NotFound);
        }
        return document;
    }

    /**
     * Returns the document template.
     *
     * @return the document template. May be {@code null}
     */
    private DocumentTemplate getTemplate() {
        if (template == null) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("documentTemplate")) {
                Entity participant = bean.getNodeParticipant("documentTemplate");
                if (participant != null) {
                    template = new DocumentTemplate(participant, ServiceHelper.getArchetypeService());
                }
            }
        }
        return template;
    }
}