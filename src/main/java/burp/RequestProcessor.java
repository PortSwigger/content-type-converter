package burp;

import java.util.List;
import java.util.UUID;

import static burp.Utilities.extractBodyFromRequest;
import static burp.Utilities.getAppropriateRequest;

public class RequestProcessor
{
    private static final String BOUNDARY_PLACEHOLDER = "${BOUNDARY}";

    private final IExtensionHelpers helpers;
    private final BodyProcessor bodyProcessor;
    private final String contentTypeHeaderValue;

    public RequestProcessor(IExtensionHelpers helpers, BodyProcessor bodyProcessor, String contentTypeHeaderValue)
    {
        this.helpers = helpers;
        this.bodyProcessor = bodyProcessor;
        this.contentTypeHeaderValue = contentTypeHeaderValue;
    }

    public byte[] convert(byte[] initialRequest)
    {
        byte[] request = getAppropriateRequest(helpers, initialRequest);

        IRequestInfo requestInfo = helpers.analyzeRequest(request);

        byte contentType = requestInfo.getContentType();
        String body = extractBodyFromRequest(requestInfo, request);

        String processedBody = bodyProcessor.process(contentType, body);

        String finalContentType = contentTypeHeaderValue;

        if (contentTypeHeaderValue.contains(BOUNDARY_PLACEHOLDER))
        {
            String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            finalContentType = contentTypeHeaderValue.replace(BOUNDARY_PLACEHOLDER, boundary);
            processedBody = processedBody.replace(BOUNDARY_PLACEHOLDER, boundary);
        }

        List<String> headers = requestInfo.getHeaders();
        headers.removeIf(s -> s.toLowerCase().startsWith("content-type"));
        headers.add("Content-Type: " + finalContentType);

        return helpers.buildHttpMessage(headers, processedBody.getBytes());
    }
}
