package it.uniroma3.siw.SiwBooks.service;

import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    // Metodo per recuperare gli ultimi n libri ordinati per id discendente
    public List<Book> findLatestBooks(int n) {
        Pageable pageable = PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "id"));
        return bookRepository.findAll(pageable).getContent();
    }

    // Metodo di ricerca per titolo
    public List<Book> searchBooksByTitle(String query) {
        return bookRepository.findByTitleContainingIgnoreCase(query);
    }

    // Metodo di ricerca per autore
    public List<Book> searchBooksByAuthor(String query) {
        return bookRepository.findByAuthorsNameContainingIgnoreCase(query);
    }
}