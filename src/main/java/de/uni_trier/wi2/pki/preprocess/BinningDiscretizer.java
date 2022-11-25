package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.Settings;
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


    static boolean binningProcedure = Settings.isBinningProcedure();
    // true = gleiche Intervallgröße
    // false = gleiche Punkteanzahl pro Intervall

    public static List<CSVAttribute[]> discretize(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {
        if (binningProcedure) {
            double max = (Double) examples.get(0)[attributeId].getBackUpValue(); //Suche der Extrema, die das Attribut annehmen kann
            double min = (Double) examples.get(0)[attributeId].getBackUpValue();
            for (int i = 0; i < examples.size(); i++) {     //Suche des größten Werts
                if ((Double) examples.get(i)[attributeId].getBackUpValue() > max)
                    max = (Double) examples.get(i)[attributeId].getBackUpValue();
            }
            for (int i = 0; i < examples.size(); i++) {     //Suche des kleinsten Werts
                if ((Double) examples.get(i)[attributeId].getBackUpValue() < min)
                    min = (Double) examples.get(i)[attributeId].getBackUpValue();
            }

            double intervalSize;
            intervalSize = (max - min) / numberOfBins;      //Bestimmung der Intervallgröße

            int intervalSlot = 1;
            for (int i = 0; i < examples.size(); i++) {  //Zuweisung des Bins
                while ((Double) examples.get(i)[attributeId].getBackUpValue() > intervalSize * intervalSlot)    //Solange die Intervallgröße*intervalSlot kleiner als der Attributwert ist, wird der intervalSlot um 1 erhöht
                    intervalSlot++;
                examples.get(i)[attributeId].setValue("" + intervalSlot);       //Zuweisung des intervalSlot als String
                intervalSlot = 1;
            }

        } else { //Wenn sich die Elemente nicht sauber auf die Anzahl der Bins verteilen lassen, werden vom kleinsten Intervall ausgehend jedem Intervall ein Element mehr zugeteilt.
            Object[][] a = new Object[examples.size()][examples.get(0).length];
            for (int i = 0; i < examples.size(); i++) {
                for (int k = 0; k < examples.get(i).length; k++) {
                    a[i][k] = examples.get(i)[k].getBackUpValue();
                }
            }
            if (numberOfBins > examples.size()) {     //Es wird einmalig eine Fehlermeldung ausgegeben
                System.out.println("More Bins than data points were selected, I proceed with a number of bins equal to data points quantity.");
                numberOfBins = examples.size();
            }
            int pointsPerInterval = examples.size() / numberOfBins;
            int overflow = examples.size() % numberOfBins; //Dies stellt die Zahl der restlichen Elemente fest, die sich nicht mehr gleichmäßig auf dem Intervall verteilen lassen.
            Continuous minCSV = new Continuous();
            minCSV.setBackUpValue(Double.MAX_VALUE);
            int lineOfCSVAttribute = 0;
            int binnumberCounter = 1;
            int pointCounter;
            if (overflow > 0) {
                System.out.println("Due to uneven dividable bin number, the first " + overflow + " intervals contain one element more.");
                pointCounter = -1;
                overflow--;
            } else {
                pointCounter = 0;
            }

            for (int k = 0; k < examples.size(); k++) {
                for (int i = 0; i < examples.size(); i++) {
                    if ((double) examples.get(i)[attributeId].getBackUpValue() < (double) minCSV.getBackUpValue()) {
                        minCSV = (Continuous) examples.get(i)[attributeId];
                        lineOfCSVAttribute = i;
                    }
                }
                examples.get(lineOfCSVAttribute)[attributeId].setValue("" + binnumberCounter);
                pointCounter++;
                if (pointCounter == pointsPerInterval && overflow == 0) {
                    pointCounter = 0;
                    binnumberCounter++;
                }
                if (pointCounter == pointsPerInterval && overflow > 0) {
                    pointCounter = -1;                 // Es soll so lange ein Element mehr pro Intervall verteilt werden, bis der Rest abgebaut wurde. Mit dieser Konstruktion wird verhindert, dass einem Bin der gesamte Rest zugeteilt wird
                    binnumberCounter++;
                    overflow--;
                }
                examples.get(lineOfCSVAttribute)[attributeId].setBackUpValue(Double.MAX_VALUE); //Indem der Wert des ermittelten Datenpunktes über den höchsten Wert gesetzt wird, wird verhindert, dass der Wert nochmal zugeteilt wird
            }
            for (int i = 0; i < examples.size(); i++) {
                for (int k = 0; k < examples.get(i).length; k++) {
                    examples.get(i)[k].setBackUpValue(a[i][k]);
                }
            }
        }
        System.out.println(Main.getIndexName((int) examples.get(0)[attributeId].getAttributIndex()));   //Ausgabe der Spalte, die diskretisiert wurde. Links die originalen Werte, rechts die zugeordneten Bins (Hilfreich zu Testzwecken)
        for (int k = 0; k < examples.size(); k++)
            System.out.println(examples.get(k)[attributeId].getBackUpValue() + " | " + examples.get(k)[attributeId].getValue());
        System.out.println("------------------------------");

        return examples;
    }
}

