package jpdftweak.tabs.input.treetable.items;

/**
 *
 * @author Vasilis Naskos
 */
public class PageNode extends Node {

    private final int pageNumber;
    
    public PageNode(Object[] data, int pageNumber) {
        super(data);
        
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public Object getValueAt(int i) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
                return super.getValueAt(i);
            default:
                return "";
        }
    }
    
}
