package de.uni_trier.wi2.pki.util;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

import java.util.*;

/**
 * Contains methods that help with computing the entropy.
 */
public class EntropyUtils {

    /**
     * Calculates the information gain for all attributes
     *
     * @param matrix     Matrix of the training data (example data), e.g. ArrayList<String[]>
     * @param labelIndex the index of the attribute that contains the class. If the dataset is [Temperature,Weather,PlayFootball] and you want to predict playing
     *                   football, than labelIndex is 2
     * @return the information gain for each attribute
     */
    public static List<Double> calcInformationGain(Collection<CSVAttribute[]> matrix, int labelIndex) {
        List<Double> gains = new ArrayList<>();
        List<CSVAttribute[]> matrix1 = (List<CSVAttribute[]>) matrix;

        if(Main.rangeFinder(matrix1,labelIndex).size()==1 || Main.rangeFinder(matrix1,labelIndex).size()==0){
            return(null);
        }

        //System.out.println("Enter labelIndex you want to predict (counting starts at 0)");
        //Scanner sc = new Scanner(System.in);
        //labelIndex = sc.nextInt();
        System.out.println("Label index: "+ Main.getIndexName((int) matrix1.get(0)[labelIndex].getAttributIndex()));
        List<String> range = Main.rangeFinder(matrix1, labelIndex);
        double he = he(matrix1, labelIndex, range);
        System.out.println("H(E) = " + he);

        int indexWithBiggestGain =0;
        int skipper = 0;
        for (int i = 0; i < matrix1.get(0).length; i++) {
            if (!(i == labelIndex)) {
                gains.add(he - gain(matrix1, i, labelIndex));
                System.out.println("Gain: " + gains.get(i+skipper));
                if(gains.get(indexWithBiggestGain)<gains.get(i+skipper)) {
                    indexWithBiggestGain = i;
                }
            }
        }
        System.out.println("Index of Attribute with biggest gain of Information: "+indexWithBiggestGain);
        return gains;
    }

    private static double gain(List<CSVAttribute[]> matrix1, int labelIndexG, int labelIndex) {
        List<String> range = Main.rangeFinder(matrix1, labelIndex);
        int[] rangeCounter = rangeCounter(matrix1, range, labelIndex);
        double gain = 0;
        List<String> rangeG = Main.rangeFinder(matrix1, labelIndexG);
        int[] rangeCounterG = rangeCounter(matrix1, rangeG, labelIndexG);
        System.out.println(rangeG+" "+Main.getIndexName((int) matrix1.get(0)[labelIndexG].getAttributIndex()));
        for (int a : rangeCounterG)
            System.out.print(a + "  ");
        System.out.println();
        for (int k = 0; k < rangeG.size(); k++) {
            double he = 0;
            for (int i = 0; i < range.size(); i++) {
                double counter = 0;
                for (int j = 0; j < matrix1.size(); j++) {
                    if (matrix1.get(j)[labelIndexG].getValue().equals(rangeG.get(k)) && matrix1.get(j)[labelIndex].getValue().equals(range.get(i))) {
                        counter++;
                    }
                }
                //System.out.println("Value: " + rangeG.get(k) + ", AimValue: " + range.get(i) + ", corresponding Lines: " +(int) counter);
                if (counter != 0) {
                    double a = -(double) counter / rangeCounterG[k];
                    double b = (double) log2(counter / (double) rangeCounterG[k]);
                    he = he + a * b;
                }
            }
            //System.out.println("H("+rangeG.get(k)+"): "+he);
            gain = gain + ((double) rangeCounterG[k] / matrix1.size()) * he;
        }
        return gain;
    }

    public static int[] rangeCounter(List<CSVAttribute[]> matrix1, List range, int labelIndex) {
        int[] rangeCounter = new int[range.size()];
        for (int i = 0; i < range.size(); i++) {
            rangeCounter[i] = 0;
            for (int k = 0; k < matrix1.size(); k++) {
                if (range.get(i).equals(matrix1.get(k)[labelIndex].getValue()))
                    rangeCounter[i]++;
            }
        }
        return rangeCounter;
    }

    public static double log2(double N) {
        double result = (Math.log(N) / Math.log(2));
        return result;
    }

    private static double he(List<CSVAttribute[]> matrix1, int labelIndex, List range) {
        System.out.println("Range: " + range);
        int[] rangeCounter = rangeCounter(matrix1, range, labelIndex);
        for (int a : rangeCounter)
            System.out.println(a);
        double he = 0;
        for (double a : rangeCounter) {
            double b = -(a / (double) matrix1.size());
            double c = log2(a / (double) matrix1.size());
            he = he + b * c;
        }
        return he;
    }
}
