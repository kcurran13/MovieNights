package night.Services;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import night.Repositories.GoogleTokenRepository;
import night.entities.GoogleToken;
import night.entities.MovieEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MyGoogleService {

    @Autowired
    private GoogleTokenRepository repo;
    final private String CLIENT_ID = "522663004580-2953u1b8ieso5htlegbe57prqdp336ab.apps.googleusercontent.com";
    final private String CLIENT_SECRET = "vz6gmR7a1j0mv5B-0wAA0_Oj";

    public String storeauthcode(String code, String userId) {
        GoogleTokenResponse tokenResponse = null;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://www.googleapis.com/oauth2/v4/token",
                    CLIENT_ID, CLIENT_SECRET,
                    code,
                    "http://localhost:3000") // Make sure you set the correct port
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }

        // Store these 3 in your DB
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        Long expiresAt = System.currentTimeMillis() + (tokenResponse.getExpiresInSeconds() * 1000);

        GoogleIdToken idToken = null;
        try {
            idToken = tokenResponse.parseIdToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GoogleIdToken.Payload payload = idToken.getPayload();

        // Use THIS ID as a key to identify a google user-account.
        String googleUserId = payload.getSubject();
        String googleEmail = payload.getEmail();

        return addToDb(userId, accessToken, refreshToken, expiresAt, googleUserId, googleEmail);
    }

    public List<GoogleToken> getAllGoogleTokens() {
        return repo.findAll();
    }

    private List<GoogleToken> getAllUsersGoogleTokens(List<String> users) {
        return repo.findByUserIdIn(users);
    }

    public List<GoogleToken> getGoogleTokens(List<String> usersId) {
        List<GoogleToken> allTokens = getAllUsersGoogleTokens(usersId);

        if (allTokens == null || allTokens.size() == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Information not found.");
        List<GoogleToken> tokensToRefresh = getTokensToRefresh(allTokens);
        if (tokensToRefresh.size() > 0) {
            List<GoogleToken> refreshedTokens = refreshCredentials(tokensToRefresh);
            if (refreshedTokens == null)
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to refresh tokens.");
            for (int i = 0; i < allTokens.size(); i++) {
                for (int f = 0; i < refreshedTokens.size(); i++) {
                    if (allTokens.get(i).getUserId() == refreshedTokens.get(f).getUserId()) {
                        allTokens.set(i, refreshedTokens.get(f));
                    }
                }
            }
        }
        return allTokens;
    }

    private List<GoogleToken> getTokensToRefresh(List<GoogleToken> googleTokens) {
        List<GoogleToken> tokensToRefresh = new ArrayList<>();
        for (GoogleToken googleToken : googleTokens) {
            if (new Date(googleToken.getExpiresAt()).before(new Date(System.currentTimeMillis()))) {
                tokensToRefresh.add(googleToken);
            }
        }
        return tokensToRefresh;
    }

    private List<GoogleToken> refreshCredentials(List<GoogleToken> tokensToRefresh) {
        List<GoogleToken> refreshedTokens = new ArrayList<>();

        try {
            for (GoogleToken toRefresh : tokensToRefresh) {
                GoogleTokenResponse googleCredential = getRefreshedCredentials(toRefresh.getRefreshToken());
                if (googleCredential == null) {
                    continue;
                }
                String accessToken = googleCredential.getAccessToken();
                long expiresAt = System.currentTimeMillis() + (googleCredential.getExpiresInSeconds() * 1000);
                toRefresh.setAccessToken(accessToken);
                toRefresh.setExpiresAt(expiresAt);
                refreshedTokens.add(repo.save(toRefresh));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return refreshedTokens;
    }

    private GoogleTokenResponse getRefreshedCredentials(String refreshCode) {
        try {
            return new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance(), refreshCode, CLIENT_ID, CLIENT_SECRET)
                    .execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    String addToDb(String userId, String accessToken, String refreshToken, Long expiresAt, String googleUserId, String googleEmail) {
        GoogleToken newToken = new GoogleToken(userId, accessToken, refreshToken, expiresAt, googleUserId, googleEmail);
        try {
            repo.save(newToken);
            return "OK";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ERROR";
        }
    }

    public List<Date> findMovieTime(List<String> usersIds) {
        List<GoogleToken> tokens = getGoogleTokens(usersIds);
        List<Event> allEvents = getAllEventsSorted(tokens);
        List<Date> movieDates = new ArrayList<>();

        for (int i = 0; i < allEvents.size() - 1; i++) {
            Date eventEnd = getDateObject(i, allEvents, false);
            Date nextEventStart = getDateObject(i + 1, allEvents, true);
            Date nextEventEnd = getDateObject(i + 1, allEvents, false);

            boolean isOverlapping = nextEventStart.before(eventEnd);
            if (isOverlapping) {
                boolean secondEventEndsFirst = nextEventEnd.before(eventEnd);
                if (secondEventEndsFirst) {
                    allEvents.remove(i + 1);
                    i--;
                }
            } else {
                if (checkSlotLength(eventEnd, nextEventStart)) {
                    movieDates.add(eventEnd);

                    List<Date> freeSlots = getFreeSlots(eventEnd, nextEventStart);
                    if (freeSlots.size() > 0) {
                        movieDates.addAll(freeSlots);
                    }
                }
            }
        }
        return movieDates;
    }

    private List<Event> getAllEventsSorted(List<GoogleToken> tokens) {
        List<Event> allEvents = new ArrayList<>();
        for (GoogleToken token : tokens) {
            List<Event> userEvents = getEvents(token);
            if (userEvents != null && !userEvents.isEmpty()) {
                allEvents.addAll(userEvents);
            }
        }

        for (Event event : allEvents) {
            if (event.getStart().getDateTime() == null) {
                DateTime startOfDay = new DateTime(event.getStart().getDate().getValue());
                event.getStart().setDateTime(startOfDay);

                DateTime endOfDay = new DateTime(event.getEnd().getDate().getValue());
                event.getEnd().setDateTime(endOfDay);
            }
        }
        allEvents.sort(Comparator.comparingInt(a -> (int) a.getStart().getDateTime().getValue()));
        return allEvents;
    }

    private List<Event> getEvents(GoogleToken token) {
        GoogleCredentials credential = GoogleCredentials.create(new AccessToken(token.getAccessToken(), new Date(token.getExpiresAt())));
        HttpRequestInitializer initializer = new HttpCredentialsAdapter(credential);
        Calendar calendar =
                new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), initializer)
                        .setApplicationName("Movie Nights")
                        .build();

        DateTime currTime = new DateTime(System.currentTimeMillis());
        DateTime twoWeeksFromNow = new DateTime(System.currentTimeMillis() + 604800000);
        Events events;

        try {
            events = calendar.events().list("primary")
                    .setTimeMin(currTime)
                    .setTimeMax(twoWeeksFromNow)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();
            if (items.isEmpty()) {
                return null;
            } else {
                return items;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Date> getFreeSlots(Date movieStart, Date nextEventStart) {
        List<Date> freeSlots = new ArrayList<>();
        boolean isFreeInOneHour = checkPlusOneHour(movieStart, nextEventStart);

        while (isFreeInOneHour) {
            movieStart = new Date(movieStart.getTime() + TimeUnit.HOURS.toMillis(1));
            isFreeInOneHour = checkPlusOneHour(movieStart, nextEventStart);
            if (isFreeInOneHour) {
                freeSlots.add(movieStart);
            }
        }
        return freeSlots;
    }

    private boolean checkSlotLength(Date eventEnd, Date nextEventStart) {
        long diffInMill = Math.abs(nextEventStart.getTime() - eventEnd.getTime());
        long diff = TimeUnit.MINUTES.convert(diffInMill, TimeUnit.MILLISECONDS);
        return diff >= 180;
    }


    private Date getDateObject(int i, List<Event> allEvents, boolean isStartDate) {
        Date date;
        if (isStartDate) {
            DateTime dateTime = allEvents.get(i).getStart().getDateTime();
            if (dateTime == null) {
                dateTime = allEvents.get(i).getStart().getDate();
                date = new Date(dateTime.getValue());
                date = getSetDate(0, 01, date);

            } else {
                date = new Date(dateTime.getValue());
            }
        } else {
            DateTime dateTime = allEvents.get(i).getEnd().getDateTime();

            if (dateTime == null) {
                dateTime = allEvents.get(i).getEnd().getDate();
                date = new Date(dateTime.getValue());
                date = getSetDate(23, 59, date);
            } else {
                date = new Date(dateTime.getValue());
            }
        }
        return date;
    }

    private boolean checkPlusOneHour(Date movieStart, Date nextEventStart) {
        Date plusOneHour = new Date(movieStart.getTime() + TimeUnit.HOURS.toMillis(1));
        return checkSlotLength(plusOneHour, nextEventStart);
    }

    private Date getSetDate(int hour, int min, Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, min);
        return calendar.getTime();
    }

    public Calendar getCalendar(String userId) {
        List<GoogleToken> tokens = getGoogleTokens(List.of(userId));
        GoogleToken token = tokens.get(0);
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(token.getAccessToken(), new Date(token.getExpiresAt())));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        return new com.google.api.services.calendar.Calendar.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                requestInitializer).setApplicationName("Movie Nights")
                .build();
    }

    public Event createCalendarEvent(MovieEvent movieEvent) {
        Calendar creatorCalendar = getCalendar(movieEvent.getCreatorId());
        Event event = new Event()
                .setSummary(movieEvent.getTitle())
                .setLocation(movieEvent.getLocation())
                .setDescription(movieEvent.getDescription());

        EventDateTime start = new EventDateTime()
                .setDateTime(movieEvent.getStartDate())
                .setTimeZone("Europe/Stockholm");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(movieEvent.getEndDate())
                .setTimeZone("Europe/Stockholm");
        event.setEnd(end);

        EventAttendee[] attendees = movieEvent.getUsersToInvite().stream().map(userMail -> new EventAttendee().setEmail(userMail)).toArray(EventAttendee[]::new);

        event.setAttendees(Arrays.asList(attendees));

        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";
        try {
            return creatorCalendar.events().insert(calendarId, event).setSendNotifications(true).execute();
        } catch (Exception err) {
            return null;
        }
    }
}