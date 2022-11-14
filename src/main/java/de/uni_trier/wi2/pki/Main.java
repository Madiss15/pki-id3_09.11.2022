package de.uni_trier.wi2.pki;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.io.attr.Categorical;
import de.uni_trier.wi2.pki.io.attr.Continuous;
import de.uni_trier.wi2.pki.io.attr.Discrete;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;

import java.util.LinkedList;
import java.util.List;


public class Main {
    //Dies ist ein Test
    static int numberOfBins = 12;

    static boolean testIfDiscrete = true;      // false -> es wird nur auf categorical und continuous getestet
    //potenzielle bereits diskrete Attribute werden nicht genutzt

    static int[] type; //Falls es eine Spalte gibt, in der nur ganze Zahlen abgespeichert werden,
    //können diese bereits als diskret vermerkt werden.
    //0=categorical 1=continuous 2=discrete

    static boolean ignoreHead = false;
    static int skippFirstLine = 0; //Wenn ignoreHead false, muss bei der Entscheidung des Attributentyps die erste Zeile,
    //die in dem Fall die Namen der Attribute enthält, übersprungen werden.


    public static void main(String[] args) {
        if (!ignoreHead) {
            skippFirstLine++;
        }

        List<String[]> content = CSVReader.readCsvToArray("src/main/resources/churn_data.csv", ";", ignoreHead);
        type = new int[content.get(0).length];
        typeTester(content);
        List<CSVAttribute[]> attributes = attributeListConverter(content);

        for (int k = 0; k < attributes.get(skippFirstLine).length; k++) {
            System.out.println(content.get(0)[k] + " is " + attributes.get(0)[k].getClass().getSimpleName());
        }
        System.out.println("##############################");

        BinningDiscretizer discretizer = new BinningDiscretizer();
        for (int i = 0; i < type.length; i++) {
            if (type[i] == 1) {
                attributes = discretizer.discretize(numberOfBins, attributes, i);
            }
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
            } else if (type[i] == 1) {
                a = new Continuous();
                a.setValue(Double.parseDouble(line[i]));
            } else {
                a = new Discrete();
                a.setValue(Integer.parseInt(line[i]));
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
}
