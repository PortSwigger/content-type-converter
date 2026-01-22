package burp;

import org.json.JSONObject;
import org.json.XML;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utilities
{
    static byte[] getAppropriateRequest(IExtensionHelpers helpers, byte[] request)
    {
        if ("GET".equals(helpers.analyzeRequest(request).getMethod()))
        {
            request = helpers.toggleRequestMethod(request);
        }

        return request;
    }

    static String extractBodyFromRequest(IRequestInfo requestInfo, byte[] request)
    {
        int bodyOffset = requestInfo.getBodyOffset();

        return new String(request, bodyOffset, request.length - bodyOffset, UTF_8);
    }

    static Map<String, String> extractParameters(String body)
    {
        Map<String, String> nameValuePairs = new LinkedHashMap<>();

        for (String pair : body.split("&"))
        {
            int idx = pair.indexOf('=');
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), UTF_8) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), UTF_8).trim() : "";
            nameValuePairs.put(key, value);
        }

        return nameValuePairs;
    }

    static Map<String, String> extractParametersFromJson(String json)
    {
        Map<String, String> params = new LinkedHashMap<>();
        JSONObject obj = new JSONObject(json);

        for (String key : obj.keySet())
        {
            Object value = obj.get(key);
            params.put(key, value == null ? "" : value.toString());
        }

        return params;
    }

    static Map<String, String> extractParametersFromXml(String xml)
    {
        JSONObject json = XML.toJSONObject(xml);

        if (json.has("root"))
        {
            json = json.getJSONObject("root");
        }

        Map<String, String> params = new LinkedHashMap<>();

        for (String key : json.keySet())
        {
            Object value = json.get(key);
            params.put(key, value == null ? "" : value.toString());
        }

        return params;
    }

    static String toUrlEncoded(Map<String, String> params)
    {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (!sb.isEmpty())
            {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(), UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), UTF_8));
        }

        return sb.toString();
    }

    static Map<String, String> extractParametersFromMultipart(String body)
    {
        Map<String, String> params = new LinkedHashMap<>();

        int firstLineEnd = body.indexOf("\r\n");
        if (firstLineEnd == -1)
        {
            firstLineEnd = body.indexOf("\n");
        }
        if (firstLineEnd == -1)
        {
            return params;
        }

        String boundary = body.substring(0, firstLineEnd).trim();

        String[] parts = body.split(boundary.replace("--", "\\-\\-"));

        for (String part : parts)
        {
            if (part.isEmpty() || part.equals("--") || part.equals("--\r\n") || part.equals("--\n"))
            {
                continue;
            }

            int nameStart = part.indexOf("name=\"");
            if (nameStart == -1)
            {
                continue;
            }

            nameStart += 6;
            int nameEnd = part.indexOf("\"", nameStart);
            if (nameEnd == -1)
            {
                continue;
            }

            String name = part.substring(nameStart, nameEnd);

            int valueStart = part.indexOf("\r\n\r\n");
            if (valueStart == -1)
            {
                valueStart = part.indexOf("\n\n");
                if (valueStart != -1)
                {
                    valueStart += 2;
                }
            }
            else
            {
                valueStart += 4;
            }

            if (valueStart == -1)
            {
                continue;
            }

            String value = part.substring(valueStart).trim();

            params.put(name, value);
        }

        return params;
    }
}
