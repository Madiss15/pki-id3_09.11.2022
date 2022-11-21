package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.awt.*;
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

    public static DecisionTreeNode mainRoot;
    public static boolean rootIsSet = false;

    public static DecisionTreeNode createTree(Collection<CSVAttribute[]> examples, int labelIndex) {
        DecisionTreeNode root = new DecisionTreeNode();
        if (rootIsSet)
            root.setParent(mainRoot);
        else {
            root.setParent(root);
            mainRoot = root;
            rootIsSet = true;
        }
        EntropyUtils entropyUtils = new EntropyUtils();
        List<CSVAttribute[]> attributes = (List<CSVAttribute[]>) examples;

        if (Main.rangeFinder(attributes, labelIndex).size() <= 1) {
            System.out.println("Ultimate value: " + attributes.get(0)[labelIndex].getValue());
            root.setAttributeIndex((int) attributes.get(0)[labelIndex].getAttributIndex());
            root.setSplits((String) attributes.get(0)[labelIndex].getValue(), null);
            return root;
        }

        List<CSVAttribute[]> attributesRek = new LinkedList<CSVAttribute[]>();
        List<Double> outcome = entropyUtils.calcInformationGain(examples, labelIndex);

        int bestIndex = 0;
        for (int k = 0; k < outcome.size(); k++) {
            if (outcome.get(k) > outcome.get(bestIndex)) {
                bestIndex = k;
            }
        }
        root.setAttributeIndex((int) attributes.get(0)[bestIndex].getAttributIndex());

        if (outcome.size() == 0 || outcome.get(bestIndex) == 0) {
            System.out.println("!No Gain here!");
            root.setSplits("Dead End", null);
            root.setAttributeIndex(-1);
            return root;
        }
        List<String> range = Main.rangeFinder(attributes, bestIndex);
        System.out.println(range + " " + Main.getIndexName((int) attributes.get(0)[bestIndex].getAttributIndex()));
        for (int k = 0; k < range.size(); k++) {
            System.out.println("#Zweig: " + range.get(k) + "# " + Main.getIndexName((int) attributes.get(0)[bestIndex].getAttributIndex()));
            for (int i = 0; i < attributes.size(); i++) {
                if (attributes.get(i)[bestIndex].getValue().equals(range.get(k))) {
                    ArrayList<CSVAttribute> test = new ArrayList<>();
                    for (int j = 0; j < attributes.get(i).length; j++) {
                        if (!(j == bestIndex)) {
                            // System.out.print(attributes.get(i)[j].getValue() + " ");
                            test.add(attributes.get(i)[j]);
                        }
                    }
                    //  System.out.println();
                    attributesRek.add(test.toArray(new CSVAttribute[0]));
                }
            }
            root.setSplits(range.get(k), createTree(attributesRek, labelIndex - 1));
            attributesRek = new LinkedList<CSVAttribute[]>();
        }
        return root;
    }
}
