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
import de.uni_trier.wi2.pki.util.GiniImpurity;
import de.uni_trier.wi2.pki.util.ID3Utils;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

//All usage settings are defined in Settings

public class Main {

    static int labelIndex = Settings.getLabelIndex();
    static String xmlPath = Settings.getXmlPath();

    static String sourcePath = Settings.getSourcePath();

    static boolean testIfDiscrete = Settings.isTestIfDiscrete();
    static boolean ignoreHead = Settings.isIgnoreHead();
    static String delimiter = Settings.getDelimiter();

    static int numberOfBins = Settings.getNumberOfBins();
    static boolean individualBins = Settings.isIndividualBins();
    static int numFolds = Settings.getNumFolds();

    static List<String[]> content = new ArrayList<>();
    static List<CSVAttribute[]> attributes = new ArrayList<>();

    /**
     * To make the code generic, the formatted list is needed to build the tree.
     */
    static List<CSVAttribute[]> formattedAttributes;
    /**
     * Contains information about what type of attribute is in a column.
     * 0 = Categorical, 1 = Continuous, 2 = Discrete.
     */
    static int[] type;
    static Scanner sc = new Scanner(System.in);

    /**
     * Contains information about the name of the attribute column.
     */
    static String[] attributeName;

    /**
     * The best root node generated by CrossValidator.
     */
    static DecisionTreeNode root;

    /**
     * Is used to calculate the runtime.
     */
    static long startTime = System.nanoTime();
    static long endTime;
    static long totalTime;

    public static void main(String[] args) {
        preProcess();
        BiFunction<List<CSVAttribute[]>, Integer, DecisionTreeNode> trainFunction = ID3Utils::createTree;
        root = CrossValidator.performCrossValidation(attributes, labelIndex, trainFunction, numFolds);

  //      root = ID3Utils.createTree(formattedAttributes,formattedAttributes.get(0).length-1);

        System.out.println(Tester.test( attributes,root,labelIndex));
        try {
            XMLWriter.writeXML(xmlPath, root);  //Speichern des Baumes
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Tree saved at: " + xmlPath);

        endTime = System.nanoTime();
        totalTime = endTime - startTime;
        System.out.println("Runtime: " + totalTime / 1000000000 + " s");

    }

    /**
     * Executes all methods up to the creation of the tree.
     */
    private static void preProcess() {
        content = CSVReader.readCsvToArray(sourcePath, delimiter, ignoreHead);
        checkForCorrectLabel();
        type = new int[content.get(0).length];
        typeTester(content);
        attributes = attributeListConverter(content);
        showInformationAboutAttributes();
        System.out.println("Binning Continuous values...");
        System.out.println("------------------------------");
        discreteAllContinuous();
        System.out.println("Formatting dataset...");
        System.out.println("------------------------------");
        formattedAttributes = Formater.format(attributes, labelIndex);
        System.out.println("Building tree...");
        System.out.println("------------------------------");
    }

    /**
     * Indicates what type of attribute is in the table column.
     */
    private static void showInformationAboutAttributes() {
        for (int k = 0; k < attributes.get(0).length; k++) {
            System.out.println(Main.getIndexName(k) + " is " + attributes.get(0)[k].getClass().getSimpleName());
        }
        System.out.println("------------------------------");
    }

    /**
     * Discretizes all selected columns. IF selected with individual bin numbers.
     */
    private static void discreteAllContinuous() {
        for (int i = 0; i < type.length; i++) {
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
    }

    /**
     * Tests whether the label index is set too large and if so sets it to the last column.
     */
    private static void checkForCorrectLabel() {
        if (labelIndex > content.get(0).length - 1) {
            labelIndex = content.get(0).length - 1;
            Settings.setLabelIndex(labelIndex);
            System.out.println("Label index is out of range. I proceed with last label index: " + (content.get(0).length - 1));
            System.out.println("------------------------------");
        }
    }

    /**
     * Converts the list to attributes line by line.
     *
     * @param content the content of the table in string form
     * @return a list of converted attributes
     */
    private static List<CSVAttribute[]> attributeListConverter(List<String[]> content) {
        for (int i = 0; i < content.size(); i++) {
            CSVAttribute[] number = attributeLineConverter(content.get(i));
            attributes.add(number);
        }
        Collections.shuffle(attributes);
        return attributes;
    }

    /**
     * Converts an array of strings to attributes based on the specified attribute types from "type".
     * 0 = Categorical, 1 = Continuous, 2 = Discrete
     *
     * @param line a line from the CSV file as a String array
     * @return a line converted to attributes
     */
    private static CSVAttribute[] attributeLineConverter(String[] line) {
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

    /**
     * Determines what type of attribute in a column is. It decides based on whether a line contains letters, double numbers or just Integers.
     *
     * @param content
     */
    private static void typeTester(List<String[]> content) {
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

    /**
     * Returns the different values, that the attribute from List takes at labelIndex.
     *
     * @param matrix1
     * @param labelIndex
     * @return
     */
    public static List rangeFinder(List<CSVAttribute[]> matrix1, int labelIndex) {
        List range = new LinkedList();
        for (int i = 0; i < matrix1.size(); i++)
            if (!range.contains(matrix1.get(i)[labelIndex].getValue()))
                range.add(matrix1.get(i)[labelIndex].getValue());
        return range;
    }

    /**
     * ignoreHead false: The attribute name of the labelIndex column gets returned.
     *
     * @param labelIndex
     * @return
     */
    public static String getIndexName(int labelIndex) {
        if (ignoreHead)
            return attributeName[labelIndex];
        return ("Index " + labelIndex);
    }

    /**
     * Counts the number of occurrences of a single attribute value from range in List at labelIndex.
     *
     * @param matrix1
     * @param range
     * @param labelIndex
     * @return
     */
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

    /**
     * Saves The column names, if ignoreHeader is true.
     *
     * @param attributeName
     */
    public static void setAttributeName(String[] attributeName) {
        Main.attributeName = attributeName;
    }

    /**
     * Converts a list to a 2-dimensional array. Using 2D arrays has been shown to improve performance
     *
     * @param attributes
     * @return
     */
    public static CSVAttribute[][] convetToArray(List<CSVAttribute[]> attributes) {
        int size = attributes.size();
        CSVAttribute[][] attributesAsArray = new CSVAttribute[size][];
        for (int i = 0; i < size; i++)
            attributesAsArray[i] = attributes.get(i);
        return attributesAsArray;
    }

    public static List<CSVAttribute[]> getFormattedAttributes() {
        return formattedAttributes;
    }
}
