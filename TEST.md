# Guide de Test - Application Camunda avec Routage par Âge

## 📋 Table des Matières
1. [Prérequis](#prérequis)
2. [Démarrage de l'Application](#démarrage-de-lapplication)
3. [Accès aux Consoles H2](#accès-aux-consoles-h2)
4. [Tests des API REST](#tests-des-api-rest)
5. [Tests des Processus BPMN](#tests-des-processus-bpmn)
6. [Tests de Routage par Âge](#tests-de-routage-par-âge)
7. [Vérification des Données](#vérification-des-données)
8. [Tests d'Intégration](#tests-dintégration)
9. [Dépannage](#dépannage)

## 🚀 Prérequis

### Vérification de l'environnement
```powershell
# Vérifier Java 21
java -version

# Vérifier Maven
mvn -version

# Position dans le projet
cd C:\Users\MDEBRAY\dev\java\camunda
```

### Compilation du projet
```powershell
# Nettoyage et compilation
mvn clean compile

# Build complet (optionnel)
mvn clean package -DskipTests
```

## 🏃 Démarrage de l'Application

### Démarrage avec Maven
```powershell
mvn spring-boot:run
```

### Démarrage avec JAR (alternative)
```powershell
# Si vous avez buildé le JAR
java -jar target/camunda-api-0.0.1-SNAPSHOT.jar
```

### Vérification du démarrage
```powershell
# Vérifier que l'application écoute sur le port 8080
netstat -an | findstr :8080
```

## 🗄️ Accès aux Consoles H2

### Console H2 Base Principale (Adultes + Camunda)
1. **URL :** `http://localhost:8080/h2-console`
2. **Configuration :**
   - Driver Class : `org.h2.Driver`
   - JDBC URL : `jdbc:h2:mem:main_db`
   - User Name : `sa`
   - Password : _(laisser vide)_

### Console H2 Base Mineurs
1. **URL :** `http://localhost:8080/h2-console`
2. **Configuration :**
   - Driver Class : `org.h2.Driver`
   - JDBC URL : `jdbc:h2:mem:minors_db`
   - User Name : `sa`
   - Password : _(laisser vide)_

### Requêtes SQL de vérification
```sql
-- Vérifier la structure des tables
SHOW TABLES;

-- Voir les données dans la base principale
SELECT * FROM PERSON;

-- Compter les enregistrements
SELECT COUNT(*) as total FROM PERSON;

-- Voir les personnes par âge
SELECT firstName, lastName, birthDate, 
       DATEDIFF('YEAR', birthDate, CURRENT_DATE()) as age 
FROM PERSON 
ORDER BY birthDate DESC;
```

## 🌐 Tests des API REST

### Tests avec PowerShell (Invoke-RestMethod)

#### 1. Test de santé de l'application
```powershell
# Vérifier que l'application répond
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
```

#### 2. Lister toutes les personnes
```powershell
# Toutes les personnes
Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method GET

# Personnes adultes uniquement
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/adults" -Method GET

# Personnes mineures uniquement
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/minors" -Method GET
```

#### 3. Statistiques
```powershell
# Statistiques globales
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
```

#### 4. Ajouter de nouvelles personnes

##### Ajouter un adulte (>= 18 ans)
```powershell
$adultData = @{
    firstName = "Jean"
    lastName = "Dupont"
    email = "jean.dupont@example.com"
    birthDate = "1990-05-15"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $adultData -ContentType "application/json"
```

##### Ajouter un mineur (< 18 ans)
```powershell
$minorData = @{
    firstName = "Alice"
    lastName = "Martin"
    email = "alice.martin@example.com"
    birthDate = "2010-03-20"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $minorData -ContentType "application/json"
```

### Tests avec cURL (alternative)

#### Installation de cURL (si nécessaire)
```powershell
# cURL est généralement disponible sur Windows 10+
curl --version
```

#### Tests des endpoints
```powershell
# GET - Toutes les personnes
curl -X GET "http://localhost:8080/api/persons"

# GET - Adultes seulement
curl -X GET "http://localhost:8080/api/persons/adults"

# GET - Mineurs seulement
curl -X GET "http://localhost:8080/api/persons/minors"

# GET - Statistiques
curl -X GET "http://localhost:8080/api/persons/statistics"

# POST - Ajouter un adulte
curl -X POST "http://localhost:8080/api/persons" ^
     -H "Content-Type: application/json" ^
     -d "{\"firstName\":\"Marie\",\"lastName\":\"Dubois\",\"email\":\"marie.dubois@example.com\",\"birthDate\":\"1985-12-01\"}"

# POST - Ajouter un mineur
curl -X POST "http://localhost:8080/api/persons" ^
     -H "Content-Type: application/json" ^
     -d "{\"firstName\":\"Tom\",\"lastName\":\"Wilson\",\"email\":\"tom.wilson@example.com\",\"birthDate\":\"2012-07-10\"}"
```

## 🔄 Tests des Processus BPMN

### Accès à Camunda Cockpit
1. **URL :** `http://localhost:8080/camunda`
2. **Credentials par défaut :** 
   - **Username :** `demo`
   - **Password :** `demo`
3. **Credentials admin (si configuré) :**
   - **Username :** `admin`
   - **Password :** `admin`

### Démarrage manuel d'un processus

#### Via l'API Custom Age Routing (RECOMMANDÉ)
```powershell
# Démarrer le processus avec une personne adulte via l'API custom
$adultData = @{
    email = "test.adult@example.com"
    firstName = "Test"
    lastName = "Adult"
    birthDate = "1990-01-01"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $adultData -ContentType "application/json"
```

```powershell
# Démarrer le processus avec une personne mineure via l'API custom
$minorData = @{
    email = "test.minor@example.com" 
    firstName = "Test"
    lastName = "Minor"
    birthDate = "2010-01-01"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $minorData -ContentType "application/json"
```

#### Via l'API Camunda directe (avec types corrects)
```powershell
# ATTENTION: Utiliser des types Long pour les IDs numériques
$processData = @{
    variables = @{
        firstName = @{ value = "Test"; type = "String" }
        lastName = @{ value = "Adult"; type = "String" }
        email = @{ value = "test.adult@example.com"; type = "String" }
        birthDate = @{ value = "1990-01-01"; type = "String" }
        personId = @{ value = [long]123; type = "Long" }  # Important: type Long
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition/key/age-based-routing-process/start" -Method POST -Body $processData -ContentType "application/json"
```

## 🎯 Tests de Routage par Âge

### ⚠️ Correction de l'Erreur Integer/Long Cast

**Problème :** `java.lang.Integer cannot be cast to class java.lang.Long`

**Solution :** Utiliser l'API custom `/api/process/age-routing/start` au lieu de l'API Camunda directe, ou spécifier explicitement les types Long.

#### Tests de l'API Age Routing Process

```powershell
# Test complet de l'API Age Routing Process
Write-Host "=== TEST API AGE ROUTING PROCESS ==="

# 1. Démarrer processus adulte
$adultRequest = @{
    personId = 100
    email = "test.adult.api@example.com"
    firstName = "TestAdult"
    lastName = "API"
    birthDate = "1990-01-01"
} | ConvertTo-Json

$adultResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $adultRequest -ContentType "application/json"
Write-Host "✅ Processus adulte: $($adultResponse.processInstanceId)"

# 2. Démarrer processus mineur  
$minorRequest = @{
    email = "test.minor.api@example.com"
    firstName = "TestMinor" 
    lastName = "API"
    birthDate = "2010-01-01"
} | ConvertTo-Json

$minorResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $minorRequest -ContentType "application/json"
Write-Host "✅ Processus mineur: $($minorResponse.processInstanceId)"

# 3. Vérifier statuts
Start-Sleep -Seconds 1
$adultStatus = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/status/$($adultResponse.processInstanceId)" -Method GET
$minorStatus = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/status/$($minorResponse.processInstanceId)" -Method GET

Write-Host "Adulte DB: $($adultStatus.variables.targetDatabase)"
Write-Host "Mineur DB: $($minorStatus.variables.targetDatabase)"

# 4. Statistiques
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/statistics" -Method GET
Write-Host "Total processus: $($stats.statistics.total)"
```

### Test Complet de Séparation des Données

#### 1. Vider les bases (pour test propre)
```sql
-- Dans la console H2 main_db
DELETE FROM PERSON;

-- Dans la console H2 minors_db  
DELETE FROM PERSON;
```

#### 2. Ajouter des données de test
```powershell
# Adulte 1 (30 ans)
$adult1 = @{
    firstName = "Pierre"
    lastName = "Durand"
    email = "pierre.durand@example.com"
    birthDate = "1994-01-15"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $adult1 -ContentType "application/json"

# Adulte 2 (25 ans)
$adult2 = @{
    firstName = "Sophie"
    lastName = "Bernard"
    email = "sophie.bernard@example.com"
    birthDate = "1999-06-20"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $adult2 -ContentType "application/json"

# Mineur 1 (15 ans)
$minor1 = @{
    firstName = "Lucas"
    lastName = "Petit"
    email = "lucas.petit@example.com"
    birthDate = "2009-03-10"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $minor1 -ContentType "application/json"

# Mineur 2 (12 ans)
$minor2 = @{
    firstName = "Emma"
    lastName = "Moreau"
    email = "emma.moreau@example.com"
    birthDate = "2012-09-05"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $minor2 -ContentType "application/json"
```

#### 3. Vérifier la séparation des données
```powershell
# Vérifier que nous avons bien 2 adultes
$adults = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/adults" -Method GET
Write-Host "Nombre d'adultes: $($adults.Count)"

# Vérifier que nous avons bien 2 mineurs
$minors = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/minors" -Method GET
Write-Host "Nombre de mineurs: $($minors.Count)"

# Vérifier les statistiques
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
$stats
```

## 🔍 Vérification des Données

### Vérifications en Base de Données

#### Base principale (main_db) - Doit contenir UNIQUEMENT les adultes
```sql
-- Connectez-vous à jdbc:h2:mem:main_db
SELECT firstName, lastName, birthDate, 
       DATEDIFF('YEAR', birthDate, CURRENT_DATE()) as age 
FROM PERSON 
ORDER BY age DESC;

-- Vérifier qu'aucun mineur n'est présent
SELECT COUNT(*) as minors_in_main_db 
FROM PERSON 
WHERE DATEDIFF('YEAR', birthDate, CURRENT_DATE()) < 18;
```

#### Base mineurs (minors_db) - Doit contenir UNIQUEMENT les mineurs
```sql
-- Connectez-vous à jdbc:h2:mem:minors_db
SELECT firstName, lastName, birthDate, 
       DATEDIFF('YEAR', birthDate, CURRENT_DATE()) as age 
FROM PERSON 
ORDER BY age DESC;

-- Vérifier qu'aucun adulte n'est présent
SELECT COUNT(*) as adults_in_minors_db 
FROM PERSON 
WHERE DATEDIFF('YEAR', birthDate, CURRENT_DATE()) >= 18;
```

## 🧪 Tests d'Intégration

### Script de Test Complet avec Correction d'Erreurs
```powershell
# Test d'intégration complète avec gestion des erreurs Integer/Long
Write-Host "=== DEBUT DU TEST D'INTEGRATION CORRIGE ==="

# 1. Vérifier que l'application répond
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
    Write-Host "✅ Application en ligne: $($health.status)"
} catch {
    Write-Host "❌ Application non accessible"
    exit 1
}

# 2. Obtenir les statistiques initiales
$initialStats = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
Write-Host "📊 Statistiques initiales - Adultes: $($initialStats.totalAdults), Mineurs: $($initialStats.totalMinors)"

# 3. Ajouter une personne adulte
$testAdult = @{
    firstName = "TestAdult"
    lastName = "Integration"
    email = "test.adult.integration@example.com"
    birthDate = "1990-01-01"
} | ConvertTo-Json

$adultResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $testAdult -ContentType "application/json"
Write-Host "✅ Adulte ajouté avec ID: $($adultResult.id)"

# 4. Ajouter une personne mineure
$testMinor = @{
    firstName = "TestMinor"
    lastName = "Integration"
    email = "test.minor.integration@example.com"
    birthDate = "2010-01-01"
} | ConvertTo-Json

$minorResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $testMinor -ContentType "application/json"
Write-Host "✅ Mineur ajouté avec ID: $($minorResult.id)"

# 5. Vérifier les nouvelles statistiques
$finalStats = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
Write-Host "📊 Statistiques finales - Adultes: $($finalStats.totalAdults), Mineurs: $($finalStats.totalMinors)"

# 6. Vérifier l'augmentation
$adultIncrease = $finalStats.totalAdults - $initialStats.totalAdults
$minorIncrease = $finalStats.totalMinors - $initialStats.totalMinors

if ($adultIncrease -eq 1 -and $minorIncrease -eq 1) {
    Write-Host "✅ Test d'intégration RÉUSSI - Routage par âge fonctionne correctement"
} else {
    Write-Host "❌ Test d'intégration ÉCHOUÉ - Problème de routage"
}

Write-Host "=== FIN DU TEST D'INTEGRATION ==="
```

## � Configuration Utilisateur Camunda

### Utilisateurs par Défaut
Camunda utilise généralement ces comptes par défaut :

#### Compte Demo (Configuration Standard)
- **Username :** `demo`
- **Password :** `demo`
- **Rôles :** Utilisateur standard avec accès Cockpit

#### Compte Admin (Si activé)
- **Username :** `admin` 
- **Password :** `admin`
- **Rôles :** Administrateur complet

### Vérification des Comptes Disponibles
```powershell
# Tester l'accès avec demo
$demoAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("demo:demo"))
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/user" -Method GET -Headers @{Authorization="Basic $demoAuth"}
    Write-Host "✅ Accès demo OK - Utilisateurs disponibles:"
    $result | ForEach-Object { Write-Host "  - $($_.id) ($($_.firstName) $($_.lastName))" }
} catch {
    Write-Host "❌ Erreur accès demo: $($_.Exception.Message)"
}

# Tester l'accès avec admin
$adminAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
try {
    $adminResult = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/user" -Method GET -Headers @{Authorization="Basic $adminAuth"}
    Write-Host "✅ Accès admin OK"
} catch {
    Write-Host "❌ Compte admin non configuré ou incorrect"
}
```

### Créer un Utilisateur Admin via API REST
```powershell
# Créer un nouvel utilisateur admin
$newAdminUser = @{
    profile = @{
        id = "admin"
        firstName = "Admin"
        lastName = "User"
        email = "admin@example.com"
    }
    credentials = @{
        password = "admin123"
    }
} | ConvertTo-Json -Depth 3

# Utiliser les credentials demo pour créer l'admin
$demoAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("demo:demo"))
try {
    Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/user/create" -Method POST -Body $newAdminUser -ContentType "application/json" -Headers @{Authorization="Basic $demoAuth"}
    Write-Host "✅ Utilisateur admin créé avec succès"
    Write-Host "   Username: admin"
    Write-Host "   Password: admin123"
} catch {
    Write-Host "❌ Erreur création admin: $($_.Exception.Message)"
}
```

### Lister Tous les Utilisateurs
```powershell
# Via API REST
$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("demo:demo"))
$users = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/user" -Method GET -Headers @{Authorization="Basic $auth"}

Write-Host "=== UTILISATEURS CAMUNDA ==="
$users | ForEach-Object {
    Write-Host "ID: $($_.id)"
    Write-Host "Nom: $($_.firstName) $($_.lastName)"
    Write-Host "Email: $($_.email)"
    Write-Host "---"
}
```

### Accès Direct aux Interfaces Camunda
```powershell
Write-Host "=== ACCÈS INTERFACES CAMUNDA ==="
Write-Host "Cockpit (Monitoring): http://localhost:8080/camunda/app/cockpit/"
Write-Host "Tasklist (Tâches): http://localhost:8080/camunda/app/tasklist/"
Write-Host "Admin (Administration): http://localhost:8080/camunda/app/admin/"
Write-Host ""
Write-Host "Credentials à essayer:"
Write-Host "1. demo / demo"
Write-Host "2. admin / admin"
Write-Host "3. admin / admin123 (si créé via script ci-dessus)"
```

## �🔧 Dépannage

### Problèmes Courants

#### 1. Erreur "Integer cannot be cast to Long"
**Problème :** `java.lang.Integer cannot be cast to class java.lang.Long`
**Cause :** Mauvais typage des variables dans les processus BPMN
**Solutions :**
```powershell
# ✅ CORRECT - Utiliser l'API custom
$correctRequest = @{
    email = "test@example.com"
    firstName = "Test"
    lastName = "User" 
    birthDate = "1990-01-01"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $correctRequest -ContentType "application/json"

# ❌ INCORRECT - API Camunda avec mauvais types
# Ne pas utiliser personId comme Integer dans l'API Camunda directe

# ✅ CORRECT - Si vous devez utiliser l'API Camunda directe
$camundaRequest = @{
    variables = @{
        email = @{ value = "test@example.com"; type = "String" }
        firstName = @{ value = "Test"; type = "String" }
        lastName = @{ value = "User"; type = "String" }
        birthDate = @{ value = "1990-01-01"; type = "String" }
        # Éviter personId ou utiliser type Long explicitement
    }
} | ConvertTo-Json -Depth 3
```

#### 2. Erreur dans AgeRoutingProcessController
**Problème :** `Error starting age-based routing process` dans les logs
**Cause :** Erreur dans le contrôleur lors du démarrage du processus BPMN
**Solutions de diagnostic :**
```powershell
# 🔍 DIAGNOSTIC AVANCÉ POUR ERREUR AGERROUTINGPROCESSCONTROLLER

Write-Host "=== DIAGNOSTIC ERREUR AGE ROUTING CONTROLLER ==="

# 1. Tester d'abord le service de base (sans processus BPMN)
Write-Host "`n1. Test du service de base sans BPMN..."
$directTest = @{
    firstName = "Marie"
    lastName = "Dubois"
    email = "marie.dubois.direct@example.com"
    birthDate = "1985-12-01"
} | ConvertTo-Json

try {
    $directResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $directTest -ContentType "application/json" -Verbose
    Write-Host "✅ Service de base fonctionne - ID: $($directResult.id)"
    Write-Host "   Database: $($directResult.targetDatabase)"
    $baseServiceOK = $true
} catch {
    Write-Host "❌ ERREUR SERVICE DE BASE: $($_.Exception.Message)"
    Write-Host "   → Le problème est dans AgeBasedPersonService, pas dans BPMN"
    $baseServiceOK = $false
}

# 2. Si le service de base fonctionne, tester le processus BPMN avec les mêmes données
if ($baseServiceOK) {
    Write-Host "`n2. Test du processus BPMN avec données identiques..."
    
    # Test avec les MÊMES données que celles qui ont échoué
    $problematicData = @{
        email = "marie.dubois.bpmn@example.com"  # Email légèrement différent pour éviter les doublons
        firstName = "Marie"
        lastName = "Dubois"
        birthDate = "1985-12-01"
    } | ConvertTo-Json
    
    try {
        Write-Host "Envoi de la requête BPMN..."
        $bpmnResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $problematicData -ContentType "application/json" -Verbose
        Write-Host "✅ Processus BPMN réussi - ID: $($bpmnResult.processInstanceId)"
        Write-Host "   Variables: $($bpmnResult.variables | ConvertTo-Json -Compress)"
    } catch {
        Write-Host "❌ ERREUR PROCESSUS BPMN: $($_.Exception.Message)"
        
        # Analyser la réponse d'erreur complète
        if ($_.Exception.Response) {
            $errorResponse = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorResponse)
            $errorBody = $reader.ReadToEnd()
            Write-Host "   Détails erreur: $errorBody"
        }
    }
}

# 3. Vérifier les définitions de processus BPMN
Write-Host "`n3. Vérification des processus BPMN..."
try {
    $processes = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition" -Method GET
    $ageRoutingProcess = $processes | Where-Object { $_.key -eq "age-based-routing-process" }
    
    if ($ageRoutingProcess) {
        Write-Host "✅ Processus age-based-routing-process trouvé:"
        Write-Host "   ID: $($ageRoutingProcess.id)"
        Write-Host "   Version: $($ageRoutingProcess.version)"
        Write-Host "   Deployed: $($ageRoutingProcess.deploymentId)"
    } else {
        Write-Host "❌ Processus age-based-routing-process NON TROUVÉ"
        Write-Host "   Processus disponibles:"
        $processes | ForEach-Object { Write-Host "   - $($_.key)" }
    }
} catch {
    Write-Host "❌ Erreur accès engine REST: $($_.Exception.Message)"
}

# 4. Test avec données simplifiées pour isoler le problème
Write-Host "`n4. Test avec données minimales..."
$minimalTest = @{
    email = "minimal.test@example.com"
    firstName = "Test"
    lastName = "Minimal"
    birthDate = "1990-01-01"
} | ConvertTo-Json

try {
    $minimalResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $minimalTest -ContentType "application/json"
    Write-Host "✅ Test minimal réussi - Le problème n'est pas dans les données"
} catch {
    Write-Host "❌ Test minimal échoué - Problème général dans le contrôleur"
    Write-Host "   Erreur: $($_.Exception.Message)"
}
```

**Diagnostic avancé pour votre erreur spécifique :**

```powershell
# 🚨 REPRODUCTION DE VOTRE ERREUR EXACTE
Write-Host "=== REPRODUCTION ERREUR MARIE DUBOIS ==="

# Reproduire exactement votre requête qui a échoué
$exactFailedRequest = @{
    email = "marie.dubos@example.com"
    firstName = "Mari" 
    lastName = "Duois"
    birthDate = "1985-12-01"
} | ConvertTo-Json

Write-Host "Reproduction de la requête qui a échoué..."
Write-Host "Data: $exactFailedRequest"

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $exactFailedRequest -ContentType "application/json" -Verbose
    Write-Host "✅ Requête réussie maintenant - Problème résolu?"
    Write-Host "Process ID: $($result.processInstanceId)"
} catch {
    Write-Host "❌ Erreur reproduite:"
    Write-Host "   Message: $($_.Exception.Message)"
    Write-Host "   Status: $($_.Exception.Response.StatusCode)"
    
    # Capturer la réponse complète d'erreur
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Body: $responseBody"
    }
}

# Test de comparaison avec des données similaires mais correctes
Write-Host "`nTest de comparaison avec données corrigées..."
$correctedRequest = @{
    email = "marie.dubois.corrected@example.com"  # Email corrigé
    firstName = "Marie"  # Nom corrigé
    lastName = "Dubois"  # Nom de famille corrigé  
    birthDate = "1985-12-01"  # Date identique
} | ConvertTo-Json

try {
    $correctedResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $correctedRequest -ContentType "application/json"
    Write-Host "✅ Données corrigées fonctionnent - Process ID: $($correctedResult.processInstanceId)"
    Write-Host "   → Le problème pourrait être lié aux caractères ou à la validation des données"
} catch {
    Write-Host "❌ Même erreur avec données corrigées - Problème système"
}
```

**Actions recommandées :**
1. **Vérifier les logs complets** dans la console Spring Boot pour voir l'exception complète
2. **Tester le service de base** d'abord : `POST /api/persons` avec les mêmes données
3. **Vérifier si le processus BPMN est correctement déployé**
4. **S'assurer que tous les beans Spring sont correctement injectés** (`AgeBasedRoutingDelegate`, `PersonProcessingDelegate`, `AgeBasedPersonService`)
5. **Redémarrer l'application** si les beans ne sont pas injectés

#### 2. Application ne démarre pas
```powershell
# Vérifier les ports utilisés
netstat -an | findstr :8080

# Logs détaillés
mvn spring-boot:run -X
```

#### 2. Erreurs de connexion H2
```powershell
# Vérifier les URLs de connexion dans application.yml
Get-Content src/main/resources/application.yml | Select-String -Pattern "jdbc"
```

#### 3. Données non routées correctement
```sql
-- Vérifier la logique d'âge
SELECT firstName, lastName, birthDate, 
       DATEDIFF('YEAR', birthDate, CURRENT_DATE()) as calculated_age,
       CASE 
           WHEN DATEDIFF('YEAR', birthDate, CURRENT_DATE()) < 18 THEN 'MINOR'
           ELSE 'ADULT'
       END as should_be_in_db
FROM PERSON;
```

#### 4. Processus BPMN ne se lance pas
- Vérifier dans Camunda Cockpit que le processus est déployé
- Contrôler les logs pour les erreurs de délégués
- Vérifier que les beans Spring sont correctement injectés

### Commandes de Diagnostic

#### Diagnostic pour "execution doesn't exist"
```powershell
# 1. Vérifier les définitions de processus déployées
$processes = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition" -Method GET
Write-Host "Processus déployés:"
$processes | ForEach-Object { Write-Host "- $($_.key) (version $($_.version))" }

# 2. Vérifier les instances actives
$activeInstances = Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-instance" -Method GET
Write-Host "Instances actives: $($activeInstances.Count)"

# 3. Tester les services de base AVANT les processus BPMN
Write-Host "`n=== TEST DES SERVICES DE BASE ==="

# Test du service AgeBasedPersonService via API REST
$baseServiceTest = @{
    firstName = "ServiceTest"
    lastName = "Base"
    email = "service.test@example.com"
    birthDate = "1990-01-01"
} | ConvertTo-Json

try {
    $serviceResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $baseServiceTest -ContentType "application/json"
    Write-Host "✅ Service de base OK - Person ID: $($serviceResult.id)"
    
    # Vérifier dans quelle base elle a été stockée
    $adults = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/adults" -Method GET
    $minors = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/minors" -Method GET
    Write-Host "✅ Routage par âge OK - Adultes: $($adults.Count), Mineurs: $($minors.Count)"
    
} catch {
    Write-Host "❌ PROBLÈME SERVICE DE BASE: $($_.Exception.Message)"
    Write-Host "   → Le problème est dans AgeBasedPersonService, pas dans BPMN"
}

# 4. Si le service de base fonctionne, tester le processus BPMN
Write-Host "`n=== TEST PROCESSUS BPMN (si service de base OK) ==="
if ($serviceResult) {
    $bpmnTest = @{
        email = "bpmn.test@example.com"
        firstName = "BPMN"
        lastName = "Test" 
        birthDate = "1990-01-01"
    } | ConvertTo-Json
    
    try {
        $bpmnResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $bpmnTest -ContentType "application/json"
        Write-Host "✅ Processus BPMN OK - ID: $($bpmnResult.processInstanceId)"
    } catch {
        Write-Host "❌ PROBLÈME PROCESSUS BPMN: $($_.Exception.Message)"
        Write-Host "   → Vérifier les délégués dans le processus BPMN"
    }
}
```

#### Logs et Monitoring
```powershell
# Voir les logs en temps réel avec filtrage
mvn spring-boot:run | Tee-Object -FilePath "application.log" | Select-String -Pattern "ERROR|Exception|age-based|AgeBasedRoutingDelegate"

# Vérifier les processus Java en cours
Get-Process -Name "java" -ErrorAction SilentlyContinue

# Nettoyer et redémarrer avec logs détaillés
mvn clean
$env:LOGGING_LEVEL_COM_EXAMPLE_CAMUNDA = "DEBUG"
mvn spring-boot:run
```

#### Script de Diagnostic Complet
```powershell
Write-Host "=== DIAGNOSTIC COMPLET ERREUR EXECUTION ==="

# Étape 1: Santé application
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
    Write-Host "✅ Application: $($health.status)"
} catch {
    Write-Host "❌ Application inaccessible"
    exit 1
}

# Étape 2: Test service minimal (sans BPMN)
Write-Host "`nÉtape 2: Test service de base..."
$minimalPerson = @{
    firstName = "Diagnostic"
    lastName = "Test"
    email = "diagnostic.$(Get-Date -Format 'HHmmss')@example.com"
    birthDate = "1990-01-01"
} | ConvertTo-Json

try {
    $personResult = Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $minimalPerson -ContentType "application/json"
    Write-Host "✅ Service de base fonctionne - ID: $($personResult.id)"
    $baseServiceOK = $true
} catch {
    Write-Host "❌ Service de base défaillant: $($_.Exception.Message)"
    $baseServiceOK = $false
}

# Étape 3: Test processus BPMN seulement si service de base OK
if ($baseServiceOK) {
    Write-Host "`nÉtape 3: Test processus BPMN..."
    $processTest = @{
        email = "process.$(Get-Date -Format 'HHmmss')@example.com"
        firstName = "Process"
        lastName = "Test"
        birthDate = "1990-01-01"
    } | ConvertTo-Json
    
    try {
        $processResult = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $processTest -ContentType "application/json"
        Write-Host "✅ Processus BPMN fonctionne - ID: $($processResult.processInstanceId)"
    } catch {
        Write-Host "❌ Processus BPMN défaillant: $($_.Exception.Message)"
        Write-Host "   → Problème dans les délégués BPMN"
        Write-Host "   → Vérifier AgeBasedRoutingDelegate dans les logs"
    }
} else {
    Write-Host "❌ Impossible de tester BPMN car le service de base ne fonctionne pas"
}

Write-Host "`n=== RECOMMANDATIONS ==="
if (-not $baseServiceOK) {
    Write-Host "1. Vérifier AgeBasedPersonService et la configuration des DataSources"
    Write-Host "2. Vérifier les connexions H2 (main_db et minors_db)"
    Write-Host "3. Regarder les logs Spring Boot pour les erreurs de bean injection"
} else {
    Write-Host "1. Le service de base fonctionne, le problème est dans le processus BPMN"
    Write-Host "2. Vérifier AgeBasedRoutingDelegate et PersonProcessingDelegate"
    Write-Host "3. Vérifier que les beans sont correctement injectés dans les délégués"
}
```

## 📈 Tests de Performance

### Test de Charge Simple
```powershell
# Test d'ajout en masse (ajustez le nombre selon vos besoins)
for ($i=1; $i -le 10; $i++) {
    $randomAdult = @{
        firstName = "Adult$i"
        lastName = "Test"
        email = "adult$i@test.com"
        birthDate = "1990-01-01"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $randomAdult -ContentType "application/json"
    Write-Host "Adulte $i ajouté"
}

for ($j=1; $j -le 10; $j++) {
    $randomMinor = @{
        firstName = "Minor$j"
        lastName = "Test"
        email = "minor$j@test.com"
        birthDate = "2010-01-01"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method POST -Body $randomMinor -ContentType "application/json"
    Write-Host "Mineur $j ajouté"
}

# Vérifier les résultats
$finalStats = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
Write-Host "Résultat final: $($finalStats.totalAdults) adultes, $($finalStats.totalMinors) mineurs"
```

---

## 🎯 Checklist de Test

- [ ] ✅ Application démarre sans erreur
- [ ] ✅ Console H2 main_db accessible 
- [ ] ✅ Console H2 minors_db accessible
- [ ] ✅ API /api/persons fonctionne
- [ ] ✅ API /api/persons/adults retourne uniquement les adultes
- [ ] ✅ API /api/persons/minors retourne uniquement les mineurs
- [ ] ✅ API /api/persons/statistics donne des chiffres cohérents
- [ ] ✅ Ajout d'un adulte va dans main_db
- [ ] ✅ Ajout d'un mineur va dans minors_db
- [ ] ✅ Processus BPMN se lance correctement
- [ ] ✅ Camunda Cockpit accessible
- [ ] ✅ Routage par âge fonctionne à 100%
- [ ] ✅ Séparation des données respectée
- [ ] ✅ Test d'intégration complet réussi

**Ce guide de test couvre tous les aspects de votre application avec routage par âge ! 🚀**