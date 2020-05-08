package night.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.model.Event;
import night.Repositories.GoogleTokenRepository;
import night.Services.MyGoogleService;
import night.entities.GoogleToken;
import night.entities.MovieEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    MyGoogleService myGoogleService;
    @Autowired
    GoogleTokenRepository googleTokenRepository;

    @PostMapping("/create_event")
    private ResponseEntity<?> createEvent(@RequestBody MovieEvent movieEvent) {
        if (movieEvent == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to retrieve event.");

        List<GoogleToken> tokens = googleTokenRepository.findByUserIdIn(movieEvent.getUsersToInvite());
        if (tokens == null || tokens.size() < movieEvent.getUsersToInvite().size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to retrieve user/s.");

        movieEvent.setUsersToInvite(tokens.stream().map(GoogleToken::getGmail).collect(Collectors.toList()));
        Event createdEvent = myGoogleService.createCalendarEvent(movieEvent);
        if (createdEvent == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create event.");
        return new ResponseEntity<>("Event created!", HttpStatus.OK);
    }

    @GetMapping
    private ResponseEntity<List<Date>> getMovieTime(@RequestParam String ids) {
        String decodedString;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            decodedString = URLDecoder.decode(ids, StandardCharsets.UTF_8);
            List<String> idsAsList = objectMapper.readValue(decodedString, List.class);
            List<Date> movieDates = myGoogleService.findMovieTime(idsAsList);
            return new ResponseEntity<>(movieDates, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
