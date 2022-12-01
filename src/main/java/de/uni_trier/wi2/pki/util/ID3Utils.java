package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for creating a decision tree with the ID3 algorithm.
 */
public class ID3Utils {

    /**
     * Create the decision tree given the example and the index of the label attribute.
     *
     * @param examples   The examples to train with. This is a collection of arrays.
     * @param labelIndex The label of the attribute that should be used as an index.
     * @return The root node of the decision tree
     */

    static List<Double> outcome;

    public static DecisionTreeNode createTree(Collection<CSVAttribute[]> examples, int labelIndex) {
        List<CSVAttribute[]> attributes = (List<CSVAttribute[]>) examples; //Konvertierung von der Collection zur List
        if (Main.rangeFinder(attributes, labelIndex).size() <= 1) {
            return testIfLeave(attributes, labelIndex);
        }
        int bestIndex = getIndexOfBestAttribute(examples, labelIndex);
        if (outcome == null || outcome.size() == 0 || outcome.get(bestIndex) == 0) {   //Wenn die Liste mit den Informationsgehalten leer ist oder der beste Wert 0, wurde ein Zustand erreicht, in dem die gleichen Attributsausprägungen zu unterschiedlichen Ergebnissen führen
            return gainIsZero(attributes, labelIndex);
        }
        //System.out.println("Attribute with biggest gain of Information: " + Main.getIndexName((int) attributes.get(0)[bestIndex].getAttributIndex()));
        System.out.println("------------------------------");
        CSVAttribute[][] attributesAsArray = Main.convetToArray(attributes);
        return setupRoot(bestIndex,attributesAsArray,labelIndex,attributes);
    }

    private static DecisionTreeNode setupRoot(int bestIndex, CSVAttribute[][] attributesAsArray,int labelIndex,List<CSVAttribute[]> attributes  ){
        DecisionTreeNode root = new DecisionTreeNode();
        root.setAttributeIndex((int) attributesAsArray[0][bestIndex].getAttributIndex());
        List<String> range = Main.rangeFinder(attributes, bestIndex);
        // System.out.println(range + " " + Main.getIndexName((int) attributes.get(0)[bestIndex].getAttributIndex()));
        for (int k = 0; k < range.size(); k++) {
            System.out.println("#Zweig: " + range.get(k) + "# " + Main.getIndexName((int) attributesAsArray[0][bestIndex].getAttributIndex()));
            DecisionTreeNode child = createTree(calcAttributesAfterSplit(bestIndex,range.get(k),attributesAsArray), labelIndex - 1);
            child.setParent(root);
            root.setSplits(range.get(k), child);
        }
        return root;
    }

    private static LinkedList<CSVAttribute[]> calcAttributesAfterSplit(int bestIndex, String rangeValue, CSVAttribute[][] attributesAsArray) {
        LinkedList<CSVAttribute[]> attributesAfterSplit = new LinkedList<CSVAttribute[]>();
        for (int i = 0; i < attributesAsArray.length; i++) {
            if (attributesAsArray[i][bestIndex].getValue().equals(rangeValue)) {
                LinkedList<CSVAttribute> test = new LinkedList<>();
                for (int j = 0; j < attributesAsArray[i].length; j++) {
                    if (!(j == bestIndex)) {
                        // System.out.print(attributes.get(i)[j].getValue() + " ");
                        test.add(attributesAsArray[i][j]);
                    }
                }
                //  System.out.println();
                attributesAfterSplit.add(test.toArray(new CSVAttribute[0]));
            }
        }
        return attributesAfterSplit;
    }

    public static DecisionTreeNode testIfLeave(List<CSVAttribute[]> attributes, int labelIndex) {
        DecisionTreeNode root = new DecisionTreeNode();
        System.out.println("LeafNode class: " + attributes.get(0)[labelIndex].getValue());
        root.setAttributeIndex((int) attributes.get(0)[labelIndex].getAttributIndex());
        root.setSplits((String) attributes.get(0)[labelIndex].getValue(), null);        //Der Blattknoten erhält den Wert von labelIndex
        return root;
    }

    private static int getIndexOfBestAttribute(Collection<CSVAttribute[]> examples, int labelIndex) {
        outcome = EntropyUtils.calcInformationGain(examples, labelIndex);  //Ermittlung des Informationsgehalts aus den aktuellen Spalten
        int bestIndex = 0;
        for (int k = 0; k < outcome.size(); k++) {      //Ermittlung des größten Informationsgehalts aus den aktuellen Spalten
            if (outcome.get(k) > outcome.get(bestIndex)) {
                bestIndex = k;
            }
        }
        return bestIndex;
    }

    private static DecisionTreeNode gainIsZero(List<CSVAttribute[]> attributes, int labelIndex) {
         DecisionTreeNode root = new DecisionTreeNode();
        // System.out.println("!Gain is zero!");
        List range = Main.rangeFinder(attributes, labelIndex);      //Um trotzdem einen Blattknoten zuordnen zu können, wird ermittelt welcher labelIndex Wert am häufigsten auftritt, dieser wird als LeafNode class gewählt
        int[] rangeCounter = Main.rangeCounter(attributes, range, labelIndex);
        int mostLikelyValue = 0;
        for (int k = 0; k < rangeCounter.length; k++) {
            if (rangeCounter[mostLikelyValue] < rangeCounter[k])
                mostLikelyValue = k;
        }
           /* System.out.println("Very likely LeafNode class:" + range.get(mostLikelyValue));
            System.out.println("------------------------------");*/
        root.setSplits("" + range.get(mostLikelyValue), null);
        root.setAttributeIndex((int) attributes.get(0)[labelIndex].getAttributIndex());
        return root;
    }
}
