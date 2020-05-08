package night.Repositories;

import night.entities.OMDBSearchResult;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OMDBRepository extends MongoRepository<OMDBSearchResult, String> {
   OMDBSearchResult findByQuery(String query);
}