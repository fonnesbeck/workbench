package org.pmiops.workbench.db.dao;

import java.util.List;
import java.util.Set;

import org.pmiops.workbench.db.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserDao extends CrudRepository<User, Long> {

  User findUserByEmail(String email);
  User findUserByUserId(long userId);

  /**
   * We know that there is no unique constraint in the DB currently,
   * it is only enforced in code, but we intend to support multiple
   * accounts for the same contact email at some point in the future.
   */
  List<User> findUserByContactEmail(String contactEmail);


  /**
   * Returns the users who's identities have not been validated by BlockScore
   */
  @Query("SELECT user FROM User user WHERE user.blockscoreVerificationIsValid IS NULL OR user.blockscoreVerificationIsValid = false")
  List<User> findUserNotValidated();

  /**
   * Returns the user with their authorities loaded.
   */
  @Query("SELECT user FROM User user LEFT JOIN FETCH user.authorities WHERE user.userId = :id")
  User findUserWithAuthorities(@Param("id") long id);

  @Query("SELECT DISTINCT user.freeTierBillingProjectName FROM User user\n" +
      "WHERE user.freeTierBillingProjectName IS NOT NULL")
  Set<String> getAllUserProjects();
}
