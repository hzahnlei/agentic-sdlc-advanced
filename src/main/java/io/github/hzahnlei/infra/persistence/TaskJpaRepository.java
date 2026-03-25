package io.github.hzahnlei.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long> {

    @Query("SELECT t.status, COUNT(t) FROM TaskJpaEntity t GROUP BY t.status")
    List<Object[]> countGroupByStatus();
}
