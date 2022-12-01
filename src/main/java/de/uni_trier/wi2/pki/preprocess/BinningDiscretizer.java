package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.Settings;
import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

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
    static double max;
    static double min;
    static int size;
    static boolean binningProcedure = Settings.isBinningProcedure();
    static CSVAttribute[][] attributesAsArray;
    static double intervalSize;
    static int intervalSlot = 1;
    static boolean set = false;
    static ArrayList<CSVAttribute> valuesToDiscretize;

    //For same Frequency
    static int pointCounter, binnumberCounter, pointsPerInterval, overflow;

    public static List<CSVAttribute[]> discretize(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {
        if (!set) {
            size = examples.size();
            attributesAsArray = new CSVAttribute[size][];
            for (int i = 0; i < size; i++)
                attributesAsArray[i] = examples.get(i);
            set = true;
        }
        valuesToDiscretize = init(attributeId);
        if (binningProcedure)
            sameFrequency(numberOfBins, examples, attributeId);
        else
            sameQuantityInit(numberOfBins, examples);
        /*System.out.println(Main.getIndexName((int) examples.get(0)[attributeId].getAttributIndex()));   //Ausgabe der Spalte, die diskretisiert wurde. Links die originalen Werte, rechts die zugeordneten Bins (Hilfreich zu Testzwecken)
        for (int k = 0; k < examples.size(); k++)
            System.out.println(examples.get(k)[attributeId].getBackUpValue() + " | " + examples.get(k)[attributeId].getValue());
        System.out.println("------------------------------");*/
        return examples;
    }

    public static void sameFrequency(int numberOfBins, List<CSVAttribute[]> examples, int attributeId) {
        min = (Double) valuesToDiscretize.get(0).getBackUpValue();
        max = (Double) valuesToDiscretize.get(valuesToDiscretize.size() - 1).getBackUpValue();
        intervalSize = (max - min) / numberOfBins;      //Bestimmung der Intervallgröße
        for (int i = 0; i < examples.size(); i++) {  //Zuweisung des Bins
            while ((double) attributesAsArray[i][attributeId].getBackUpValue() - min > intervalSize * intervalSlot)    //Solange die Intervallgröße*intervalSlot kleiner als der Attributwert ist, wird der intervalSlot um 1 erhöht
                intervalSlot++;
            attributesAsArray[i][attributeId].setValue(Integer.toString(intervalSlot));       //Zuweisung des intervalSlot als String
            intervalSlot = 1;
        }
    }

    public static void sameQuantityInit(int numberOfBins, List<CSVAttribute[]> examples) {
        //Wenn sich die Elemente nicht sauber auf die Anzahl der Bins verteilen lassen, werden vom kleinsten Intervall ausgehend jedem Intervall ein Element mehr zugeteilt
        if (numberOfBins > examples.size()) {     //Es wird einmalig eine Fehlermeldung ausgegeben
            System.out.println("More Bins than data points were selected, I proceed with a number of bins equal to data points quantity.");
            numberOfBins = examples.size();
        }
        pointsPerInterval = examples.size() / numberOfBins; //Es wird die Zahl von Datenpunkten ermittelt
        overflow = examples.size() % numberOfBins; //Es wird die Zahl restlichen Elemente ermittelt, die sich nicht mehr gleichmäßig auf dem Intervall verteilen lassen
        binnumberCounter = 1;   //Zählt die Binnummer
        pointCounter = 0;   //Zählt die Punkte pro Intervall (soll nicht größer als pointsPerInterval werden)
        if (overflow > 0) {
            System.out.println("Due to uneven dividable bin number, the first " + overflow + " intervals contain one element more.");
            pointCounter = -1;
            overflow--;
        }
        sameQuantity(examples);
    }

    public static void sameQuantity(List<CSVAttribute[]> examples) {
        for (int k = 0; k < examples.size(); k++) {   //In dieser doppelten Schleife wird jedes Attribut mit jedem verglichen. Die Attributwerte sollen in aufsteigender Reihenfolge erfasst werden.
            valuesToDiscretize.get(k).setValue(Integer.toString(binnumberCounter));
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
        }
    }

    public static ArrayList<CSVAttribute> init(int attributeId) {
        ArrayList<CSVAttribute> valuesToDiscretize = new ArrayList<>();
        for (int i = 0; i < size; i++)
            valuesToDiscretize.add(attributesAsArray[i][attributeId]);
        sort(valuesToDiscretize);
        return valuesToDiscretize;
    }

    public static void sort(ArrayList<CSVAttribute> valuesToDiscretize) {
        valuesToDiscretize.sort(Comparator.comparing(attribute -> ((Double) attribute.getBackUpValue())));
    }
}

