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

    public static List<Double> calcInformationGain(Collection<CSVAttribute[]> matrix, int labelIndex){
        List<Double> gains = new ArrayList<>();     //Eine Liste von Gain-Werten
        List<CSVAttribute[]> listMatrix = (List<CSVAttribute[]>) matrix;   //Konvertierung von der Collection zur List
        range = Main.rangeFinder(listMatrix, labelIndex);     //Erfassung der möglichen Werte, die labelIndex annimmt
        if (range.size() <= 1)     //Wenn nur noch ein Wert in range steht, wurde ein Blattknoten erreicht
            return (null);
        sizeOfMatrix = matrix.size();
        attributesAsArray = Main.convetToArray(listMatrix);
        for (int i = 0; i < listMatrix.get(0).length; i++) { //Es wird über alle Spalten Iteriert (bis auf labelIndex) und deren Informationsgehalt bestimmt
            if (!(i == labelIndex)) {
                gains.add(ra(listMatrix, i, labelIndex));  //Gain(A) = H(E) - R(A)
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
            double counter = EntropyUtils.countOcourences(labelIndexToAnalyze, rangeToAnalyze, labelIndex, range.get(i));
            if (counter != 0) { //Es wird H(E) bzw. hek für die Ausprägung k berechnet und auf gesamt R(A) bzw. ra addiert
                double a = -(double) counter / amount;
                heOfk = heOfk + a * -a;      //H(E) bzw. hek für die Ausprägung k wird durch das Aufsummieren berechnet und zum Ende der beiden inneren for-Schleifen in Abhängigkeit von seiner Häufigkeit (die in rangeCounter ermittelt wurde) auf ra addiert
            }
        }
        return 1-heOfk;
    }
}
