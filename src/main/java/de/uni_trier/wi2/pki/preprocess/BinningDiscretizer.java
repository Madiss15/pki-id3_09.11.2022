package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.Main;
import de.uni_trier.wi2.pki.Settings;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;
import de.uni_trier.wi2.pki.io.attr.Continuous;

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
    static double max = Double.MIN_VALUE; //Suche der Extrema, die das Attribut annehmen kann
    static double min = Double.MAX_VALUE;
    static int size;
    static boolean binningProcedure = Settings.isBinningProcedure();
    static CSVAttribute[][] save;
    static double intervalSize;
    static int intervalSlot = 1;
    static boolean set = false;
    // true = gleiche Intervallgröße
    // false = gleiche Punkteanzahl pro Intervall

    public static List<CSVAttribute[]> discretize(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {
        if (!set) {
            size = examples.size();
            save = new CSVAttribute[size][];
            for (int i = 0; i < size; i++) {
                save[i] = examples.get(i);
            }
            set = true;
        }
        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;
        if (binningProcedure) {
            for (int i = 0; i < size; i++) {     //Suche des größten Werts
                if ((double) save[i][attributeId].getBackUpValue() > max)
                    max = (double) save[i][attributeId].getBackUpValue();
                if ((double) save[i][attributeId].getBackUpValue() < min)
                    min = (double) save[i][attributeId].getBackUpValue();
            }
            intervalSize = (max - min) / numberOfBins;      //Bestimmung der Intervallgröße

            for (int i = 0; i < examples.size(); i++) {  //Zuweisung des Bins
                while ((double) save[i][attributeId].getBackUpValue() - min > intervalSize * intervalSlot)    //Solange die Intervallgröße*intervalSlot kleiner als der Attributwert ist, wird der intervalSlot um 1 erhöht
                    intervalSlot++;
                save[i][attributeId].setValue(Integer.toString(intervalSlot));       //Zuweisung des intervalSlot als String
                intervalSlot = 1;
            }

        } else { //Wenn sich die Elemente nicht sauber auf die Anzahl der Bins verteilen lassen, werden vom kleinsten Intervall ausgehend jedem Intervall ein Element mehr zugeteilt
            Object[][] valueBackup = new Object[examples.size()][examples.get(0).length]; //Es wird eine Kopie der Attributwerte vorgenommen, sie werden vorübergehend manipuliert
            for (int i = 0; i < examples.size(); i++) {
                for (int k = 0; k < examples.get(i).length; k++) {
                    valueBackup[i][k] = examples.get(i)[k].getBackUpValue();
                }
            }
            if (numberOfBins > examples.size()) {     //Es wird einmalig eine Fehlermeldung ausgegeben
                System.out.println("More Bins than data points were selected, I proceed with a number of bins equal to data points quantity.");
                numberOfBins = examples.size();
            }
            int pointsPerInterval = examples.size() / numberOfBins; //Es wird die Zahl von Datenpunkten ermittelt
            int overflow = examples.size() % numberOfBins; //Es wird die Zahl restlichen Elemente ermittelt, die sich nicht mehr gleichmäßig auf dem Intervall verteilen lassen
            Continuous minCSV = new Continuous();
            minCSV.setBackUpValue(Double.MAX_VALUE); // Es wird ein Dummy Continuous Objekt mit maximalem Wert erstellt. Es wird später als Vergleich verwendet
            int lineOfCSVAttribute = 0;
            int binnumberCounter = 1;   //Zählt die Binnummer
            int pointCounter = 0;   //Zählt die Punkte pro Intervall (soll nicht größer als pointsPerInterval werden)
            if (overflow > 0) {
                System.out.println("Due to uneven dividable bin number, the first " + overflow + " intervals contain one element more.");
                pointCounter = -1;
                overflow--;
            }

            for (int k = 0; k < examples.size(); k++) {   //In dieser doppelten Schleife wird jedes Attribut mit jedem verglichen. Die Attributwerte sollen in aufsteigender Reihenfolge erfasst werden.
                for (int i = 0; i < examples.size(); i++) {
                    if ((double) examples.get(i)[attributeId].getBackUpValue() < (double) minCSV.getBackUpValue()) { //Das aktuelle Element soll mit dem, kleinsten bisher bekannten Wert verglichen. Ist es kleiner, wird es selbst zum kleinsten bekannten Wert
                        minCSV = (Continuous) examples.get(i)[attributeId];
                        lineOfCSVAttribute = i;    //Es wird festgehalten, in welcher Zeile sich der aktuell kleinste bekannte Attributwert befindet
                    }
                }
                examples.get(lineOfCSVAttribute)[attributeId].setValue("" + binnumberCounter);  //Dem aktuell kleinsten Wert wird nun eine Binnummer zugeteilt
                pointCounter++; // Es gibt nun einen Datenpunkt mehr in diesem Intervall
                if (pointCounter == pointsPerInterval && overflow == 0) {   //Ist das Intervall voll, wird das nächste gefüllt
                    pointCounter = 0;
                    binnumberCounter++;
                }
                if (pointCounter == pointsPerInterval && overflow > 0) {    //
                    pointCounter = -1;                 // Es soll so lange ein Element mehr pro Intervall verteilt werden, bis der Rest abgebaut wurde. Mit dieser Konstruktion wird verhindert, dass einem Bin der gesamte Rest zugeteilt wird
                    binnumberCounter++;
                    overflow--;     // Der Überschuss wird abgebaut
                }
                examples.get(lineOfCSVAttribute)[attributeId].setBackUpValue(Double.MAX_VALUE); //Indem der Wert des ermittelten Datenpunktes über den höchsten Wert gesetzt wird, wird verhindert, dass der Wert nochmal zugeteilt wird
            }
            for (int i = 0; i < examples.size(); i++) {     //Zum ermittel der Werte der Größe nach, haben wurden alle Attribute auf Max gesetzt, dies wird nun wieder korrigiert
                for (int k = 0; k < examples.get(i).length; k++) {
                    examples.get(i)[k].setBackUpValue(valueBackup[i][k]);
                }
            }
        }
        System.out.println(Main.getIndexName((int) examples.get(0)[attributeId].getAttributIndex()));   //Ausgabe der Spalte, die diskretisiert wurde. Links die originalen Werte, rechts die zugeordneten Bins (Hilfreich zu Testzwecken)
        /*for (int k = 0; k < examples.size(); k++)
            System.out.println(examples.get(k)[attributeId].getBackUpValue() + " | " + examples.get(k)[attributeId].getValue());*/
        System.out.println("------------------------------");

        return examples;
    }
}

