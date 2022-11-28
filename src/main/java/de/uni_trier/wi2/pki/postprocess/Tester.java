package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;


public class Tester {
    static int counter = 0;
    private static final DecimalFormat df = new DecimalFormat("0.000");


    public static String test(List<CSVAttribute[]> compare, DecisionTreeNode decisionTree, int labelIndex) {
        for (CSVAttribute[] line : compare)
            test(line, decisionTree, labelIndex);
        double a = 100 - (double) counter / (compare.size()) * 100;
        return df.format(a) + "% Wrong predicted lines";
    }

    public static void test(CSVAttribute[] compare, DecisionTreeNode decisionTree, int labelIndex) {
        String a = (String) compare[decisionTree.getAttributeIndex()].getValue();
        DecisionTreeNode node = (DecisionTreeNode) decisionTree.getSplits().get(a);
        if (decisionTree.getAttributeIndex() == labelIndex) {
            if (compare[labelIndex].getValue().equals(decisionTree.getSplits().keySet().toArray()[0]))
                counter++;
            return;
        }
        test(compare, node, labelIndex);
    }
}


