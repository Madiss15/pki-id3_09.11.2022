package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.text.DecimalFormat;
import java.util.List;


public class Tester {
    static int counter;
    private static final DecimalFormat df = new DecimalFormat("0.000");


    public static double test(List<CSVAttribute[]> compare, DecisionTreeNode decisionTree, int labelIndex) {
        counter = 0;
        for (CSVAttribute[] line : compare)
            test(line, decisionTree, labelIndex);
        double a = 100 - (double) counter / (compare.size()) * 100;
        //return df.format(a) + "% Wrong predicted lines";
        return a;
    }

    public static void test(CSVAttribute[] compare, DecisionTreeNode decisionTree, int labelIndex) {
        if(decisionTree==null){
            System.out.println("Problem");
            return;
        }
        String a = (String) compare[decisionTree.getAttributeIndex()].getValue();
        if (decisionTree.getAttributeIndex() == labelIndex) {
            if (a.equals(decisionTree.getSplits().keySet().toArray()[0]))
                counter++;
            return;
        }
        DecisionTreeNode node = (DecisionTreeNode) decisionTree.getSplits().get(a);
        test(compare, node, labelIndex);
    }
}


