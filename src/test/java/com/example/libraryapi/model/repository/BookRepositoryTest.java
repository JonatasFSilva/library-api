package com.example.libraryapi.model.repository;

import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.repositoy.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    BookRepository repository;
    @Test
    @DisplayName("deve retornar verdadeiro quando existir o livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        //CENARIO
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);
        //EXECUCAO
        boolean exists = repository.existsByIsbn(isbn);
        //VERIFICACAO
        assertThat(exists).isTrue();
    }
    @Test
    @DisplayName("deve retornar falso quando nao existir o livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesntExists(){
        //CENARIO
        String isbn = "123";
        //EXECUCAO
        boolean exists = repository.existsByIsbn(isbn);
        //VERIFICACAO
        assertThat(exists).isFalse();
    }
    @Test
    @DisplayName("Deve obter um livro pelo id")
    public void findByIdTest(){
        //CENARIO
        Book book = createNewBook("123");
        entityManager.persist(book);
        //EXECUCAO
        Optional<Book> foundBook = repository.findById(book.getId());
        //VERIFICACAO
        assertThat(foundBook.isPresent()).isTrue();
    }
    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //CENARIO
        Book book = createNewBook("123");
        //EXECUCAO
        Book savedBook = repository.save(book);
        //VERIFICACAO
        assertThat(savedBook.getId()).isNotNull();
    }
    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        //CENARIO
        Book book = createNewBook("123");
        entityManager.persist(book);
        //EXECUTCAO
        Book foundBook = entityManager.find(Book.class, book.getId());
        repository.delete(foundBook);
        //VERIFICACAO
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }

    private static Book createNewBook(String isbn) {
        return Book.builder()
                .author("Fulano")
                .title("As Aventuras")
                .isbn(isbn).build();
    }
}
