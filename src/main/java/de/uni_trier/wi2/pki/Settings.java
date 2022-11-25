package de.uni_trier.wi2.pki;

public class Settings {

    static int labelIndex = 20;                  //Das Ergebnis dieser Spalte soll prognostiziert werden

    static String sourcePath = "src/main/resources/churn_data.csv"; //Ort der zu untersuchenden CSV-Datei
    static String xmlPath = "src/main/resources/xml/Test.xml";     //Ort der generierten XML-Datei

    static boolean ignoreHead = false;          //Dieser Wert soll false sein, wenn in der ersten Zeile die Attributenbezeichnungen stehen
    static String delimiter = ";";              //Das Trennzeichen in der CSV-Datei
    static boolean testIfDiscrete = false;      //true: Potenzielle bereits diskrete Attribute werden erfasst (Es wird erfasst, ob in einer Spalte nur ganze Zahlen stehen), false: Es wird nur auf kategorische und kontinuierliche Werte getestet

    static int numberOfBins = 5;                //Anzahl der Bins, es wird ab 1 gezählt
    static boolean binningProcedure = false;     //true: Gleiche Intervallgröße, false: gleiche Punkteanzahl pro Intervall
    static boolean individualBins = false;      //true: Es wird für jede Spalte eine individuelle Anzahl von Bins angefordert, false: es wird immer numberOfBins angewendet

    public static String getDelimiter() {
        return delimiter;
    }

    public static int getLabelIndex() {
        return labelIndex;
    }

    public static String getXmlPath() {
        return xmlPath;
    }

    public static String getSourcePath() {
        return sourcePath;
    }

    public static boolean isTestIfDiscrete() {
        return testIfDiscrete;
    }

    public static boolean isIgnoreHead() {
        return ignoreHead;
    }

    public static int getNumberOfBins() {
        return numberOfBins;
    }

    public static boolean isIndividualBins() {
        return individualBins;
    }

    public static boolean isBinningProcedure() {
        return binningProcedure;
    }
}
