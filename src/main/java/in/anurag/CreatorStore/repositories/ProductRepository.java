package in.anurag.CreatorStore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import in.anurag.CreatorStore.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
}
