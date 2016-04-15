package jpdftweak.tabs.input.treetable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.tree.TreePath;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.treetable.items.FolderNode;
import jpdftweak.tabs.input.treetable.items.Node;
import jpdftweak.tabs.input.treetable.items.PageNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

/**
 *
 * @author Vasilis Naskos
 */
public class CustomTreeTableModel extends DefaultTreeTableModel implements SwapObserver {

    protected final Class[] columnClasses;
    protected HashMap<String, FolderNode> data;
    
    public CustomTreeTableModel(String[] columnHeaders, Class[] columnClasses) {
        super();
        setRoot(new FolderNode("Root", this));
        this.columnClasses = columnClasses;
        this.columnIdentifiers = Arrays.asList(columnHeaders);
        this.data = new HashMap<String, FolderNode>();
        data.put(getRoot().getKey(), getRoot());
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnClasses[column];
    }

    @Override
    public final FolderNode getRoot() {
        return (FolderNode) super.getRoot();
    }

    @Override
    public void insertNodeInto(MutableTreeTableNode newChild, MutableTreeTableNode parent, int index) {
        super.insertNodeInto(newChild, parent, index);
        
        if (newChild instanceof FolderNode) {
            data.put(((FolderNode) newChild).getKey(), (FolderNode) newChild);
        }
    }
    
    /**
     * Get the count of all leaf Nodes
     *
     * @return count of leaves
     */
    public int getChildNodeCount() {
        int childCount = 0;

        for (FolderNode node : data.values()) {
            childCount += node.getFileCount();
        }

        return childCount;
    }
    
    public boolean isEmpty() {
        return getRoot().getChildCount() == 0;
    }
    
    public FolderNode createParents(File file) {
        ArrayList<String> parents = new ArrayList<String>();
        File temp = file.getParentFile();
        while (temp != null) {
            parents.add(temp.getPath());
            temp = temp.getParentFile();
        }

        Collections.reverse(parents);

        FolderNode tempNode = null;
        for (String parent : parents) {
            if (data.containsKey(parent)) {
                tempNode = data.get(parent);
            } else {
                if (tempNode == null) {
                    tempNode = (FolderNode) root;
                }
                FolderNode newNode = new FolderNode(parent);

                insertNodeInto(newNode, tempNode, tempNode.getChildCount());
                tempNode = newNode;
            }
        }

        return tempNode;
    }
    
    @Override
    public Object getValueAt(Object row, int column) {
        Node node = (Node) row;
        
        if(column == 11) {
            return new TreePath(getPathToRoot(node)).getPathCount()-1;
        }
        
        return node.getValueAt(column);
    }
    
    @Override
    public boolean isCellEditable(Object node, int column) {
        return node instanceof FileNode && column >= 5;
    }
    
    @Override
    public void setValueAt(Object value, Object node, int column) {
        FileNode child = (FileNode) node;
        switch (column) {
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                child.getData()[column] = value;
        }
    }

    public void removeNodeFromParent(MutableTreeTableNode node, boolean recursive) {
        if(node instanceof FolderNode) {
            if(recursive) {
                recursiveClean((FolderNode) node);
            }
            data.remove(((FolderNode) node).getKey());
        }
        
        super.removeNodeFromParent(node);
    }
    
    /**
     * Remove all children from a Parent Node recursively
     *
     * @param parent Starting Point/Node
     */
    private void recursiveClean(FolderNode parent) {
        int childCount = parent.getChildCount() - 1;
        for (int i = childCount; i >= 0; i--) {
            if (parent.getChildAt(i) instanceof FolderNode) {
                FolderNode parentChild = (FolderNode) parent.getChildAt(i);
                removeNodeFromParent(parentChild, true);
            } else if (parent.getChildAt(i) instanceof FileNode) {
                FileNode child = (FileNode) parent.getChildAt(i);
                ((InputFile) child.getData()[0]).close();
                removeNodeFromParent(child, true);
            } else if(parent.getChildAt(i) instanceof PageNode) {
                PageNode child = (PageNode) parent.getChildAt(i);
                removeNodeFromParent(child, true);
            }
        }
    }
    
    /**
     * Remove all Nodes/Rows except root
     */
    public void clear() {
        FolderNode rootNode = getRoot();

        //Nothing to remove
        if (rootNode.getChildCount() == 0) {
            return;
        }

        recursiveClean(rootNode);
    }
    
    public TreePath moveRow(TreePath path, int offset) {
        MutableTreeTableNode node = (MutableTreeTableNode) path.getLastPathComponent();
        MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();
        
        if(offset == -1 && parent.getIndex(node) == 0) {
           return path;
        }
        
        if(offset == 1 && parent.getIndex(node) == parent.getChildCount()-1) {
           return path;
        }
        
        int index = parent.getIndex(node);
        
        removeNodeFromParent(node, false);
        insertNodeInto(node, parent, index+offset);
        
        return new TreePath(getPathToRoot(node));
    }
    
    
    public ArrayList<FolderNode> listFolders(FolderNode folder) {
        ArrayList<FolderNode> allFolders = new ArrayList<FolderNode>();
        allFolders.add(folder);
        int folderCount, offset = 0;
        
        while(offset != allFolders.size()) {
            folder = allFolders.get(offset);
            folderCount = folder.getFolderCount();
            
            for(int i=offset; i<offset+folderCount; i++) {
                allFolders.addAll(offset+1, allFolders.get(i).getFolders());
            }
            
            if(folder.isEmpty()) {
                allFolders.remove(folder);
            } else {
                offset++;
            }
        }
        
        return allFolders;
    }
    
    /**
     * Create a list of all leaf Nodes from every Parent
     *
     * @param folder
     * @return list of leaf nodes
     */
    public ArrayList<FileNode> getLeafRows(FolderNode folder) {
        ArrayList<FileNode> leaves = new ArrayList<FileNode>();
        
        for(MutableTreeTableNode node : folder.getChildren()) {
            if(node instanceof FolderNode) {
                leaves.addAll(getLeafRows((FolderNode) node));
            } else if(node instanceof FileNode) {
                leaves.add((FileNode) node);
            }
        }

        return leaves;
    }
    
    /**
     * Get the count of parents containing leaf Nodes
     *
     * @return count of parents with leaves
     */
    public int getParentsCount() {
        int count = 0;

        for (FolderNode node : data.values()) {
            if (node.getFileCount() != 0) {
                count++;
            }
        }

        return count;
    }

    @Override
    public void notify(Node node, int index) {
        Node parent = (Node) node.getParent();
        removeNodeFromParent(node, false);
        insertNodeInto(node, parent, index);
    }
    
}
