package jpdftweak.tabs.input.treetable.items;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jpdftweak.tabs.input.treetable.SwapObserver;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

/**
 *
 * @author Vasilis Naskos
 */
public class FolderNode extends Node implements Comparable<Object> {

    private int fileCount, folderCount;

    public FolderNode(String key) {
        super(new Object[]{key});
    }
    
    public FolderNode(String key, SwapObserver observer) {
        super(new Object[]{key}, observer);
    }

    public String getKey() {
        return getData()[0].toString();
    }

    @Override
    public void insert(MutableTreeTableNode node, int index) {
        manageCounters(node, 1);
        super.insert(node, index);
    }

    @Override
    public void add(MutableTreeTableNode node) {
        super.add(node);
        manageCounters(node, 1);
    }

    @Override
    public void remove(MutableTreeTableNode node) {
        super.remove(node);

        manageCounters(node, -1);
    }

    @Override
    public void remove(int index) {
        super.remove(index);
        
        manageCounters(getChildAt(index), -1);
    }
    
    private void manageCounters(TreeTableNode node, int offset) {
        if (node instanceof FileNode) {
            fileCount += offset;
        } else if (node instanceof FolderNode) {
            folderCount += offset;
        }
    }

    public int getFileCount() {
        return fileCount;
    }
    
    public int getFolderCount() {
        return folderCount;
    }

    public boolean isEmpty() {
        return fileCount == 0;
    }

    public ArrayList<FileNode> getFiles() {
        ArrayList<FileNode> files = new ArrayList<FileNode>();
        for(MutableTreeTableNode child : children) {
            if(child instanceof FileNode) {
                files.add((FileNode) child);
            }
        }
        return files;
    }
    
    public ArrayList<FolderNode> getFolders() {
        ArrayList<FolderNode> folders = new ArrayList<FolderNode>();
        for(MutableTreeTableNode child : children) {
            if(child instanceof FolderNode) {
                folders.add((FolderNode) child);
            }
        }
        return folders;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof FolderNode) {
            return this.getKey().equals(((FolderNode) o).getKey()) ? 0 : 1;
        } else if (o instanceof String) {
            return this.getKey().equals((String) o) ? 0 : 1;
        }
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FolderNode) {
            return this.getKey().equals(((FolderNode) obj).getKey());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
    
    @Override
    public Object getValueAt(int i) {
        switch (i) {
            case 0:
                File file = new File((String) getData()[0]);
                String name = file.getName();
                return name.equals("") ? file.getPath() : name;
            default:
                return "";
        }
    }

    public List<MutableTreeTableNode> getChildren() {
        return children;
    }
    
}
