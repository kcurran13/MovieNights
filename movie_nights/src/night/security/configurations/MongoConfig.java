package night.security.configurations;

import night.security.entities.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import javax.annotation.PostConstruct;

@Configuration
public class MongoConfig {

    @Autowired
    MongoTemplate mongoTemplate;

    @Value("${jwt.refresh.expiration.seconds}")
    private int REFRESH_TOKEN_EXPIRATION;

    @PostConstruct
    public void initIndexes(){
        mongoTemplate.indexOps(Whitelist.class).dropAllIndexes();
        mongoTemplate.indexOps(Whitelist.class).ensureIndex(new Index().on("createdAt",Sort.Direction.ASC).expire(REFRESH_TOKEN_EXPIRATION));
    }
}
