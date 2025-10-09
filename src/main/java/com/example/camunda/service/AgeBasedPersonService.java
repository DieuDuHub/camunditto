package com.example.camunda.service;

import com.example.camunda.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les personnes avec routage automatique par âge
 * Base principale (primaryDataSource) : Camunda + Adultes (18+)
 * Base mineurs (minorsDataSource) : Mineurs (-18 ans) uniquement
 */
@Service
public class AgeBasedPersonService {

    private static final Logger logger = LoggerFactory.getLogger(AgeBasedPersonService.class);
    
    @Autowired
    @Qualifier("adultsJdbcTemplate")
    private JdbcTemplate adultsJdbcTemplate;
    
    @Autowired
    @Qualifier("minorsJdbcTemplate")
    private JdbcTemplate minorsJdbcTemplate;

    /**
     * Initialise les tables dans les deux bases de données
     */
    public void initializeTables() {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS persons (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(255) UNIQUE,
                phone_number VARCHAR(20),
                date_of_birth DATE,
                address VARCHAR(255),
                city VARCHAR(100),
                country VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        // Créer la table dans les deux bases
        adultsJdbcTemplate.execute(createTableSql);
        minorsJdbcTemplate.execute(createTableSql);
        
        logger.info("Tables 'persons' created in both databases");
    }

    /**
     * Détermine si une personne est mineure (< 18 ans)
     */
    public boolean isMinor(Person person) {
        if (person.getDateOfBirth() == null) {
            return false;
        }
        return Period.between(person.getDateOfBirth(), LocalDate.now()).getYears() < 18;
    }

    /**
     * Calcule l'âge d'une personne
     */
    public int calculateAge(Person person) {
        if (person.getDateOfBirth() == null) {
            return 18; // Âge par défaut
        }
        return Period.between(person.getDateOfBirth(), LocalDate.now()).getYears();
    }

    /**
     * Sauvegarde une personne dans la bonne base selon son âge
     */
    public Person savePerson(Person person) {
        initializeTables(); // S'assurer que les tables existent
        
        boolean isMinor = isMinor(person);
        JdbcTemplate targetTemplate = isMinor ? minorsJdbcTemplate : adultsJdbcTemplate;
        String database = isMinor ? "MINORS" : "ADULTS";
        
        logger.info("Saving person: {} {} (Age: {}) -> Database: {}", 
                   person.getFirstName(), person.getLastName(), calculateAge(person), database);

        String sql = """
            INSERT INTO persons (first_name, last_name, email, phone_number, date_of_birth, 
                               address, city, country, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

        // Utiliser KeyHolder pour récupérer l'ID généré (compatible H2)
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        targetTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, person.getFirstName());
            ps.setString(2, person.getLastName());
            ps.setString(3, person.getEmail());
            ps.setString(4, person.getPhoneNumber());
            ps.setDate(5, person.getDateOfBirth() != null ? Date.valueOf(person.getDateOfBirth()) : null);
            ps.setString(6, person.getAddress());
            ps.setString(7, person.getCity());
            ps.setString(8, person.getCountry());
            return ps;
        }, keyHolder);

        // Récupérer l'ID généré
        Number key = keyHolder.getKey();
        if (key != null) {
            person.setId(key.longValue());
        }

        return person;
    }

    /**
     * Récupère toutes les personnes des deux bases
     */
    public List<Person> getAllPersons() {
        initializeTables();
        
        List<Person> allPersons = new ArrayList<>();
        
        // Récupérer des adultes
        try {
            List<Person> adults = adultsJdbcTemplate.query(
                    "SELECT * FROM persons", 
                    new BeanPropertyRowMapper<>(Person.class));
            allPersons.addAll(adults);
        } catch (Exception e) {
            logger.warn("Could not retrieve adults: {}", e.getMessage());
        }
        
        // Récupérer des mineurs
        try {
            List<Person> minors = minorsJdbcTemplate.query(
                    "SELECT * FROM persons", 
                    new BeanPropertyRowMapper<>(Person.class));
            allPersons.addAll(minors);
        } catch (Exception e) {
            logger.warn("Could not retrieve minors: {}", e.getMessage());
        }
        
        return allPersons;
    }

    /**
     * Récupère uniquement les adultes
     */
    public List<Person> getAllAdults() {
        initializeTables();
        try {
            return adultsJdbcTemplate.query(
                    "SELECT * FROM persons", 
                    new BeanPropertyRowMapper<>(Person.class));
        } catch (Exception e) {
            logger.warn("Could not retrieve adults: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère uniquement les mineurs
     */
    public List<Person> getAllMinors() {
        initializeTables();
        try {
            return minorsJdbcTemplate.query(
                    "SELECT * FROM persons", 
                    new BeanPropertyRowMapper<>(Person.class));
        } catch (Exception e) {
            logger.warn("Could not retrieve minors: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Recherche une personne par ID dans les deux bases
     */
    public Optional<Person> getPersonById(Long id) {
        initializeTables();
        
        // Chercher dans les adultes d'abord
        try {
            List<Person> adults = adultsJdbcTemplate.query(
                    "SELECT * FROM persons WHERE id = ?", 
                    new BeanPropertyRowMapper<>(Person.class), id);
            if (!adults.isEmpty()) {
                return Optional.of(adults.get(0));
            }
        } catch (Exception e) {
            logger.warn("Error searching adults for ID {}: {}", id, e.getMessage());
        }
        
        // Chercher dans les mineurs
        try {
            List<Person> minors = minorsJdbcTemplate.query(
                    "SELECT * FROM persons WHERE id = ?", 
                    new BeanPropertyRowMapper<>(Person.class), id);
            if (!minors.isEmpty()) {
                return Optional.of(minors.get(0));
            }
        } catch (Exception e) {
            logger.warn("Error searching minors for ID {}: {}", id, e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Recherche une personne par email dans les deux bases
     */
    public Optional<Person> getPersonByEmail(String email) {
        initializeTables();
        
        // Chercher dans les adultes d'abord
        try {
            List<Person> adults = adultsJdbcTemplate.query(
                    "SELECT * FROM persons WHERE email = ?", 
                    new BeanPropertyRowMapper<>(Person.class), email);
            if (!adults.isEmpty()) {
                return Optional.of(adults.get(0));
            }
        } catch (Exception e) {
            logger.warn("Error searching adults for email {}: {}", email, e.getMessage());
        }
        
        // Chercher dans les mineurs
        try {
            List<Person> minors = minorsJdbcTemplate.query(
                    "SELECT * FROM persons WHERE email = ?", 
                    new BeanPropertyRowMapper<>(Person.class), email);
            if (!minors.isEmpty()) {
                return Optional.of(minors.get(0));
            }
        } catch (Exception e) {
            logger.warn("Error searching minors for email {}: {}", email, e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Compte le nombre total de personnes
     */
    public long countPersons() {
        long adultsCount = countAdults();
        long minorsCount = countMinors();
        return adultsCount + minorsCount;
    }

    /**
     * Compte le nombre d'adultes
     */
    public long countAdults() {
        initializeTables();
        try {
            Long count = adultsJdbcTemplate.queryForObject("SELECT COUNT(*) FROM persons", Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.warn("Error counting adults: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Compte le nombre de mineurs
     */
    public long countMinors() {
        initializeTables();
        try {
            Long count = minorsJdbcTemplate.queryForObject("SELECT COUNT(*) FROM persons", Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.warn("Error counting minors: {}", e.getMessage());
            return 0;
        }
    }
}