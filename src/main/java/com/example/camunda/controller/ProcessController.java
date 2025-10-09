package com.example.camunda.controller;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process")
@CrossOrigin(origins = "*")
public class ProcessController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);
    
    @Autowired
    private ProcessEngine processEngine;
    
    @PostMapping("/start-person-process")
    public ResponseEntity<Map<String, Object>> startPersonProcess(@RequestBody Map<String, Object> variables) {
        try {
            logger.info("Starting person process with variables: {}", variables);
            
            ProcessInstance processInstance = processEngine.getRuntimeService()
                    .startProcessInstanceByKey("person-process", variables);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceId", processInstance.getId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
            response.put("businessKey", processInstance.getBusinessKey());
            response.put("message", "Person process started successfully");
            
            logger.info("Person process started with ID: {}", processInstance.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error starting person process", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error starting process: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/instance/{processInstanceId}/status")
    public ResponseEntity<Map<String, Object>> getProcessStatus(@PathVariable String processInstanceId) {
        try {
            logger.info("Getting status for process instance: {}", processInstanceId);
            
            ProcessInstance processInstance = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            
            Map<String, Object> response = new HashMap<>();
            
            if (processInstance != null) {
                // Process is still running
                Map<String, Object> variables = processEngine.getRuntimeService()
                        .getVariables(processInstanceId);
                
                response.put("success", true);
                response.put("status", "running");
                response.put("processInstanceId", processInstance.getId());
                response.put("processDefinitionId", processInstance.getProcessDefinitionId());
                response.put("businessKey", processInstance.getBusinessKey());
                response.put("variables", variables);
                response.put("message", "Process is still running");
                
            } else {
                // Process has completed or doesn't exist
                // Check if it completed successfully
                long completedCount = processEngine.getHistoryService()
                        .createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .finished()
                        .count();
                
                if (completedCount > 0) {
                    response.put("success", true);
                    response.put("status", "completed");
                    response.put("message", "Process completed successfully");
                } else {
                    response.put("success", false);
                    response.put("status", "not_found");
                    response.put("message", "Process instance not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }
            
            logger.info("Process status retrieved for: {}", processInstanceId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting process status", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting process status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/instances")
    public ResponseEntity<Map<String, Object>> getActiveProcessInstances() {
        try {
            logger.info("Getting all active process instances");
            
            long activeCount = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processDefinitionKey("person-process")
                    .count();
            
            long completedCount = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processDefinitionKey("person-process")
                    .finished()
                    .count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeProcesses", activeCount);
            response.put("completedProcesses", completedCount);
            response.put("totalProcesses", activeCount + completedCount);
            response.put("message", "Process statistics retrieved successfully");
            
            logger.info("Retrieved process statistics - Active: {}, Completed: {}", activeCount, completedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting process instances", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting process instances: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}