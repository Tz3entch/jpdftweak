package jpdftweak.tabs.input.treetable.items;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import jpdftweak.tabs.input.treetable.SwapObserver;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

/**
 *
 * @author Vasilis Naskos
 */
public class Node extends AbstractMutableTreeTableNode {

    private SwapObserver observer;
    
    public Node(Object[] data, SwapObserver observer) {
        super(data);
        this.observer = observer;
    }
    
    public Node(Object[] data) {
        super(data);
//        this.observer = ((Node)getParent()).getObserver();
    }
    
    public void setObserver(SwapObserver observer) {
        this.observer = observer;
    }
    
    public SwapObserver getObserver() {
        return observer;
    }

    @Override
    public void setParent(MutableTreeTableNode newParent) {
        super.setParent(newParent);
        if(newParent == null)
            return;
        this.observer = ((Node)getParent()).getObserver();
    }
    
    @Override
    public Object getValueAt(int i) {
        return getData()[i];
    }

    @Override
    public int getColumnCount() {
        return getData().length;
    }
    
    public Object[] getData() {
        return (Object[]) getUserObject();
    }
    
    /**
     * This method recursively (or not) sorts the nodes, ascending, or
     * descending by the specified column.
     *
     * @param sortColumn Column to do the sorting by.
     * @param sortAscending Boolean value of weather the sorting to be done
     * ascending or not (descending).
     * @param recursive Boolean value of weather or not the sorting should be
     * recursively applied to children nodes.
     */
    public void sortNode(int sortColumn, boolean sortAscending, boolean recursive) {
        int childCount = this.getChildCount();
        TreeMap<Object, Node> nodeData = new TreeMap();

        for (int i = 0; i < childCount; i++) {
            Node child = (Node) this.getChildAt(i);
            if(child instanceof PageNode)
                return;
            
            if (child.getChildCount() > 0 && recursive) {
                child.sortNode(sortColumn, sortAscending, recursive);
            }
            String key = child.getData()[sortColumn].toString();
            nodeData.put(key, child);
        }

        Iterator<Map.Entry<Object, Node>> nodesIterator;
        if (sortAscending) {
            nodesIterator = nodeData.entrySet().iterator();
        } else {
            nodesIterator = nodeData.descendingMap().entrySet().iterator();
        }

        int index = 0;
        while (nodesIterator.hasNext()) {
            Map.Entry<Object, Node> nodeEntry = nodesIterator.next();
            swap(nodeEntry.getValue(), index);
            index++;
        }
    }
    
    private void swap(Node node, int index) {
        observer.notify(node, index);
    }
    
}
