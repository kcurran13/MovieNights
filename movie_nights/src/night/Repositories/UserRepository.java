package night.Repositories;

import night.entities.DbUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<DbUser, String> {
    DbUser findDistinctFirstByUsernameIgnoreCase(String username);
    DbUser findDistinctById(String id);
    Optional<DbUser> findByUsername(String username);
}