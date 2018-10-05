package io.silverstring.core.repository.hibernate;

import io.silverstring.domain.hibernate.AdminWalletHistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminWalletHistoryLogRepository extends JpaRepository<AdminWalletHistoryLog, Long> {
}
