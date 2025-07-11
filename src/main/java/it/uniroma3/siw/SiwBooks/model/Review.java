package it.uniroma3.siw.SiwBooks.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il titolo della recensione è obbligatorio")
    private String title;

    @NotNull(message = "La valutazione è obbligatoria")
    @Min(value = 1, message = "La valutazione deve essere almeno 1 stella")
    @Max(value = 5, message = "La valutazione non può superare le 5 stelle")
    private Integer rating;

    @NotBlank(message = "Il testo della recensione è obbligatorio")
    @Column(length = 2000)
    private String text;

    @ManyToOne
    private Book book;

    @ManyToOne
    private User user;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}