package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)/* CRIA UM MINI CONTEXTO (DE INJECAO DE DEPENDENCIA COM A CLASSES QUE SERAO DEFINIDAS )PARA RODAR O TESTE*/
@ActiveProfiles("test")/* RODA OS TESTE COM PERFIL DE TEST, PODENDO REALIZAR ALGUMAS CONFIGURACOES QUE VAO RODAR APENAS NO AMBIENTE DE TESTE*/
@WebMvcTest/*TESTES UNITARIOS TESTE APENAS O COMPORTAMENTO DA API DOS METODOS*/
@AutoConfigureMockMvc/*CONFIGURA O OBJETO PARA QUE POSSAMOS REALIZAR AS REQUISICOES*/
public class BookControllerTest {

    /*DEFINICAO DA ROTA DE DESIGN DA API*/
    static String BOOK_API = "/api/books";
    @Autowired /* INJETA UMA DEPENDENCIA DO SPRING*/
    MockMvc mvc; /*OBJETO VAI MOKAR AS REQUISICOES SIMULANDO UMA REQUISACAO PARA A API*/
    @MockBean
    BookService service;
    @Test/*ANNOTATION PARA DEFINIR UM TESTE*/
    @DisplayName("deve criar um livro com sucesso.")/* ANNOTATION DO JUNIT5 QUE CRIA UMA DEFINICAO PARA OS TESTE*/
    public void createBookTest()throws Exception{

        /*CENARIO*/
        BookDTO dto = createNewBook();

        Book savedBook = Book.builder()
                .id(10L)
                .author("Artur")
                .title("As Aventuras")
                .isbn("001")
                .build();
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        /*MONTAGEM DA REQUEST POST COM O JSON*/
        //POST = CREATE
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json /*PASSA O CORPO DA REQUISICAO*/);

        /*REALIZAR A REQUISICAO*/
        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }
    @Test/*ANNOTATION PARA DEFINIR UM TESTE*/
    @DisplayName("deve lancar erro de validacao quando nao houver dados suficientes para criacao do livro.")/* ANNOTATION DO JUNIT5 QUE CRIA UMA DEFINICAO PARA OS TESTE*/
    public void createInvalidBookTest() throws Exception{

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        /*MONTAGEM DA REQUEST POST COM O JSON VAZIO*/
        //POST = POST
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json /*PASSA O CORPO DA REQUISICAO*/);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors" , hasSize(3)));
    }
    @Test
    @DisplayName("deve lancar erro ao tentar cadastrar um livro com isbn ja utilizado por outro")
    public void createBookWithDuplicateIsbn()throws Exception{

        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String mensagemErro = "Isbn ja cadastrado.";
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemErro));

        /*MONTAGEM DA REQUEST POST COM O JSON VAZIO*/
        //POST = POST
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json /*PASSA O CORPO DA REQUISICAO*/);

        mvc.perform(request).andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));
    }
    @Test
    @DisplayName("Deve obter informacoes do livro")
    public void bookDetailsTest() throws Exception{
        //CENARIO (given)
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //EXECUCAO (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        //VERIFICACAO
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
    }
    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado nao exitir")
    public void bookNotFoundTest() throws Exception{

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest()throws Exception{

        BDDMockito.given(service
                .getById(anyLong()))
                .willReturn(Optional.of(Book.builder()
                        .id(1L)
                        .build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc.perform(request)
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("Deve retornar resource not found quando nao encontrar o livro para deletar")
    public void deleteNonexistentBookTest()throws Exception{

        BDDMockito.given(service
                        .getById(anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest() throws Exception{
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder()
                .id(id)
                .author("some author")
                .title("some title")
                .isbn("321")
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

        Book updatedBook = Book
                .builder()
                .id(id)
                .author("Artur")
                .title("As Aventuras")
                .isbn("321")
                .build();

        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value("321"));
    }
    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente.")
    public void updateNonexistentBookTest() throws Exception{
       String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception{
        //CENARIO
        Long id = 1L;
        Book book = Book.builder()
                .id(id).author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,100),1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        //EXECUCAO
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1))) // REGISTROS
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0))
        ;

    }

    private BookDTO createNewBook() {
        return BookDTO
                .builder()
                .author("Artur")
                .title("As Aventuras")
                .isbn("001")
                .build();
    }
}
