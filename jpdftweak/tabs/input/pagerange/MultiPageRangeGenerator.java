package jpdftweak.tabs.input.pagerange;

import java.util.ArrayList;
import java.util.List;
import jpdftweak.core.PageRange;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.ModelReader;

/**
 *
 * @author Vasilis Naskos
 */
public class MultiPageRangeGenerator extends PageRangeGenerator {
    
    private final ArrayList<FileNode> files;
    
    public MultiPageRangeGenerator(ModelReader model) {
        super(model);
        
        files = model.getFileNodes();
    }

    @Override
    public List<PageRange> generate(int taskIndex) {
        List<PageRange> ranges = new ArrayList<PageRange>();

        for (FileNode node : files) {
            PageRange pageRange = getPageRange(node);
            if (pageRange != null)
                ranges.add(pageRange);
        }

        return ranges;
    }
    
}
