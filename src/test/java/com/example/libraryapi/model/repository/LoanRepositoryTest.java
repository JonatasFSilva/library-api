package com.example.libraryapi.model.repository;

import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.model.repositoy.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.example.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository repository;

    @Test
    @DisplayName("Deve verificar se existe emprestimo não devolvido para o livro")
    public void existsByBookAndNotReturnedTest(){
        //CENARIO
        Loan loan = createAndPersistLoan();
        Book book = loan.getBook();

        //EXECUCAO
        boolean exists =  repository.existsByBookAndNotReturned(book);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar emprestimo pelo ibns do livro ou pelo cusrtomer")
    public void findByBookIsbnOrCustomerTest(){
        //CENARIO
        Loan loan = createAndPersistLoan();

        Page<Loan> result =repository.findByBookIsbnOrCustomer(
                        "123",
                        "Fulano",
                        PageRequest.of(0,10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    public Loan createAndPersistLoan(){
        Book book = createNewBook("123");
        entityManager.persist(book);
        Loan loan = Loan.builder()
                .book(book)
                .customer("Fulano")
                .loanDate(LocalDate.now())
                .build();
        entityManager.persist(loan);

        return loan;
    }
}
