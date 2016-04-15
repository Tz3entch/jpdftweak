package jpdftweak.tabs.input.treetable.items;

import java.io.File;
import java.util.ArrayList;
import jpdftweak.core.IntegerList;
import jpdftweak.core.Utils;
import jpdftweak.tabs.input.items.InputFile;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

/**
 *
 * @author Vasilis Naskos
 */
public class FileNode extends Node implements Comparable<Object>{
    
    private final ArrayList<Integer> pageOrder;
    private final InputFile inputFileItem;
    
    public FileNode(Object[] data) {
        super(data);
        
        this.inputFileItem = (InputFile) data[0];
        this.pageOrder = new ArrayList<Integer>();
    }
    
    public static FileNode createFileNodeFromInputFile(InputFile inputFileItem) {
        File inputFile = inputFileItem.getFile();
        Long fileSize = inputFile.length();
        Object[] data = new Object[11];
        
        data[0] = inputFileItem;
        data[4] = Utils.readableFileSize(fileSize);
        data[5] = inputFileItem.getPageCount();
        data[6] = 1;
        data[7] = inputFileItem.getPageCount();
        data[8] = true;
        data[9] = true;
        data[10] = new IntegerList("0");
        
        return new FileNode(data);
    }

    @Override
    public void insert(MutableTreeTableNode child, int index) {
        super.insert(child, index);
        
        if(child instanceof PageNode)
            pageOrder.add(index, ((PageNode)child).getPageNumber());
    }

    @Override
    public void remove(MutableTreeTableNode node) {
        pageOrder.remove(getIndex(node));
        
        super.remove(node);
    }

    @Override
    public void remove(int index) {
        pageOrder.remove(index);
        
        super.remove(index);
    }

    public ArrayList<Integer> getPageOrder() {
        return pageOrder;
    }

    public InputFile getInputFileItem() {
        return inputFileItem;
    }
    
    @Override
    public int compareTo(Object o) {
        if (o instanceof FileNode) {
            InputFile input = (InputFile) this.getData()[0];
            InputFile input2 = (InputFile) ((FileNode) o).getData()[0];
            String path1 = input.getFile().getName();
            String path2 = input2.getFile().getPath();
            return path1.compareTo(path2);
        }
        return 1;
    }


}
