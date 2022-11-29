package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.Settings;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Prunes a trained decision tree in a post-pruning way.
 */
public class ReducedErrorPruner {

    /**
     * Prunes the given decision tree in-place.
     *
     * @param trainedDecisionTree The decision tree to prune.
     * @param validationExamples  the examples to validate the pruning with.
     * @param labelAttributeId    The label attribute.
     */
    int labelAttributeId;

    public void prune(DecisionTreeNode trainedDecisionTree, Collection<CSVAttribute[]> validationExamples, int labelAttributeId) {
        this.labelAttributeId = labelAttributeId;
        double classificationAccuracy = Tester.test((List<CSVAttribute[]>) validationExamples, trainedDecisionTree, labelAttributeId);
        System.out.println(findMostLikelyOutcome(trainedDecisionTree));

        System.out.println(classificationAccuracy);
    }

    private String findMostLikelyOutcome(DecisionTreeNode node) {
        String mostComonHere;
        HashMap<String, DecisionTreeNode> map = node.getSplits();
        HashMap<String, Integer> completeOutcome = new HashMap<String,Integer>();
        for (HashMap.Entry<String, DecisionTreeNode> test : map.entrySet()) {
            String b = test.getKey();
            DecisionTreeNode child = test.getValue();
            if (node.getAttributeIndex() == labelAttributeId)
                return (b);
            mostComonHere = findMostLikelyOutcome(child);
            if (!completeOutcome.containsKey(mostComonHere))
                completeOutcome.put(mostComonHere,1);
            else
                completeOutcome.replace(mostComonHere,completeOutcome.get(mostComonHere)+1);
        }
        return getObjektWithHighestValue(completeOutcome);
    }

    private String getObjektWithHighestValue(HashMap<String, Integer> completeOutcome){
        Object[] values = completeOutcome.keySet().toArray();
        int counts = 0;
        String mostCommon = null;
        for ( Object a : values){
            if (completeOutcome.get((String) a)>counts){
                counts = completeOutcome.get(a);
                mostCommon=(String)a;
            }
        }
        return mostCommon;
    }
}
