package com.example.deliverydemoapi.data.repository;

import com.example.deliverydemoapi.data.entity.RegionalBaseFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionalBaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {
}
