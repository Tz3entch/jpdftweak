package jpdftweak.tabs.input.items;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import jpdftweak.Main;

import com.itextpdf.text.Document;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.PdfPageLabels.PdfPageLabelFormat;
import jpdftweak.core.PdfBookmark;
import jpdftweak.core.Utils;

public class PdfInputFile implements InputFile {

    private final File file;
    private final String ownerPassword;
    private PdfReader rdr;
    private String md5;

    public PdfInputFile(File file, String ownerPassword) throws IOException {
        this.file = file;
        this.ownerPassword = ownerPassword;
        open();
    }

    @Override
    public final void open() throws IOException {
        
        md5 = Utils.getMd5(file.getPath());
        
        try {
            RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.getAbsolutePath(), false, true);
            open(new PdfReader(raf, ownerPassword.getBytes("ISO-8859-1")));
        } catch (ExceptionConverter ex) {
            while (ex.getException() instanceof ExceptionConverter) {
                ex = (ExceptionConverter) ex.getCause();
            }
            if (ex.getException() instanceof InvalidPdfException) {
                try {
                    // The PdfReader constructor that takes a file name does more thorough checking and reparing, 
                    // but it will need more RAM. Therefore, if the first one fails, try that one now.
                    open(new PdfReader(file.getAbsolutePath(), ownerPassword.getBytes("ISO-8859-1")));
                } catch (ExceptionConverter ex2) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        } catch(NullPointerException ex) {
            throw new IOException(ex);
        }
    }

    private void open(PdfReader reader) throws IOException {
        rdr = reader;
        if (!rdr.isOpenedWithFullPermissions()) {
            throw new BadPasswordException("PdfReader not opened with owner password");
        }
        rdr.consolidateNamedDestinations();
        rdr.removeUnusedObjects();
        rdr.shuffleSubsetNames();
    }

    @Override
    public void close() {
        rdr.close();
    }

    @Override
    public void reopen() throws IOException {
        
        if (md5.equals(Utils.getMd5(file.getPath()))) {
            return;
        }
        
        close();
        open();
    }

    @Override
    public File getFile() {
        return file;
    }
    
    @Override
    public String getParentDir() {
        return file.getParent();
    }

    @Override
    public int getPageCount() {
        return rdr.getNumberOfPages();
    }

    @Override
    public Rectangle getPageSize(int page) {
        return rdr.getPageSizeWithRotation(page);
    }

    @Override
    public String toString() {
        return file.getName();
    }

    @Override
    public PdfReader getReader() {
        return rdr;
    }

    @Override
    public PdfImportedPage getImportedPage(PdfWriter destination, int page) {
        return destination.getImportedPage(rdr, page);
    }

    @Override
    public Map<String, String> getInfoDictionary() {
        Map<String, String> result = rdr.getInfo();
        if (result.containsKey("Producer") && result.get("Producer").indexOf(Document.getProduct()) == -1) {
            result.put("Producer", result.get("Producer") + "; modified by jPDF Tweak " + Main.VERSION + " (based on " + Document.getVersion() + ")");
        }
        return result;
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
        return ownerPassword == null ? "" : ownerPassword;
    }

    @Override
    public String getUserPassword() {
        byte[] userPwd = rdr.computeUserPassword();
        if (userPwd == null) {
            return "";
        }
        return new String(userPwd);
    }

    @SuppressWarnings("unchecked")
    public List<PdfBookmark> getBookmarks(int initialDepth) {
        List bmk = SimpleBookmark.getBookmark(rdr);
        return PdfBookmark.parseBookmarks(bmk, initialDepth);
    }

    @Override
    public PdfPageLabelFormat[] getPageLabels() {
        return PdfPageLabels.getPageLabelFormats(rdr);
    }

    @Override
    public String getDepth() {
        return null;
    }
    
}
