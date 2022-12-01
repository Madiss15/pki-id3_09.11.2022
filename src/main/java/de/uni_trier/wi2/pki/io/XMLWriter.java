package de.uni_trier.wi2.pki.io;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.Settings;
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

/**
 * Serializes the decision tree in form of an XML structure.
 */
public class XMLWriter {

    /**
     * Serialize decision tree to specified path.
     *
     * @param path         the path to write to.
     * @param decisionTree the tree to serialize.
     * @throws IOException if something goes wrong.
     */

    static String classTitle = "Node";

    static Element rootElement;
    static Element firstSplitt;
    static Document doc;

    public static void writeXML(String path, DecisionTreeNode decisionTree) throws IOException {

        init(decisionTree);
        testIfSameLeaveNode(decisionTree);
        buildXmlTree(decisionTree, firstSplitt, doc);

        try (FileOutputStream output = new FileOutputStream(path)) {
            writeXml(doc, output);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void init(DecisionTreeNode decisionTree) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        doc = docBuilder.newDocument();
        rootElement = doc.createElement("DecisionTree");
        doc.appendChild(rootElement);
        firstSplitt = doc.createElement(classTitle);
        firstSplitt.setAttribute("attribute", Main.getIndexName(decisionTree.getAttributeIndex()));
        rootElement.appendChild(firstSplitt);
    }

    private static void buildXmlTree(DecisionTreeNode decisionTree, Element parent, Document doc) {
        HashMap<String, DecisionTreeNode> map = decisionTree.getSplits();
        for (HashMap.Entry<String, DecisionTreeNode> branch : map.entrySet()) {
            Element childElement = doc.createElement(classTitle);
            String b = branch.getKey();
            DecisionTreeNode child = branch.getValue();
            if (child == null)
                return;
            childElement = testIfLeaveNode(child, childElement, doc);
            Element compareElement = doc.createElement("IF");
            parent.appendChild(compareElement);
            Attr attributeName = doc.createAttribute("value");
            attributeName.setValue(b);
            compareElement.setAttributeNode(attributeName);
            compareElement.appendChild(childElement);
            buildXmlTree(child, childElement, doc);
        }
    }

    private static Element testIfLeaveNode(DecisionTreeNode child, Element childElement, Document doc) {
        if (child.getSplits().
                size() == 1) {
            childElement = doc.createElement("LeafNode");
            childElement.setAttribute("class", "" + child.getSplits().keySet().toArray()[0]);
        } else
            childElement.setAttribute("attribute", Main.getIndexName(child.getAttributeIndex()));
        return childElement;
    }

    private static String testIfSameLeaveNode(DecisionTreeNode decisionTree) {
        HashMap<String, DecisionTreeNode> map = decisionTree.getSplits();
        int counter = 0;
        String[] leave = new String[map.size()];
        for (HashMap.Entry<String, DecisionTreeNode> test : map.entrySet()) {
            String b = test.getKey();
            DecisionTreeNode child = test.getValue();
            if (child == null)
                return b;
            leave[counter] = testIfSameLeaveNode(child);
            counter++;
        }
        setNewLeaveNode(decisionTree, leave);
        return null;
    }

    private static void setNewLeaveNode(DecisionTreeNode decisionTree, String[] leave) {
        boolean same = true;
        if (leave[0] != null) {
            for (String a : leave)
                if (!leave[0].equals(a))
                    same = false;
            if (same) {
                decisionTree.resetSplits(leave[0], null);
                decisionTree.setAttributeIndex(Settings.getLabelIndex());
            }
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
