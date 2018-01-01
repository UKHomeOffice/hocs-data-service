package uk.gov.digital.ho.hocs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.dto.DataListEntityRecord;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@Profile("logtoconsole")
public class DataListRepositoryTest {

    @Autowired
    private DataListRepository repository;

    private static List<DataListEntity> asList(Set<DataListEntity> set) {
        return new ArrayList<>(set);
    }

    @Before
    public void setup() {
        repository.deleteAll();

        List<DataListEntityRecord> firstEntityList = new ArrayList<>();
        firstEntityList.add(new DataListEntityRecord("Text", "Value"));
        repository.save(new DataList(new DataListRecord("Test List One", firstEntityList)));

        List<DataListEntityRecord> secondEntityList = new ArrayList<>();
        DataListEntityRecord dle = new DataListEntityRecord("\"SecondText\"", "se!c,ond va,l");
        secondEntityList.add(dle);
        repository.save(new DataList(new DataListRecord("Test List Two", secondEntityList)));
        repository.save(new DataList(new DataListRecord("Test List Three", null)));
    }

    @Test
    public void shouldRetrieveAllEntries() {
        final Iterable<DataList> all = repository.findAll();
        assertThat(all).size().isEqualTo(3);
    }

    @Test
    public void shouldRetrieveListByName() {
        final DataList dataList = repository.findOneByName("Test List One");
        assertThat(dataList.getName()).isEqualTo("Test List One");
    }

    @Test
    public void shouldRetrieveListByNameNotFound() {
        final DataList dataList = repository.findOneByName("Test List Five");
        assertThat(dataList).isNull();
    }

    @Test
    public void shouldRetrieveListEntities() {
        final DataList dataList = repository.findOneByName("Test List One");
        assertThat(dataList.getName()).isEqualTo("Test List One");
        assertThat(dataList.getEntities()).size().isEqualTo(1);
        assertThat(asList(dataList.getEntities()).get(0).getText()).isEqualTo("Text");
        assertThat(asList(dataList.getEntities()).get(0).getValue()).isEqualTo("VALUE");
    }

    @Test
    public void shouldRetrieveListSubEntities() {
        final DataList dataList = repository.findOneByName("Test List Two");
        assertThat(dataList.getName()).isEqualTo("Test List Two");
        assertThat(dataList.getEntities()).size().isEqualTo(1);

        DataListEntity dataListEntityOne = asList(dataList.getEntities()).get(0);
        assertThat(dataListEntityOne.getText()).isEqualTo("SecondText");
        assertThat(dataListEntityOne.getValue()).isEqualTo("SECOND_VAL");
    }

    @Test
    public void shouldRetrieveListEntitiesNone() {
        final DataList dataList = repository.findOneByName("Test List Three");
        assertThat(dataList.getName()).isEqualTo("Test List Three");
        assertThat(dataList.getEntities()).isEmpty();
    }
}