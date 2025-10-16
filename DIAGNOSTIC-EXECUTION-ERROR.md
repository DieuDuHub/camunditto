# Diagnostic - Erreur "execution doesn't exist: execution is null"

## 🚨 Problème
**Erreur :** `execution 101f1772-a848-11f0-8524-0a0027000016 doesn't exist: execution is null`

Cette erreur indique que le processus BPMN s'est arrêté de manière inattendue, souvent à cause d'un problème dans un délégué Java.

## 🔍 Diagnostic Immédiat

### Étape 1: Vérification Rapide
```powershell
# Test de santé application
$health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
Write-Host "Application Status: $($health.status)"

# Vérifier les processus déployés
$processes = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition" -Method GET
$ageRoutingProcess = $processes | Where-Object { $_.key -eq "age-based-routing-process" }
Write-Host "Processus age-based-routing déployé: $($ageRoutingProcess -ne $null)"
```

### Étape 2: Test du Service de Base (Sans BPMN)
```powershell
Write-Host "`n=== TEST SERVICE DE BASE (SANS BPMN) ==="

# Test 1: Ajouter une personne directement via l'API REST
$directTest = @{
    firstName = "DirectTest"
    lastName = "NoBPMN"
    email = "direct.test@example.com"
    birthDate = "1990-01-01"
} | ConvertTo-Json

try {
    $directResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $directTest -ContentType "application/json"
    Write-Host "✅ Service de base fonctionne - Person ID: $($directResult.id)"
    Write-Host "   Database utilisée: $($directResult.targetDatabase -or 'main_db (par défaut)')"
    
    # Vérifier la répartition
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
    Write-Host "   Total adultes: $($stats.totalAdults), Total mineurs: $($stats.totalMinors)"
    
    $serviceBaseOK = $true
} catch {
    Write-Host "❌ ERREUR SERVICE DE BASE: $($_.Exception.Message)"
    Write-Host "   → Le problème n'est PAS dans BPMN mais dans AgeBasedPersonService"
    $serviceBaseOK = $false
}
```

### Étape 3: Diagnostic des Délégués BPMN
```powershell
if ($serviceBaseOK) {
    Write-Host "`n=== TEST PROCESSUS BPMN ==="
    
    # Test avec données minimales pour isoler le problème
    $bpmnMinimal = @{
        email = "bpmn.minimal@example.com"
        firstName = "BPMN"
        lastName = "Test"
        birthDate = "1990-01-01"
    } | ConvertTo-Json
    
    try {
        $bpmnResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $bpmnMinimal -ContentType "application/json"
        Write-Host "✅ Processus BPMN OK - ID: $($bpmnResult.processInstanceId)"
        
        # Attendre et vérifier le statut
        Start-Sleep -Seconds 2
        $status = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/status/$($bpmnResult.processInstanceId)" -Method GET
        Write-Host "   Status: $($status.status)"
        Write-Host "   Target DB: $($status.variables.targetDatabase)"
        
    } catch {
        Write-Host "❌ ERREUR PROCESSUS BPMN: $($_.Exception.Message)"
        
        # Analyser le type d'erreur
        if ($_.Exception.Message -match "execution.*doesn't exist") {
            Write-Host "`n🔍 ANALYSE DE L'ERREUR 'execution doesn't exist':"
            Write-Host "   → Un délégué dans le processus BPMN a échoué"
            Write-Host "   → Vérifier les beans suivants:"
            Write-Host "     - AgeBasedRoutingDelegate"
            Write-Host "     - PersonProcessingDelegate"  
            Write-Host "     - AgeBasedPersonService"
            Write-Host "`n   → Actions recommandées:"
            Write-Host "     1. Redémarrer l'application"
            Write-Host "     2. Vérifier les logs Spring Boot"
            Write-Host "     3. Tester d'abord sans personnalisation BPMN"
        }
    }
}
```

## 🛠️ Solutions par Ordre de Priorité

### Solution 1: Redémarrage Complet
```powershell
Write-Host "=== SOLUTION 1: REDÉMARRAGE COMPLET ==="

# Arrêter l'application (Ctrl+C dans le terminal Maven)
# Puis relancer proprement
Write-Host "1. Arrêtez l'application (Ctrl+C)"
Write-Host "2. Exécutez les commandes suivantes:"
Write-Host ""
Write-Host "mvn clean"
Write-Host "mvn spring-boot:run"
Write-Host ""
Write-Host "3. Attendez le démarrage complet avant de tester"
```

### Solution 2: Test Sans Processus BPMN
```powershell
Write-Host "`n=== SOLUTION 2: CONTOURNEMENT BPMN ==="

