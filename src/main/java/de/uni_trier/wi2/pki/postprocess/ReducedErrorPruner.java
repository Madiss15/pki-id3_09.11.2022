package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

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
    double OriginalClassificationAccuracy;
    String leaveNodeValue;
    List range;
    HashMap<String, DecisionTreeNode> splitsChace;
    HashMap<String, DecisionTreeNode> splitsChace1;
    int attributeIndexChace;
    int attributeIndexChace1;
    DecisionTreeNode trainedDecisionTree;
    DecisionTreeNode node;
    Collection<CSVAttribute[]> validationExamples;
    double currentBest = 0;
    double toCompare = 0;

    public void prune(DecisionTreeNode trainedDecisionTree, Collection<CSVAttribute[]> validationExamples, int labelAttributeId) {
        this.labelAttributeId = labelAttributeId;
        OriginalClassificationAccuracy = Tester.test((List<CSVAttribute[]>) validationExamples, trainedDecisionTree, labelAttributeId);
        range = Main.rangeFinder((List) validationExamples, labelAttributeId);
        this.trainedDecisionTree = trainedDecisionTree;
        this.validationExamples = validationExamples;
        do {
            findNodeToPrune(trainedDecisionTree);
        }while (currentBest>OriginalClassificationAccuracy);
    }

    public void findNodeToPrune(DecisionTreeNode trainedDecisionTree) {
        HashMap<String, DecisionTreeNode> map = trainedDecisionTree.getSplits();
        for (HashMap.Entry<String, DecisionTreeNode> test : map.entrySet()) {
            DecisionTreeNode child = test.getValue();
            if (child== null)
                return;
            splitsChace1 = child.getSplits();
            attributeIndexChace1 = child.getAttributeIndex();
            child.resetSplits(findLeaveNode(child), null);
            child.setAttributeIndex(labelAttributeId);
            toCompare = Tester.test((List<CSVAttribute[]>) validationExamples, trainedDecisionTree, labelAttributeId);
            if (toCompare > currentBest) {
                currentBest = toCompare;
                node = child;
            }
            child.resetSplits(splitsChace1);
            child.setAttributeIndex(attributeIndexChace1);
            findNodeToPrune(child);
        }
        node.resetSplits(findLeaveNode(node), null);
        node.setAttributeIndex(labelAttributeId);
    }

    public String findLeaveNode(DecisionTreeNode child) {
        double accuracyWithBestLabel = 0;
        double searchBestLeaveNode;
        leaveNodeValue = null;
        splitsChace = child.getSplits();
        attributeIndexChace = child.getAttributeIndex();
        for (int a = 0; a < range.size(); a++) {
            child.resetSplits((String) range.get(a), null);
            child.setAttributeIndex(labelAttributeId);
            searchBestLeaveNode = Tester.test((List<CSVAttribute[]>) validationExamples, trainedDecisionTree, labelAttributeId);
            if (searchBestLeaveNode > accuracyWithBestLabel) {
                accuracyWithBestLabel = searchBestLeaveNode;
                leaveNodeValue = (String) range.get(a);
            }
        }
        child.setAttributeIndex(attributeIndexChace);
        child.resetSplits(splitsChace);
        return leaveNodeValue;
    }
}
