package com.example.camunda.config;

import com.example.camunda.model.Person;
import com.example.camunda.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    
    @Autowired
    private PersonService personService;
    
    @Override
    public void run(String... args) throws Exception {
        if (personService.countPersons() == 0) {
            logger.info("Loading sample data...");
            loadSampleData();
            logger.info("Sample data loaded successfully");
        } else {
            logger.info("Sample data already exists, skipping data loading");
        }
    }
    
    private void loadSampleData() {
        // Create sample persons
        Person person1 = new Person();
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setEmail("john.doe@example.com");
        person1.setPhoneNumber("+1-555-123-4567");
        person1.setDateOfBirth(LocalDate.of(1985, 3, 15));
        person1.setAddress("123 Main St");
        person1.setCity("New York");
        person1.setCountry("USA");
        
        Person person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Smith");
        person2.setEmail("jane.smith@example.com");
        person2.setPhoneNumber("+1-555-987-6543");
        person2.setDateOfBirth(LocalDate.of(1990, 7, 22));
        person2.setAddress("456 Oak Ave");
        person2.setCity("Los Angeles");
        person2.setCountry("USA");
        
        Person person3 = new Person();
        person3.setFirstName("Alice");
        person3.setLastName("Johnson");
        person3.setEmail("alice.johnson@example.com");
        person3.setPhoneNumber("+44-20-7946-0958");
        person3.setDateOfBirth(LocalDate.of(1982, 11, 8));
        person3.setAddress("789 High Street");
        person3.setCity("London");
        person3.setCountry("UK");
        
        Person person4 = new Person();
        person4.setFirstName("Bob");
        person4.setLastName("Wilson");
        person4.setEmail("bob.wilson@example.com");
        person4.setPhoneNumber("+33-1-42-86-83-00");
        person4.setDateOfBirth(LocalDate.of(1978, 5, 30));
        person4.setAddress("10 Rue de Rivoli");
        person4.setCity("Paris");
        person4.setCountry("France");
        
        Person person5 = new Person();
        person5.setFirstName("Emma");
        person5.setLastName("Brown");
        person5.setEmail("emma.brown@example.com");
        person5.setPhoneNumber("+49-30-12345678");
        person5.setDateOfBirth(LocalDate.of(1993, 12, 3));
        person5.setAddress("Unter den Linden 1");
        person5.setCity("Berlin");
        person5.setCountry("Germany");
        
        // Save all sample persons
        personService.savePerson(person1);
        personService.savePerson(person2);
        personService.savePerson(person3);
        personService.savePerson(person4);
        personService.savePerson(person5);
        
        logger.info("Created {} sample persons", 5);
    }
}