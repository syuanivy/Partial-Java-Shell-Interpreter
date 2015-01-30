/**
 * Created by ivy on 1/29/15.
 */
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

public class STListener implements STErrorListener {
    @Override
    public void compileTimeError(STMessage stMessage) {
        ErrorManager.instance().error(stMessage.toString());
    }

    @Override
    public void runTimeError(STMessage stMessage) {
        ErrorManager.instance().error(stMessage.toString());
    }

    @Override
    public void IOError(STMessage stMessage) {
        ErrorManager.instance().error(stMessage.toString());
    }

    @Override
    public void internalError(STMessage stMessage) {
        ErrorManager.instance().error(stMessage.toString());
    }
}
