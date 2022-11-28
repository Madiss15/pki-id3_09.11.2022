package de.uni_trier.wi2.pki;

import de.uni_trier.wi2.pki.io.CSVReader;
import de.uni_trier.wi2.pki.io.XMLWriter;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.io.attr.Categorical;
import de.uni_trier.wi2.pki.io.attr.Continuous;
import de.uni_trier.wi2.pki.io.attr.Discrete;
import de.uni_trier.wi2.pki.postprocess.CrossValidator;
import de.uni_trier.wi2.pki.postprocess.Tester;
import de.uni_trier.wi2.pki.preprocess.BinningDiscretizer;
import de.uni_trier.wi2.pki.preprocess.Formater;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import de.uni_trier.wi2.pki.util.EntropyUtils;
import de.uni_trier.wi2.pki.util.ID3Utils;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.BiFunction;

//Alle Einstellungen zur Nutzung werden in Settings festgelegt

public class Main {

    static int labelIndex = Settings.getLabelIndex();                 //Das Ergebnis dieser Spalte soll prognostiziert werden

    static String xmlPath = Settings.getXmlPath();                    //Ort der generierten XML-Datei
    static String sourcePath = Settings.getSourcePath();              //Ort der zu untersuchenden CSV-Datei

    static boolean testIfDiscrete = Settings.isTestIfDiscrete();      //true: Potenzielle bereits diskrete Attribute werden erfasst (Es wird erfasst, ob in einer Spalte nur ganze Zahlen stehen), false: Es wird nur auf kategorische und kontinuierliche Werte getestet
    static boolean ignoreHead = Settings.isIgnoreHead();              //Dieser Wert soll false sein, wenn in der ersten Zeile die Attributenbezeichnungen stehen
    static String delimiter = Settings.getDelimiter();                //Das Trennzeichen in der CSV-Datei

    static int numberOfBins = Settings.getNumberOfBins();             //Anzahl der Bins
    static boolean individualBins = Settings.isIndividualBins();      //true: es wird für jede Spalte eine individuelle Anzahl von Bins angefordert, false: es wird immer numberOfBins angewendet

    static List<String[]> content = new ArrayList<>();                 //Eine List von Strings, in der die gelesene CSV-Datei gespeichert wird
    static List<CSVAttribute[]> attributes;                            //Eine Liste von CSV-Attributen, die durch Konvertierung von content entsteht

    static int[] type;                                                 //Hier wird der Attributstyp der jeweiligen Spalte festgelegt (1: Continuous, 0: Categorical, 2: Discrete)
    static Scanner sc = new Scanner(System.in);                        //Wird benötigt für die individuelle Anzahl von Bins
    static String[] attributeName;                                     //Speichert ggf. die Attributbezeichnungen

    static DecisionTreeNode root;                                      //Wurzelknoten
    static int numFolds = Settings.getNumFolds();

