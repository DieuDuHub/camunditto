# Camunda Person API - PlantUML Documentation

This document contains PlantUML diagrams for the Camunda Person API project.

## 1. System Architecture Overview

```plantuml
@startuml system-architecture
!theme plain
title Camunda Person API - System Architecture

package "Presentation Layer" {
  [PersonController] as PC
  [ProcessController] as PRC
}

package "Business Layer" {
  [PersonService] as PS
  [PersonValidationDelegate] as PVD
  [PersonProcessingDelegate] as PPD
}

package "Data Layer" {
  [PersonRepository] as PR
  interface JpaRepository
  [H2 Database] as DB
}

package "Process Engine" {
  [Camunda BPM Engine] as CBE
  [person-process.bpmn] as BPMN
}

package "Configuration" {
  [DataLoader] as DL
  [Spring Boot Configuration] as SBC
}

' Relationships
PC --> PS : uses
PRC --> CBE : uses
PS --> PR : uses
PR --> JpaRepository : implements
JpaRepository --> DB : persists to

CBE --> BPMN : deploys
CBE --> PVD : executes
CBE --> PPD : executes
PVD --> PS : uses
PPD --> PS : uses

DL --> PS : initializes data
SBC --> CBE : configures
SBC --> DB : configures

' External interfaces
[REST API Clients] --> PC : HTTP requests
[Camunda Cockpit] --> CBE : monitoring
[H2 Console] --> DB : admin access

@enduml
```

## 2. BPMN Process Flow

```plantuml
@startuml person-process-flow
!theme plain
title Person Validation Process Flow

start
:Start Person Process;
note right
  Input variables:
  - personId (Long)
  - email (String)
  - processingType (String)
end note

:Validate Person;
note right
  Executed by:
  PersonValidationDelegate
  
  Logic:
  - Check if person exists by ID or email
  - Set isPersonValid variable
  - Set validationResult message
end note

if (Is Person Valid?) then (yes)
  :Process Person;
  note right
    Executed by:
    PersonProcessingDelegate
    
    Processing types:
    - create
    - update
    - validate
    - default
  end note
  :Process Completed;
  stop
else (no)
  :Validation Failed;
  stop
endif

@enduml
```

## 3. Class Diagram - Domain Model

```plantuml
@startuml class-diagram
!theme plain
title Camunda Person API - Class Diagram

class Person {
  -Long id
  -String firstName
  -String lastName  
  -String email
  -String phoneNumber
  -LocalDate dateOfBirth
  -String address
  -String city
  -String country
  -LocalDateTime createdAt
  -LocalDateTime updatedAt
  
  +getFullName(): String
  +onCreate(): void
  +onUpdate(): void
}

interface PersonRepository {
  +findByEmail(email: String): Optional<Person>
  +findByFirstNameIgnoreCaseContaining(firstName: String): List<Person>
  +findByLastNameIgnoreCaseContaining(lastName: String): List<Person>
  +findBySearchTerm(searchTerm: String): List<Person>
  +findByCity(city: String): List<Person>
  +findByCountry(country: String): List<Person>
  +countAllPersons(): long
}

class PersonService {
  -PersonRepository personRepository
  
  +getAllPersons(): List<Person>
  +getPersonById(id: Long): Optional<Person>
  +getPersonByEmail(email: String): Optional<Person>
  +searchPersons(searchTerm: String): List<Person>
  +savePerson(person: Person): Person
  +updatePerson(id: Long, personDetails: Person): Person
  +deletePerson(id: Long): void
  +existsById(id: Long): boolean
  +countPersons(): long
}

class PersonController {
  -PersonService personService
  
  +getAllPersons(): ResponseEntity<Map<String, Object>>
  +getPersonById(id: Long): ResponseEntity<Map<String, Object>>
  +getPersonByEmail(email: String): ResponseEntity<Map<String, Object>>
  +searchPersons(term: String): ResponseEntity<Map<String, Object>>
  +createPerson(person: Person): ResponseEntity<Map<String, Object>>
  +updatePerson(id: Long, person: Person): ResponseEntity<Map<String, Object>>
  +deletePerson(id: Long): ResponseEntity<Map<String, Object>>
}

class PersonValidationDelegate {
  -PersonService personService
  
  +execute(execution: DelegateExecution): void
}

class PersonProcessingDelegate {
  -PersonService personService
  
  +execute(execution: DelegateExecution): void
}

class ProcessController {
  -ProcessEngine processEngine
  
  +startPersonProcess(variables: Map<String, Object>): ResponseEntity<Map<String, Object>>
  +getProcessStatus(processInstanceId: String): ResponseEntity<Map<String, Object>>
  +getActiveProcessInstances(): ResponseEntity<Map<String, Object>>
}

PersonRepository --|> JpaRepository
PersonService --> PersonRepository : uses
PersonController --> PersonService : uses
PersonValidationDelegate --> PersonService : uses
PersonProcessingDelegate --> PersonService : uses
ProcessController --> ProcessEngine : uses

@enduml
```

