package jpdftweak.tabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import jpdftweak.core.PageRange;
import jpdftweak.core.PdfBookmark;
import jpdftweak.core.PdfTweak;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.treetable.items.FolderNode;
import jpdftweak.tabs.input.InputTabPanel;
import jpdftweak.tabs.input.InputValidator;
import jpdftweak.tabs.input.ModelReader;
import jpdftweak.tabs.input.pagerange.PageRangeGenerator;

/**
 *
 * @author Vasilis Naskos
 */
public class InputTab extends ActionTab {

    private static final String TAB_NAME = "Input";
    private InputTabPanel inputTabPanel;
    private boolean isModelEmpty, mergeByDir, batch, interleave, useTempFiles;
    private int interleaveSize;
    private int batchTaskSelection;
    private ArrayList<FolderNode> folders;
    private ArrayList<FileNode> fileNodes;

    public InputTabPanel getInputTabPanel() {
        return inputTabPanel;
    }
    
    @Override
    public String getTabName() {
        return TAB_NAME;
    }

    @Override
    public JPanel getUserInterface() {
        inputTabPanel = InputTabPanel.getInputPanel();
        
        return inputTabPanel;
    }
    
    @Override
    public void checkRun() {
        collectInputTabInfo();
        validateCollectedInfo();
    }
    
    private void collectInputTabInfo() {
        isModelEmpty = inputTabPanel.isModelEmpty();
        mergeByDir = inputTabPanel.isMergeByDirSelected();
        batch = inputTabPanel.isBatchSelected();
        interleave = inputTabPanel.isInterleaveSelected();
        
        if(interleave)
            interleaveSize = inputTabPanel.getInterleaveSize();
        else
            interleaveSize = 0;
        
        ModelReader reader = inputTabPanel.getModelReader();
        folders = reader.getFolderNodes();
        fileNodes = reader.getFileNodes();
    }
    
    private void validateCollectedInfo() {
        InputValidator validator = new InputValidator();
        List<PageRange> ranges = generatePageRangesForCheck();
        
        validator.setIsModelEmpty(isModelEmpty);
        validator.setInterleave(interleave);
        validator.setInterleaveSize(interleaveSize);
        validator.setPageRanges(ranges);
        
        validator.checkValidity();
    }
    
    private List<PageRange> generatePageRangesForCheck() {
        return generatePageRanges(0, false, false);
    }

    @Override
    public PdfTweak run(PdfTweak input) {
        ArrayList<String> list = new ArrayList<String>();

        for (FileNode child : fileNodes) {
            InputFile file = (InputFile) child.getInputFileItem();
            list.add(file.getFile().getPath());
            try {
                file.reopen();
            } catch (IOException ex) {
                Logger.getLogger(InputTab.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        List<PageRange> ranges = generatePageRanges(batchTaskSelection, batch, mergeByDir);
        
        int n = 0;
        if (mergeByDir) {
            FileNode firstChild = folders.get(batchTaskSelection).getFiles().get(0);
            InputFile firstFile = (InputFile) firstChild.getData()[0];
            try {
                return new PdfTweak(firstFile, ranges, useTempFiles, mergeByDir, interleaveSize, list);
            } catch (Exception ex) {
                Logger.getLogger(InputTab.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (batch) {
            n = batchTaskSelection;
        }

        try {
            return new PdfTweak(fileNodes.get(n).getInputFileItem(), ranges, useTempFiles, mergeByDir, interleaveSize, list);
        } catch (Exception ex) {
            Logger.getLogger(InputTab.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<PageRange> generatePageRanges(int taskIndex, boolean batch, boolean merge) {
        ModelReader modelReader = inputTabPanel.getModelReader();
        PageRangeGenerator generator;
        generator = PageRangeGenerator.initGenerator(modelReader, batch, merge);

        return generator.generate(taskIndex);
    }
    
    //TODO
    public void selectBatchTask(int batchTaskSelection) {
        this.batchTaskSelection = batchTaskSelection;
    }
    
    public int getBatchLength() {
        return inputTabPanel.getBatchLength();
    }
    
    public List<PdfBookmark> loadBookmarks() {
//        if (model.isEmpty()) {
//            return Collections.EMPTY_LIST;
//        }
        
        List<PageRange> ranges = generatePageRangesForCheck();
        return PdfBookmark.buildBookmarks(ranges);
    }
    
    public void setUseTempFiles(boolean useTempFiles) {
        this.useTempFiles = useTempFiles;
        inputTabPanel.setUseTempFiles(useTempFiles);
    }
}
