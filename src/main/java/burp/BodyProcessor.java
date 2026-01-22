package burp;

import com.google.gson.Gson;
import org.json.JSONTokener;
import org.json.XML;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import java.util.Map;

import static burp.Utilities.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public interface BodyProcessor
{
    Gson GSON = new Gson();

    String process(byte contentType, String body);

    class JsonBodyProcessor implements BodyProcessor
    {
        @Override
        public String process(byte contentType, String body)
        {
            return switch (contentType)
            {
                case 0, 1 -> GSON.toJson(extractParameters(body));
                case 2 -> GSON.toJson(extractParametersFromMultipart(body));
                case 3 -> XML.toJSONObject(body).toString(2);
                default -> body;
            };
        }
    }

    class XmlBodyProcessor implements BodyProcessor
    {
        private static final DocumentBuilderFactory DOC_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

        @Override
        public String process(byte contentType, String body)
        {
            if (contentType == 0 || contentType == 1)
            {
                body = GSON.toJson(extractParameters(body));
            }
            else if (contentType == 2)
            {
                body = GSON.toJson(extractParametersFromMultipart(body));
            }

            Object json = new JSONTokener(body).nextValue();
            String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root>" + XML.toString(json) + "</root>";

            try
            {
                DocumentBuilder builder = DOC_BUILDER_FACTORY.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(UTF_8)));
                return prettyPrint(doc);
            }
            catch (ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        private static String prettyPrint(Document xml)
        {
            try
            {
                Transformer tf = TRANSFORMER_FACTORY.newTransformer();
                tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter out = new StringWriter();
                tf.transform(new DOMSource(xml), new StreamResult(out));
                return out.toString();
            }
            catch (TransformerConfigurationException e)
            {
                throw new RuntimeException(e);
            }
            catch (TransformerException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    class UrlEncodedBodyProcessor implements BodyProcessor
    {
        @Override
        public String process(byte contentType, String body)
        {
            Map<String, String> params = switch (contentType)
            {
                case 2 -> extractParametersFromMultipart(body);
                case 3 -> extractParametersFromXml(body);
                case 4 -> extractParametersFromJson(body);
                default -> extractParameters(body);
            };

            return toUrlEncoded(params);
        }
    }

    class MultipartBodyProcessor implements BodyProcessor
    {
        @Override
        public String process(byte contentType, String body)
        {
            Map<String, String> params = switch (contentType)
            {
                case 0, 1 -> extractParameters(body);
                case 2 -> extractParametersFromMultipart(body);
                case 3 -> extractParametersFromXml(body);
                case 4 -> extractParametersFromJson(body);
                default -> extractParameters(body);
            };

            return toMultipart(params);
        }

        private String toMultipart(Map<String, String> params)
        {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet())
            {
                sb.append("--${BOUNDARY}\r\n");
                sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
                sb.append(entry.getValue()).append("\r\n");
            }

            sb.append("--${BOUNDARY}--\r\n");

            return sb.toString();
        }
    }
}
