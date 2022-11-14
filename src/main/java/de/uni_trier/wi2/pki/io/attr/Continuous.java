package de.uni_trier.wi2.pki.io.attr;

public class Continuous implements CSVAttribute{

    private double value;
    private int binNumber;

    @Override
    public void setValue(Object value) {
        this.value = (double) value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setBinNumber(Object binNumber) {
        this.binNumber = (int) binNumber;
    }

    @Override
    public Object getBinNumber() {
        return binNumber;
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
