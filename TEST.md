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
2. **Credentials :** `demo / demo`

### Démarrage manuel d'un processus
```powershell
# Démarrer le processus avec une personne adulte
$processData = @{
    variables = @{
        firstName = @{ value = "Test"; type = "String" }
        lastName = @{ value = "Adult"; type = "String" }
        email = @{ value = "test.adult@example.com"; type = "String" }
        birthDate = @{ value = "1990-01-01"; type = "String" }
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition/key/age-based-routing-process/start" -Method POST -Body $processData -ContentType "application/json"
```

```powershell
# Démarrer le processus avec une personne mineure
$processDataMinor = @{
    variables = @{
        firstName = @{ value = "Test"; type = "String" }
        lastName = @{ value = "Minor"; type = "String" }
        email = @{ value = "test.minor@example.com"; type = "String" }
        birthDate = @{ value = "2010-01-01"; type = "String" }
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/engine-rest/process-definition/key/age-based-routing-process/start" -Method POST -Body $processDataMinor -Content-Type "application/json"
```

## 🎯 Tests de Routage par Âge

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

### Script de Test Complet
```powershell
# Test d'intégration complète
Write-Host "=== DEBUT DU TEST D'INTEGRATION ==="

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

## 🔧 Dépannage

### Problèmes Courants

#### 1. Application ne démarre pas
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
```powershell
# Voir les logs en temps réel
mvn spring-boot:run | Tee-Object -FilePath "application.log"

# Vérifier les processus Java en cours
Get-Process -Name "java" -ErrorAction SilentlyContinue

# Nettoyer et redémarrer
mvn clean
mvn spring-boot:run
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