package night.controllers;

import night.Repositories.OMDBRepository;
import night.entities.OMDBSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/movies/")
public class MovieController {

    @Autowired
    private OMDBRepository OMDBRepo;

    @GetMapping("/search/{params}")
    ResponseEntity getMovies(@PathVariable String params) {
        OMDBSearchResult result = OMDBRepo.findByQuery(params);
        RestTemplate restTemplate = new RestTemplate();

        if (result == null) {
            result = restTemplate.getForObject("http://www.omdbapi.com/?apikey=af87eef1&s=" + params, OMDBSearchResult.class);
            result.setQuery(params);
            OMDBRepo.save(result);
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }
}

