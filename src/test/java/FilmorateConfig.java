import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;

@Configuration
public class FilmorateConfig {

    @Bean
    public FilmRowMapper filmRowMapper() {
        return new FilmRowMapper();
    }
}
