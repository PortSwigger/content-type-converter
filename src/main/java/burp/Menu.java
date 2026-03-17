package burp;

import burp.BodyProcessor.JsonBodyProcessor;
import burp.BodyProcessor.MultipartBodyProcessor;
import burp.BodyProcessor.UrlEncodedBodyProcessor;
import burp.BodyProcessor.XmlBodyProcessor;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

import static burp.IBurpExtenderCallbacks.TOOL_INTRUDER;
import static burp.IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST;

public class Menu implements IContextMenuFactory
{
    private static final BodyProcessor XML_PROCESSOR = new XmlBodyProcessor();
    private static final BodyProcessor JSON_PROCESSOR = new JsonBodyProcessor();
    private static final BodyProcessor URL_ENCODED_PROCESSOR = new UrlEncodedBodyProcessor();
    private static final BodyProcessor MULTIPART_PROCESSOR = new MultipartBodyProcessor();

    private final IBurpExtenderCallbacks callbacks;
    private final IExtensionHelpers helpers;

    public Menu(IBurpExtenderCallbacks callbacks)
    {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
    }

    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation)
    {
        if (invocation.getToolFlag() != TOOL_INTRUDER && invocation.getInvocationContext() != CONTEXT_MESSAGE_EDITOR_REQUEST)
        {
            return Collections.emptyList();
        }

        JMenuItem sendXMLToRepeater = new JMenuItem("Convert to XML");
        sendXMLToRepeater.addActionListener(
                new ErrorHandlingActionListener(
                        callbacks,
                        new RequestUpdatingActionListener(
                                invocation,
                                new RequestProcessor(helpers, XML_PROCESSOR, "application/xml;charset=UTF-8")
                        )
                )
        );

        JMenuItem sendJSONToRepeater = new JMenuItem("Convert to JSON");
        sendJSONToRepeater.addActionListener(
                new ErrorHandlingActionListener(
                        callbacks,
                        new RequestUpdatingActionListener(
                                invocation,
                                new RequestProcessor(helpers, JSON_PROCESSOR, "application/json;charset=UTF-8")
                        )
                )
        );

        JMenuItem sendUrlEncodedToRepeater = new JMenuItem("Convert to URL Encoded");
        sendUrlEncodedToRepeater.addActionListener(
                new ErrorHandlingActionListener(
                        callbacks,
                        new RequestUpdatingActionListener(
                                invocation,
                                new RequestProcessor(helpers, URL_ENCODED_PROCESSOR, "application/x-www-form-urlencoded;charset=UTF-8")
                        )
                )
        );

        JMenuItem sendMultipartToRepeater = new JMenuItem("Convert to Multipart Form");
        sendMultipartToRepeater.addActionListener(
                new ErrorHandlingActionListener(
                        callbacks,
                        new RequestUpdatingActionListener(
                                invocation,
                                new RequestProcessor(helpers, MULTIPART_PROCESSOR, "multipart/form-data; boundary=${BOUNDARY}")
                        )
                )
        );

        return List.of(sendXMLToRepeater, sendJSONToRepeater, sendUrlEncodedToRepeater, sendMultipartToRepeater);
    }
}