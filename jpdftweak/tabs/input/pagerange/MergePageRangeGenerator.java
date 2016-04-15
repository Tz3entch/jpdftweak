package jpdftweak.tabs.input.pagerange;

import java.util.ArrayList;
import java.util.List;
import jpdftweak.core.PageRange;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.treetable.items.FolderNode;
import jpdftweak.tabs.input.treetable.items.Node;
import jpdftweak.tabs.input.ModelReader;

/**
 *
 * @author Vasilis Naskos
 */
public class MergePageRangeGenerator extends PageRangeGenerator {

    private final ArrayList<FolderNode> folders;
    
    public MergePageRangeGenerator(ModelReader model) {
        super(model);
        
        folders = model.getFolderNodes();
    }

    @Override
    public List<PageRange> generate(int taskIndex) {
        List<PageRange> ranges = new ArrayList<PageRange>();
        
        FolderNode parent = folders.get(taskIndex);
        for (int i = 0; i < parent.getChildCount(); i++) {
            Node node = (Node) parent.getChildAt(i);
            if (node instanceof FileNode) {
                PageRange pageRange = getPageRange((FileNode) node);
                if (pageRange != null)
                    ranges.add(pageRange);
            }
        }
        
        return ranges;
    }
    
}
