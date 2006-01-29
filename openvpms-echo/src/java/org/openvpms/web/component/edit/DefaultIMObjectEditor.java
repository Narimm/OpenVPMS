package org.openvpms.web.component.edit;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.IMObjectViewer;
import org.openvpms.web.component.list.LookupListModel;


/**
 * Editor for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultIMObjectEditor extends AbstractIMObjectEditor {

    /**
     * The object viewer.
     */
    private IMObjectViewer _viewer;

    /**
     * The component factory.
     */
    private IMObjectComponentFactory _factory = new NodeEditorFactory();

    /**
     * Lookup fields. These may beed to be refreshed.
     */
    private List<SelectField> _lookups = new ArrayList<SelectField>();

    /**
     * If <code>true</code> show required and optional fields.
     */
    private boolean _showAll;

    /**
     * Action listener for layout changes.
     */
    private ActionListener _layoutChangeListener;


    /**
     * Construct a new <code>DefaultIMObjectEditor</code>.
     *
     * @param object  the object to edit
     * @param showAll if <code>true</code> show optional and required fields;
     *                otherwise show required fields.
     */
    public DefaultIMObjectEditor(IMObject object, boolean showAll) {
        this(object, null, null, showAll);
    }

    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object.
     * @param descriptor the parent descriptor
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent,
                                 NodeDescriptor descriptor) {
        this(object, parent, descriptor, false);
    }

    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object.
     * @param descriptor the parent descriptor
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent,
                                 NodeDescriptor descriptor, boolean showAll) {
        super(object, parent, descriptor);
        _viewer = new IMObjectViewer(object) {
            protected IMObjectComponentFactory getComponentFactory() {
                _lookups.clear();
                return new IMObjectComponentFactory() {
                    public Component create(IMObject object, NodeDescriptor descriptor) {
                        return createComponent(object, descriptor);
                    }

                };
            }
        };
        _showAll = showAll;
        initLayout();
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        return _viewer.getComponent();
    }

    /**
     * Sets a listener to be notified when the layout changes.
     *
     * @param listener the listener
     */
    public void setLayoutListener(ActionListener listener) {
        _viewer.setLayoutListener(listener);
    }

    /**
     * Initialises the layout.
     */
    protected void initLayout() {
        ExpandableLayoutStrategy layout = (ExpandableLayoutStrategy) _viewer.getLayout();
        Component oldValue = null;
        ActionListener listener = getLayoutChangeListener();
        if (layout != null) {
            oldValue = getComponent();
            Button button = layout.getButton();
            if (button != null) {
                button.removeActionListener(listener);
            }
        }
        layout = new ExpandableLayoutStrategy(_showAll);
        _viewer.setLayout(layout);
        Button button = layout.getButton();
        if (button != null) {
            button.addActionListener(listener);
        }
        Component newValue = getComponent();
        firePropertyChange(COMPONENT_CHANGED_PROPERTY, oldValue, newValue);
    }

    /**
     * Change the layout/
     */
    protected void onLayout() {
        _showAll = !_showAll;
        initLayout();
    }

    /**
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getComponentFactory() {
        _lookups.clear();
        return new IMObjectComponentFactory() {
            public Component create(IMObject object, NodeDescriptor descriptor) {
                return createComponent(object, descriptor);
            }

        };
    }

    private Component createComponent(IMObject object,
                                      NodeDescriptor descriptor) {
        Component editor = _factory.create(object, descriptor);
        if (editor instanceof SelectField) {
            SelectField lookup = (SelectField) editor;
            lookup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    refreshLookups((SelectField) event.getSource());
                }
            });
            _lookups.add(lookup);
        }
        return editor;
    }

    protected void refreshLookups(SelectField source) {
        for (SelectField lookup : _lookups) {
            if (source != lookup) {
                LookupListModel model = (LookupListModel) lookup.getModel();
                model.refresh();
            }
        }
    }

    /**
     * Returns the layout change action listener.
     *
     * @return the layout change listener
     */
    protected ActionListener getLayoutChangeListener() {
        if (_layoutChangeListener == null) {
            _layoutChangeListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLayout();
                }
            };
        }
        return _layoutChangeListener;
    }


}
