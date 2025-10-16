# Diagnostic - Erreur AgeRoutingProcessController

## 🚨 Erreur Rapportée
**Timestamp :** 2025-10-16T11:54:42.416+02:00  
**Logger :** AgeRoutingProcessController  
**Endpoint :** `POST http://localhost:8080/api/process/age-routing/start`  
**Data :**
```json
{
  "email": "marie.dubos@example.com",
  "firstName": "Mari",
  "lastName": "Duois", 
  "birthDate": "1985-12-01"
}
```

## 🔍 Diagnostic Immédiat

### Étape 1: Reproduire l'Erreur
```powershell
# Reproduction exacte de votre requête
$failedRequest = @{
    email = "marie.dubos@example.com"
    firstName = "Mari"
    lastName = "Duois" 
    birthDate = "1985-12-01"
} | ConvertTo-Json

Write-Host "=== REPRODUCTION DE L'ERREUR ==="
Write-Host "Requête: $failedRequest"

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $failedRequest -ContentType "application/json" -Verbose
    Write-Host "✅ Succès inattendu - L'erreur a peut-être été corrigée"
    Write-Host "Process ID: $($result.processInstanceId)"
} catch {
    Write-Host "❌ ERREUR REPRODUITE:"
    Write-Host "   HTTP Status: $($_.Exception.Response.StatusCode)"
    Write-Host "   Message: $($_.Exception.Message)"
    
    # Capturer les détails complets de l'erreur
    if ($_.ErrorDetails) {
        Write-Host "   Détails: $($_.ErrorDetails.Message)"
    }
}
```

### Étape 2: Test du Service de Base (Sans BPMN)
```powershell
Write-Host "`n=== TEST SERVICE DE BASE (SANS BPMN) ==="

# Tester avec exactement les mêmes données mais via l'API directe
$sameDataDirect = @{
    firstName = "Mari"
    lastName = "Duois" 
    email = "marie.dubos.direct@example.com"  # Email légèrement modifié
    birthDate = "1985-12-01"
} | ConvertTo-Json

try {
    $directResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $sameDataDirect -ContentType "application/json"
    Write-Host "✅ Service direct fonctionne - Person ID: $($directResult.id)"
    Write-Host "   Database: $($directResult.targetDatabase -or 'main_db')"
    Write-Host "   → Le problème est spécifiquement dans le processus BPMN"
    $directServiceOK = $true
} catch {
    Write-Host "❌ Service direct échoue aussi: $($_.Exception.Message)"
    Write-Host "   → Le problème est dans AgeBasedPersonService ou les DataSources"
    $directServiceOK = $false
}
```

### Étape 3: Analyse des Causes Possibles
```powershell
Write-Host "`n=== ANALYSE DES CAUSES POSSIBLES ==="

# Vérifier la santé de l'application
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
    Write-Host "✅ Application health: $($health.status)"
} catch {
    Write-Host "❌ Application health check failed"
}

# Vérifier les processus BPMN déployés
try {
    $processes = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition" -Method GET
    $ageProcess = $processes | Where-Object { $_.key -eq "age-based-routing-process" }
    
    if ($ageProcess) {
        Write-Host "✅ Processus BPMN déployé:"
        Write-Host "   Key: $($ageProcess.key)"
        Write-Host "   Version: $($ageProcess.version)" 
        Write-Host "   ID: $($ageProcess.id)"
    } else {
        Write-Host "❌ Processus age-based-routing-process NON DÉPLOYÉ"
        Write-Host "Processus disponibles:"
        $processes | ForEach-Object { Write-Host "  - $($_.key)" }
    }
} catch {
    Write-Host "❌ Erreur accès Camunda Engine: $($_.Exception.Message)"
}

# Tester avec des données différentes pour isoler le problème
Write-Host "`n=== TEST AVEC DONNÉES ALTERNATIVES ==="

$testCases = @(
    @{ 
        name = "Données ASCII simples"
        data = @{
            email = "simple.test@example.com"
            firstName = "John"
            lastName = "Doe"
            birthDate = "1990-01-01"
        }
    },
    @{
        name = "Données similaires à l'erreur"
        data = @{
            email = "similar.test@example.com" 
            firstName = "Marie"
            lastName = "Dubois"
            birthDate = "1985-12-01"
        }
    },
    @{
        name = "Mineur pour tester autre branche"
        data = @{
            email = "minor.test@example.com"
            firstName = "Alice"
            lastName = "Smith" 
            birthDate = "2010-01-01"
        }
    }
)

