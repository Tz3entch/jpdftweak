package jpdftweak.gui.dialogs;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author vasilis
 */
public class OutputProgressDialog extends JFrame {

    private static final long serialVersionUID = 6693111834626226043L;
    private static final int progressBarMaximum = 10000;

    JProgressBar overallProgress, tweaksProgress, pagesProgress;
    JButton cancelButton;
    JLabel currentAction, waitIcon, currentTab;

    int overallTotalCount;
    int tweaksTotalCount = 10;
    int pagesTotalCount;
    
    public OutputProgressDialog() {
        initComponents();
        buildGui();
    }
    
    private void initComponents() {
        overallProgress = new JProgressBar();
        overallProgress.setValue(0);
        
        tweaksProgress = new JProgressBar();
        tweaksProgress.setValue(0);
        tweaksProgress.setMaximum(10);
        
        pagesProgress = new JProgressBar();
        pagesProgress.setValue(0);
        
        overallProgress.setStringPainted(true);
        tweaksProgress.setStringPainted(true);
        pagesProgress.setStringPainted(true);
        
        overallProgress.setMaximum(progressBarMaximum);
        tweaksProgress.setMaximum(progressBarMaximum);
        pagesProgress.setMaximum(progressBarMaximum);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        currentAction = new JLabel();
        currentTab = new JLabel("Tabs:");
        
        Image image = Toolkit.getDefaultToolkit()
                .createImage(getClass().getResource("/output_gears.gif"));
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
        
        builder.addLabel("Output Files:", cc.xy(1, 4));
        builder.add(overallProgress, cc.xyw(3, 4, 3));

        builder.add(currentTab, cc.xy(1, 6));
        builder.add(tweaksProgress, cc.xyw(3, 6, 3));
        
        builder.addLabel("Pages:", cc.xy(1, 8));
        builder.add(pagesProgress, cc.xyw(3, 8, 3));
        
        builder.add(waitIcon, cc.xywh(7, 3, 1, 7));
        
        builder.addSeparator("Currently", cc.xyw(1, 11, 7));
        builder.add(currentAction, cc.xyw(1, 13, 6));
        builder.add(cancelButton, cc.xy(7, 13));
        
        this.setMinimumSize(new Dimension(460, 250));
        this.setResizable(false);
        this.setTitle("Output Progress");
        this.add(builder.getPanel());
        this.setUndecorated(false);
        this.setResizable(true);
        pack();
        this.setLocationRelativeTo(null);
    }
    
    public void setFileCount(int fileCount) {
    	overallTotalCount = fileCount;
    }
      
    public void setAction(String action) {
        currentAction.setText(action);
    }
    
    public void setPageCount(int pageCount) {
        pagesTotalCount = pageCount;
    }
    
    public void resetTweaksValue() {
        tweaksProgress.setValue(0);
        tweaksProgress.setString("0%");
    }
    
    public void resetProcessedPages() {
        pagesProgress.setValue(0);
        pagesProgress.setString("0%");
    }
    
    public void updateOverallProgress() {
        overallProgress.setValue((int) overallProgress.getValue()+overallProgress.getMaximum()/overallTotalCount);
        overallProgress.setString(overallProgress.getValue()/100.0+"%");
    }
    
    public void updateTweaksProgress(String tabname) {
        tweaksProgress.setValue((int) tweaksProgress.getValue()+tweaksProgress.getMaximum()/tweaksTotalCount);
        tweaksProgress.setString(tweaksProgress.getValue()/100.0+"%");
        currentTab.setText(tabname+":");
    }
    
    public void updatePagesProgress() {
        pagesProgress.setValue((int) pagesProgress.getValue()+pagesProgress.getMaximum()/pagesTotalCount);
        pagesProgress.setString(pagesProgress.getValue()/100.0+"%");
    }
    
    public void update(String action) {
        currentAction.setText(action);
        updateOverallProgress();
    }
    
}
