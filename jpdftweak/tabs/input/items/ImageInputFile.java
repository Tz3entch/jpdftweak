package jpdftweak.tabs.input.items;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import ij.IJ;
import ij.ImagePlus;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpdftweak.Main;
import jpdftweak.core.Utils;
import loci.plugins.BF;

/**
 *
 * @author Vasilis Naskos
 */
public class ImageInputFile implements InputFile {
    
    private final boolean useTempFiles;
    private final File file;
    private PdfReader rdr;
    private Rectangle dimensions;
    private File temp;
    private String md5;
    private String depth;
    
    public ImageInputFile(File file, boolean useTempFiles) throws IOException {
        this.file = file;
        this.useTempFiles = useTempFiles;
        
        if(useTempFiles) {
            openUsingTemp();
        } else {
            open();
        }
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public String getParentDir() {
        return file.getParent();
    }

    @Override
    public PdfImportedPage getImportedPage(PdfWriter destination, int page) {
        return destination.getImportedPage(rdr, page);
    }

    @Override
    public Rectangle getPageSize(int page) {
        return dimensions;
    }

    @Override
    public final void open() throws IOException {
        try {
            md5 = Utils.getMd5(file.getPath());
            
            Document document = new Document();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            com.itextpdf.text.Image pdfImage = readImage();
            
            if(pdfImage == null) {
                throw new IOException(String.format("Image %s\n not supported or corrupted!", file));
            }
            
            document.setPageSize(new Rectangle(pdfImage.getWidth(), pdfImage.getHeight()));
            document.setMargins(0, 0, 0, 0);
            document.newPage();
            document.add(pdfImage);
            
            document.close();
            writer.close();
            
            this.dimensions = document.getPageSize();
            rdr = new PdfReader(baos.toByteArray());
            
            baos.close();
        } catch (DocumentException ex) {
            Logger.getLogger(ImageInputFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final void openUsingTemp() {
        try {
            md5 = Utils.getMd5(file.getPath());
            
            Document document = new Document();
            
            temp = File.createTempFile("jpdf", ".tmp");
            temp.deleteOnExit();
            
            FileOutputStream fos = new FileOutputStream(temp);

            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            com.itextpdf.text.Image pdfImage = readImage();

            if (pdfImage == null) {
                throw new IOException(String.format("Image %s\n not supported or corrupted!", file));
            }

            document.setPageSize(new Rectangle(pdfImage.getWidth(), pdfImage.getHeight()));
            document.setMargins(0, 0, 0, 0);
            document.newPage();
            document.add(pdfImage);

            document.close();
            writer.close();

            this.dimensions = document.getPageSize();
            rdr = new PdfReader(temp.getPath());
            
            fos.close();
        } catch (DocumentException ex) {
            Logger.getLogger(ImageInputFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageInputFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private com.itextpdf.text.Image readImage() throws IOException {
        try {
            java.awt.Image awtImage = null;
            
            try {
                ImagePlus imp = IJ.openImage(file.getPath());
                awtImage = imp.getImage();
                depth = imp.getBitDepth()+" bit";
            } catch (Exception ex) {
                try {
                    ImagePlus[] imps = BF.openImagePlus(file.getPath());
                    for (ImagePlus imp : imps) {
                        awtImage = imp.getImage();
                    }
                    
                } catch (Exception exc) {
                    throw new IOException(exc);
                }
            }
            
            if(awtImage == null) {
                return null;
            }
            
            com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(awtImage, null);
            awtImage.flush();
            
            return pdfImage;
        } catch (BadElementException ex) {
            Logger.getLogger(ImageInputFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    @Override
    public void close() {
        if(temp != null) {
            temp.delete();
        }
        
        rdr.close();
    }
    
    @Override
    public int getPageCount() {
        return rdr.getNumberOfPages();
    }
    
    @Override
    public Map<String, String> getInfoDictionary() {
        Map<String, String> result = rdr.getInfo();
        if (result.containsKey("Producer") && !result.get("Producer").contains(Document.getProduct())) {
            result.put("Producer", result.get("Producer") + "; modified by jPDF Tweak " + Main.VERSION + " (based on " + Document.getVersion() + ")");
        }
        return result;
    }
    
    @Override
    public PdfPageLabels.PdfPageLabelFormat[] getPageLabels() {
        return PdfPageLabels.getPageLabelFormats(rdr);
    }
    
    @Override
    public int getCryptoMode() {
        return rdr.getCryptoMode();
    }

    @Override
    public boolean isMetadataEncrypted() {
        return rdr.isMetadataEncrypted();
    }

    @Override
    public int getPermissions() {
        if (rdr.getCryptoMode() == 0) {
            // 40-bit encryption does not support some flags, but sets them.
            // Clear them so that they do not show up in -info output.
            return rdr.getPermissions() & 0xFFFF00FF;
        }
        return rdr.getPermissions();
    }

    @Override
    public String getOwnerPassword() {
        return null;
    }

    @Override
    public String getUserPassword() {
        byte[] userPwd = rdr.computeUserPassword();
        if (userPwd == null) {
            return "";
        }
        return new String(userPwd);
    }
    
    @Override
    public PdfReader getReader() {
        return rdr;
    }
    
    @Override
    public void reopen() throws IOException {
        
        if (md5.equals(Utils.getMd5(file.getPath()))) {
            return;
        }
        
        close();
        
        if(useTempFiles) {
            openUsingTemp();
        } else {
            open();
        }
    }

    @Override
    public String getDepth() {
        return depth;
    }
    
    @Override
    public String toString() {
        return file.getName();
    }
}
