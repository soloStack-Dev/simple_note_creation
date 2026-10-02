package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.ErrorEnquiry;

public interface ErrorEnquiryRepository extends JpaRepository<ErrorEnquiry, Long> {

    List<ErrorEnquiry> findTop20ByOrderByCreatedAtDesc();
}
