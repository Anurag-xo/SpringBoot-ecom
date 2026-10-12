package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.CustomRevisionEntity;
import in.anurag.CreatorStore.entities.Product;
import jakarta.persistence.EntityManager;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final EntityManager entityManager;

  @Transactional(readOnly = true)
  public List<Map<String, Object>> getProductHistory(Long productId) {
    AuditReader auditReader = AuditReaderFactory.get(entityManager);

    // Get all revision numbers for this specific product ID
    List<Number> revisions = auditReader.getRevisions(Product.class, productId);

    List<Map<String, Object>> history = new ArrayList<>();

    for (Number revision : revisions) {
      // Fetch the state of the product at this specific revision
      Product productAtRevision = auditReader.find(Product.class, productId, revision);

      // Fetch the metadata about the revision (who made it and when)
      CustomRevisionEntity revisionEntity =
          auditReader.findRevision(CustomRevisionEntity.class, revision);

      // Get the type of change (ADD, MOD, DEL). Cast productId to Object to match Hibernate 6 API
      RevisionType revisionType =
          auditReader.getRevisionType(Product.class, (Object) productId, revision);

      Map<String, Object> entry = new HashMap<>();
      entry.put("revisionId", revisionEntity.getId());
      entry.put("timestamp", revisionEntity.getRevisionDate());
      entry.put("username", revisionEntity.getUsername());
      entry.put("changeType", revisionType.name()); // "ADD", "MOD", or "DEL"
      entry.put("data", productAtRevision); // The actual state of the product at this point in time

      history.add(entry);
    }

    // Sort by revision ID descending (newest changes first)
    history.sort(
        (a, b) -> Integer.compare((Integer) b.get("revisionId"), (Integer) a.get("revisionId")));

    return history;
  }
}
