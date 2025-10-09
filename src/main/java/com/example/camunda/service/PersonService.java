package com.example.camunda.service;

import com.example.camunda.model.Person;
import com.example.camunda.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);
    
    @Autowired
    private PersonRepository personRepository;
    
    public List<Person> getAllPersons() {
        logger.info("Fetching all persons");
        return personRepository.findAll();
    }
    
    public Optional<Person> getPersonById(Long id) {
        logger.info("Fetching person with id: {}", id);
        return personRepository.findById(id);
    }
    
    public Optional<Person> getPersonByEmail(String email) {
        logger.info("Fetching person with email: {}", email);
        return personRepository.findByEmail(email);
    }
    
    public List<Person> searchPersons(String searchTerm) {
        logger.info("Searching persons with term: {}", searchTerm);
        return personRepository.findBySearchTerm(searchTerm);
    }
    
    public List<Person> getPersonsByFirstName(String firstName) {
        logger.info("Fetching persons with first name: {}", firstName);
        return personRepository.findByFirstNameIgnoreCaseContaining(firstName);
    }
    
    public List<Person> getPersonsByLastName(String lastName) {
        logger.info("Fetching persons with last name: {}", lastName);
        return personRepository.findByLastNameIgnoreCaseContaining(lastName);
    }
    
    public List<Person> getPersonsByCity(String city) {
        logger.info("Fetching persons from city: {}", city);
        return personRepository.findByCity(city);
    }
    
    public List<Person> getPersonsByCountry(String country) {
        logger.info("Fetching persons from country: {}", country);
        return personRepository.findByCountry(country);
    }
    
    public Person savePerson(Person person) {
        logger.info("Saving person: {}", person.getFullName());
        return personRepository.save(person);
    }
    
    public Person updatePerson(Long id, Person personDetails) {
        logger.info("Updating person with id: {}", id);
        
        return personRepository.findById(id)
                .map(person -> {
                    person.setFirstName(personDetails.getFirstName());
                    person.setLastName(personDetails.getLastName());
                    person.setEmail(personDetails.getEmail());
                    person.setPhoneNumber(personDetails.getPhoneNumber());
                    person.setDateOfBirth(personDetails.getDateOfBirth());
                    person.setAddress(personDetails.getAddress());
                    person.setCity(personDetails.getCity());
                    person.setCountry(personDetails.getCountry());
                    return personRepository.save(person);
                })
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
    }
    
    public void deletePerson(Long id) {
        logger.info("Deleting person with id: {}", id);
        personRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return personRepository.existsById(id);
    }
    
    public long countPersons() {
        return personRepository.countAllPersons();
    }
}