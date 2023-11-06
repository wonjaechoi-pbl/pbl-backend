package com.gogofnd.kb.domain.seller.repository;

import com.gogofnd.kb.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller,Long> {
    Seller findSellerBySellerCode(String sellerCode);
}
