package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipCollectionViewer;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Displays printers associated with a practice location, including a status column that shows if they are
 * available/unknown.
 *
 * @author Tim Anderson
 */
public class LocationPrinterCollectionViewer extends RelationshipCollectionViewer {

    /**
     * The set of available printer names.
     */
    private Set<String> printers;

    /**
     * The print column model index.
     */
    private int printIndex;

    /**
     * Constructs a {@link LocationPrinterCollectionViewer}.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be {@code null}
     */
    public LocationPrinterCollectionViewer(CollectionProperty property, IMObject parent, LayoutContext context) {
        super(property, parent, false, context);
    }

    /**
     * Returns the available printers.
     *
     * @return the printers
     */
    protected Set<String> getPrinters() {
        if (printers == null) {
            printers = new HashSet<>(Arrays.asList(PrintHelper.getPrinters()));
        }
        return printers;
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<RelationshipState> createTableModel(LayoutContext context) {
        return new RelationshipStateTableModel(context, true) {

            /**
             * Returns the value found at the given coordinate within the table.
             *
             * @param object the object
             * @param column the table column
             * @param row    the table row
             */
            @Override
            protected Object getValue(RelationshipState object, TableColumn column, int row) {
                Object result;
                if (column.getModelIndex() == printIndex) {
                    if (getPrinters().contains(object.getTargetName())) {
                        result = Messages.get("printer.status.available");
                    } else {
                        result = Messages.get("printer.status.unknown");
                    }
                } else {
                    result = super.getValue(object, column, row);
                }
                return result;
            }

            /**
             * Creates a new column model.
             *
             * @return a new column model
             */
            @Override
            protected TableColumnModel createTableColumnModel() {
                DefaultTableColumnModel model = new DefaultTableColumnModel();
                model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
                printIndex = getNextModelIndex(model);
                model.addColumn(createTableColumn(printIndex, "printer.status"));
                return model;
            }
        };
    }
}
