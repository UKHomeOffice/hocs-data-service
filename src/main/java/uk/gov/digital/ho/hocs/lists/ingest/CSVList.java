package uk.gov.digital.ho.hocs.lists.ingest;

import java.util.Set;

public interface CSVList<T> {
    Set<T> getLines();
}
