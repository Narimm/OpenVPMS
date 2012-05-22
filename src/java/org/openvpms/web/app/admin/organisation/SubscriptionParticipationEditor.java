package org.openvpms.web.app.admin.organisation;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.doc.AbstractDocumentParticipationEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.RowFactory;


/**
 * Editor for <em>participation.subscription</em> objects.
 *
 * @author Tim Anderson
 */
public class SubscriptionParticipationEditor extends AbstractDocumentParticipationEditor {

    /**
     * The subscription viewer.
     */
    private SubscriptionViewer viewer;

    /**
     * @param participation the participation to edit
     * @param parent        the parent entity
     * @param context       the layout context. May be <tt>null</tt>.
     */
    public SubscriptionParticipationEditor(Participation participation, Party parent, LayoutContext context) {
        super(participation, parent, context);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        if (viewer == null) {
            viewer = new SubscriptionViewer(getLayoutContext());
        }
        return new IMObjectLayoutStrategy() {
            public void addComponent(ComponentState state) {
            }

            public ComponentState apply(IMObject object, PropertySet properties, IMObject parent,
                                        LayoutContext context) {
                viewer.setSubscription(getDocumentAct());
                Component selector = getSelector().getComponent();
                Row row = RowFactory.create("CellSpacing", viewer.getComponent(), selector);
                return new ComponentState(row);
            }
        };
    }

    /**
     * Creates a new document act.
     *
     * @return a new document act
     */
    @Override
    protected DocumentAct createDocumentAct() {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        return (DocumentAct) service.create("act.subscription");
    }

    /**
     * Invoked when a document is uploaded.
     *
     * @param document the uploaded document
     */
    @Override
    protected void onUpload(Document document) {
        super.onUpload(document);
        viewer.setSubscription(getDocumentAct());
        viewer.refresh();
    }

    /**
     * Updates the display with the selected act.
     *
     * @param act the act
     */
    @Override
    protected void updateDisplay(DocumentAct act) {
        // no-op
    }
}