## 4. API Endpoints Sequence Diagram

```plantuml
@startuml api-sequence
!theme plain
title Person API - REST Endpoint Sequence

actor Client
participant PersonController
participant PersonService  
participant PersonRepository
database H2Database

== Get All Persons ==
Client -> PersonController: GET /api/persons
PersonController -> PersonService: getAllPersons()
PersonService -> PersonRepository: findAll()
PersonRepository -> H2Database: SELECT * FROM persons
PersonRepository <-- H2Database: List<Person>
PersonService <-- PersonRepository: List<Person>
PersonController <-- PersonService: List<Person>
Client <-- PersonController: JSON Response

== Create Person ==
Client -> PersonController: POST /api/persons\n{person data}
PersonController -> PersonService: savePerson(person)
PersonService -> PersonRepository: save(person)
PersonRepository -> H2Database: INSERT INTO persons
PersonRepository <-- H2Database: Person (with ID)
PersonService <-- PersonRepository: Person
PersonController <-- PersonService: Person
Client <-- PersonController: JSON Response (201 Created)

== Search Persons ==
Client -> PersonController: GET /api/persons/search?term=John
PersonController -> PersonService: searchPersons("John")
PersonService -> PersonRepository: findBySearchTerm("John")
PersonRepository -> H2Database: SELECT * FROM persons\nWHERE name LIKE '%John%'
PersonRepository <-- H2Database: List<Person>
PersonService <-- PersonRepository: List<Person>
PersonController <-- PersonService: List<Person>
Client <-- PersonController: JSON Response

@enduml
```

## 5. Camunda Process Execution Sequence

```plantuml
@startuml camunda-process-sequence
!theme plain
title Camunda Process Execution Sequence

actor Client
participant ProcessController
participant CamundaEngine
participant PersonValidationDelegate
participant PersonProcessingDelegate
participant PersonService
database H2Database

== Start Process ==
Client -> ProcessController: POST /api/process/start-person-process\n{personId: 1, processingType: "validate"}
ProcessController -> CamundaEngine: startProcessInstanceByKey("person-process", variables)
CamundaEngine -> CamundaEngine: Create process instance

== Validation Task ==
CamundaEngine -> PersonValidationDelegate: execute(delegateExecution)
PersonValidationDelegate -> PersonService: getPersonById(1)
PersonService -> H2Database: SELECT * FROM persons WHERE id = 1
PersonService <-- H2Database: Person
PersonValidationDelegate <-- PersonService: Optional<Person>
PersonValidationDelegate -> CamundaEngine: setVariable("isPersonValid", true)
PersonValidationDelegate -> CamundaEngine: setVariable("person", person)
CamundaEngine <-- PersonValidationDelegate: execution complete

== Gateway Decision ==
CamundaEngine -> CamundaEngine: evaluate ${isPersonValid == true}

== Processing Task ==
CamundaEngine -> PersonProcessingDelegate: execute(delegateExecution)
PersonProcessingDelegate -> CamundaEngine: getVariable("person")
PersonProcessingDelegate -> CamundaEngine: getVariable("processingType")
PersonProcessingDelegate -> PersonProcessingDelegate: process validation logic
PersonProcessingDelegate -> CamundaEngine: setVariable("processingSuccess", true)
CamundaEngine <-- PersonProcessingDelegate: execution complete

== Process Complete ==
CamundaEngine -> CamundaEngine: End process instance
ProcessController <-- CamundaEngine: ProcessInstance
Client <-- ProcessController: JSON Response

@enduml
```

