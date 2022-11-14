package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.io.attr.Continuous;

import java.sql.DatabaseMetaData;
import java.util.*;

/**
 * Class that holds logic for discretizing values.
 */
public class BinningDiscretizer {

    /**
     * Discretizes a collection of examples according to the number of bins and the respective attribute ID.
     *
     * @param numberOfBins Specifies the number of numeric ranges that the data will be split up in.
     * @param examples     The list of examples to discretize.
     * @param attributeId  The ID of the attribute to discretize.
     * @return the list of discretized examples.
     */

    static boolean errorMessageShown = false;

    static boolean binningProcedure = false;
    // true = gleiche Intervallgröße
    // false = gleiche Punkteanzahl pro Intervall

    public List<CSVAttribute[]> discretize(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {
        if (binningProcedure) {
            double max = (Double) examples.get(0)[attributeId].getValue();
            double min = (Double) examples.get(0)[attributeId].getValue();      //Es hat sich als nützlich erwiesen zunächst den größten und kleinsten Wert,
            for (int i = 0; i < examples.size(); i++) {                         //Den das Attribut annehmen kan, zu bestimmen.
                if ((Double) examples.get(i)[attributeId].getValue() > max)
                    max = (Double) examples.get(i)[attributeId].getValue();
            }
            for (int i = 0; i < examples.size(); i++) {
                if ((Double) examples.get(i)[attributeId].getValue() < min)
                    min = (Double) examples.get(i)[attributeId].getValue();
            }
            double intervalSize;
            intervalSize = (max - min) / numberOfBins;
            int intervalSlot = 1;
            for (int i = 0; i < examples.size(); i++) {
                while ((Double) examples.get(i)[attributeId].getValue() > intervalSize * intervalSlot)
                    intervalSlot++;
                examples.get(i)[attributeId].setBinNumber(intervalSlot);
                //System.out.println(examples.get(i)[attributeId].getValue()+" Bnr.: "+ examples.get(i)[attributeId].getBinNumber());
                intervalSlot = 0;
            }

        } else { //Wenn sich die Elemente nicht sauber auf die Binanzahl verteilen lassen, werden vom kleinsten Intervall jedem Intervall einem Element mehr zugeteilt.
            Object[][] a = new Object[examples.size()][examples.get(0).length];
            for (int i = 0; i < examples.size(); i++) {
                for (int k = 0; k < examples.get(i).length; k++) {
                    a[i][k] = examples.get(i)[k].getValue();
                }
            }
            if (numberOfBins > examples.size() &&!errorMessageShown) {
                System.out.println("More Bins than data points were selected, I proceed with a number of bins equal to data points quantity");
                numberOfBins = examples.size();
                errorMessageShown = true;
            }
            int pointsPerInterval = examples.size() / numberOfBins;
            int overflow = examples.size() % numberOfBins; //Dies stellt die Zahl der restlichen Elemente fest, die sich nicht mehr gleichmäßig auf dem Intervall verteilen lassen.
            Continuous minCSV = new Continuous();
            minCSV.setValue(Double.MAX_VALUE);
            int lineOfCSVAttribute = 0;
            int binnumberCounter = 1;
            int pointCounter;
            if(overflow>0&&!errorMessageShown) {
                System.out.println("Due to uneven dividable bin number, the first "+overflow+" intervals contain one element more");
                pointCounter = -1;
                overflow--;
                errorMessageShown = true;
            }else {
                pointCounter = 0;
            }
            for (int k = 0; k < examples.size(); k++) {
                for (int i = 0; i < examples.size(); i++) {
                    if ((double) examples.get(i)[attributeId].getValue() < (double) minCSV.getValue()) {
                        minCSV = (Continuous) examples.get(i)[attributeId];
                        lineOfCSVAttribute = i;
                    }
                }
                examples.get(lineOfCSVAttribute)[attributeId].setBinNumber(binnumberCounter);
                pointCounter++;
                if (pointCounter == pointsPerInterval && overflow == 0) {
                    pointCounter = 0;
                    binnumberCounter++;
                }
                if (pointCounter == pointsPerInterval && overflow > 0) {
                    pointCounter = -1;                 // Es soll so lange ein Element mehr pro Intervall verteilt werden, bis der Rest abgebaut wurde. Mit dieser Konstruktion wird verhindert, dass einem Bin der gesamte Rest zugeteilt wird.
                    binnumberCounter++;
                    overflow--;
                }
                examples.get(lineOfCSVAttribute)[attributeId].setValue(Double.MAX_VALUE); //Indem der Wert des ermittelten Datenpunktes über den höchsten Wert gesetzt wird, wird verhindert, dass der Wert nochmal zugeteilt wird.
            }
            for (int i = 0; i < examples.size(); i++) {
                for (int k = 0; k < examples.get(i).length; k++) {
                    examples.get(i)[k].setValue(a[i][k]);
                }
            }
        }
        System.out.println("##############################");
            for (int k = 0; k < examples.size(); k++)
                    System.out.println(examples.get(k)[attributeId].getValue() + " | " + examples.get(k)[attributeId].getBinNumber());

        return examples;
    }
}

