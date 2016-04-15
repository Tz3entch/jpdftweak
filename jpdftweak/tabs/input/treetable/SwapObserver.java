package jpdftweak.tabs.input.treetable;

import jpdftweak.tabs.input.treetable.items.Node;

/**
 *
 * @author Vasilis Naskos
 */
public interface SwapObserver {
    
    public void notify(Node node, int index);
    
}
