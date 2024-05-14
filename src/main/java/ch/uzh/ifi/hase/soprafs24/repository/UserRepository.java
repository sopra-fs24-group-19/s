package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
  User findByName(String name);

  User findByUsername(String username);

  User findUserById(Long id);

  User findUserByToken(String token);

  User findByToken(String token);

  @Query("SELECT u FROM User u JOIN u.applications t WHERE t.id = :taskId")
  List<User> findUsersByTaskId(Long taskId);

    @Query("SELECT u.id, COUNT(t) FROM User u LEFT JOIN u.assignedTasks t WHERE t.status = 0 GROUP BY u.id ORDER BY COUNT(t) DESC")
    List<Object[]> findUsersWithMostTasksAsHelper();





}
