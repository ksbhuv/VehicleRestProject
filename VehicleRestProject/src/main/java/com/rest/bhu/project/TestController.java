package com.rest.bhu.project;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestController {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String getRootUrl() {
        return "http://localhost:" + port;
    }

    
    
    @Before
    public void beforeAll() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        String[] makes = {"Audi", "Ford", "Honda", "Volkswagen", "BMV",
                "Nissan", "Volvo", "Toyota"};
        String[] models = {"A3", "Fiesta", "Accord", "Polo", "i8",
                "350Z", "V90", "Camry"};
        int[] years = {2012, 2013, 2014, 2015, 2016, 2012, 2018, 2019};
        for (int i = 0; i < makes.length; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setYear(years[i]);
            vehicle.setModel(models[i]);
            vehicle.setMake(makes[i]);
            restTemplate.postForEntity(getRootUrl() + "/vehicles", vehicle, String.class);
        }
    }

    @Test
    public void testVehiclesInDatabase() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Audi", "Ford", "Honda", "Volkswagen", "ABC",
                "Nissan", "Volvo", "Toyota"};
        String[] models = {"A3", "Fiesta", "Accord", "Polo", "i7",
                "350Z", "V90", "Camry"};
        int[] years = {2012, 2013, 2014, 2015, 2016, 2012, 2018, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() + "/vehicles",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < makes.length; i++) {
        	System.out.println("==>"+models[i]);
            Assert.assertEquals(response.getBody().get(i).getYear(), years[i]);
            Assert.assertEquals(response.getBody().get(i).getModel(), models[i]);
            Assert.assertEquals(response.getBody().get(i).getMake(), makes[i]);
        }
        Assert.assertNotNull(response.getBody());
    }
    @Test
    public void testGetAllVehicles() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/vehicles",
                HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteVehicleById()  {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        Vehicle vehicle = new Vehicle();
        vehicle.setYear(1999);
        vehicle.setModel("Maruthi");
        vehicle.setMake("Zen");
        ResponseEntity postResponse = restTemplate.postForEntity(getRootUrl() + "/vehicles",
                vehicle, Vehicle.class);
        ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/vehicles/11",
                HttpMethod.DELETE, entity, String.class);
        ResponseEntity<List<Vehicle>> list = restTemplate.exchange(getRootUrl() +
                        "/vehicles?make=Maruthi&model=Zen",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        Assert.assertEquals(0, list.getBody().size());
    }

    @Test
    public void testGetVehicleById() {
        Vehicle vehicle = restTemplate.getForObject(getRootUrl() + "/vehicles/1", Vehicle.class);
        Assert.assertEquals(2012, vehicle.getYear());
        Assert.assertEquals("A3", vehicle.getModel());
        Assert.assertEquals( "Audi", vehicle.getMake());
    }

    @Test
    public void testUpdateVehicle() {
        HttpHeaders headers = new HttpHeaders();
        Vehicle vehicle = restTemplate.getForObject(getRootUrl() + "/vehicles/5", Vehicle.class);
        vehicle.setModel("i7");
        vehicle.setMake("ABC");
        System.out.println(vehicle.getId());
        HttpEntity<Vehicle> entity = new HttpEntity<Vehicle>(vehicle, headers);
        ResponseEntity<Vehicle> updatedVehicle = restTemplate.exchange(getRootUrl() + "/vehicles",
                HttpMethod.PUT, entity, Vehicle.class);
        Assert.assertEquals(vehicle.getId(), updatedVehicle.getBody().getId());
        Assert.assertEquals(2016, updatedVehicle.getBody().getYear());
        Assert.assertEquals("i7", updatedVehicle.getBody().getModel());
        Assert.assertEquals( "ABC", updatedVehicle.getBody().getMake());
    }


    @Test
    public void testCreateVehicle() {
        HttpHeaders headers =  new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        Vehicle vehicle = new Vehicle();
        vehicle.setYear(2010);
        vehicle.setModel("Civic");
        vehicle.setMake("Honda");
        ResponseEntity<Vehicle> vehicleCreated = restTemplate.postForEntity(getRootUrl() + "/vehicles", vehicle,
                Vehicle.class);
        ResponseEntity<Vehicle> vehicleGet = restTemplate.exchange(getRootUrl() + "/vehicles/" +
                        vehicleCreated.getBody().getId(), HttpMethod.GET, entity, Vehicle.class);
        Assert.assertEquals(2010, vehicleGet.getBody().getYear());
        Assert.assertEquals("Honda", vehicleGet.getBody().getMake());
        Assert.assertEquals("Civic", vehicleGet.getBody().getModel());
    }

    @Test
    public void testFilterVehicleByYear() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?year=2012",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});

        Assert.assertEquals(2012, response.getBody().get(0).getYear());
        Assert.assertEquals("A3", response.getBody().get(0).getModel());
        Assert.assertEquals("Audi", response.getBody().get(0).getMake());
        
        Assert.assertEquals(2012, response.getBody().get(1).getYear());
        Assert.assertEquals("350Z", response.getBody().get(1).getModel());
        Assert.assertEquals("Nissan", response.getBody().get(1).getMake());
        
        Assert.assertNotEquals(2013, response.getBody().get(1).getYear());
        
    }

    @Test
    public void testFilterVehicleByMake() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Audi", "Ford", "Honda", "Volkswagen", "BMV",
                "Nissan", "Volvo", "Toyota"};
        String[] models = {"A3", "Fiesta", "Accord", "Polo", "i8",
                "350Z", "V90", "Camry"};
        int[] years = {2012, 2013, 2014, 2015, 2016, 2012, 2018, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?make=Audi,Ford,Honda",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < 3; i++) {
            Assert.assertEquals(years[i], response.getBody().get(i).getYear());
            Assert.assertEquals(models[i], response.getBody().get(i).getModel());
            Assert.assertEquals(makes[i], response.getBody().get(i).getMake());
        }
        Assert.assertNotEquals("BMV", response.getBody().get(0).getMake());
        Assert.assertNotEquals("Volkswagen", response.getBody().get(1).getMake());
    }

    @Test
    public void testFilterVehicleByModel() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        String[] makes = {"Audi", "Ford", "Honda", "Volkswagen", "BMV",
                "Nissan", "Volvo", "Toyota"};
        String[] models = {"A3", "Fiesta", "Accord", "Polo", "i8",
                "350Z", "V90", "Camry"};
        int[] years = {2012, 2013, 2014, 2015, 2016, 2012, 2018, 2019};
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?model=A3,Fiesta,Accord",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(years[i], response.getBody().get(i).getYear());
            Assert.assertEquals(models[i], response.getBody().get(i).getModel());
            Assert.assertEquals(makes[i], response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByMakeModel() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?model=Camry&make=Toyota",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < response.getBody().size(); i++) {
            Assert.assertEquals("Camry", response.getBody().get(i).getModel());
            Assert.assertEquals("Toyota", response.getBody().get(i).getMake());
        }
    }

    @Test
    public void testFilterVehicleByYearMake() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(null, headers);
        ResponseEntity<List<Vehicle>> response = restTemplate.exchange(getRootUrl() +
                        "/vehicles?year=2013&make=Ford",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Vehicle>>() {});
        for(int i = 0; i < response.getBody().size(); i++) {
            Assert.assertEquals(2013, response.getBody().get(i).getYear());
            Assert.assertEquals("Fiesta", response.getBody().get(i).getModel());
            Assert.assertEquals("Ford", response.getBody().get(i).getMake());
        }
    }

}
