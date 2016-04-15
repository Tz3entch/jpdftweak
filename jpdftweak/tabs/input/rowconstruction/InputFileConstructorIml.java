package jpdftweak.tabs.input.rowconstruction;

import com.itextpdf.text.exceptions.BadPasswordException;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jpdftweak.tabs.input.items.ImageInputFile;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.items.PdfInputFile;
import jpdftweak.tabs.input.password.PasswordObject;
import jpdftweak.tabs.input.password.PdfUnlockDialog;
import jpdftweak.tabs.input.InvalidInputFileType;
import jpdftweak.utils.SupportedFileTypes;

/**
 *
 * @author Vasilis Naskos
 */
public class InputFileConstructorIml implements InputFileConstructor {

    private static final String PDF = "pdf";
    private final JFrame parent;
    private final boolean useTemp;

    public InputFileConstructorIml(JFrame parent, boolean useTemp) {
        this.parent = parent;
        this.useTemp = useTemp;
    }
    
    @Override
    public InputFile createInputFile(File physicalFile) throws IOException {
        String extension = SupportedFileTypes.getFileExtension(physicalFile);
        
        if(!SupportedFileTypes.isSupported(extension))
            throw new InvalidInputFileType();
        
        if(isPDF(extension))
            return createPdfInput(physicalFile);
        else
            return createImageInput(physicalFile);
    }
    
    private boolean isPDF(String fileType) {
        return fileType.equalsIgnoreCase(PDF);
    }
    
    private PdfInputFile createPdfInput(File file) throws IOException {
        return createPdfInputWithoutPass(file);
    }
    
    private PdfInputFile createPdfInputWithoutPass(File file) throws IOException {
        try {
            return new PdfInputFile(file, "");
        } catch(BadPasswordException ex) {
            return tryToCreatePdfInputWithPass(file);
        }
    }
    
    private PdfInputFile tryToCreatePdfInputWithPass(File file) throws IOException {
        try {
            return createPdfInputWithPass(file);
        } catch(BadPasswordException ex) {
            JOptionPane.showMessageDialog(
                    parent,"Wrong password", "Cannot open input file",
                    JOptionPane.WARNING_MESSAGE);
            throw new IOException(ex);
        }
    }
    
    private PdfInputFile createPdfInputWithPass(File file) throws IOException {
        PasswordObject unlockInfo = PdfUnlockDialog.askForPassword(parent, file.getPath());

        if (unlockInfo.getUnlockedFilePath() != null)
            return new PdfInputFile(new File(unlockInfo.getUnlockedFilePath()), "");

        String password = unlockInfo.getPasswordAsString();

        return new PdfInputFile(file, password);
    }
    
    private ImageInputFile createImageInput(File file) throws IOException {
        ImageInputFile imageFileItem = new ImageInputFile(file, useTemp);
        return imageFileItem;
    }
    
}
