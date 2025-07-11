package it.uniroma3.siw.SiwBooks.repository;

import it.uniroma3.siw.SiwBooks.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}

