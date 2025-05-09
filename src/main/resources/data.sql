INSERT INTO genres (name)
SELECT * FROM (
    SELECT 'Комедия' AS name WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Комедия')
    UNION ALL
    SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Драма')
    UNION ALL
    SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Мультфильм')
    UNION ALL
    SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Триллер')
    UNION ALL
    SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Документальный')
    UNION ALL
    SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Боевик')
) AS temp;



INSERT INTO rating (name)
SELECT * FROM (
    SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE name = 'G')
    UNION ALL
    SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE name = 'PG')
    UNION ALL
    SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE name = 'PG-13')
    UNION ALL
    SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE name = 'R')
    UNION ALL
    SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE name = 'NC-17')
) AS temp;
