package com.lineargs.watchnext.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {Credits.class, Episodes.class, Favourites.class,
        Movies.class, Person.class, Reviews.class,
        Seasons.class, Series.class, Videos.class}, version = WatchNextDatabase.DB_VERSION)
public abstract class WatchNextDatabase extends RoomDatabase {

    private static volatile WatchNextDatabase INSTANCE;

    private static final int VERSION_41 = 41;

    private static final int VERSION_42_ROOM = 42;

    static final int DB_VERSION = VERSION_42_ROOM;

    static WatchNextDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WatchNextDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WatchNextDatabase.class, "watchnext.db")
                            .addMigrations(MIGRATION_41_42)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_41_42 = new Migration(VERSION_41, VERSION_42_ROOM) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            /* If we do not want to lose data we need to create a new table for
             * example credits_new -> copy all the data from credits
             * Drop credits and then ALTER credits_new -> credits
             * We will recreate only the favourites table without loosing the data as any other
             * we can sync from the MovieDb.
             */
            //Credits table
            database.execSQL("DROP TABLE credits");
            database.execSQL("CREATE TABLE credits ('tmdb_id' INTEGER NOT NULL, 'character_name' TEXT," +
                    " 'name' TEXT, 'profile_path' TEXT, 'id' INTEGER NOT NULL, 'job' TEXT, 'type' INTEGER NOT NULL," +
                    " 'person_id' INTEGER NOT NULL, PRIMARY KEY ('id'))");

            //Episodes table
            database.execSQL("DROP TABLE episodes");
            database.execSQL("CREATE TABLE episodes('tmdb_id' INTEGER NOT NULL, 'overview' TEXT, 'directors' TEXT, " +
                    "'season_number' INTEGER NOT NULL, 'season_id' INTEGER NOT NULL, 'still_path' TEXT, 'writers' TEXT, " +
                    "'guest_stars' TEXT, 'episode_number' INTEGER NOT NULL, 'release_date' TEXT, 'vote_average' TEXT, " +
                    "'name' TEXT, 'id' INTEGER NOT NULL, 'vote_count' INTEGER NOT NULL, PRIMARY KEY ('id'))");

            //Favourites table
            database.execSQL("CREATE TABLE favourites ('tmdb_id' INTEGER NOT NULL, 'overview' TEXT NOT NULL, 'imdb_id' TEXT, " +
                    "'runtime' INTEGER NOT NULL, 'title' TEXT NOT NULL, 'networks' TEXT, 'poster_path' TEXT, " +
                    "'backdrop_path' TEXT, 'release_date' TEXT, 'production_companies' TEXT, 'genres' TEXT, " +
                    "'vote_average' TEXT, 'production_countries' TEXT, 'id' INTEGER NOT NULL, 'homepage' TEXT, " +
                    "'status' TEXT, PRIMARY KEY ('id'))");
            database.execSQL("INSERT INTO favourites ('tmdb_id', 'overview', 'imdb_id', 'runtime', 'title', " +
                    "'poster_path', 'backdrop_path', 'release_date', 'production_companies', 'genres', 'vote_average', " +
                    "'production_countries', 'id', 'homepage', 'status') SELECT 'movie_id', 'overview', 'imdb_id', " +
                    "'runtime', 'title', 'poster_path', 'backdrop_path', 'release_date', 'production_companies', " +
                    "'genres', 'vote_average', 'production_countries', '_id', 'homepage', 'status' FROM favorites");
            database.execSQL("DROP TABLE favorites");
            //TODO Add below statement in 42_43 Migration
//            database.execSQL("ALTER TABLE favourites ADD COLUMN type INTEGER");

            //Movies table
            database.execSQL("DROP TABLE popularmovies");
            database.execSQL("DROP TABLE topmovies");
            database.execSQL("DROP TABLE upcomingmovies");
            database.execSQL("DROP TABLE theatermovies");
            database.execSQL("CREATE TABLE movies ('tmdb_id' INTEGER NOT NULL, 'overview' TEXT NOT NULL, 'imdb_id' TEXT, " +
                    "'runtime' INTEGER NOT NULL, 'title' TEXT NOT NULL, 'type' INTEGER NOT NULL, 'poster_path' TEXT, " +
                    "'backdrop_path' TEXT, 'release_date' TEXT, 'production_companies' TEXT, 'genres' TEXT, " +
                    "'vote_average' TEXT, 'production_countries' TEXT, 'id' INTEGER NOT NULL, 'homepage' TEXT, " +
                    "'status' TEXT, PRIMARY KEY ('id'))");

            //Person table
            database.execSQL("DROP TABLE person");
            database.execSQL("CREATE TABLE person ('place_of_birth' TEXT, 'tmdb_id' INTEGER NOT NULL, 'name' TEXT, " +
                    "'profile_path' TEXT, 'id' INTEGER NOT NULL, 'biography' TEXT, 'homepage' TEXT, PRIMARY KEY ('id'))");

            //Reviews table
            database.execSQL("DROP TABLE review");
            database.execSQL("CREATE TABLE reviews ('tmdb_id' INTEGER NOT NULL, 'id' INTEGER NOT NULL, 'author' TEXT, 'content' TEXT, " +
                    "'url' TEXT, PRIMARY KEY ('id'))");

            //Search tables
            database.execSQL("DROP TABLE search");
            database.execSQL("DROP TABLE searchtv");

            //Seasons table
            database.execSQL("DROP TABLE seasons");
            database.execSQL("CREATE TABLE seasons ('tmdb_id' INTEGER NOT NULL, 'episode_count' INTEGER NOT NULL, 'show_name' TEXT, " +
                    "'season_number' TEXT, 'season_id' INTEGER NOT NULL, 'id' INTEGER NOT NULL, " +
                    "'poster_path' TEXT, PRIMARY KEY ('id'))");

            //Series table
            database.execSQL("DROP TABLE ontheairseries");
            database.execSQL("DROP TABLE popularseries");
            database.execSQL("DROP TABLE topseries");
            database.execSQL("CREATE TABLE series ('tmdb_id' INTEGER NOT NULL, 'overview' TEXT NOT NULL, " +
                    "'runtime' INTEGER NOT NULL, 'title' TEXT NOT NULL, 'networks' TEXT, 'type' INTEGER NOT NULL, 'poster_path' TEXT, " +
                    "'backdrop_path' TEXT, 'release_date' TEXT, 'production_companies' TEXT, 'genres' TEXT, " +
                    "'vote_average' TEXT, 'id' INTEGER NOT NULL, 'homepage' TEXT, " +
                    "'status' TEXT, PRIMARY KEY ('id'))");

            //Videos table
            database.execSQL("DROP TABLE videos");
            database.execSQL("CREATE TABLE videos ('name' TEXT, 'tmdb_id' INTEGER NOT NULL, 'image' TEXT, " +
                    "'id' INTEGER NOT NULL, 'key' TEXT, PRIMARY KEY ('id'))");
        }
    };

    public abstract MoviesDao moviesDao();

    public abstract SeriesDao seriesDao();
}