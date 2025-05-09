CREATE TABLE IF NOT EXISTS users (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  login VARCHAR NOT NULL,
  name VARCHAR,
  birthday DATE NOT NULL,
  email VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS friendship (
  following_user_id INT,
  followed_user_id INT,
  status VARCHAR(9),
  PRIMARY KEY (following_user_id, followed_user_id),
  FOREIGN KEY (following_user_id) REFERENCES users(user_id),
  FOREIGN KEY (followed_user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS genres (
  genre_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS rating (
  rating_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS movies (
  movie_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR NOT NULL,
  description VARCHAR(1000),
  release_date DATE NOT NULL,
  duration BIGINT NOT NULL,
  rating_id INT,
  FOREIGN KEY (rating_id) REFERENCES rating(rating_id)
);

CREATE TABLE IF NOT EXISTS movie_genre (
  genre_id INT NOT NULL,
  movie_id INT NOT NULL,
  PRIMARY KEY (movie_id, genre_id),
  FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

CREATE TABLE IF NOT EXISTS likes (
  movie_id INT,
  user_id INT,
  PRIMARY KEY (user_id, movie_id),
  FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);
