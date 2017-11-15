package uk.gov.digital.ho.hocs.legacy;

public abstract class AbstractFilePasrer<T> implements CSVList<T> {

    protected static String[] splitLine(String row) {
        return row.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

}