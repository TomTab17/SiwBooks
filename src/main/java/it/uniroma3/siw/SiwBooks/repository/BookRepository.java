package it.uniroma3.siw.SiwBooks.repository;

import it.uniroma3.siw.SiwBooks.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}

