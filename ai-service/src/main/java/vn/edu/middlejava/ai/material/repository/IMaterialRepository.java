package vn.edu.middlejava.ai.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.ai.material.entity.Material;

public interface IMaterialRepository extends JpaRepository<Material, Long> {
}
