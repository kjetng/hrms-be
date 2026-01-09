package org.httt2.hrms.auth.repository;

import org.httt2.hrms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmpId(Long empId); // Tìm user theo empId
}
