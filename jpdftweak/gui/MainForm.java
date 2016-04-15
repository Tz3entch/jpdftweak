package jpdftweak.gui;

import com.itextpdf.text.DocumentException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import javax.swing.filechooser.FileNameExtensionFilter;
import jpdftweak.Main;
import jpdftweak.core.PdfTweak;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.gui.dialogs.OutputProgressDialog;
import jpdftweak.tabs.AttachmentTab;
import jpdftweak.tabs.BookmarkTab;
import jpdftweak.tabs.DocumentInfoTab;
import jpdftweak.tabs.EncryptSignTab;
import jpdftweak.tabs.InteractionTab;
import jpdftweak.tabs.OutputTab;
import jpdftweak.tabs.PageNumberTab;
import jpdftweak.tabs.PageSizeTab;
import jpdftweak.tabs.ShuffleTab;
import jpdftweak.tabs.Tab;
import jpdftweak.tabs.WatermarkTab;
import jpdftweak.tabs.InputTab;

public class MainForm extends JFrame {

    private static final long serialVersionUID = 5541931608656450178L;

    private final InputTab inputTab = new InputTab();

    private Tab[] tabs = {
        //inputTab,
        new PageSizeTab(this),
        new WatermarkTab(this),
        new ShuffleTab(this),
        new PageNumberTab(this),
        new BookmarkTab(this),
        new AttachmentTab(this),
        new InteractionTab(this),
        new DocumentInfoTab(this),
        new EncryptSignTab(this),
        new OutputTab(this),};

    private InputFile inputFile;
    private JFileChooser pdfChooser = new JFileChooser();

    public MainForm() {
        super("jPDF Tweak " + Main.VERSION);
        setIconImage(Toolkit.getDefaultToolkit().createImage(MainForm.class.getResource("/icon.png")));
        setFileChooserProperties(pdfChooser);
        setLayout(new FormLayout("f:p:g,f:p,f:p", "f:p:g,f:p"));
        CellConstraints cc = new CellConstraints();
        JTabbedPane jtp;
        add(jtp = new JTabbedPane(), cc.xyw(1, 1, 3));
        jtp.addTab(inputTab.getTabName(), inputTab.getUserInterface());
        for (Tab tab : tabs) {
            jtp.addTab(tab.getTabName(), tab);
        }
        JButton run;
        add(run = new JButton("Run"), cc.xy(2, 2));
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        runTweaks();
                    }
                }).start();
            }
        });
        JButton quit;
        add(quit = new JButton("Quit"), cc.xy(3, 2));
        quit.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pack();
        getRootPane().setDefaultButton(run);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        PageSizeTab pst = (PageSizeTab)tabs[0];
        inputTab.getInputTabPanel().setPageSizeTab(pst);
//        pst.setPdfTweak(inputTab.getInputTabPanel().getPdfTweak());
//        pst.setInputTab(inputTab);
//        pst.setPreviewPanel(inputTab.getInputTabPanel().getPreviewPanel());

        setVisible(true);
    }

    protected void runTweaks() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        PdfTweak tweak = null;
        int batchLength = inputTab.getBatchLength();
        try {
            try {
                inputTab.checkRun();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            for (Tab tab : tabs) {
                tab.checkRun();
            }
            OutputProgressDialog outputProgress = new OutputProgressDialog();
            outputProgress.setFileCount(batchLength);
            outputProgress.setVisible(rootPaneCheckingEnabled);
            for (int task = 0; task < batchLength; task++) {
                outputProgress.resetTweaksValue();
                inputTab.selectBatchTask(task);
                tweak = inputTab.run(tweak);
                for (Tab tab : tabs) {
                    if (!outputProgress.isVisible()) {
                        break;
                    }
                    tweak = tab.run(tweak, outputProgress);
                }
                outputProgress.updateOverallProgress();
            }

            if (outputProgress.isVisible()) {
                outputProgress.dispose();
                JOptionPane.showMessageDialog(this, "Finished", "JPDFTweak", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DocumentException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "jPDF Tweak has run out of memory. You may configure Java so that it may use more RAM, or you can enable the Tempfile option on the output tab.", "Out of memory: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } finally {
            if (tweak != null) {
                tweak.cleanup();
            }
        }
        this.setCursor(null);
    }

    public JFileChooser getPdfChooser() {
        return pdfChooser;
    }

    public InputFile getInputFile() {
        return inputFile;
    }

    public void setInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    public InputTab getInputTab() {
        return inputTab;
    }
    
    public static void setFileChooserProperties(JFileChooser chooser) {
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter[] filters = {
            new FileNameExtensionFilter("JPEG Image(*.jpg, *.jpeg)", "JPG", "JPEG"),
            new FileNameExtensionFilter("JPEG2000 Image(*.jp2, *.j2k, *.jpf, *.jpx, *.jpm, *.mj2)", "JP2", "J2K", "JPF", "JPX", "JPM", "MJ2"),
            new FileNameExtensionFilter("PNG Images(*.png)", "PNG"),
            new FileNameExtensionFilter("BMP Images(*.bmp)", "BMP"),
            new FileNameExtensionFilter("TIFF Images(*.tiff, *.tif)", "TIFF", "TIF"),
            new FileNameExtensionFilter("GIF Images(*.gif)", "GIF"),
            new FileNameExtensionFilter("Photoshop Files(*.psd)", "PSD"),
            new FileNameExtensionFilter("TGA Images(*.tga)", "TGA"),
            new FileNameExtensionFilter("PDF Files(*.pdf)", "PDF"),
            new FileNameExtensionFilter("All supported file types", "JPG", "JPEG", "JP2", "J2K", "JPF", "JPX", "JPM", "MJ2", "PNG", "GIF", "BMP", "TIFF", "TIF", "TGA", "PSD", "PDF")
        };

        for (FileNameExtensionFilter filter : filters) {
            chooser.setFileFilter(filter);
        }
    }
}
