package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GiniImpurity {

    static List<String> range;
    static CSVAttribute[][] attributesAsArray;
    static int sizeOfMatrix;

    /**
     * This form basically works like EntropyUtils, except for a few adjustments to the new formula
     *
     * @param matrix
     * @param labelIndex
     * @return
     */
    public static List<Double> calcInformationGain(Collection<CSVAttribute[]> matrix, int labelIndex) {
        List<Double> gains = new ArrayList<>();
        List<CSVAttribute[]> listMatrix = (List<CSVAttribute[]>) matrix;
        range = Main.rangeFinder(listMatrix, labelIndex);
        if (range.size() <= 1)
            return (null);
        sizeOfMatrix = matrix.size();
        attributesAsArray = Main.convetToArray(listMatrix);
        for (int i = 0; i < listMatrix.get(0).length; i++) {
            if (!(i == labelIndex)) {
                gains.add(ra(listMatrix, i, labelIndex));
            }
        }
        return gains;
    }

    private static double ra(List<CSVAttribute[]> matrix, int labelIndexToAnalyze, int labelIndex) {
        double ra = 0;
        List<String> rangeToAnalyze = Main.rangeFinder(matrix, labelIndexToAnalyze);
        int[] rangeCounter = Main.rangeCounter(matrix, rangeToAnalyze, labelIndexToAnalyze);
        for (int k = 0; k < rangeToAnalyze.size(); k++) {
            double heOfk = 0;
            heOfk = calculateOccurrencesLabelIndex(labelIndexToAnalyze, rangeToAnalyze.get(k), labelIndex, rangeCounter[k]);
            ra = ra + ((double) rangeCounter[k] / matrix.size()) * heOfk;
        }
        return ra;
    }

    private static double calculateOccurrencesLabelIndex(int labelIndexToAnalyze, String rangeToAnalyze, int labelIndex, int amount) {
        double heOfk = 0;
        for (int i = 0; i < range.size(); i++) {
            double counter = EntropyUtils.countOcourences(labelIndexToAnalyze, rangeToAnalyze, labelIndex, range.get(i));
            if (counter != 0) {
                double a = -(double) counter / amount;
                heOfk = heOfk + a * -a;
            }
        }
        return 1 - heOfk;
    }
}
