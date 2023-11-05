package com.example.londonAPI.service;

import com.example.londonAPI.models.User;
import com.example.londonAPI.utils.CityLocationEnum;
import com.example.londonAPI.utils.HaversineDistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Value("${apiUrl}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<User> getUsersFromCityWithinRange(String city, double range){
        List<User> usersListWithinRange = new ArrayList<User>();
        try{
            validateInputs(city,range);
        }catch(IllegalArgumentException ex){
            ex.printStackTrace();
            return usersListWithinRange;
        }

        List<User> usersList = getAllUsers();

        //Should be replaced with proper log statements
        System.out.println("List size of all the users:"+usersList.size());

        CityLocationEnum cityLocationEnum = CityLocationEnum.getByCityName(city);

        double originLatitude = cityLocationEnum.getLatitude();
        double originLongitude = cityLocationEnum.getLongitude();

        usersListWithinRange = usersList.stream().filter(user->
            HaversineDistanceCalculator.calculateDistance(originLatitude, originLongitude, user.getLatitude(), user.getLongitude()) <= range
        ).collect(Collectors.toList());

        //Should be replaced with proper log statements
        System.out.println("List size of all the users withing range:"+usersListWithinRange.size());
        return usersListWithinRange;
    }

    private void validateInputs(String city, double range) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City name is required and cannot be empty.");
        }

        CityLocationEnum cityLocationEnum = CityLocationEnum.getByCityName(city);
        if(cityLocationEnum == null){
            throw new IllegalArgumentException("City name is does not exist.");
        }

        if (range < 0) {
            throw new IllegalArgumentException("Range must be a non-negative value.");
        }
    }

    public List<User> getAllUsers(){
        ResponseEntity<User[]> responseEntity = restTemplate.getForEntity(apiUrl,User[].class);
        User[] users = responseEntity.getBody();
        List<User> usersList = List.of(users);
        return usersList;
    }
}
