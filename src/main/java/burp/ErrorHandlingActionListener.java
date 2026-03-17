package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandlingActionListener implements ActionListener
{
    private final IBurpExtenderCallbacks callbacks;
    private final ActionListener actionListener;

    public ErrorHandlingActionListener(IBurpExtenderCallbacks callbacks, ActionListener actionListener)
    {
        this.callbacks = callbacks;
        this.actionListener = actionListener;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            actionListener.actionPerformed(e);
        }
        catch (RuntimeException ex)
        {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            callbacks.printError(out.toString());
        }
    }
}
