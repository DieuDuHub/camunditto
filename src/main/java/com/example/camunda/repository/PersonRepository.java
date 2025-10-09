package com.example.camunda.repository;

import com.example.camunda.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Optional<Person> findByEmail(String email);
    
    List<Person> findByFirstNameIgnoreCaseContaining(String firstName);
    
    List<Person> findByLastNameIgnoreCaseContaining(String lastName);
    
    @Query("SELECT p FROM Person p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Person> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    List<Person> findByCity(String city);
    
    List<Person> findByCountry(String country);
    
    @Query("SELECT COUNT(p) FROM Person p")
    long countAllPersons();
}