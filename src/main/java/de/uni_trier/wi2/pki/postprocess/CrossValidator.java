package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.Settings;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.preprocess.Formater;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import de.uni_trier.wi2.pki.util.ID3Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains util methods for performing a cross-validation.
 */
public class CrossValidator {

    /**
     * Performs a cross-validation with the specified dataset and the function to train the model.
     *
     * @param dataset        the complete dataset to use.
     * @param labelAttribute the label attribute.
     * @param trainFunction  the function to train the model with.
     * @param numFolds       the number of data folds.
     */
    public static DecisionTreeNode performCrossValidation(List<CSVAttribute[]> dataset, int labelAttribute,
                                                          BiFunction<List<CSVAttribute[]>, Integer, DecisionTreeNode> trainFunction,
                                                          int numFolds) {
        if (numFolds == 0) {
            try {
                throw new Exception("CrossValidation cannot use 0 numFolds!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.shuffle(dataset);

        int dataAmount = dataset.size();
        double accumulatedClassAccuracy = 0.0;
        for (int i = 0; i < numFolds; i++) {
            double foldSize = (double) (dataAmount - 1) / numFolds;
            int minIndexTesting = (int) (i * foldSize);
            int maxIndexTesting = (int) ((i + 1) * foldSize);

            List<CSVAttribute[]> datasetForTest = dataset.subList(minIndexTesting, maxIndexTesting);
            List<CSVAttribute[]> datasetForTrain = Stream.concat(dataset.subList(0, minIndexTesting).stream(), dataset.subList(maxIndexTesting, dataAmount - 1).stream())
                    .collect(Collectors.toList());
            datasetForTrain = Formater.format(datasetForTrain,Settings.getLabelIndex());
            DecisionTreeNode rootOfTree = trainFunction.apply(datasetForTrain, datasetForTrain.get(0).length-1);
            double classAccuracy = Tester.test(datasetForTest, rootOfTree, labelAttribute);
            accumulatedClassAccuracy += classAccuracy;
        }

        System.out.println(accumulatedClassAccuracy / numFolds);
        return null;
    }

}
