package de.uni_trier.wi2.pki.preprocess;

import de.uni_trier.wi2.pki.io.attr.CSVAttribute;

import java.util.ArrayList;
import java.util.List;

public class Formater {

    public static List<CSVAttribute[]> format(List<CSVAttribute[]> attributes, int labelIndex) {
        if (attributes.get(0).length == labelIndex - 1) {
            return attributes;
        } else {
            List<CSVAttribute[]> formatedAtributes = new ArrayList<>();
            CSVAttribute[] index = new CSVAttribute[attributes.size()];
            for (int k = 0; k < index.length; k++)      //Die Spalte, die verschoben wird, wird gespeichert
                index[k] = attributes.get(k)[labelIndex];
            for (int k = 0; k < attributes.size(); k++) {       //Die richtig formatierte Liste wird aufgebaut (Zeile für Zeile)
                formatedAtributes.add(new CSVAttribute[attributes.get(0).length]);
                int skipper =0;
                for (int j = 0; j < attributes.get(k).length - 1; j++) {
                    if (j == labelIndex)    //Wenn die Spalte die verschoben werden soll erreicht ist, wird stattdessen eine Spalte weitergesprungen
                        skipper++;
                        formatedAtributes.get(k)[j] = attributes.get(k)[j + skipper];   //Die Inhalte werden der neuen Liste zugeteilt
                }
            }
            for (int k = 0; k < index.length; k++) {     //Die neue Liste wird nun in der letzten Spalte, die noch leer ist, mit dem anfangs erfassten Array gefüllt
                formatedAtributes.get(k)[attributes.get(0).length-1] = index[k];
            }
            return formatedAtributes;
        }
    }
}
