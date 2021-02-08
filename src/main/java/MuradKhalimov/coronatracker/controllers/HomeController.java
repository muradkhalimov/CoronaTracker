package MuradKhalimov.coronatracker.controllers;

import MuradKhalimov.coronatracker.models.LocationStats;
import MuradKhalimov.coronatracker.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;


@Controller
@Component
public class HomeController { //Контроллер - это класс, предназначенный для непосредственной обработки запросов от клиента и возвращения результатов. Чаще всего, в самом контроллере не описывается логика обработки данных. Создаются дополнительные, сервисные классы, которые выполняют все основные задачи по обработке данных.
    @Autowired // Используя эту аннотацию, не нужно заботиться о том, как лучше всего передать классу или bean'у экземпляр другого bean'a. Фреймворк Spring сам найдет нужный bean и подставит его значение в свойство, которое отмечено аннотацией @Autowired. && Controller getting access to @Service CoronaVirusDataService
    CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/") // когда заходят по ссылке, направляет на home.html (то что в скобках)
    public String home(Model model)  { // Model - дает u put things in the model and in home.html can access things in the model and use them
       // model.addAttribute("locationStats", coronaVirusDataService.getAllStats()); // locationStats - ссылка, coronaVirusDataService - то что покажется& getter для того что бы данные из allStats запихнул
        List<LocationStats> allStats = coronaVirusDataService.getAllStats(); // добавяем все данные в список allStats
        int totalCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum(); //суммируем все latesttotalcases
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalReportedCases", totalCases);
        model.addAttribute("totalNewCases", totalNewCases);
        return "home";
    }




}
