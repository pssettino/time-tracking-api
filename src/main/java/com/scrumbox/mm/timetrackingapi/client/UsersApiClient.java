package com.scrumbox.mm.timetrackingapi.client;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.scrumbox.mm.timetrackingapi.exception.UsersApiClientException;
import com.scrumbox.mm.timetrackingapi.model.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class UsersApiClient {
    private static final Logger log = LoggerFactory.getLogger(UsersApiClient.class);

    private RestTemplate restTemplate = restTemplate();

    @Autowired
    private @Qualifier("eurekaClient")
    EurekaClient eurekaClient;

    public Absence findAbsenceByDocumentNumber(Integer documentNumber) throws UsersApiClientException {
        try {
            InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);
            String serviceUrl = String.format("http://%s:%s/api/absences/dni/%s", userInstance.getIPAddr(), userInstance.getPort(), documentNumber.toString());

            log.info("User service url: {}", serviceUrl);

            ResponseEntity<Absence> response = restTemplate.getForEntity(serviceUrl, Absence.class);

            log.info("Response: {}", response.getBody());

            return response.getBody();
        } catch (RestClientException re) {
            return null;
        } catch (Exception ex) {
            throw new UsersApiClientException("Error when trying to find justifications: " + ex.getMessage());
        }
    }

    public Sanction findSanctionByDocumentNumber(Integer documentNumber) throws UsersApiClientException {
        try {
            InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);
            String serviceUrl = String.format("http://%s:%s/api/sanction/%d", userInstance.getIPAddr(), userInstance.getPort(), documentNumber);

            ResponseEntity<Sanction> response = restTemplate.getForEntity(serviceUrl, Sanction.class);

            return response.getBody();
        } catch (RestClientException re) {
            return null;
        } catch (Exception ex) {
            throw new UsersApiClientException("Error when trying to find sanctions: " + ex.getMessage());
        }
    }

    public String findEmployeeByDocumentNumber(Integer documentNumber) throws UsersApiClientException {
        try {
            InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);

            String serviceUrl = String.format("http://%s:%s/api/employees/dni/%d", userInstance.getIPAddr(), userInstance.getPort(), documentNumber);

            ResponseEntity<Employee> response = restTemplate.getForEntity(serviceUrl, Employee.class);

            Employee employee = response.getBody();

            return employee.getShiftId();
        } catch (RestClientException re) {
            return null;
        } catch (Exception ex) {
            throw new UsersApiClientException("Error when trying to find employees: " + ex.getMessage());
        }
    }

    public Shift findShiftByShiftId(String shiftId) throws UsersApiClientException {
        try {
            InstanceInfo userInstance = eurekaClient.getApplication("users-api").getInstances().get(0);

            String serviceUrl = String.format("http://%s:%s/api/shifts/%s", userInstance.getIPAddr(), userInstance.getPort(), shiftId);

            ResponseEntity<Shift> response = restTemplate.getForEntity(serviceUrl, Shift.class);

            return response.getBody();
        } catch (RestClientException re) {
            return null;
        } catch (Exception ex) {
            throw new UsersApiClientException("Error when trying to find shifts: " + ex.getMessage());
        }
    }

    public Boolean isHoliday(DateTime today) throws UsersApiClientException {
        try {
            String serviceUrl = String.format("https://nolaborables.com.ar/api/v2/feriados/%s", today.getYear());
            ResponseEntity<Holiday[]> response = restTemplate.getForEntity(serviceUrl, Holiday[].class);

            List<Holiday> holidays = Arrays.asList(response.getBody());

            return holidays.stream().filter(it -> it.getDia() == today.getDayOfMonth() && it.getMes() == today.getMonthOfYear()).count() > 0;

        } catch (RestClientException re) {
            return null;
        } catch (Exception ex) {
            throw new UsersApiClientException("Error when trying to find holidays: " + ex.getMessage());
        }
    }


    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
