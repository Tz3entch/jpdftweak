package jpdftweak.tabs.input;

import com.itextpdf.text.DocumentException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;

import jpdftweak.core.IntegerList;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.Preview;
import jpdftweak.tabs.PageSizeTab;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.treetable.CustomTreeTableModel;
import jpdftweak.tabs.input.treetable.TreeTableComponent;
import jpdftweak.tabs.input.treetable.items.FileNode;
import jpdftweak.tabs.input.treetable.items.FolderNode;

/**
 *
 * @author Vasilis Naskos
 */
public class InputTabPanel extends JPanel {
    
    private final JFrame parentFrame;
    private final CellConstraints cc = new CellConstraints();
    private JTextField fileCountField;
    private TreeTableComponent inputFilesTable;
    private JButton selectfile, clear;
    private InputOptionsPanel optionsPanel;
    private CustomTreeTableModel model;

    private Preview previewPanel;
    private PdfTweak pdfTweak;
    private PageSizeTab pageSizeTab;

    private boolean useTempFiles;
    private final String[] columnHeaders = new String[]{
        "File", "PaperSize", "Orientation", "Color Depth",
        "Size", "Pages", "From Page", "To Page", "Include Odd",
        "Include Even", "Empty Before", "Bookmark Level"
    };
    private final Class[] itemClassType = new Class[]{
        InputFile.class, String.class, String.class,
        String.class, Integer.class, Integer.class,
        Integer.class, Integer.class, Boolean.class,
        Boolean.class, IntegerList.class, Integer.class
    };

    public void setPageSizeTab(PageSizeTab pageSizeTab) { this.pageSizeTab = pageSizeTab;}
    
    public static InputTabPanel getInputPanel() {
        return new InputTabPanel();
    }
    
    public InputTabPanel() {
        super(new FormLayout("f:p, f:p:g, f:p, f:p, f:p, f:p, f:p",
                "f:p, f:p, f:p:g"));
        
        this.parentFrame = (JFrame) this.getParent();
        generateUserInterface();
        updateFileCount();
        
        useTempFiles = false;
    }

    public void setPdfTweak(PdfTweak pdfTweak) {
        this.pdfTweak = pdfTweak;
    }
    public PdfTweak getPdfTweak() {return pdfTweak;}

    public Preview getPreviewPanel() {
        return previewPanel;
    }

    public void setPreviewPanel(Preview previewPanel) {
        this.previewPanel = previewPanel;
    }


    private void generateUserInterface() {
        generateFileRow();
        generateOptionsPanel();
        generateInputFilesTable();
        generatePreviewPanel();
    }
    
    private void generateFileRow() {
        initializeFileRowComponents();
        positionFileRowComponents();
    }
    
    private void initializeFileRowComponents() {
        fileCountField = new JTextField();
        fileCountField.setEditable(false);
        
        selectfile = new JButton("Select...");
        selectfile.addActionListener(importFilesListener());
        
        clear = new JButton("Clear");
        clear.addActionListener(clearButtonListener());
    }
    
    private ActionListener importFilesListener() {
        ActionListener importFilesListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                importFilesActionPerformed();
            }
        };

        return importFilesListener;
    }

    private void importFilesActionPerformed() {
        ModelHandler modelHandler = new ModelHandler() {

            @Override
            public void insertFileNode(FileNode node) {
                InputFile inputItem = node.getInputFileItem();
                PdfTweak pdfTweak = new PdfTweak(inputItem,false, null );
                setPdfTweak(pdfTweak);
                pageSizeTab.setPdfTweak(pdfTweak);
                try {
                    previewPanel.updatePreview(pdfTweak.getByteBuffer());
                    pageSizeTab.getPreviewPanel().updatePreview(pdfTweak.getByteBuffer());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }


                FolderNode parent = model.createParents(inputItem.getFile());
                model.insertNodeInto(node, parent, parent.getChildCount());
                
                updateFileCount();
            }
        };
        
        final FileImporter importer = new FileImporter(parentFrame, modelHandler, useTempFiles);
        final Thread importFiles = new Thread(importer);
        importFiles.start();
    }

    private ActionListener clearButtonListener() {
        ActionListener clearButtonListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearActionPerformed();
            }
        };

        return clearButtonListener;
    }

    private void clearActionPerformed() {
        //TODO mf.setInputFile(null);
        model.clear();

        previewPanel.clearPreview();
        pageSizeTab.getPreviewPanel().clearPreview();
        pageSizeTab.setPdfTweak(null);

        updateFileCount();
    }
    
    private void updateFileCount() {
        String fn;
        
        if (model.isEmpty())
            fn = "(No file selected)";
        else
            fn = "(" + model.getChildNodeCount() + " files selected)";
        
        fileCountField.setText(fn);
    }
    
    private void positionFileRowComponents() {
        this.add(new JLabel("Filename"), cc.xy(1, 1));
        this.add(fileCountField, cc.xyw(2, 1, 3));
        this.add(selectfile, cc.xy(5, 1));
        this.add(clear, cc.xy(6, 1));
    }
    
    private void generateOptionsPanel() {
        optionsPanel = new InputOptionsPanel();
        
        this.add(optionsPanel, cc.xyw(1, 2, 6));
    }
    
    private void generateInputFilesTable() {
        initializeInputFilesTable();
        positionInputFilesTable();
    }

    private void generatePreviewPanel() {

        previewPanel = new Preview(new Dimension(400, 500));

        this.add(previewPanel ,cc.xyw(7, 3, 1));

    }
    
    private void initializeInputFilesTable() {
        inputFilesTable = new TreeTableComponent(columnHeaders, itemClassType);
        
        model = inputFilesTable.getModel();
    }
    
    private void positionInputFilesTable() {
        this.add(inputFilesTable, cc.xyw(1, 3, 6));
    }
    
    public void setUseTempFiles(boolean useTempFiles) {
        this.useTempFiles = useTempFiles;
    }
    
    public boolean isModelEmpty() {
        return model.isEmpty();
    }
    
    public boolean isMergeByDirSelected() {
        return optionsPanel.isMergeByDirSelected();
    }
    
    public boolean isBatchSelected() {
        return optionsPanel.isBatchSelected();
    }
    
    public boolean isInterleaveSelected() {
        return optionsPanel.isInterleaveSelected();
    }
    
    public int getInterleaveSize() {
        String interleaveSizeValue = optionsPanel.getInterleaveSize();
        int interleaveSize = Integer.parseInt(interleaveSizeValue);
        
        return interleaveSize;
    }
    
    public ModelReader getModelReader() {
        ModelReader reader = new ModelReader() {

            @Override
            public ArrayList<FolderNode> getFolderNodes() {
                FolderNode root = model.getRoot();
                ArrayList<FolderNode> folders = model.listFolders(root);
                return folders;
            }

            @Override
            public ArrayList<FileNode> getFileNodes() {
                FolderNode root = model.getRoot();
                ArrayList<FileNode> files = model.getLeafRows(root);
                return files;
            }
        };
        
        return reader;
    }
    
    //TODO
    public int getBatchLength() {
        if (isBatchSelected()) {
            return model.getChildNodeCount();
        } else if (isMergeByDirSelected()) {
            return model.getParentsCount();
        } else {
            return 1;
        }
    }
}
