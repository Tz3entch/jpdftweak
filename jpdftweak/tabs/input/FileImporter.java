package jpdftweak.tabs.input;

import jpdftweak.tabs.input.error.ErrorHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.swing.JFrame;
import jpdftweak.core.FilenameUtils;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.rowconstruction.FileNodeConstructor;
import jpdftweak.tabs.input.rowconstruction.InputFileConstructorIml;
import jpdftweak.tabs.input.treetable.items.FileNode;

/**
 *
 * @author Vasilis Naskos
 */
public class FileImporter implements Runnable {
    
    private final FileChooser fileChooser;
    private final JFrame parentFrame;
    private final InputProgressDialog importDialog;
    private final ModelHandler modelHandler;
    private final ErrorHandler errorHandler;
    private final boolean useTempFiles;
    private ArrayList<File[]> files;
    
    public FileImporter(JFrame parentFrame, ModelHandler modelHandler, boolean useTempFiles) {
        this.parentFrame = parentFrame;
        this.modelHandler = modelHandler;
        this.importDialog = new InputProgressDialog();
        this.fileChooser = new FileChooser(parentFrame);
        this.errorHandler = new ErrorHandler();
        this.useTempFiles = useTempFiles;
    }
    
    @Override
    public void run() {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        if(selectedFiles == null)
            return;
        
        showProgressDialog();
        
        DirectoryScanner scanner = new DirectoryScanner(selectedFiles);
        files = scanner.getFiles();
        
        setProgressBarLimits();
        
        for(File[] directory : files)
            importDirectory(directory);
        
        importDialog.closeDialogWithDelay();
        errorHandler.showErrors();
    }
    
    private void setProgressBarLimits() {
        int foldersCount = files.size();
        int[] filesInFolders = new int[foldersCount];
        int totalFiles = 0;
        
        for(int i=0; i<foldersCount; i++) {
            filesInFolders[i] = files.get(i).length;
            totalFiles += filesInFolders[i];
        }
        
        importDialog.setFileCount(totalFiles);
        importDialog.setFoldersCount(foldersCount);
        importDialog.setFilesInFolderCount(filesInFolders);
    }
    
    private void showProgressDialog() {
        importDialog.setVisible(true);
    }
    
    private void importDirectory(File[] directory) {
        for(File file : directory) {
            importDialog.updateCurrentFile(FilenameUtils.normalize(file.getPath()));
            importFile(file);
            importDialog.updateProgress();
        }
    }
    
    private void importFile(File file) {
        try {
            InputFileConstructorIml inputItemConstructor = new InputFileConstructorIml(parentFrame, useTempFiles);
            InputFile inputItem = inputItemConstructor.createInputFile(file);
            FileNodeConstructor fileNodeConstructor = new FileNodeConstructor(inputItem);
            FileNode node = fileNodeConstructor.createFileNode();
            modelHandler.insertFileNode(node);
        } catch(IOException ex) {
            String exceptionTrace = getExceptionTrace(ex);
            errorHandler.reportError(file.getPath(), exceptionTrace);
        }
    }
    
    private String getExceptionTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        
        return sw.toString();
    }
    
}
