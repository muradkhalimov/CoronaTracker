package MuradKhalimov.coronatracker.services;

import MuradKhalimov.coronatracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service //содержит бизнес-логику и вызывает методы на уровне хранилища(можно использоваться @Component т.к. это часть @Component) - SpringBootApplication будет работать без вызовы в методе main
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() { //что бы только данные из allStats закинуть в home.html через model.addAtribute в контроллере
        return allStats;
    }

    @PostConstruct //команда программе запустить метод как только создаться CoronaVirusDataService класс(при запуске приложения) - а создаться он при запуске потому что это Сервис Spring (@Service) - SpringBootApplication будет работать без вызовы в методе main
    @Scheduled(cron = "* * 1 * * *") //можно указать график работы приложения * * * * * * - s m h d mm y - когда все зведочки значит каждую секунду
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>(); //сделано дабы не получить ошибку в случае одновременного запроса к серверу со стороны пользователя и обновления данных в allStats - вопрос concurrency (многопоточности), показывает allStats данные пока newStats обновляется, а затем обновляет allStats данными из newStats
        HttpClient client = HttpClient.newHttpClient();  //создаем клиент для отправки запросов и приема ответов
        HttpRequest request = HttpRequest.newBuilder()  //создаем запрос вместе с ссылкой с данными
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString()); //отправляем запрос
      //  System.out.println(httpResponse.body()); //выводим тело ответа (еще имеются header, request, uri и тому подобное)

        //библиотека CSV Apache ->
        StringReader csvBodyReader = new StringReader(httpResponse.body()); // StringReader - считывает String символы (Reader - считывает символы, needed as per CSV lib docs). Даем Reader счесть тело httpResponse.
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader); //форматируем CSV файл
        for (CSVRecord record : records) { //считываем header и получаем данные этого header
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));
            int latestCases= Integer.parseInt(record.get(record.size()-1));
            int previousLatest = Integer.parseInt(record.get(record.size()-2));
            locationStats.setLatestTotalCases(latestCases); //получаем данные из последней колонки. -1 потому что отсчет начинается с 0.
            locationStats.setDiffFromPrevDay(latestCases-previousLatest);
            newStats.add(locationStats);
        }
        this.allStats = newStats;

    }

}
