package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RequestUpdatingActionListener implements ActionListener
{
    private final IContextMenuInvocation invocation;
    private final RequestProcessor requestProcessor;

    public RequestUpdatingActionListener(IContextMenuInvocation invocation, RequestProcessor requestProcessor)
    {
        this.invocation = invocation;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];

        byte[] request = requestProcessor.convert(iReqResp.getRequest());

        if (request != null)
        {
            iReqResp.setRequest(request);
        }
    }
}
