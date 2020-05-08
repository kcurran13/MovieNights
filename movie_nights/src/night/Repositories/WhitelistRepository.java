package night.Repositories;

import night.security.entities.Whitelist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WhitelistRepository extends MongoRepository<Whitelist,String> {
    Whitelist findDistinctFirstByToken(String token);
    void deleteByToken(String token);
}
