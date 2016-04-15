package jpdftweak.tabs.input.rowconstruction;

import com.itextpdf.text.Rectangle;
import java.text.DecimalFormat;
import jpdftweak.core.UnitTranslator;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.treetable.items.PageNode;

/**
 *
 * @author Vasilis Naskos
 */
public class PageNodeConstructor {

    private static final int DPI = 72;
    private final InputFile inputFileItem;
    private final int indexInFile;
    private final int indexInNode;
    private final Rectangle pageSize;
    
    public PageNodeConstructor(InputFile inputFileItem, int index) {
        this.inputFileItem = inputFileItem;
        this.indexInNode = index;
        this.indexInFile = index + 1;
        this.pageSize = inputFileItem.getPageSize(indexInFile);
    }
    
    public PageNode createPageNode() {
        Object[] data = new Object[4];
        
        data[0] = getPageName();
        data[1] = getPaperSize();
        data[2] = getPageOrientation();
        data[3] = getColorDepth();

        return new PageNode(data, indexInNode);
    }
    
    private String getPageName() {
        return "Page " + indexInFile;
    }
    
    private String getPaperSize() {
        StringBuilder paperSizeBuilder = new StringBuilder();
        
        paperSizeBuilder.append(getPageWidthFormated());
        paperSizeBuilder.append(" x ");
        paperSizeBuilder.append(getPageHeightFormated());
        paperSizeBuilder.append(" inch");
        
        return paperSizeBuilder.toString();
    }
    
    private String getPageWidthFormated() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        
        double widthInInches = getPageWidthInInches();
        
        return df.format(widthInInches);
    }
    
    private double getPageWidthInInches() {
        int width = (int) pageSize.getWidth();
        double widthInInches = UnitTranslator.pixelsToInch(width, DPI);
        
        return widthInInches;
    }
    
    private String getPageHeightFormated() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        
        double heightInInches = getPageHeightInInches();
        
        return df.format(heightInInches);
    }
    
    private double getPageHeightInInches() {
        int height = (int) pageSize.getHeight();
        double heightInInches = UnitTranslator.pixelsToInch(height, DPI);
        
        return heightInInches;
    }
    
    private String getPageOrientation() {
        String orientation = "Portait";
        
        if(pageSize.getWidth() > pageSize.getHeight())
            orientation = "Landscape";
        
        return orientation;
    }
    
    private String getColorDepth() {
        return inputFileItem.getDepth();
    }
}
