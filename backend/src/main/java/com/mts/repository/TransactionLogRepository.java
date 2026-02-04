package com.mts.repository;

import com.mts.domain.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TransactionLog entity operations.
 */
@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {

    /**
     * Find transaction by idempotency key.
     * 
     * @param idempotencyKey The unique idempotency key
     * @return Optional containing the transaction if found
     */
    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all transactions where account is either source or destination.
     * 
     * @param fromAccountId Source account ID
     * @param toAccountId   Destination account ID
     * @return List of transactions involving the account
     */
    List<TransactionLog> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);
}
