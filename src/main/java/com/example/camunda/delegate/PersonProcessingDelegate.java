package com.example.camunda.delegate;

import com.example.camunda.model.Person;
import com.example.camunda.service.PersonService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonProcessingDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonProcessingDelegate.class);
    
    @Autowired
    private PersonService personService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Executing person processing delegate");
        
        try {
            // Get person from process variables
            Person person = (Person) execution.getVariable("person");
            String processingType = (String) execution.getVariable("processingType");
            
            if (person == null) {
                throw new RuntimeException("No person object found in process variables");
            }
            
            String processingResult = "";
            boolean processingSuccess = false;
            
            switch (processingType != null ? processingType.toLowerCase() : "default") {
                case "create":
                    Person savedPerson = personService.savePerson(person);
                    processingResult = "Person created successfully with ID: " + savedPerson.getId();
                    execution.setVariable("personId", savedPerson.getId());
                    execution.setVariable("person", savedPerson);
                    processingSuccess = true;
                    logger.info("Person created successfully: {}", savedPerson.getId());
                    break;
                    
                case "update":
                    Long personId = person.getId();
                    if (personId != null && personService.existsById(personId)) {
                        Person updatedPerson = personService.updatePerson(personId, person);
                        processingResult = "Person updated successfully: " + personId;
                        execution.setVariable("person", updatedPerson);
                        processingSuccess = true;
                        logger.info("Person updated successfully: {}", personId);
                    } else {
                        processingResult = "Cannot update person: ID not found";
                        logger.error("Cannot update person: ID not found");
                    }
                    break;
                    
                case "validate":
                    if (person.getFirstName() != null && !person.getFirstName().isEmpty() &&
                        person.getLastName() != null && !person.getLastName().isEmpty()) {
                        processingResult = "Person validation passed for: " + person.getFullName();
                        processingSuccess = true;
                        logger.info("Person validation passed for: {}", person.getFullName());
                    } else {
                        processingResult = "Person validation failed: Missing required fields";
                        logger.warn("Person validation failed for person with ID: {}", person.getId());
                    }
                    break;
                    
                default:
                    processingResult = "Person processed successfully: " + person.getFullName();
                    processingSuccess = true;
                    logger.info("Default person processing completed for: {}", person.getFullName());
                    break;
            }
            
            // Set result variables
            execution.setVariable("processingSuccess", processingSuccess);
            execution.setVariable("processingResult", processingResult);
            
        } catch (Exception e) {
            String errorMessage = "Error during person processing: " + e.getMessage();
            logger.error("Error during person processing", e);
            
            execution.setVariable("processingSuccess", false);
            execution.setVariable("processingResult", errorMessage);
            
            // Optionally re-throw the exception to trigger error handling in the process
            // throw new RuntimeException(errorMessage, e);
        }
        
        logger.info("Person processing delegate completed");
    }
}