    public static void main(String[] args) {

        long startTime = System.nanoTime();

        content = CSVReader.readCsvToArray(sourcePath, delimiter, ignoreHead);  //Lesen der CSV
        if (labelIndex > content.get(0).length - 1) {      //Testen auf validen Labelindex
            labelIndex = content.get(0).length - 1;
            Settings.setLabelIndex(labelIndex);
            System.out.println("Label index is out of range. I proceed with last label Index: " + (content.get(0).length - 1));
            System.out.println("------------------------------");
        }
        type = new int[content.get(0).length];
        typeTester(content);    //Der Attributstyp der jeweiligen Spalte wird festgelegt
        attributes = attributeListConverter(content);   // Konvertierung zu Objekten
        for (int k = 0; k < attributes.get(0).length; k++) {   //Ausgeben der Typen
            System.out.println(Main.getIndexName(k) + " is " + attributes.get(0)[k].getClass().getSimpleName());
        }
        System.out.println("------------------------------");

        for (int i = 0; i < type.length; i++) { //Diskretisierung aller Spalten, die kontinuierlich sind ggf. mit individueller Anzahl von Bins
            if (individualBins) {
                if (type[i] == 1) {
                    System.out.println("------------------------------");
                    System.out.println("Bin Number of " + getIndexName(i) + " " + rangeFinder(attributes, i) + ":");
                    attributes = BinningDiscretizer.discretize(sc.nextInt(), attributes, i);
                }
            } else {
                if (type[i] == 1) {
                    attributes = BinningDiscretizer.discretize(numberOfBins, attributes, i);
                }
            }
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println(totalTime / 1000000 + " ms");
        List<CSVAttribute[]> FormatedAttributes = Formater.format(attributes, labelIndex); //Der Code funktioniert nur, wenn labelIndex an der letzten Spalte steht, folglich muss er in allen anderen Fällen ans Ende verschoben werden

        BiFunction<List<CSVAttribute[]>, Integer, DecisionTreeNode> trainFunction = (data, labelAttribute) -> {
            return ID3Utils.createTree(data, labelAttribute);
        };
        CrossValidator.performCrossValidation(attributes, labelIndex, trainFunction, numFolds);

        /*root = ID3Utils.createTree(FormatedAttributes, attributes.get(0).length - 1);  //Erstellung des Baumes


        try {
            XMLWriter.writeXML(xmlPath, root);  //Speichern des Baumes
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("------------------------------");
        System.out.println("##############################");
        System.out.println("Saved at: " + xmlPath);
        System.out.println(Tester.test(attributes, root, labelIndex));
*/
        endTime = System.nanoTime();
        totalTime = endTime - startTime;
        System.out.println(totalTime / 1000000 + " ms");
    }

    public static List<CSVAttribute[]> attributeListConverter(List<String[]> content) {    //Konvertiert die gelesene CSV in eine Liste von CSV-Attributen (Fügt die Zeilen zusammen)
        List<CSVAttribute[]> attributes = new LinkedList<>();
        for (int i = 0; i < content.size(); i++) {
            CSVAttribute[] number = attributeLineConverter(content.get(i));
            attributes.add(number);
        }
        return attributes;
    }

    public static CSVAttribute[] attributeLineConverter(String[] line) {        //Konvertiert die gelesene CSV in eine Liste von CSV-Attributen (Zeile für Zeile)
        CSVAttribute[] attributeLine = new CSVAttribute[line.length];
        for (int i = 0; i < line.length; i++) {
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
                a.setBackUpValue((int) Double.parseDouble(line[i]));
                a.setAttributIndex(i);
            }
            attributeLine[i] = a;
        }
        return attributeLine;
    }

    public static void typeTester(List<String[]> content) {         //Ordnet den Spalten einen Typ in type zu
        for (int k = 0; k < content.get(0).length; k++) {
            for (int i = 0; i < content.size(); i++) {
                try {
                    double value = Double.parseDouble(content.get(i)[k]);
                    if (value != (int) value) {
                        type[k] = 1;      // 1: Continuous
                        break;
                    }
                } catch (Exception e) {
                    type[k] = 0;         // 0: Categorical
                    break;
                }
                if (testIfDiscrete)
                    type[k] = 2;         // 2: Discrete
                else
                    type[k] = 1;
            }
        }
    }

    public static String getIndexName(int labelIndex) { //ignoreHead false: Es wird die Attributenbezeichnung der Spalte labelIndex zurückgegeben
        if (ignoreHead)
            return attributeName[labelIndex];
        return ("Index " + labelIndex);
    }

    public static List rangeFinder(List<CSVAttribute[]> matrix1, int labelIndex) { //Liefert die unterschiedlichen Werte, die das Attribut an der Stelle labelIndex annimmt (kann beim Binning hilfreich sein)
        List range = new LinkedList();
        for (int i = 0; i < matrix1.size(); i++)
            if (!range.contains(matrix1.get(i)[labelIndex].getValue()))
                range.add(matrix1.get(i)[labelIndex].getValue());

        return range;
    }

    public static int[] rangeCounter(List<CSVAttribute[]> matrix1, List range, int labelIndex) { //Zählt, wie oft ein einzelner Attributwert aus range vorkommt
        int[] rangeCounter = new int[range.size()];
        for (int i = 0; i < range.size(); i++) {
            rangeCounter[i] = 0;
            for (int k = 0; k < matrix1.size(); k++) {
                if (range.get(i).equals(matrix1.get(k)[labelIndex].getValue()))
                    rangeCounter[i]++;
            }
        }
        return rangeCounter;
    }

    public static void setAttributeName(String[] attributeName) {
        Main.attributeName = attributeName;
    }

}
