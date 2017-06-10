package org.openvpms.web.workspace.admin.system;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

/**
 * Plugin browserr.
 *
 * @author Tim Anderson
 */
public class PluginBrowser extends AbstractTabComponent {

    /**
     * The container.
     */
    private Component component;

    private Label status;

    private final PluginManager manager;

    /**
     * Constructs a {@link PluginBrowser}.
     *
     * @param help the help context for the tab
     */
    public PluginBrowser(HelpContext help) {
        super(help);
        status = LabelFactory.create(Styles.BOLD);
        component = ColumnFactory.create(Styles.INSET, status);
        manager = ServiceHelper.getBean(PluginManager.class);
        getButtonSet().add("button.configure", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onConfigure();
            }
        });
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        if (manager.isStarted()) {
            status.setText("Plugins support is active");
        } else {
            status.setText("Plugins support is inactive");
        }
    }

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    @Override
    public Component getComponent() {
        // Cannot cache the SplitPane for some reason. Get a:
        // "Cannot process ServerMessage (Phase 2) Error: Element c_246 already exists in document; cannot add"
        // message.
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "SplitPaneWithButtonRow",
                                       getButtons(), component);
    }

    private void onConfigure() {
        ArchetypeQuery query = new ArchetypeQuery("entity.pluginConfiguration", false, false);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(query);
        Entity configuration;
        if (iterator.hasNext()) {
            configuration = iterator.next();
        } else {
            configuration = (Entity) IMObjectCreator.create("entity.pluginConfiguration");
        }
        HelpContext help = getHelpContext().topic(configuration, "edit");
        LayoutContext context = new DefaultLayoutContext(true, new LocalContext(), help);
        final IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(configuration, context);
        editor.getComponent();
        EditDialog dialog = EditDialogFactory.create(editor, context.getContext());
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                if (editor.isSaved()) {
                    try {
                        manager.stop();
                        manager.start();
                    } catch (Exception exception) {
                        ErrorHelper.show(exception);
                    }
                }
            }
        });
        dialog.show();
    }
}
