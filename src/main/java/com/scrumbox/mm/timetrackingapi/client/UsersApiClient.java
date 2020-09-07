package com.scrumbox.mm.timetrackingapi.client;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.scrumbox.mm.timetrackingapi.model.Employee;
import com.scrumbox.mm.timetrackingapi.model.Holiday;
import com.scrumbox.mm.timetrackingapi.model.Justification;
import com.scrumbox.mm.timetrackingapi.model.Shift;
import com.scrumbox.mm.timetrackingapi.service.TrackingService;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class UsersApiClient {
    private static final Logger log = LoggerFactory.getLogger(UsersApiClient.class);

    private RestTemplate restTemplate = restTemplate();

    @Autowired
    private @Qualifier("eurekaClient") EurekaClient eurekaClient;

    public Justification findJustificationByDni(Integer dni) {
        try{
            InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);
            String serviceUrl = String.format("http://%s:%s/api/justification/dni?dni=%s",userInstance.getIPAddr(), userInstance.getPort(),dni.toString());

            log.info("User service url: {}", serviceUrl);

            //Get the user
            ResponseEntity<Justification> response = restTemplate.getForEntity(serviceUrl, Justification.class);

            log.info("Response: {}", response.getBody());

            return response.getBody();
        }catch (Exception ex) {
            // TODO: Improve this catch
            System.out.println("Error");
        }
        return null;
    }

    public Integer findEmployeeByDni(Integer dni) throws JSONException {

        //Find the User Microservice
        InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);

        //Get the service URL
        String serviceUrl = String.format("http://%s:%s/api/employees/dni?dni=%s",userInstance.getIPAddr(), userInstance.getPort(),dni.toString());

        log.info("User service url: {}", serviceUrl);

        //Get the Employee
        ResponseEntity<Employee> response = restTemplate.getForEntity(serviceUrl, Employee.class);

        log.info("Response: {}", response.getBody());

        Employee employee = (Employee) response.getBody();
        return employee.getShiftId();
    }

    public Shift findShiftByShiftId(Integer shiftId) {

        InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);

        String serviceUrl = String.format("http://%s:%s/api/shift/shiftId?shiftId=%d",userInstance.getIPAddr(), userInstance.getPort(),shiftId);

        log.info("User service url: {}", serviceUrl);

        //Get the user
        ResponseEntity<Shift> response = restTemplate.getForEntity(serviceUrl, Shift.class);

        log.info("Response: {}", response.getBody());

        return response.getBody();
    }

    public Boolean isHoliday(DateTime today){
        String serviceUrl = String.format("https://nolaborables.com.ar/api/v2/feriados/%s", today.getYear());
        ResponseEntity<Holiday[]> response = restTemplate.getForEntity(serviceUrl, Holiday[].class);

        List<Holiday> holidays = Arrays.asList(response.getBody());

        return holidays.stream().filter(it -> it.getDia() == today.getDayOfMonth() && it.getMes() == today.getMonthOfYear()).count() > 0;
    }


    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
