package com.example.top100list;

class FilmDescription{
    private final String id;

    private final String name;
    private final String year;
    private final String previewUrl;
    private final String genre;
    private final String posterUrl;
    FilmDescription(String id, String name, String year, String previewUrl, String genre, String posterUrl) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.previewUrl = previewUrl;
        this.genre = genre;
        this.posterUrl = posterUrl;
    }

    public String getFilmName(){return name;}
    public String getFilmGenre(){return genre;}
    public String getPreviewUrl(){return previewUrl;}

    public String getYear(){return year;}

    public String getId(){return id;}
    public String getPosterUrl(){return posterUrl;}
}