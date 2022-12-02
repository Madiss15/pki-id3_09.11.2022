package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.text.DecimalFormat;
import java.util.List;


public class Tester {
    static int counter;
    //private static final DecimalFormat df = new DecimalFormat("0.000");

    public static double test(List<CSVAttribute[]> compare, DecisionTreeNode decisionTree, int labelIndex) {
        counter = 0;
        for (CSVAttribute[] line : compare)
            testLine(line, decisionTree, labelIndex);
        double accuracyOfRightPredicted = (double) counter / (compare.size());
        return accuracyOfRightPredicted;
    }

    public static void testLine(CSVAttribute[] compare, DecisionTreeNode decisionTree, int labelIndex) {
        if(decisionTree==null){
            return;
        }
        String toCompare = (String) compare[decisionTree.getAttributeIndex()].getValue();
        if (decisionTree.getAttributeIndex() == labelIndex) {
            if (toCompare.equals(decisionTree.getSplits().keySet().toArray()[0]))
                counter++;
            return;
        }
        DecisionTreeNode child = (DecisionTreeNode) decisionTree.getSplits().get(toCompare);
        testLine(compare, child, labelIndex);
    }
}


