package jpdftweak.tabs.input;

import java.util.ArrayList;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.treetable.items.FolderNode;

/**
 *
 * @author Vasilis Naskos
 */
public interface ModelReader {
    
    public ArrayList<FolderNode> getFolderNodes();
    public ArrayList<FileNode> getFileNodes();
    
}
