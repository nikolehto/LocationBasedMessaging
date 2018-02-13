# WeatherInformation3
Get temperature of Oulu and publish it as notification

Mobile social computing week-exercise3 

Uses intent service 
- to make http-request to web api
- to parse temperature from json-response
- to store and get temperature from SQLite-database

When application is started, first it shows latest temperature from database (if exists), and then updates latest temperature from server response
Application also shows notification with temperature-data.

Program uses very short delay between http-requests - and actually sleeps during it,  just because it emphasizes the action and program is meant to be just an exercise. 
