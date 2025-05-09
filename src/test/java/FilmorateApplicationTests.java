
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, GenreDbStorage.class, FilmDbStorage.class, MpaDbStorage.class})
@ContextConfiguration(classes = {UserDbStorage.class, GenreDbStorage.class, FilmDbStorage.class, MpaDbStorage.class, FilmorateConfig.class})
class FilmorateApplicationTests {

	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;
	private final GenreDbStorage genreStorage;
	private final MpaDbStorage mpaStorage;

	@BeforeEach
	void setUp() {
		filmStorage.deleteAllFilms();
		userStorage.deleteAllUsers();

		// Добавляем пользователей
		userStorage.addUser(new User(1, "john@example.com","john_doe", "John Doe", LocalDate.of(1990, 1, 15)));
		userStorage.addUser(new User(2, "jane@example.com","jane_smith", "Jane Smith",  LocalDate.of(1985, 6, 30)));
		userStorage.addUser(new User(3, "michael@example.com","michael_b", "Michael Brown",  LocalDate.of(1992, 11, 22)));

		// Добавляем фильмы
		Film inception = new Film(1, "Inception", "A mind-bending thriller by Christopher Nolan.",
				LocalDate.of(2010, 7, 16), 148, new HashSet<>(), mpaStorage.getMpaById(4), new LinkedHashSet<>());
		Film notebook = new Film(2, "The Notebook", "A romantic drama based on the novel by Nicholas Sparks.",
				LocalDate.of(2004, 6, 25), 123, new HashSet<>(), mpaStorage.getMpaById(2), new LinkedHashSet<>());
		Film interstellar = new Film(3, "Interstellar", "A space exploration drama.",
				LocalDate.of(2014, 11, 7), 169, new HashSet<>(), mpaStorage.getMpaById(4), new LinkedHashSet<>());

		filmStorage.addFilm(inception);
		filmStorage.addFilm(notebook);
		filmStorage.addFilm(interstellar);

		int id = userStorage.getAllUsers().getFirst().getId();
		userStorage.addFriend(id, id+1);
	}

	@Test
	void testGetAllUsers() {
		List<User> users = userStorage.getAllUsers();

		assertThat(users).hasSize(3);
		assertThat(users).extracting(User::getName)
				.containsExactlyInAnyOrder("John Doe", "Jane Smith", "Michael Brown");
	}

	@Test
	public void testGetUserById() {

		int id = userStorage.getAllUsers().getFirst().getId();
		Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(id));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	void testGetUserFriends() {
		int id = userStorage.getAllUsers().getFirst().getId();
		Set<Integer> friends = userStorage.getFriends(id);
		assertThat(friends).hasSize(1);
	}

	@Test
	void testAddUser() {
		User newUser = new User(7, "lucas@example.com", "lucas_s", "Lucas Smith", LocalDate.of(2004, 5, 20));
		userStorage.addUser(newUser);
		int id = userStorage.getAllUsers().getLast().getId();

		User addedUser = userStorage.getUserById(id);

		assertThat(addedUser).isNotNull();
		assertThat(addedUser.getName()).isEqualTo("Lucas Smith");
	}

	@Test
	void testGetAllFilms() {
		List<Film> films = filmStorage.getAllFilms();

		assertThat(films).hasSize(3);
		assertThat(films).extracting(Film::getName)
				.containsExactlyInAnyOrder("Inception", "The Notebook", "Interstellar");
	}

	@Test
	void testGetMovieById() {
		Film film = filmStorage.getFilmById(1);

		assertThat(film).isNotNull();
		assertThat(film.getName()).isEqualTo("Inception");
		assertThat(film.getDescription()).isEqualTo("A mind-bending thriller by Christopher Nolan.");
	}


	@Test
	void testGetAllGenres() {
		List<Genre> genres = genreStorage.getAllGenres();

		assertThat(genres).hasSize(6);
		assertThat(genres).extracting(Genre::getName)
				.containsExactlyInAnyOrder("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
	}

	@Test
	void testGetGenreById() {
		Genre genre = genreStorage.getGenreById(1);

		assertThat(genre).isNotNull();
		assertThat(genre.getName()).isEqualTo("Комедия");
	}

	@Test
	void testGetAllRatings() {
		List<Mpa> ratings = mpaStorage.getAllMpa();

		assertThat(ratings).hasSize(5);
		assertThat(ratings).extracting(Mpa::getName)
				.containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
	}

	@Test
	void testGetRatingById() {
		Mpa rating = mpaStorage.getMpaById(4);

		assertThat(rating).isNotNull();
		assertThat(rating.getName()).isEqualTo("R");
	}
}