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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //CENARIO
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        when(repository.save(book)).thenReturn(Book.builder()
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
    @DisplayName("Deve lancar erro de negocio ao tentar salvar um livro com isbn duplicado")
    public void shoulNotSaveABookWithIsbnDuplicateISBNTest(){
        //CENARIO
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //EXECUCAO
        Throwable exception = Assertions.catchThrowable(() ->service.save(book));

        //VERIFICACAO
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn ja cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }
    @Test
    @DisplayName("Deve obter um livro por Id")
    public void getByIdTest(){
        Long id = 1L;

        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //EXECUCAO
        Optional<Book> foundBook = service.getById(id);

        //VERIFICACOES
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }
    @Test
    @DisplayName("Deve retornar vazio obter um livro por Id quando ele nao existe na base")
    public void bookNotFoundByIdTest(){
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        //EXECUCAO
        Optional<Book> book = service.getById(id);

        //VERIFICACOES
        assertThat(book.isPresent()).isFalse();
    }
    @Test
    @DisplayName("Deve detelar um livro pelo Id")
    public void deleteBookTest(){
        //CENARIO
        Book book = Book.builder()
                .id(1L)
                .build();
        //EXECUCAO
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book)) ;
        //VERIFICACAO
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }
    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente")
    public void deleteInvalidBookTest(){
        //CENARIO
        Book book = new Book();
        //EXECUCAO
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));
        //VERIFICACAO
        Mockito.verify(repository, Mockito.never()).delete(book);
    }
    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente")
    public void updateInvalidBookTest(){
        //CENARIO
        Book book = new Book();
        //EXECUCAO
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));
        //VERIFICACAO
        Mockito.verify(repository, Mockito.never()).save(book);
    }
    @Test
    @DisplayName("Deve atualizar um livro com sucesso")
    public void updateBookTest(){
        //CENARIO
        Long id = 1L;
        //livro a a atualizar
        Book updatingBook = Book.builder().id(id).build();
        //simulacao do livro
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        when(repository.save(updatingBook)).thenReturn(updatedBook);
        //EXECUCAO
        Book book = service.update(updatingBook);

        //VERIFICACAO
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }
    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        //CENARIO
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0,10);
        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista,pageRequest , 1);
        when(repository.
                findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);
        //EXECUCAO
        Page<Book> result = service.find(book, pageRequest);
        //VERIFICACAO
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbnTest(){
        String isbn = "1230";
        when(repository.findByIsbn(isbn))
                .thenReturn(Optional.of(Book.builder()
                        .id(1L).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);


    }
    private static Book createValidBook() {
        return Book.builder()
                .author("Fulano")
                .title("As Aventuras")
                .isbn("123")
                .build();
    }
}
