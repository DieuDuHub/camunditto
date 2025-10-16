# Fix pour l'erreur Integer/Long Cast - Tests API Age Routing

## 🚨 Problème Identifié
**Erreur :** `java.lang.Integer cannot be cast to class java.lang.Long`
**Cause :** Problème de typage dans les variables du processus BPMN

## ✅ Solution Recommandée
**Utiliser l'API custom `/api/process/age-routing/start` au lieu de l'API Camunda directe**

## 🧪 Tests de Validation

### Test 1: API Age Routing (Solution Recommandée)
```powershell
# Test avec un adulte
Write-Host "Test 1: Adulte via API Age Routing..."
$adultRequest = @{
    email = "fix.test.adult@example.com"
    firstName = "FixTest"
    lastName = "Adult"
    birthDate = "1990-01-01"
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $adultRequest -ContentType "application/json"
    Write-Host "✅ SUCCÈS - Processus ID: $($result.processInstanceId)"
    Write-Host "   Target DB: $($result.variables.targetDatabase)"
} catch {
    Write-Host "❌ ÉCHEC: $($_.Exception.Message)"
}
```

```powershell
# Test avec un mineur
Write-Host "`nTest 2: Mineur via API Age Routing..."
$minorRequest = @{
    email = "fix.test.minor@example.com"
    firstName = "FixTest"
    lastName = "Minor"
    birthDate = "2010-01-01"
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/start" -Method POST -Body $minorRequest -ContentType "application/json"
    Write-Host "✅ SUCCÈS - Processus ID: $($result.processInstanceId)"
    Write-Host "   Target DB: $($result.variables.targetDatabase)"
} catch {
    Write-Host "❌ ÉCHEC: $($_.Exception.Message)"
}
```

### Test 2: Validation de la Séparation des Données
```powershell
Write-Host "`n=== VALIDATION DE LA SÉPARATION DES DONNÉES ==="

# Vérifier les adultes
$adults = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/adults" -Method GET
Write-Host "Adultes dans main_db: $($adults.Count)"

# Vérifier les mineurs  
$minors = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/minors" -Method GET
Write-Host "Mineurs dans minors_db: $($minors.Count)"

# Statistiques
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/persons/statistics" -Method GET
Write-Host "Total adultes: $($stats.totalAdults)"
Write-Host "Total mineurs: $($stats.totalMinors)"
```

### Test 3: Statistiques des Processus
```powershell
Write-Host "`n=== STATISTIQUES DES PROCESSUS ==="

try {
    $processStats = Invoke-RestMethod -Uri "http://localhost:8080/api/process/age-routing/statistics" -Method GET
    Write-Host "✅ Statistiques processus:"
    Write-Host "   Total: $($processStats.statistics.total)"
    Write-Host "   Actifs: $($processStats.statistics.active)"
    Write-Host "   Complétés: $($processStats.statistics.completed)"
    Write-Host "   Taux completion: $($processStats.statistics.completionRate)%"
} catch {
    Write-Host "❌ Erreur statistiques: $($_.Exception.Message)"
}
```

## 🔍 Diagnostic Avancé

### Vérifier les Variables dans Camunda Cockpit
1. Aller sur `http://localhost:8080/camunda`
2. Login: `demo/demo`
3. Aller dans "Processes" > "age-based-routing-process"
4. Cliquer sur une instance de processus
5. Vérifier l'onglet "Variables" pour voir les types de données

### Requêtes SQL de Diagnostic
```sql
-- Dans H2 Console main_db (jdbc:h2:mem:main_db)
SELECT 'MAIN_DB' as database_name, COUNT(*) as person_count FROM PERSON;
SELECT firstName, lastName, birthDate, 
       DATEDIFF('YEAR', birthDate, CURRENT_DATE()) as age 
FROM PERSON WHERE DATEDIFF('YEAR', birthDate, CURRENT_DATE()) >= 18;

-- Dans H2 Console minors_db (jdbc:h2:mem:minors_db)  
SELECT 'MINORS_DB' as database_name, COUNT(*) as person_count FROM PERSON;
SELECT firstName, lastName, birthDate,
       DATEDIFF('YEAR', birthDate, CURRENT_DATE()) as age
FROM PERSON WHERE DATEDIFF('YEAR', birthDate, CURRENT_DATE()) < 18;
```

## ⚠️ Ce qu'il faut ÉVITER

### ❌ NE PAS utiliser l'API Camunda directe avec personId
```powershell
# ÉVITER CECI - Cause l'erreur Integer/Long
$badRequest = @{
    variables = @{
        personId = @{ value = 123; type = "Integer" }  # ❌ Problématique
        email = @{ value = "test@example.com"; type = "String" }
    }
} | ConvertTo-Json -Depth 3

# Cette requête provoque l'erreur de cast
```

## ✅ Script de Test Complet et Sûr
```powershell
Write-Host "=== TEST COMPLET SANS ERREUR INTEGER/LONG ==="

# Configuration
$baseUrl = "http://localhost:8080"
$testPersons = @(
    @{ email = "safe.test1@example.com"; firstName = "Safe"; lastName = "Adult1"; birthDate = "1985-01-01" },
    @{ email = "safe.test2@example.com"; firstName = "Safe"; lastName = "Adult2"; birthDate = "1995-06-15" },
    @{ email = "safe.test3@example.com"; firstName = "Safe"; lastName = "Minor1"; birthDate = "2008-03-20" },
    @{ email = "safe.test4@example.com"; firstName = "Safe"; lastName = "Minor2"; birthDate = "2012-11-10" }
)

# Exécuter les tests
foreach ($person in $testPersons) {
    Write-Host "`nTesting: $($person.firstName) $($person.lastName) ($(Split-Path $person.birthDate -Leaf))"
    
    $request = $person | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/process/age-routing/start" -Method POST -Body $request -ContentType "application/json"
        Write-Host "✅ Processus créé: $($response.processInstanceId)"
        Write-Host "   Database: $($response.variables.targetDatabase)"
    } catch {
        Write-Host "❌ Erreur: $($_.Exception.Message)"
    }
}

# Vérification finale
Write-Host "`n=== VÉRIFICATION FINALE ==="
$stats = Invoke-RestMethod -Uri "$baseUrl/api/persons/statistics" -Method GET
Write-Host "Adultes: $($stats.totalAdults), Mineurs: $($stats.totalMinors)"

$processStats = Invoke-RestMethod -Uri "$baseUrl/api/process/age-routing/statistics" -Method GET  
Write-Host "Processus total: $($processStats.statistics.total)"
```

**🎯 Résultat Attendu :** Tous les tests passent sans erreur Integer/Long, et la séparation par âge fonctionne correctement !