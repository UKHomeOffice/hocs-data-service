package uk.gov.digital.ho.hocs.ingest;

import java.util.Set;

public interface CSVList<T> {
    Set<T> getLines();
}
