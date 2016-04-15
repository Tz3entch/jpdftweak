package jpdftweak.tabs.input.pagerange;

import java.util.List;
import jpdftweak.core.IntegerList;
import jpdftweak.core.PageRange;
import jpdftweak.tabs.input.items.ImageInputFile;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.items.PdfInputFile;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.ModelReader;

/**
 *
 * @author Vasilis Naskos
 */
public abstract class PageRangeGenerator {
    
    ModelReader model;
    
    public static PageRangeGenerator initGenerator(ModelReader model, boolean batch, boolean mergeByDir) {
        if(batch)
            return new BatchPageRangeGenerator(model);
        else if(mergeByDir)
            return new MergePageRangeGenerator(model);
        else
            return new MultiPageRangeGenerator(model);
    }
    
    public PageRangeGenerator(ModelReader model) {
        this.model = model;
    }
    
    public abstract List<PageRange> generate(int taskIndex);
    
    public PageRange getPageRange(FileNode node) {
        if (node.getData()[0] == null) {
            return null;
        }

        InputFile ifile = (InputFile) node.getData()[0];
        int from = (Integer) node.getData()[6];
        int to = (Integer) node.getData()[7];
        boolean odd = (Boolean) node.getData()[8];
        boolean even = (Boolean) node.getData()[9];
        IntegerList emptyBefore = (IntegerList) node.getData()[10];

        if (ifile instanceof PdfInputFile) {
            return new PageRange((PdfInputFile) ifile, from, to, odd, even, emptyBefore, node.getPageOrder());
        } else {
            return new PageRange((ImageInputFile) ifile, node.getPageOrder());
        }
    }
    
}
