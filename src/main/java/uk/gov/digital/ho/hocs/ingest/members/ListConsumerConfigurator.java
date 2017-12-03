package uk.gov.digital.ho.hocs.ingest.members;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class ListConsumerConfigurator {


    final String LIST_LORDS = "lords_list";
    final String LIST_COMMONS = "commons_list";

    final String API_UK_PARLIAMENT;
    final String API_SCOTTISH_PARLIAMENT;
    final String API_NORTHERN_IRISH_ASSEMBLY;
    final String API_EUROPEAN_PARLIAMENT;


    public ListConsumerConfigurator(@Value("${api.uk.parliament}") String apiUkParliament,
                             @Value("${api.scottish.parliament}") String apiScottishParliament,
                             @Value("${api.ni.assembly}") String apiNorthernIrishAssembly,
                             @Value("${api.european.parliament}") String apiEuropeanParliament) {

        this.API_UK_PARLIAMENT = apiUkParliament;
        this.API_SCOTTISH_PARLIAMENT = apiScottishParliament;
        this.API_NORTHERN_IRISH_ASSEMBLY = apiNorthernIrishAssembly;
        this.API_EUROPEAN_PARLIAMENT = apiEuropeanParliament;

    }
}
