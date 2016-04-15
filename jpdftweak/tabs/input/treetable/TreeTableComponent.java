package jpdftweak.tabs.input.treetable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jpdftweak.tabs.input.treetable.items.FolderNode;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

/**
 *
 * @author Vasilis Naskos
 */
public class TreeTableComponent extends JPanel {
    
    private final JScrollPane scrollPane;
    private final CustomTreeTableModel model;
    private JXTreeTable treeTable;
    private final JButton addBtn, upBtn, downBtn, deleteBtn, orderBtn;
    private boolean expandCollapse = false, ascendingOrder = true;
    TreeTableExpansionState expansionState;

    public TreeTableComponent(String[] headers, Class[] classes) {
        if (headers.length != classes.length) {
            throw new IllegalArgumentException();
        }
        
        setLayout(new FormLayout("f:p:g,f:p:g,f:p:g,f:p:g,f:p:g", "f:p:g, f:p"));
        CellConstraints cc = new CellConstraints();

        model = new CustomTreeTableModel(headers, classes);
        
        treeTable = new JXTreeTable(model);
        treeTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        treeTable.getColumnModel().getColumn(8).setPreferredWidth(30);
        treeTable.getColumnModel().getColumn(9).setPreferredWidth(30);
        treeTable.getColumnModel().getColumn(8).setCellRenderer(new ConditionalCheckBoxRenderer());
        treeTable.getColumnModel().getColumn(9).setCellRenderer(new ConditionalCheckBoxRenderer());
        treeTable.setRootVisible(false);
        treeTable.setShowGrid(true);
        treeTable.setColumnControlVisible(true);
        treeTable.setSortable(true);
        treeTable.setSortOrder(0, SortOrder.ASCENDING);
        BorderHighlighter topHighlighter = new BorderHighlighter(new HighlightPredicate() {

			@Override
			public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
				return true;
			}

        }, BorderFactory.createMatteBorder(0, 0, 1, 1, Color.DARK_GRAY));
        ColorHighlighter colorHighlighter = new ColorHighlighter(new HighlightPredicate() {

			@Override
			public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
				return !(arg0 instanceof JTree);
			}
        	
        }, new Color(243, 242, 241), Color.BLACK);
        treeTable.addHighlighter(topHighlighter);
        treeTable.addHighlighter(colorHighlighter);
        
        
        expansionState = new TreeTableExpansionState(treeTable);
        
        scrollPane = new JScrollPane(treeTable);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        add(scrollPane, cc.xyw(1, 1, 5));
        
        addBtn = new JButton("Expand/Collapse");
        add(addBtn, cc.xy(1, 2));
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (treeTable.getCellEditor() != null && !treeTable.getCellEditor().stopCellEditing()) {
                    return;
                }
                if(expandCollapse) {
                    treeTable.collapseAll();
                } else {
                    treeTable.expandAll();
                }
                expandCollapse = !expandCollapse;
            }
        });
        
        upBtn = new JButton("Up");
        add(upBtn, cc.xy(2, 2));
        upBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expansionState.store();
                
                final ArrayList<TreePath> newPaths = new ArrayList<TreePath>();

                int selectedRowCount = treeTable.getSelectedRowCount();
                
                for (int i=0; i<selectedRowCount; i++) {
                    int row = treeTable.getSelectedRows()[0];
                    
                    if (treeTable.getCellEditor() != null && !treeTable.getCellEditor().stopCellEditing()) {
                        return;
                    }
                    
                    TreePath path = treeTable.getPathForRow(row);
                    TreePath newPath = model.moveRow(path, -1);
                    
                    newPaths.add(newPath);
                }
                expansionState.restore();

                Runnable setSelectionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        TreeSelectionModel tsm = treeTable.getTreeSelectionModel();
                        tsm.setSelectionPaths(newPaths.toArray(new TreePath[0]));
                    }
                };
                SwingUtilities.invokeLater(setSelectionRunnable);
            }
        });
        
        downBtn = new JButton("Down");
        add(downBtn, cc.xy(3, 2));
        downBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expansionState.store();

                final ArrayList<TreePath> newPaths = new ArrayList<TreePath>();
                
                for (int i = treeTable.getSelectedRowCount()-1; i >= 0; i--) {
                    int row = treeTable.getSelectedRows()[i];
                    
                    if (treeTable.getCellEditor() != null && !treeTable.getCellEditor().stopCellEditing()) {
                        return;
                    }

                    TreePath path = treeTable.getPathForRow(row);
                    TreePath newPath = model.moveRow(path, 1);
                    
                    newPaths.add(newPath);
                }
                
                expansionState.restore();
                
                Runnable setSelectionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        TreeSelectionModel tsm = treeTable.getTreeSelectionModel();
                        tsm.setSelectionPaths(newPaths.toArray(new TreePath[0]));
                    }
                };
                SwingUtilities.invokeLater(setSelectionRunnable);
            }
        });
        
        deleteBtn = new JButton("Delete");
        add(deleteBtn, cc.xy(4, 2));
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = treeTable.getSelectedRowCount()-1; i >= 0; i--) {
                    try {
                        if(treeTable.getSelectedRowCount() == 0) {
                            break;
                        }
                        
                        int row = treeTable.getSelectedRows()[i];
                        
                        if (treeTable.getCellEditor() != null && !treeTable.getCellEditor().stopCellEditing()) {
                            return;
                        }

                        TreePath path = treeTable.getPathForRow(row);

                        model.removeNodeFromParent((MutableTreeTableNode) path.getLastPathComponent(), true);
                    } catch(Exception ex) {
                        
                    }
                }
            }
        });
        
        orderBtn = new JButton("Alphabetical order");
        add(orderBtn, cc.xy(5, 2));
        orderBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                expansionState.store();
                FolderNode parent = (FolderNode) model.getRoot();
                parent.sortNode(0, ascendingOrder, true);
                ascendingOrder = !ascendingOrder;
                expansionState.restore();
            }
        });
        
    }
    
    public CustomTreeTableModel getModel() {
        return model;
    }
    
    public void clear() {
        model.clear();
    }
}