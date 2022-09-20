package com.example.libraryapi.service;

import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.repositoy.BookRepository;
import com.example.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)/* CRIA UM MINI CONTEXTO (DE INJECAO DE DEPENDENCIA COM A CLASSES QUE SERAO DEFINIDAS )PARA RODAR O TESTE*/
@ActiveProfiles("test")/* RODA OS TESTE COM PERFIL DE TEST, PODENDO REALIZAR ALGUMAS CONFIGURACOES QUE VAO RODAR APENAS NO AMBIENTE DE TESTE*/
public class BookServiceTest {

    BookService service;
    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("deve salvar um livro")
    public void saveBookTest(){
        //CENARIO
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        Mockito.when(repository.save(book)).thenReturn(Book.builder()
                .id(1L)
                .author("Fulano")
                .title("As Aventuras")
                .isbn("123")
                .build());

        //EXECUCAO
        Book savedBook = service.save(book);

        //VERIFICACAO
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As Aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

    }

    @Test
    @DisplayName("deve lancar erro de negocio ao tentar salvar um livro com isbn duplicado")
    public void shoulNotSaveABookWithIsbnDuplicateISBN(){
        //CENARIO
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //EXECUCAO
        Throwable exception = Assertions.catchThrowable(() ->service.save(book));

        //VERIFICACAO
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn ja cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private static Book createValidBook() {
        return Book.builder()
                .author("Fulano")
                .title("As Aventuras")
                .isbn("123")
                .build();
    }
}
