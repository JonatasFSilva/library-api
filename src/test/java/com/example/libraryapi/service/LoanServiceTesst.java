package com.example.libraryapi.service;

import com.example.libraryapi.api.dto.LoanFilterDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.model.repositoy.LoanRepository;
import com.example.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    }

    @Test
    @DisplayName("Deve obter as informacoes de um emprestimo pelo id")
    public void getLoanDetailsTest(){
        //CENARIO
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        //EXECUCAO
        Optional<Loan> result = service.getById(id);

        //VERIFICACAO
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);

    }

    @Test
    @DisplayName("Deve atualizar um emprestimo")
    public void updateLoanTest(){
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar emprestimos pelas propriedades")
    public void findLoanTest(){
        //CENARIO
        LoanFilterDTO loanFilterDTO = LoanFilterDTO
                .builder()
                .customer("Fulano")
                .isbn("321")
                .build();

        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        PageRequest pageRequest = PageRequest.of(0,10);
        List<Loan> lista = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(lista,pageRequest , lista.size());
        when(repository.
                findByBookIsbnOrCustomer(
                        Mockito.anyString(),
                        Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(page);
        //EXECUCAO
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);
        //VERIFICACAO
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }


    public static Loan createLoan(){
        //CRIO UM LIVRO (book) COM ID 1
        Book book = Book.builder().id(1L).build();
        //CRIO UMA PESSOA (customer) QUE VAI PEDIR EMPRESTADO O LIVRO
        String customer = "Fulano";

        //CRIO E RETORNO UM EMPRESTIMO (loan) PARA O FULANO (customer) COM UM LIVRO DE ID:1 NA DATA DE HOJE
        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }

}
