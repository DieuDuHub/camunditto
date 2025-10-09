package com.example.camunda.config;

import com.example.camunda.model.Person;
import com.example.camunda.service.AgeBasedPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AgeBasedDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AgeBasedDataLoader.class);
    
    @Autowired
    private AgeBasedPersonService personService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Loading sample data for both adults and minors databases...");
        
        // === ADULTES (18+ ans) ===
        logger.info("Creating adult persons...");
        
        Person adult1 = new Person();
        adult1.setFirstName("John");
        adult1.setLastName("Doe");
        adult1.setEmail("john.doe@example.com");
        adult1.setPhoneNumber("+33123456789");
        adult1.setDateOfBirth(LocalDate.of(1990, 5, 15)); // 34 ans
        adult1.setAddress("123 Main Street");
        adult1.setCity("Paris");
        adult1.setCountry("France");
        
        Person adult2 = new Person();
        adult2.setFirstName("Jane");
        adult2.setLastName("Smith");
        adult2.setEmail("jane.smith@example.com");
        adult2.setPhoneNumber("+33987654321");
        adult2.setDateOfBirth(LocalDate.of(1985, 8, 22)); // 39 ans
        adult2.setAddress("456 Oak Avenue");
        adult2.setCity("Lyon");
        adult2.setCountry("France");
        
        Person adult3 = new Person();
        adult3.setFirstName("Bob");
        adult3.setLastName("Johnson");
        adult3.setEmail("bob.johnson@example.com");
        adult3.setPhoneNumber("+33555123456");
        adult3.setDateOfBirth(LocalDate.of(1992, 12, 3)); // 32 ans
        adult3.setAddress("789 Pine Road");
        adult3.setCity("Marseille");
        adult3.setCountry("France");
        
        Person adult4 = new Person();
        adult4.setFirstName("Alice");
        adult4.setLastName("Brown");
        adult4.setEmail("alice.brown@example.com");
        adult4.setPhoneNumber("+33444567890");
        adult4.setDateOfBirth(LocalDate.of(1988, 3, 17)); // 36 ans
        adult4.setAddress("321 Elm Street");
        adult4.setCity("Toulouse");
        adult4.setCountry("France");
        
        Person adult5 = new Person();
        adult5.setFirstName("Marie");
        adult5.setLastName("Dupont");
        adult5.setEmail("marie.dupont@example.com");
        adult5.setPhoneNumber("+33111222333");
        adult5.setDateOfBirth(LocalDate.of(2006, 7, 11)); // 18 ans (limite)
        adult5.setAddress("100 République Avenue");
        adult5.setCity("Bordeaux");
        adult5.setCountry("France");
        
        // === MINEURS (-18 ans) ===
        logger.info("Creating minor persons...");
        
        Person minor1 = new Person();
        minor1.setFirstName("Emma");
        minor1.setLastName("Martin");
        minor1.setEmail("emma.martin@example.com");
        minor1.setPhoneNumber("+33222333444");
        minor1.setDateOfBirth(LocalDate.of(2010, 3, 20)); // 14 ans
        minor1.setAddress("50 Jeunesse Street");
        minor1.setCity("Lille");
        minor1.setCountry("France");
        
        Person minor2 = new Person();
        minor2.setFirstName("Lucas");
        minor2.setLastName("Bernard");
        minor2.setEmail("lucas.bernard@example.com");
        minor2.setPhoneNumber("+33333444555");
        minor2.setDateOfBirth(LocalDate.of(2008, 9, 15)); // 16 ans
        minor2.setAddress("25 Lycée Avenue");
        minor2.setCity("Strasbourg");
        minor2.setCountry("France");
        
        Person minor3 = new Person();
        minor3.setFirstName("Chloe");
        minor3.setLastName("Moreau");
        minor3.setEmail("chloe.moreau@example.com");
        minor3.setPhoneNumber("+33444555666");
        minor3.setDateOfBirth(LocalDate.of(2007, 11, 8)); // 17 ans (proche de la majorité)
        minor3.setAddress("75 Collège Road");
        minor3.setCity("Nantes");
        minor3.setCountry("France");
        
        Person minor4 = new Person();
        minor4.setFirstName("Hugo");
        minor4.setLastName("Leroy");
        minor4.setEmail("hugo.leroy@example.com");
        minor4.setPhoneNumber("+33555666777");
        minor4.setDateOfBirth(LocalDate.of(2012, 5, 3)); // 12 ans
        minor4.setAddress("30 École Street");
        minor4.setCity("Montpellier");
        minor4.setCountry("France");
        
        Person minor5 = new Person();
        minor5.setFirstName("Léa");
        minor5.setLastName("Petit");
        minor5.setEmail("lea.petit@example.com");
        minor5.setPhoneNumber("+33666777888");
        minor5.setDateOfBirth(LocalDate.of(2009, 12, 25)); // 15 ans
        minor5.setAddress("10 Adolescence Avenue");
        minor5.setCity("Rennes");
        minor5.setCountry("France");
        
        // Sauvegarder toutes les personnes (le service se charge du routage automatique)
        logger.info("Saving persons to appropriate databases...");
        
        // Adultes
        personService.savePerson(adult1);
        personService.savePerson(adult2);
        personService.savePerson(adult3);
        personService.savePerson(adult4);
        personService.savePerson(adult5);
        
        // Mineurs
        personService.savePerson(minor1);
        personService.savePerson(minor2);
        personService.savePerson(minor3);
        personService.savePerson(minor4);
        personService.savePerson(minor5);
        
        // Statistiques finales
        logger.info("=== DATABASE STATISTICS ===");
        logger.info("Adults in adults database: {}", personService.countAdults());
        logger.info("Minors in minors database: {}", personService.countMinors());
        logger.info("Total persons across both databases: {}", personService.countPersons());
        logger.info("Sample data loaded successfully!");
        
        // Vérification du routage
        logger.info("=== ROUTING VERIFICATION ===");
        personService.getAllPersons().forEach(person -> {
            int age = personService.calculateAge(person);
            String database = personService.isMinor(person) ? "MINORS" : "ADULTS";
            logger.info("Person: {} {} (Age: {}) -> Database: {}", 
                       person.getFirstName(), person.getLastName(), age, database);
        });
    }
}