package com.example.camunda.controller;

import com.example.camunda.model.Person;
import com.example.camunda.service.PersonService;
import com.example.camunda.service.AgeBasedPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);
    
    @Autowired
    private PersonService personService;
    
    @Autowired
    private AgeBasedPersonService ageBasedPersonService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPersons() {
        try {
            List<Person> persons = personService.getAllPersons();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", persons);
            response.put("count", persons.size());
            response.put("message", "Persons retrieved successfully");
            
            logger.info("Retrieved {} persons", persons.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving persons", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving persons: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPersonById(@PathVariable Long id) {
        try {
            Optional<Person> person = personService.getPersonById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (person.isPresent()) {
                response.put("success", true);
                response.put("data", person.get());
                response.put("message", "Person found");
                logger.info("Retrieved person with id: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Person not found with id: " + id);
                logger.warn("Person not found with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error retrieving person with id: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving person: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getPersonByEmail(@PathVariable String email) {
        try {
            Optional<Person> person = personService.getPersonByEmail(email);
            Map<String, Object> response = new HashMap<>();
            
            if (person.isPresent()) {
                response.put("success", true);
                response.put("data", person.get());
                response.put("message", "Person found");
                logger.info("Retrieved person with email: {}", email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Person not found with email: " + email);
                logger.warn("Person not found with email: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error retrieving person with email: {}", email, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving person: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPersons(@RequestParam String term) {
        try {
            List<Person> persons = personService.searchPersons(term);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", persons);
            response.put("count", persons.size());
            response.put("message", "Search completed successfully");
            
            logger.info("Search for '{}' returned {} results", term, persons.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching persons with term: {}", term, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching persons: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/firstName/{firstName}")
    public ResponseEntity<Map<String, Object>> getPersonsByFirstName(@PathVariable String firstName) {
        try {
            List<Person> persons = personService.getPersonsByFirstName(firstName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", persons);
            response.put("count", persons.size());
            response.put("message", "Persons retrieved successfully");
            
            logger.info("Retrieved {} persons with first name: {}", persons.size(), firstName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving persons with first name: {}", firstName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving persons: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/lastName/{lastName}")
    public ResponseEntity<Map<String, Object>> getPersonsByLastName(@PathVariable String lastName) {
        try {
            List<Person> persons = personService.getPersonsByLastName(lastName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", persons);
            response.put("count", persons.size());
            response.put("message", "Persons retrieved successfully");
            
            logger.info("Retrieved {} persons with last name: {}", persons.size(), lastName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving persons with last name: {}", lastName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving persons: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/city/{city}")
    public ResponseEntity<Map<String, Object>> getPersonsByCity(@PathVariable String city) {
        try {
            List<Person> persons = personService.getPersonsByCity(city);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", persons);
            response.put("count", persons.size());
            response.put("message", "Persons retrieved successfully");
            
            logger.info("Retrieved {} persons from city: {}", persons.size(), city);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving persons from city: {}", city, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving persons: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/country/{country}")
    public ResponseEntity<Map<String, Object>> getPersonsByCountry(@PathVariable String country) {
        try {
            List<Person> persons = personService.getPersonsByCountry(country);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", persons);
            response.put("count", persons.size());
            response.put("message", "Persons retrieved successfully");
            
            logger.info("Retrieved {} persons from country: {}", persons.size(), country);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving persons from country: {}", country, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving persons: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getPersonCount() {
        try {
            long count = personService.countPersons();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            response.put("message", "Person count retrieved successfully");
            
            logger.info("Total persons count: {}", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving person count", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving person count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPerson(@Valid @RequestBody Person person) {
        try {
            Person savedPerson = personService.savePerson(person);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedPerson);
            response.put("message", "Person created successfully");
            
            logger.info("Created new person with id: {}", savedPerson.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating person", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating person: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePerson(@PathVariable Long id, @Valid @RequestBody Person personDetails) {
        try {
            Person updatedPerson = personService.updatePerson(id, personDetails);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedPerson);
            response.put("message", "Person updated successfully");
            
            logger.info("Updated person with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Person not found with id: {}", id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error updating person with id: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating person: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePerson(@PathVariable Long id) {
        try {
            if (!personService.existsById(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Person not found with id: " + id);
                logger.warn("Attempted to delete non-existent person with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            personService.deletePerson(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Person deleted successfully");
            
            logger.info("Deleted person with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting person with id: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting person: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // === NOUVEAUX ENDPOINTS POUR LES DEUX BASES DE DONNÉES ===
    
    /**
     * Récupère uniquement les adultes (18+ ans)
     */
    @GetMapping("/adults")
    public ResponseEntity<Map<String, Object>> getAdults() {
        try {
            logger.info("Fetching all adults");
            List<Person> adults = ageBasedPersonService.getAllAdults();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", adults);
            response.put("count", adults.size());
            response.put("message", "Adults retrieved successfully");
            response.put("database", "ADULTS");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching adults: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching adults: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère uniquement les mineurs (-18 ans)
     */
    @GetMapping("/minors")
    public ResponseEntity<Map<String, Object>> getMinors() {
        try {
            logger.info("Fetching all minors");
            List<Person> minors = ageBasedPersonService.getAllMinors();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", minors);
            response.put("count", minors.size());
            response.put("message", "Minors retrieved successfully");
            response.put("database", "MINORS");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching minors: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching minors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Statistiques des deux bases de données
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            logger.info("Fetching database statistics");
            
            long adultsCount = ageBasedPersonService.countAdults();
            long minorsCount = ageBasedPersonService.countMinors();
            long totalCount = ageBasedPersonService.countPersons();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "adults", adultsCount,
                "minors", minorsCount,
                "total", totalCount,
                "adultsPercentage", totalCount > 0 ? (adultsCount * 100.0 / totalCount) : 0,
                "minorsPercentage", totalCount > 0 ? (minorsCount * 100.0 / totalCount) : 0
            ));
            response.put("message", "Statistics retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching statistics: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Vérifie l'âge d'une personne et sa base de données cible
     */
    @GetMapping("/{id}/age-info")
    public ResponseEntity<Map<String, Object>> getPersonAgeInfo(@PathVariable Long id) {
        try {
            logger.info("Fetching age info for person with id: {}", id);
            Optional<Person> personOpt = personService.getPersonById(id);
            
            if (personOpt.isPresent()) {
                Person person = personOpt.get();
                int age = ageBasedPersonService.calculateAge(person);
                boolean isMinor = ageBasedPersonService.isMinor(person);
                String targetDatabase = isMinor ? "MINORS" : "ADULTS";
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("person", Map.of(
                    "id", person.getId(),
                    "fullName", person.getFullName(),
                    "dateOfBirth", person.getDateOfBirth(),
                    "age", age,
                    "isMinor", isMinor,
                    "targetDatabase", targetDatabase
                ));
                response.put("message", "Age information retrieved successfully");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Person not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error fetching age info for person with id: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching age info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}