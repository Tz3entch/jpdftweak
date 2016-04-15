package jpdftweak.tabs.input.rowconstruction;

import java.io.File;
import java.io.IOException;
import jpdftweak.tabs.input.items.InputFile;

/**
 *
 * @author Vasilis Naskos
 */
public interface InputFileConstructor {
    
    public InputFile createInputFile(File physicalFile) throws IOException;
    
}