foreach ($testCase in $testCases) {
    Write-Host "`nTest: $($testCase.name)"
    $testData = $testCase.data | ConvertTo-Json
    
    try {
        $testResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $testData -ContentType "application/json"
        Write-Host "  ✅ Succès - Process ID: $($testResult.processInstanceId)"
    } catch {
        Write-Host "  ❌ Échec: $($_.Exception.Message)"
    }
}
```

## 🛠️ Solutions par Ordre de Priorité

### Solution 1: Vérifier les Logs Complets
```powershell
Write-Host "=== ACTIONS POUR VOIR L'ERREUR COMPLÈTE ==="
Write-Host "1. Dans votre terminal Spring Boot, regardez l'erreur complète après le message 'Error starting age-based routing process'"
Write-Host "2. Recherchez des exceptions comme:"
Write-Host "   - NullPointerException"
Write-Host "   - ClassCastException" 
Write-Host "   - ProcessEngineException"
Write-Host "   - Bean creation errors"
Write-Host ""
Write-Host "3. Si les logs sont trop courts, activez les logs DEBUG:"
Write-Host "   Ajoutez dans application.yml:"
Write-Host "   logging:"
Write-Host "     level:"
Write-Host "       com.example.camunda: DEBUG"
Write-Host "       org.camunda: DEBUG"
```

### Solution 2: Redémarrage Complet avec Logs Détaillés
```powershell
Write-Host "`n=== REDÉMARRAGE AVEC DIAGNOSTIC ==="
Write-Host "1. Arrêtez l'application (Ctrl+C)"
Write-Host "2. Nettoyez et recompilez:"
Write-Host "   mvn clean compile"
Write-Host "3. Démarrez avec logs verbeux:"
Write-Host "   mvn spring-boot:run -X"
Write-Host "4. Testez immédiatement après le démarrage complet"
```

### Solution 3: Test de Contournement
```powershell
Write-Host "`n=== CONTOURNEMENT TEMPORAIRE ==="
Write-Host "En attendant la correction, utilisez l'API directe:"

$workaroundData = @{
    firstName = "Mari"
    lastName = "Duois"
    email = "marie.dubos.workaround@example.com"
    birthDate = "1985-12-01"
} | ConvertTo-Json

try {
    $workaroundResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $workaroundData -ContentType "application/json"
    Write-Host "✅ Contournement fonctionne - Person ID: $($workaroundResult.id)"
    Write-Host "   → Utilisez /api/persons au lieu de /api/process/age-routing/start"
} catch {
    Write-Host "❌ Même le contournement échoue - Problème plus profond"
}
```

## 🔍 Checklist de Diagnostic

- [ ] ✅ Application accessible et healthy
- [ ] ✅ Service direct `/api/persons` fonctionne
- [ ] ✅ Processus BPMN `age-based-routing-process` déployé
- [ ] ✅ Autres données de test fonctionnent avec `/api/process/age-routing/start`
- [ ] ✅ Logs complets examinés pour identifier l'exception racine
- [ ] ✅ Beans Spring correctement injectés

## 🚨 Causes Probables

1. **Bean injection failure** - `AgeBasedRoutingDelegate` ou `PersonProcessingDelegate` non injecté
2. **Processus BPMN non déployé** - Le processus `age-based-routing-process` n'est pas disponible
3. **Problème de DataSource** - Connexion H2 échoue dans le contexte BPMN
4. **Exception dans les délégués** - Erreur dans `AgeBasedPersonService.savePerson()`
5. **Validation des données** - Problème avec les caractères dans les noms

## 📋 Prochaines Actions

1. **Examinez les logs Spring Boot complets** pour voir l'exception after "Error starting age-based routing process"
2. **Testez d'abord le service direct** `/api/persons` avec les mêmes données
3. **Si le service direct fonctionne**, le problème est dans le processus BPMN
4. **Si le service direct échoue aussi**, le problème est dans `AgeBasedPersonService`

**L'objectif est d'identifier si c'est un problème de données, de BPMN, ou de service de base !** 🎯