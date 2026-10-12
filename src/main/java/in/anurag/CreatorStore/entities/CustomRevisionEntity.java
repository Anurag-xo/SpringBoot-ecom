package in.anurag.CreatorStore.entities;

import jakarta.persistence.Entity;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@RevisionEntity(in.anurag.CreatorStore.config.CustomRevisionListener.class)
public class CustomRevisionEntity extends DefaultRevisionEntity {

  private String username;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
