package uk.gov.digital.ho.hocs.api_lists;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
class ListConsumerConfigurator {

    final String PROP_HOUSE = "house";

    final String API_UK_PARLIAMENT;
    final String API_SCOTTISH_PARLIAMENT;
    final String API_NORTHERN_IRISH_ASSEMBLY;
    final String API_EUROPEAN_PARLIAMENT;

    final String HOUSE_LORDS = "lords";
    final String HOUSE_COMMONS = "commons";
    final String HOUSE_SCOTTISH_PARLIAMENT = "scottish_parliament";
    final String HOUSE_NORTHERN_IRISH_ASSEMBLY = "northern_irish_assembly";
    final String HOUSE_EUROPEAN_PARLIAMENT = "european_parliament";

    final String LIST_LORDS = "lords_list";
    final String LIST_COMMONS = "commons_list";
    final String LIST_SCOTTISH_PARLIAMENT = "scottish_parliament_list";
    final String LIST_NORTHERN_IRISH_ASSEMBLY = "northern_irish_assembly_list";
    final String LIST_EUROPEAN_PARLIAMENT = "european_parliament_list";
    final String LIST_WELSH_ASSEMBLY = "welsh_assembly_list";

    ListConsumerConfigurator(@Value("${api.uk.parliament}") String apiUkParliament,
                             @Value("${api.scottish.parliament}") String apiScottishParliament,
                             @Value("${api.ni.assembly}") String apiNorthernIrishAssembly,
                             @Value("${api.european.parliament}") String apiEuropeanParliament) {

        this.API_UK_PARLIAMENT = apiUkParliament;
        this.API_SCOTTISH_PARLIAMENT = apiScottishParliament;
        this.API_NORTHERN_IRISH_ASSEMBLY = apiNorthernIrishAssembly;
        this.API_EUROPEAN_PARLIAMENT = apiEuropeanParliament;

    }
}
