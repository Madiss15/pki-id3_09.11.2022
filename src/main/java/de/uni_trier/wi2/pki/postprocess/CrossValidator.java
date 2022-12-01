package de.uni_trier.wi2.pki.postprocess;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.tree.DecisionTreeNode;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains util methods for performing a cross-validation.
 */
public class CrossValidator {
    private static final DecimalFormat df = new DecimalFormat("0.0000");

    /**
     * Performs a cross-validation with the specified dataset and the function to train the model.
     *
     * @param dataset        the complete dataset to use.
     * @param labelAttribute the label attribute.
     * @param trainFunction  the function to train the model with.
     * @param numFolds       the number of data folds.
     */
    public static DecisionTreeNode performCrossValidation(List<CSVAttribute[]> dataset, int labelAttribute, BiFunction<List<CSVAttribute[]>, Integer, DecisionTreeNode> trainFunction, int numFolds) {

        if (numFolds == 0) {
            try {
                throw new Exception("CrossValidation cannot use 0 numFolds!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int dataAmount = dataset.size();
        double accumulatedClassAccuracy = 0d;
        for (int i = 0; i < numFolds; i++) {

            double foldSize = (double) (dataAmount - 1) / numFolds;
            int minIndexTesting = (int) (i * foldSize);
            int maxIndexTesting = (int) ((i + 1) * foldSize);
            List<CSVAttribute[]> formatedDataset = Main.getFormattedAttributes();

            List<CSVAttribute[]> datasetForTest = dataset.subList(minIndexTesting, maxIndexTesting);
            List<CSVAttribute[]> datasetForTrain = Stream.concat(formatedDataset.subList(0, minIndexTesting).stream(), formatedDataset.subList(maxIndexTesting, dataAmount - 1).stream()).collect(Collectors.toList());

            DecisionTreeNode rootOfTree = trainFunction.apply(datasetForTrain, datasetForTrain.get(0).length-1);
            ReducedErrorPruner pruner = new ReducedErrorPruner();
            System.out.println("Now pruning -- numFold: "+i);
            System.out.println("------------------------------");

            pruner.prune(rootOfTree,datasetForTest,labelAttribute);
            double classAccuracy = Tester.test(datasetForTest, rootOfTree, labelAttribute);
            accumulatedClassAccuracy += classAccuracy;
        }

        System.out.println(df.format(accumulatedClassAccuracy / numFolds)+"% of training data was correct labeled");
        System.out.println("------------------------------");
        return null;
    }

}
