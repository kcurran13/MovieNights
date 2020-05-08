package night.Repositories;

import night.entities.GoogleToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GoogleTokenRepository extends MongoRepository<GoogleToken, String> {
    List<GoogleToken> findByUserIdIn(List<String> userIds);
}
