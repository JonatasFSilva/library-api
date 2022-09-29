package com.example.libraryapi.service;

import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.service.impl.LoanRepository;
import com.example.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTesst {

    @MockBean
    LoanRepository repository;
    LoanService service;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoanTest(){
        //CRIO UM LIVRO (book) COM ID 1
        Book book = Book.builder().id(1L).build();
        //CRIO UMA PESSOA (customer) QUE VAI PEDIR EMPRESTADO O LIVRO
        String customer = "Fulano";

        //CRIO UM EMPRESTIMO (loan) PARA O FULANO (customer) COM UM LIVRO DE ID:1 NA DATA DE HOJE
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        //SALVO ESSE MESMO EMPRESTIMO(loan) DENTRO DE UMA VARIAVEL (savedLoan) COM UMA ID:1 DE EMPRESTIMO
        Loan savedLoan = Loan.builder().id(1L).customer(customer).book(book).build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        //COM ESSE EMPRESTIMO (savedLoan) FEITO ENVIO PARA O REPOSITORY SALVAR (save) UM DADO DO TIPO EMPRESTIMO (loan)
        when(repository.save(savingLoan)).thenReturn(savedLoan);

        //CHAMA O METODO DE SALVAR(save) DO SERVICE PASSANDO O EMPRESTIMO CRIADO E SALVA NA VERIAVEL loan
        Loan loan = service.save(savingLoan);

        //VERIFICA SE O ID DO saveLoan É IGUAL AO ID DO loan
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        //VERIFICA SE O LIVRO DO saveLoan É IGUAL AO LIVRO DO loan
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        //VERIFICA SE O CUSTOMER DO saveLoan É IGUAL AO CUSTOMER DO loan
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        //VERIFICA SE O A DATA DO EMPRESTIMO DO saveLoan É IGUAL A DATA DO EMPRESTIMO DO loan
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lancar erro de negocio ao tentar salvar um emprestimo com livro ja emprestado")
    public void loanedBookSaveTest(){
        //CRIO UM LIVRO (book) COM ID 1
        Book book = Book.builder().id(1L).build();
        //CRIO UMA PESSOA (customer) QUE VAI PEDIR EMPRESTADO O LIVRO
        String customer = "Fulano";

        //CRIO UM EMPRESTIMO (loan) PARA O FULANO (customer) COM UM LIVRO DE ID:1 NA DATA DE HOJE
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);

    }}
