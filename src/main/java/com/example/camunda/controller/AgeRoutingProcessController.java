package com.example.camunda.controller;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/process/age-routing")
@CrossOrigin(origins = "*")
public class AgeRoutingProcessController {
    
    private static final Logger logger = LoggerFactory.getLogger(AgeRoutingProcessController.class);
    
    @Autowired
    private ProcessEngine processEngine;
    
    /**
     * Démarre le processus de routage par âge
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startAgeRoutingProcess(@RequestBody Map<String, Object> variables) {
        try {
            logger.info("Starting age-based routing process with variables: {}", variables);
            
            // Valider les variables requises
            if (!variables.containsKey("personId") && !variables.containsKey("email")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Either 'personId' or 'email' must be provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Ajouter des métadonnées au processus
            variables.put("processType", "age-based-routing");
            variables.put("startTime", System.currentTimeMillis());
            
            // Démarrer le processus
            ProcessInstance processInstance = processEngine.getRuntimeService()
                    .startProcessInstanceByKey("age-based-routing-process", variables);
            
            logger.info("Age-based routing process started with id: {}", processInstance.getId());
            
            // Récupérer les variables du processus depuis l'historique (car le processus peut être terminé)
            Map<String, Object> processVariables = new HashMap<>();
            processEngine.getHistoryService()
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstance.getId())
                    .list()
                    .forEach(variable -> {
                        processVariables.put(variable.getName(), variable.getValue());
                    });
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceId", processInstance.getId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
            response.put("businessKey", processInstance.getBusinessKey());
            response.put("isEnded", processInstance.isEnded());
            response.put("variables", processVariables);
            response.put("message", "Age-based routing process started successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error starting age-based routing process: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error starting process: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère le statut d'un processus spécifique
     */
    @GetMapping("/status/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> getProcessStatus(@PathVariable String processInstanceId) {
        try {
            logger.info("Fetching status for process instance: {}", processInstanceId);
            
            // Vérifier si le processus est encore actif
            ProcessInstance activeProcess = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            
            Map<String, Object> response = new HashMap<>();
            
            if (activeProcess != null) {
                // Processus encore actif
                Map<String, Object> variables = processEngine.getRuntimeService()
                        .getVariables(processInstanceId);
                
                response.put("success", true);
                response.put("status", "ACTIVE");
                response.put("processInstanceId", activeProcess.getId());
                response.put("variables", variables);
                response.put("message", "Process is still running");
                
            } else {
                // Vérifier dans l'historique
                HistoricProcessInstance historicProcess = processEngine.getHistoryService()
                        .createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();
                
                if (historicProcess != null) {
                    List<HistoricVariableInstance> historicVariables = processEngine.getHistoryService()
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .list();
                    
                    // Utiliser forEach pour gérer les valeurs null
                    Map<String, Object> variables = new HashMap<>();
                    historicVariables.forEach(var -> 
                        variables.put(var.getName(), var.getValue() != null ? var.getValue() : "null")
                    );
                    
                    response.put("success", true);
                    response.put("status", "COMPLETED");
                    response.put("processInstanceId", historicProcess.getId());
                    response.put("startTime", historicProcess.getStartTime());
                    response.put("endTime", historicProcess.getEndTime());
                    response.put("duration", historicProcess.getDurationInMillis());
                    response.put("variables", variables);
                    response.put("message", "Process completed successfully");
                } else {
                    response.put("success", false);
                    response.put("message", "Process instance not found: " + processInstanceId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching process status for instance: {}", processInstanceId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching process status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère tous les processus de routage par âge actifs
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveAgeRoutingProcesses() {
        try {
            logger.info("Fetching active age-based routing processes");
            
            List<ProcessInstance> activeProcesses = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processDefinitionKey("age-based-routing-process")
                    .list();
            
            List<Map<String, Object>> processData = activeProcesses.stream()
                    .map(process -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("processInstanceId", process.getId());
                        data.put("businessKey", process.getBusinessKey());
                        
                        // Récupérer quelques variables clés
                        Map<String, Object> variables = processEngine.getRuntimeService()
                                .getVariables(process.getId());
                        data.put("personId", variables.get("personId"));
                        data.put("email", variables.get("email"));
                        data.put("targetDatabase", variables.get("targetDatabase"));
                        
                        return data;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeProcesses", processData);
            response.put("count", processData.size());
            response.put("message", "Active processes retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching active processes: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching active processes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Statistiques des processus de routage par âge
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getProcessStatistics() {
        try {
            logger.info("Fetching age-based routing process statistics");
            
            // Processus actifs
            long activeCount = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processDefinitionKey("age-based-routing-process")
                    .count();
            
            // Processus complétés
            long completedCount = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processDefinitionKey("age-based-routing-process")
                    .finished()
                    .count();
            
            // Processus total
            long totalCount = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processDefinitionKey("age-based-routing-process")
                    .count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "active", activeCount,
                "completed", completedCount,
                "total", totalCount,
                "completionRate", totalCount > 0 ? (completedCount * 100.0 / totalCount) : 0
            ));
            response.put("message", "Process statistics retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching process statistics: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching process statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}