package com.example.camunda.delegate;

import com.example.camunda.model.Person;
import com.example.camunda.service.PersonService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PersonValidationDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonValidationDelegate.class);
    
    @Autowired
    private PersonService personService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Executing person validation delegate");
        
        // Get person ID from process variables
        Long personId = (Long) execution.getVariable("personId");
        String email = (String) execution.getVariable("email");
        
        boolean isValid = false;
        String validationResult = "";
        
        try {
            if (personId != null) {
                Optional<Person> person = personService.getPersonById(personId);
                if (person.isPresent()) {
                    isValid = true;
                    validationResult = "Person found with ID: " + personId;
                    execution.setVariable("person", person.get());
                    logger.info("Person validation successful for ID: {}", personId);
                } else {
                    validationResult = "Person not found with ID: " + personId;
                    logger.warn("Person not found with ID: {}", personId);
                }
            } else if (email != null && !email.isEmpty()) {
                Optional<Person> person = personService.getPersonByEmail(email);
                if (person.isPresent()) {
                    isValid = true;
                    validationResult = "Person found with email: " + email;
                    execution.setVariable("person", person.get());
                    execution.setVariable("personId", person.get().getId());
                    logger.info("Person validation successful for email: {}", email);
                } else {
                    validationResult = "Person not found with email: " + email;
                    logger.warn("Person not found with email: {}", email);
                }
            } else {
                validationResult = "No person ID or email provided for validation";
                logger.error("No person ID or email provided for validation");
            }
            
        } catch (Exception e) {
            validationResult = "Error during person validation: " + e.getMessage();
            logger.error("Error during person validation", e);
        }
        
        // Set process variables
        execution.setVariable("isPersonValid", isValid);
        execution.setVariable("validationResult", validationResult);
        
        logger.info("Person validation completed. Valid: {}, Result: {}", isValid, validationResult);
    }
}