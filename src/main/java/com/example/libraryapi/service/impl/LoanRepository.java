package com.example.libraryapi.service.impl;

import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByBookAndNotReturned(Book book);
}
