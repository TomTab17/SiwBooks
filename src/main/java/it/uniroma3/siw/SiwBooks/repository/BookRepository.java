package it.uniroma3.siw.SiwBooks.repository;

import it.uniroma3.siw.SiwBooks.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorsNameContainingIgnoreCase(@Param("authorName") String authorName);
}