package de.uni_trier.wi2.pki.io;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializes the decision tree in form of an XML structure.
 */
public class XMLWriter {
    static String classTitle = "Node";

    /**
     * Serialize decision tree to specified path.
     *
     * @param path         the path to write to.
     * @param decisionTree the tree to serialize.
     * @throws IOException if something goes wrong.
     */
    public static void writeXML(String path, DecisionTreeNode decisionTree) throws IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("DescisionTree");
        doc.appendChild(rootElement);

        Element a = doc.createElement(classTitle);
        a.setAttribute("attribute", Main.getIndexName(decisionTree.getAttributeIndex()));

        rootElement.appendChild(a);
        rek(decisionTree, a, doc);

        try (FileOutputStream output = new FileOutputStream(path)) {
            writeXml(doc, output);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void rek(DecisionTreeNode decisionTree, Element a, Document doc) {
        HashMap<String, DecisionTreeNode> map = decisionTree.getSplits();
        for (HashMap.Entry<String, DecisionTreeNode> test : map.entrySet()) {
            Element c = doc.createElement(classTitle);
            String b = test.getKey();
            DecisionTreeNode child = test.getValue();
            if (child == null) {
                return;
            }
            if (!child.getSplits().containsValue(child.getSplits().keySet().toArray()[0])){
                c = doc.createElement(classTitle);
                System.out.println("Treffer");
            }
            c.setAttribute("attribute", Main.getIndexName(child.getAttributeIndex()));
            Element node = doc.createElement("IF");
            a.appendChild(node);
            Attr attributeName = doc.createAttribute("value");
            attributeName.setValue(b);
            node.setAttributeNode(attributeName);
            node.appendChild(c);
            rek(child, c, doc);
        }
    }

    private static void writeXml(Document doc, OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }
}