## 6. Data Model ERD

```plantuml
@startuml data-model
!theme plain
title Person Data Model

entity "persons" {
  *id : BIGINT <<PK>>
  --
  *first_name : VARCHAR(50)
  *last_name : VARCHAR(50)
  email : VARCHAR(255) <<UK>>
  phone_number : VARCHAR(20)
  date_of_birth : DATE
  address : VARCHAR(255)
  city : VARCHAR(100)
  country : VARCHAR(100)
  created_at : TIMESTAMP
  updated_at : TIMESTAMP
}

note right of persons
  **Constraints:**
  - id: Primary Key (Auto-generated)
  - email: Unique constraint
  - first_name, last_name: NOT NULL
  - Validation annotations in JPA entity
  
  **Indexes:**
  - Primary key on id
  - Unique index on email
  
  **Lifecycle:**
  - created_at: Set on @PrePersist
  - updated_at: Updated on @PreUpdate
end note

@enduml
```

## 7. Component Deployment Diagram

```plantuml
@startuml deployment-diagram
!theme plain
title Deployment Architecture

node "Development Environment" {
  
  component "Spring Boot Application" {
    port "8080" as http
    
    package "Web Layer" {
      [PersonController]
      [ProcessController]
    }
    
    package "Service Layer" {
      [PersonService]
    }
    
    package "Camunda Components" {
      [PersonValidationDelegate]
      [PersonProcessingDelegate]
      [Camunda BPM Engine]
    }
    
    package "Data Layer" {
      [PersonRepository]
    }
  }
  
  database "H2 Database" {
    [persons table]
    [camunda tables]
  }
}

cloud "External Clients" {
  [REST API Client]
  [Camunda Cockpit]
  [H2 Console]
}

[REST API Client] --> http : HTTP/JSON
[Camunda Cockpit] --> http : Web UI
[H2 Console] --> http : Database Admin

[PersonRepository] --> [persons table] : JPA/Hibernate
[Camunda BPM Engine] --> [camunda tables] : Process Data

@enduml
```

## 8. Use Case Diagram

```plantuml
@startuml use-cases
!theme plain
title Camunda Person API - Use Cases

left to right direction

actor "API Client" as client
actor "Process Administrator" as admin
actor "Business User" as user

rectangle "Person Management System" {
  
  package "Person CRUD Operations" {
    usecase "Create Person" as UC1
    usecase "Get Person by ID" as UC2
    usecase "Get Person by Email" as UC3
    usecase "Update Person" as UC4
    usecase "Delete Person" as UC5
    usecase "Search Persons" as UC6
    usecase "List All Persons" as UC7
  }
  
  package "Process Operations" {
    usecase "Start Person Process" as UC8
    usecase "Monitor Process Status" as UC9
    usecase "View Process Statistics" as UC10
  }
  
  package "Administration" {
    usecase "View Process Definitions" as UC11
    usecase "Monitor Running Instances" as UC12
    usecase "Access Database Console" as UC13
  }
}

client --> UC1
client --> UC2
client --> UC3
client --> UC4
client --> UC5
client --> UC6
client --> UC7
client --> UC8
client --> UC9
client --> UC10

user --> UC8
user --> UC9

admin --> UC11
admin --> UC12
admin --> UC13

@enduml
```

## Usage Instructions

To render these PlantUML diagrams:

1. **In VS Code**: Install the PlantUML extension and use Ctrl+Shift+P → "PlantUML: Preview Current Diagram"
2. **Online**: Copy the diagram code to http://www.plantuml.com/plantuml/
3. **CLI**: Use PlantUML JAR to generate images: `java -jar plantuml.jar documentation.md`

Each diagram provides a different perspective on the system architecture and can be used for:
- **Development**: Understanding code structure and relationships
- **Documentation**: Technical documentation for the project
- **Communication**: Explaining the system to stakeholders
- **Maintenance**: Visual reference for future modifications