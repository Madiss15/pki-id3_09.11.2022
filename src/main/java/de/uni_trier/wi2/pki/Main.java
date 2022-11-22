package de.uni_trier.wi2.pki;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.XMLWriter;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.io.attr.Categorical;
import de.uni_trier.wi2.pki.io.attr.Continuous;
import de.uni_trier.wi2.pki.io.attr.Discrete;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.preprocess.Formater;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import de.uni_trier.wi2.pki.util.EntropyUtils;
import de.uni_trier.wi2.pki.util.ID3Utils;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class Main {
    static String xmlPath = "src/main/resources/ATest.xml";
    static int numberOfBins = 5;
    static int labelIndex = 4;
    static List<String[]> content = new ArrayList<>();
    static boolean testIfDiscrete = true;      // false -> es wird nur auf categorical und continuous getestet
    //potenzielle bereits diskrete Attribute werden nicht genutzt

    public static int[] type; //Falls es eine Spalte gibt, in der nur ganze Zahlen abgespeichert werden,
    //können diese bereits als diskret vermerkt werden.
    //0=categorical 1=continuous 2=discrete

    static boolean ignoreHead = false;
    static int skippFirstLine = 1; //Dieser Wert muss 1 sein, wenn in der ersten Zeile die Attributenbezeichnungen stehen.

    public static void main(String[] args) {
        if (ignoreHead)
            skippFirstLine = 1;

        content = CSVReader.readCsvToArray("src/main/resources/Weather.csv", ";", ignoreHead);
        type = new int[content.get(0).length];
        typeTester(content);
        List<CSVAttribute[]> attributes = attributeListConverter(content);
        for (int k = 0; k < attributes.get(skippFirstLine).length; k++) {
            System.out.println(content.get(0)[k] + " is " + attributes.get(0)[k].getClass().getSimpleName());
        }

        BinningDiscretizer discretizer = new BinningDiscretizer();
        for (int i = 0; i < type.length; i++) {
            if (type[i] == 1) {
                attributes = discretizer.discretize(numberOfBins, attributes, i);
            }
        }
        Formater formater = new Formater();  // Der Code funktioniert nur, wenn der Labelindex am Ende CSV-Datei ist, folglich muss er in allen anderen Fällen ans Ende verschoben werden
        List<CSVAttribute[]> formatedAtributes = formater.format(attributes, labelIndex);


        HashMap<String, String> a = new HashMap<>();

        ID3Utils utils = new ID3Utils();
        DecisionTreeNode root = utils.createTree(formatedAtributes, formatedAtributes.get(0).length - 1);
        System.out.println("##################################");
        System.out.println("Saved at: "+xmlPath);
        XMLWriter writer = new XMLWriter();
        try {
            writer.writeXML(xmlPath,root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: this should be the main executable method for your project
    }

    public static List<CSVAttribute[]> attributeListConverter(List<String[]> list) {
        List<CSVAttribute[]> attribute = new LinkedList<>();
        for (int i = skippFirstLine; i < list.size(); i++) {
            CSVAttribute[] number = attributeLineConverter(list.get(i));
            attribute.add(number);
        }
        return attribute;
    }

    public static CSVAttribute[] attributeLineConverter(String[] line) {
        int length = line.length;
        CSVAttribute[] attributeLine = new CSVAttribute[length];
        for (int i = 0; i < length; i++) {
            Object value;
            CSVAttribute a;
            if (type[i] == 0) {
                a = new Categorical();
                a.setValue(line[i]);
                a.setBackUpValue(line[i]);
                a.setAttributIndex(i);
            } else if (type[i] == 1) {
                a = new Continuous();
                a.setValue(line[i]);
                a.setBackUpValue(Double.parseDouble(line[i]));
                a.setAttributIndex(i);
            } else {
                a = new Discrete();
                a.setValue(line[i]);
                a.setBackUpValue(Integer.parseInt(line[i]));
                a.setAttributIndex(i);
            }
            attributeLine[i] = a;
        }
        return attributeLine;
    }

    public static void typeTester(List<String[]> content) {
        for (int k = 0; k < content.get(0).length; k++) {
            for (int i = skippFirstLine; i < content.size(); i++) {
                try {
                    double value = Double.parseDouble(content.get(i)[k]);
                    if (value != (int) value) {
                        type[k] = 1;
                        break;
                    }
                } catch (Exception e) {
                    type[k] = 0;
                    break;
                }
                if (testIfDiscrete) {
                    type[k] = 2;
                } else {
                    type[k] = 1;
                }
            }
        }
    }

    public static String getIndexName(int a) {
        if(a == -1)
            return ("Dead End");
        return (content.get(0)[a]);
    }

    public static List rangeFinder(List<CSVAttribute[]> matrix1, int labelIndex) {
        List range = new LinkedList();
        for (int i = 0; i < matrix1.size(); i++)
            if (!range.contains(matrix1.get(i)[labelIndex].getValue()))
                range.add(matrix1.get(i)[labelIndex].getValue());

        return range;
    }
}