# Utiliser uniquement les APIs REST directes
$alternativeTest = @{
    firstName = "Alternative"
    lastName = "Method"
    email = "alternative.test@example.com"
    birthDate = "1990-01-01"
} | ConvertTo-Json

Write-Host "Test sans BPMN (API directe):"
try {
    $altResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $alternativeTest -ContentType "application/json"
    Write-Host "✅ Méthode alternative fonctionne - ID: $($altResult.id)"
    Write-Host "   Utilisez cette méthode en attendant la correction BPMN"
} catch {
    Write-Host "❌ Problème plus profond dans l'application: $($_.Exception.Message)"
}
```

### Solution 3: Diagnostic Avancé avec Camunda Cockpit
```powershell
Write-Host "`n=== SOLUTION 3: DIAGNOSTIC CAMUNDA COCKPIT ==="
Write-Host "1. Ouvrir http://localhost:8080/camunda"
Write-Host "2. Login: demo/demo"
Write-Host "3. Aller dans 'Processes' → 'age-based-routing-process'"
Write-Host "4. Regarder les instances qui ont échoué"
Write-Host "5. Identifier le service task qui cause l'erreur"
Write-Host ""
Write-Host "Incidents courants à rechercher:"
Write-Host "- NullPointerException dans AgeBasedRoutingDelegate"
Write-Host "- Bean not found pour AgeBasedPersonService"
Write-Host "- Problème de connexion base de données H2"
```

## 🔧 Script de Réparation Automatique

```powershell
Write-Host "=== SCRIPT DE RÉPARATION AUTOMATIQUE ==="

function Test-ApplicationHealth {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
        return $health.status -eq "UP"
    } catch {
        return $false
    }
}

function Test-DirectPersonService {
    $testPerson = @{
        firstName = "Health"
        lastName = "Check"
        email = "health.check@example.com"
        birthDate = "1990-01-01"
    } | ConvertTo-Json
    
    try {
        $result = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $testPerson -ContentType "application/json" -TimeoutSec 10
        return $true
    } catch {
        Write-Host "Erreur service direct: $($_.Exception.Message)"
        return $false
    }
}

function Test-BPMNProcess {
    $testProcess = @{
        email = "process.health@example.com"
        firstName = "Process"
        lastName = "Health"
        birthDate = "1990-01-01"
    } | ConvertTo-Json
    
    try {
        $result = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $testProcess -ContentType "application/json" -TimeoutSec 15
        return $true
    } catch {
        Write-Host "Erreur processus BPMN: $($_.Exception.Message)"
        return $false
    }
}

# Exécution du diagnostic
Write-Host "Diagnostic automatique en cours..."

if (-not (Test-ApplicationHealth)) {
    Write-Host "❌ Application non accessible"
    Write-Host "   → Vérifiez que l'application est démarrée (mvn spring-boot:run)"
    exit 1
}

Write-Host "✅ Application accessible"

if (-not (Test-DirectPersonService)) {
    Write-Host "❌ Service de base défaillant"
    Write-Host "   → Problème dans AgeBasedPersonService ou configuration H2"
    Write-Host "   → Redémarrez l'application complètement"
} else {
    Write-Host "✅ Service de base fonctionne"
    
    if (-not (Test-BPMNProcess)) {
        Write-Host "❌ Processus BPMN défaillant"
        Write-Host "   → Utilisez l'API directe en attendant: /api/persons"
        Write-Host "   → Problème dans les délégués BPMN"
    } else {
        Write-Host "✅ Processus BPMN fonctionne"
        Write-Host "   → Tout est opérationnel!"
    }
}
```

## 📋 Checklist de Résolution

- [ ] ✅ Application démarrée et accessible
- [ ] ✅ Service de base `/api/persons` fonctionne
- [ ] ✅ Statistiques `/api/persons/statistics` disponibles
- [ ] ✅ Processus BPMN `/api/process/age-routing/start` fonctionne
- [ ] ✅ Cockpit Camunda accessible
- [ ] ✅ Aucune erreur dans les logs Spring Boot

## 🚨 Actions d'Urgence

Si le problème persiste :

1. **Redémarrage propre :**
   ```powershell
   # Terminal 1: Arrêter l'application (Ctrl+C)
   # Terminal 2: 
   mvn clean
   mvn spring-boot:run
   ```

2. **Utiliser l'API de contournement :**
   ```powershell
   # En attendant la correction, utilisez directement :
   POST http://localhost:8080/api/persons
   # Au lieu de :
   POST http://localhost:8080/api/process/age-routing/start
   ```

3. **Vérifier les logs :**
   - Regarder les erreurs au démarrage de l'application
   - Identifier les beans non injectés
   - Vérifier les connexions H2

**L'objectif est d'identifier si le problème vient du service de base ou spécifiquement du processus BPMN !** 🎯