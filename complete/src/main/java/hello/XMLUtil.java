package hello;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

public class XMLUtil {

	public static int getCount(Document doc, XPath xpath, String expression) {
        String count = null;
        try {
        	//System.out.println(expression);
            XPathExpression expr = xpath.compile(expression);
            count = (String) expr.evaluate(doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null != count ? Integer.parseInt(count) : 0;
    }
}
