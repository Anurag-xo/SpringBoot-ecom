package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.CustomRevisionEntity;
import in.anurag.CreatorStore.entities.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductHistory(Long productId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        // Query all revisions for this specific product using Hibernate 6 AuditQuery API
        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(Product.class, false, true)
                .add(AuditEntity.id().eq(productId))
                .addOrder(AuditEntity.property("revisionNumber").desc())
                .getResultList();
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (Object[] result : results) {
            // result[0] is the entity at that revision
            Product productAtRevision = (Product) result[0];
            // result[1] is the custom revision entity (contains username and timestamp)
            CustomRevisionEntity revisionEntity = (CustomRevisionEntity) result[1];
            // result[2] is the RevisionType (ADD, MOD, or DEL)
            RevisionType revisionType = (RevisionType) result[2];
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("revisionId", revisionEntity.getId());
            entry.put("timestamp", revisionEntity.getRevisionDate());
            entry.put("username", revisionEntity.getUsername());
            entry.put("changeType", revisionType.name());
            entry.put("data", productAtRevision);
            
            history.add(entry);
        }
        
        return history;
    }
}
