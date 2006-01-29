package org.openvpms.web.component.edit;

import echopointng.GroupBox;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.query.Browser;
import org.openvpms.web.component.query.BrowserDialog;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;


/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class RelationshipEditor extends AbstractIMObjectEditor {

    /**
     * The relationship.
     */
    private final EntityRelationship _relationship;

    /**
     * The source descriptor.
     */
    private final NodeDescriptor _sourceDesc;

    /**
     * The target descriptor.
     */
    private final NodeDescriptor _targetDesc;

    /**
     * The entity representing the source of the relationship.
     */
    private Entity _source;

    /**
     * The entity representing the target of the relationship.
     */
    private Entity _target;

    /**
     * The component.
     */
    private Component _component;


    /**
     * Construct a new <code>RelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param descriptor   the parent descriptor
     */
    public RelationshipEditor(EntityRelationship relationship, IMObject parent,
                              NodeDescriptor descriptor) {
        super(relationship, parent, descriptor);
        _relationship = relationship;
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        _sourceDesc = archetype.getNodeDescriptor("source");
        _targetDesc = archetype.getNodeDescriptor("target");
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        if (_component == null) {
            _component = doLayout();
        }
        return _component;
    }

    protected Component doLayout() {
        _source = new Entity(_relationship.getSource(), _sourceDesc);
        _source.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect(_source);
            }
        });
        _target = new Entity(_relationship.getTarget(), _targetDesc);
        _target.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect(_target);
            }
        });

        return RowFactory.create(_source.getComponent(),
                _target.getComponent());
    }

    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getDescriptor();
        final Browser browser = new Browser(descriptor.getArchetypeRange());
        String title = Messages.get("relationship.select",
                descriptor.getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject object = popup.getSelected();
                if (object != null) {
                    onSelected(entity, object);
                }
            }
        });

        popup.show();
    }

    protected void onSelected(Entity entity, IMObject object) {
        entity.setObject(object);
    }

    private class Entity {

        private NodeDescriptor _descriptor;
        private Label _label;
        private Button _select;
        private Component _component;

        public Entity(IMObjectReference reference, NodeDescriptor descriptor) {
            _descriptor = descriptor;
            doLayout(descriptor);
            setObject(reference);
        }

        public NodeDescriptor getDescriptor() {
            return _descriptor;
        }

        public Button getSelect() {
            return _select;
        }

        public Component getComponent() {
            return _component;
        }

        public void setObject(IMObjectReference reference) {
            IMObject object = null;
            if (reference != null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                object = service.getById(reference.getArchetypeId(),
                        reference.getUid());
            }
            setObject(object);
        }

        public void setObject(IMObject object) {
            if (object != null) {
                String key = "relationship.entity.summary";
                String summary = Messages.get(key, object.getName(), object.getDescription());
                _label.setText(summary);
            } else {
                _label.setText(Messages.get("relationship.select"));
            }
        }

        protected void doLayout(NodeDescriptor descriptor) {
            _select = ButtonFactory.create("select");
            _label = LabelFactory.create();
            Row row = RowFactory.create(_select, _label);
            GroupBox box = new GroupBox();
            box.setTitle(descriptor.getDisplayName());
            box.add(row);
            _component = box;
        }


    }

}
