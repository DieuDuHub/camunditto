package com.example.camunda.delegate;

import com.example.camunda.model.Person;
import com.example.camunda.service.AgeBasedPersonService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Délégué Camunda pour router les personnes vers la bonne base de données selon l'âge
 */
@Component("ageBasedRoutingDelegate")
public class AgeBasedRoutingDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AgeBasedRoutingDelegate.class);

    @Autowired
    private AgeBasedPersonService personService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("=== Age-Based Routing Delegate Execution Started ===");
        
        try {
            // Récupérer les variables du processus
            Long personId = (Long) execution.getVariable("personId");
            String email = (String) execution.getVariable("email");
            
            logger.info("Processing person routing - ID: {}, Email: {}", personId, email);
            
            Person person = null;
            String searchMethod = "";
            
            // Chercher la personne par ID ou email
            if (personId != null) {
                Optional<Person> personOpt = personService.getPersonById(personId);
                if (personOpt.isPresent()) {
                    person = personOpt.get();
                    searchMethod = "ID";
                }
            } else if (email != null && !email.trim().isEmpty()) {
                Optional<Person> personOpt = personService.getPersonByEmail(email);
                if (personOpt.isPresent()) {
                    person = personOpt.get();
                    searchMethod = "EMAIL";
                }
            }
            
            if (person != null) {
                // Calculer l'âge et déterminer la base de données appropriée
                int age = personService.calculateAge(person);
                boolean isMinor = personService.isMinor(person);
                String targetDatabase = isMinor ? "MINORS" : "ADULTS";
                
                logger.info("Person found: {} {} (Age: {}, Target DB: {})", 
                           person.getFirstName(), person.getLastName(), age, targetDatabase);
                
                // Définir les variables du processus
                execution.setVariable("personFound", true);
                execution.setVariable("person", person);
                execution.setVariable("personAge", age);
                execution.setVariable("isMinor", isMinor);
                execution.setVariable("targetDatabase", targetDatabase);
                execution.setVariable("searchMethod", searchMethod);
                execution.setVariable("routingResult", String.format(
                    "Person %s %s (age %d) routed to %s database", 
                    person.getFirstName(), person.getLastName(), age, targetDatabase));
                
                // Statistiques pour le monitoring
                execution.setVariable("adultsCount", personService.countAdults());
                execution.setVariable("minorsCount", personService.countMinors());
                execution.setVariable("totalCount", personService.countPersons());
                
            } else {
                logger.warn("Person not found - ID: {}, Email: {}", personId, email);
                
                // Personne non trouvée
                execution.setVariable("personFound", false);
                execution.setVariable("person", null);
                execution.setVariable("personAge", -1);
                execution.setVariable("isMinor", false);
                execution.setVariable("targetDatabase", "NONE");
                execution.setVariable("searchMethod", searchMethod);
                execution.setVariable("routingResult", "Person not found");
                
                // Statistiques même en cas d'échec
                execution.setVariable("adultsCount", personService.countAdults());
                execution.setVariable("minorsCount", personService.countMinors());
                execution.setVariable("totalCount", personService.countPersons());
            }
            
            logger.info("=== Age-Based Routing Delegate Execution Completed ===");
            
        } catch (Exception e) {
            logger.error("Error in AgeBasedRoutingDelegate: ", e);
            
            // Variables d'erreur
            execution.setVariable("personFound", false);
            execution.setVariable("routingResult", "Error during routing: " + e.getMessage());
            execution.setVariable("targetDatabase", "ERROR");
            
            throw e;
        }
    }
}