package jpdftweak.core;

import java.util.ArrayList;
import java.util.Arrays;
import jpdftweak.tabs.input.items.ImageInputFile;
import jpdftweak.tabs.input.items.InputFile;
import jpdftweak.tabs.input.items.PdfInputFile;

public class PageRange {

    private final InputFile inputFile;
    private final int from;
    private final int to;
    private final boolean includeOdd;
    private final boolean includeEven;
    private final IntegerList emptyBefore;
    private final ArrayList<Integer> pageOrder;

    public PageRange(PdfInputFile inputFile, int from, int to, boolean includeOdd, boolean includeEven, IntegerList emptyBefore, ArrayList<Integer> pageOrder) {
        this.inputFile = inputFile;
        if (from < 0) {
            from += inputFile.getPageCount() + 1;
        }
        if (to < 0) {
            to += inputFile.getPageCount() + 1;
        }
        this.from = from;
        this.to = to;
        this.includeOdd = includeOdd;
        this.includeEven = includeEven;
        this.emptyBefore = emptyBefore;
        this.pageOrder = pageOrder;
    }
    
    public PageRange(ImageInputFile inputFile, ArrayList<Integer> pageOrder) {
        this.inputFile = inputFile;
        this.from = 1;
        this.to = 1;
        this.includeOdd = true;
        this.includeEven = true;
        this.emptyBefore = new IntegerList("0");
        this.pageOrder = pageOrder;
    }

    public InputFile getInputFile() {
        return inputFile;
    }

    public int[] getPages(int pagesBefore) {
        int emptyPagesBefore = emptyBefore.getValue()[pagesBefore % emptyBefore.getValue().length];
        int[] pages = new int[emptyPagesBefore + Math.abs(to - from) + 1];
        Arrays.fill(pages, 0, emptyPagesBefore, -1);
        int length = emptyPagesBefore;
        for (int i = from;; i += from > to ? -1 : 1) {
            if ((i % 2 == 0 && includeEven)
                    || (i % 2 == 1 && includeOdd)) {
                if(pageOrder.size() >= i)
                    pages[length++] = pageOrder.get(i-1)+1;
            }
            if (i == to) {
                break;
            }
        }
        if (length != pages.length) {
            int[] newPages = new int[length];
            System.arraycopy(pages, 0, newPages, 0, length);
            return newPages;
        }
        return pages;
    }

    public int getPage(int i) {
        return pageOrder.get(i);
    }
    
}
