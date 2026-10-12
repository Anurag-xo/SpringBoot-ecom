package in.anurag.CreatorStore.config;

import in.anurag.CreatorStore.entities.CustomRevisionEntity;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

  @Override
  public void newRevision(Object revisionEntity) {
    CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal())) {
      customRevisionEntity.setUsername(authentication.getName());
    } else {
      customRevisionEntity.setUsername("SYSTEM");
    }
  }
}
