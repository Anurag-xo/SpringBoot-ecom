package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.*;
import jakarta.persistence.EntityManager;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {
  private final EntityManager entityManager;

  // ✅ REFACTORED: Generic method to fetch history for ANY audited entity
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getEntityHistory(Class<?> entityClass, Long entityId) {
    AuditReader auditReader = AuditReaderFactory.get(entityManager);

    List<Object[]> results =
        auditReader
            .createQuery()
            .forRevisionsOfEntity(entityClass, false, true)
            .add(AuditEntity.id().eq(entityId))
            .addOrder(AuditEntity.property("revisionNumber").desc())
            .getResultList();

    List<Map<String, Object>> history = new ArrayList<>();
    for (Object[] result : results) {
      Object entityAtRevision = result[0];
      CustomRevisionEntity revisionEntity = (CustomRevisionEntity) result[1];
      RevisionType revisionType = (RevisionType) result[2];

      Map<String, Object> entry = new HashMap<>();
      entry.put("revisionId", revisionEntity.getId());
      entry.put("timestamp", revisionEntity.getRevisionDate());
      entry.put("username", revisionEntity.getUsername());
      entry.put("changeType", revisionType.name());
      entry.put("data", entityAtRevision);
      history.add(entry);
    }
    return history;
  }
}
