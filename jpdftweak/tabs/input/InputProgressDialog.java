package jpdftweak.tabs.input;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author Vasilis Naskos
 */
public class InputProgressDialog extends JFrame {
    
    JProgressBar overallProgress, subFolderProgress, fileProgress;
    JButton cancelButton;
    JLabel currentFile, waitIcon;
    
    int currentFolderIndex;
    int filesCount;
    int foldersCount;
    int[] filesInFoldersCount;
    
    public InputProgressDialog() {
        initComponents();
        buildGui();
        setupFrame();
    }
    
    private void initComponents() {
        overallProgress = new JProgressBar();
        overallProgress.setStringPainted(true);
        
        subFolderProgress = new JProgressBar();
        subFolderProgress.setStringPainted(true);
//        subFolderProgress.setValue(1);
        
        fileProgress = new JProgressBar();
        fileProgress.setStringPainted(true);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        currentFile = new JLabel();
        
        Image image = Toolkit.getDefaultToolkit()
                .createImage(getClass().getResource("/Gears-3.gif"));
        ImageIcon xIcon = new ImageIcon(image);
        xIcon.setImageObserver(this);
        waitIcon = new JLabel(xIcon);
    }
    
    private void buildGui() {
        FormLayout layout = new FormLayout(
                "right:p, 7dlu, p:g, f:p:g, f:p, 4dlu, f:p",
                "f:p, 2dlu, 5dlu, f:p:g, 4dlu, f:p:g, 4dlu, f:p:g, 7dlu, 4dlu, f:p, 4dlu, f:p:g");
        
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator("Progress", cc.xyw(1, 1, 7));
        builder.addLabel("Overall:", cc.xy(1, 4));
        builder.add(overallProgress, cc.xyw(3, 4, 3));
        builder.addLabel("Folder(s):", cc.xy(1, 6));
        builder.add(subFolderProgress, cc.xyw(3, 6, 3));
        builder.addLabel("File(s):", cc.xy(1, 8));
        builder.add(fileProgress, cc.xyw(3, 8, 3));
        builder.add(waitIcon, cc.xywh(7, 3, 1, 7));

        builder.addSeparator("Currently Processing", cc.xyw(1, 11, 7));
        builder.add(currentFile, cc.xyw(1, 13, 6));
        builder.add(cancelButton, cc.xy(7, 13));
        
        this.add(builder.getPanel());
    }
    
    private void setupFrame() {
        this.setMinimumSize(new Dimension(460, 250));
        this.setResizable(false);
        this.setTitle("Input Progress");
        this.setUndecorated(false);
        this.setResizable(true);
        this.pack();
        this.setLocationRelativeTo(null);
    }
    
    public void setFileCount(int filesCount) {
        this.filesCount = filesCount;
        overallProgress.setMaximum(filesCount);
    }
    
    public void setFoldersCount(int foldersCount) {
        this.foldersCount = foldersCount;
        subFolderProgress.setMaximum(foldersCount);
    }

    public void setFilesInFolderCount(int[] filesInFoldersCount) {
        this.filesInFoldersCount = filesInFoldersCount;
        currentFolderIndex = 0;
        fileProgress.setMaximum(filesInFoldersCount[currentFolderIndex]);
    }
    
    public void updateProgress() {
        updateFileProgress();
        
        int folderProcessed = fileProgress.getValue();
        
        if(folderProcessed == filesInFoldersCount[currentFolderIndex])
            updateFolderProgress();
        
        updateOverallProgress();
        
    }
    
    private void updateOverallProgress() {
        int overalProgressValue = overallProgress.getValue();
        overalProgressValue++;
        
        int percentage = overalProgressValue*100/filesCount;
        
        overallProgress.setValue(overalProgressValue);
        overallProgress.setString(percentage + "%");
    }
    
    private void updateFolderProgress() {
        int folderProgressValue = subFolderProgress.getValue();
        folderProgressValue++;
        
        int percentage = folderProgressValue*100/foldersCount;
        
        subFolderProgress.setValue(folderProgressValue);
        subFolderProgress.setString(percentage + "%");
        
        if(currentFolderIndex != foldersCount-1)
            resetFileProgress();
    }
    
    private void resetFileProgress() {
        fileProgress.setValue(0);
        fileProgress.setString("0%");
        
        currentFolderIndex++;
        fileProgress.setMaximum(filesInFoldersCount[currentFolderIndex]);
    }
    
    private void updateFileProgress() {
        int fileProgressValue = fileProgress.getValue();
        fileProgressValue++;
        
        int percentage = fileProgressValue*100/filesInFoldersCount[currentFolderIndex];
        
    	fileProgress.setValue((int)fileProgressValue);
    	fileProgress.setString(percentage + "%");
    }
    
    public void updateCurrentFile(String filepath) {
        currentFile.setText(filepath);
    }
    
    public void closeDialogWithDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(InputProgressDialog.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.dispose();
        }
    }
}
