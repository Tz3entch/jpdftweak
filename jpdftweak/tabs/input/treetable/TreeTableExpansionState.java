package jpdftweak.tabs.input.treetable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;

/**
 * Store/Restore the expanded paths in a JXTreeTable.
 *
 * @author jorgen rapp
 *
 */
public class TreeTableExpansionState extends AbstractExpansionState {

    protected List<TreePath> expandedPaths = new ArrayList<TreePath>();

    public TreeTableExpansionState(JXTreeTable aTreeTable) {
        super(aTreeTable);
    }

    @Override
    public void store() {
        expandedPaths.clear();
        Enumeration expandedDescendants = ((JXTreeTable) getAssociatedComponent())
                .getExpandedDescendants(new TreePath(
                                ((JXTreeTable) getAssociatedComponent())
                                .getTreeTableModel().getRoot()));
        if (expandedDescendants != null) {
            while (expandedDescendants.hasMoreElements()) {
                Object nex = expandedDescendants.nextElement();
                TreePath np = (TreePath) nex;
                if (!(np.getLastPathComponent() == ((JXTreeTable) getAssociatedComponent())
                        .getTreeTableModel().getRoot())) {
                    expandedPaths.add(np);
                }
            }
        }
    }

    @Override
    public void restore() {
        JXTreeTable treeTable = (JXTreeTable) getAssociatedComponent();
        treeTable.collapseAll();
        for (TreePath path : expandedPaths) {
            treeTable.expandPath(path);
        }
    }
}
