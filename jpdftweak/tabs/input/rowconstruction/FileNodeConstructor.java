package jpdftweak.tabs.input.rowconstruction;

import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.treetable.items.PageNode;

/**
 *
 * @author Vasilis Naskos
 */
public class FileNodeConstructor {

    private FileNode newFileNode;
    private final InputFile inputFileItem;
    
    public FileNodeConstructor(InputFile inputFileItem) {
        this.inputFileItem = inputFileItem;
    }
    
    public FileNode createFileNode() {
        newFileNode = FileNode.createFileNodeFromInputFile(inputFileItem);
        
        insertPages();
        
        return newFileNode;
    }
    
    private void insertPages() {
        int pageCount = inputFileItem.getPageCount();
        
        for(int i=0; i<pageCount; i++) {
            PageNodeConstructor pageConstructor = new PageNodeConstructor(inputFileItem, i);
            PageNode page = pageConstructor.createPageNode();
            newFileNode.insert(page, i);
        }    
    }
    
}
