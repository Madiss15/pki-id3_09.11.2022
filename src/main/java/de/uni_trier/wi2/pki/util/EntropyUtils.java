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
     * football, than labelIndex is 2
     * @return the information gain for each attribute
     */

    static List<String> range;
    static CSVAttribute[][] attributesAsArray;
    static int sizeOfMatrix;

    public static List<Double> calcInformationGain(Collection<CSVAttribute[]> matrix, int labelIndex) {
        List<Double> gains = new ArrayList<>();     //Eine Liste von Gain-Werten
        List<CSVAttribute[]> listMatrix = (List<CSVAttribute[]>) matrix;   //Konvertierung von der Collection zur List
        range = Main.rangeFinder(listMatrix, labelIndex);     //Erfassung der möglichen Werte, die labelIndex annimmt
        if (range.size() <= 1)     //Wenn nur noch ein Wert in range steht, wurde ein Blattknoten erreicht
            return (null);
        sizeOfMatrix = matrix.size();
        attributesAsArray = Main.convetToArray(listMatrix);
        double he = he(listMatrix, labelIndex, range); //Berechnung von H(E)
        for (int i = 0; i < listMatrix.get(0).length; i++) { //Es wird über alle Spalten Iteriert (bis auf labelIndex) und deren Informationsgehalt bestimmt
            if (!(i == labelIndex)) {
                gains.add(he - ra(listMatrix, i, labelIndex));  //Gain(A) = H(E) - R(A)
            }
        }
        return gains;
    }

    private static double ra(List<CSVAttribute[]> matrix, int labelIndexToAnalyze, int labelIndex) { //Berechnet R(A) von der Spalte labelIndexToAnalyze
        double ra = 0;
        List<String> rangeToAnalyze = Main.rangeFinder(matrix, labelIndexToAnalyze);       //Ermittelt die Attributwerte, die labelIndexToAnalyze annimmt
        int[] rangeCounter = Main.rangeCounter(matrix, rangeToAnalyze, labelIndexToAnalyze);    //Ermittel, wie häufig die einzelnen Attributwerte vorkommen
        for (int k = 0; k < rangeToAnalyze.size(); k++) {   //Es wird über alle unterschiedlichen Attributwerte der Spalte labelIndexToAnalyze iteriert
            double heOfk = 0;
            heOfk = calculateOccurrencesLabelIndex(labelIndexToAnalyze, rangeToAnalyze.get(k), labelIndex, rangeCounter[k]);
            ra = ra + ((double) rangeCounter[k] / matrix.size()) * heOfk;     //Die äußere for-Schleife geht nun einen Schritt weiter und berechnet H(E) für eine neue Ausprägung k
        }
        return ra;
    }

    private static double calculateOccurrencesLabelIndex(int labelIndexToAnalyze, String rangeToAnalyze, int labelIndex, int amount) {
        double heOfk = 0;
        for (int i = 0; i < range.size(); i++) {    //Es wird über alle unterschiedlichen Attributwerte der Spalte labelIndex (Die Spalte, deren Wert vorhergesagt werden soll) iteriert
            double counter = countOcourences(labelIndexToAnalyze, rangeToAnalyze, labelIndex, range.get(i));
            if (counter != 0) { //Es wird H(E) bzw. hek für die Ausprägung k berechnet und auf gesamt R(A) bzw. ra addiert
                double a = -(double) counter / amount;
                double b = log2(counter / (double) amount);
                heOfk = heOfk + a * b;      //H(E) bzw. hek für die Ausprägung k wird durch das Aufsummieren berechnet und zum Ende der beiden inneren for-Schleifen in Abhängigkeit von seiner Häufigkeit (die in rangeCounter ermittelt wurde) auf ra addiert
            }
        }
        return heOfk;
    }

    private static int countOcourences(int labelIndexToAnalyze, String valueToAnalyze, int labelIndex, String vlaueFromRange) {
        int counter = 0;
        for (int j = 0; j < sizeOfMatrix; j++) {   //Es wird über die gesamte Tabelle Zeile für Zeile iteriert, und getestet, ob der Attributwert aus labelIndexToAnalyze und der Attributwert aus labelIndex gleichzeitig vorkommen
            if (attributesAsArray[j][labelIndexToAnalyze].getValue().equals(valueToAnalyze) && attributesAsArray[j][labelIndex].getValue().equals(vlaueFromRange)) {         //Dies muss für alle aus labelIndexToAnalyze*labelIndex vorgenommen werden
                counter++; //Die Vorkommen werden gezählt
            }
        }
        return counter;
    }

    private static double log2(double N) {   //Berechnet den Logarithmus zur Basis 2
        double result = (Math.log(N) / Math.log(2));
        return result;
    }

    private static double he(List<CSVAttribute[]> matrix1, int labelIndex, List range) {    //Berechnet H(E) der Spalte, deren Wert vorhergesagt werden soll
        int[] rangeCounter = Main.rangeCounter(matrix1, range, labelIndex);
        double he = 0;
        for (double a : rangeCounter) {
            double b = -(a / (double) matrix1.size());
            double c = log2(a / (double) matrix1.size());
            he = he + b * c;
        }
        return he;
    }
}
