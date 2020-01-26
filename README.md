# ApartmentGate
ApartmentGate is an Android App for browsing apartment listings in San Francisco, with data from Apartments.com.
The app takes information about the user's work address, work hours, preferred method of commute, ideal budget,
apartment size, and preferred atmosphere, and uses this profile information to grade apartment listings based
on how closely they fit the user's criteria. The user's preferences can be changed anytime, and the grades of
apartment listings will be recomputed accordingly. In addition, the app uses information from all users to
show information regarding the most popular neighborhoods in San Francisco, and the average rent paid for
listings in those neighborhoods. To learn more about this project, see the writeup: https://docs.google.com/document/d/1ayQ5g0qbl4OtGOBDV0ITrwSb7HU9cpAF2meZeHnpJ0Q/edit?usp=sharing

## Screenshots
[![MapView](https://i.postimg.cc/5QXWDLmY/Screenshot-20191206-090910.png)](https://postimg.cc/5QXWDLmY)
[![OneListing](https://i.postimg.cc/hhvnJRqD/Screenshot-20191206-090935.png)](https://postimg.cc/hhvnJRqD)
[![Directions](https://i.postimg.cc/MMKSbhMY/Screenshot-20191206-090950.png)](https://postimg.cc/MMKSbhMY)
[![Trending Neighborhoods](https://i.postimg.cc/34SHLn2Q/Screenshot-20191206-091026.png)](https://postimg.cc/p9z2p8hM)
[![Favorites](https://i.postimg.cc/06CvS8QQ/Screenshot-20191206-090954.png)](https://postimg.cc/06CvS8QQ)

## Getting Started
To recreate this project on your local machine for development and testing purposes, start by cloning this
repository. Acquire API keys for the WalkScore API, Soundscore API, and the Google Maps APIs. Next, request
to get access as a contributor to our Firebase Cloud Firestore Database. Our database contains data of
apartment listings, the preferences of all user accounts registered with the app, and the popularity and
average rent of different neighborhoods.

## App Components
**Built With:** Android Studio and Kotlin

**APIs and SDKs used:** WalkScore API, HowLoud SoundScore API, Google Maps SDK, Google Maps Directions API,
Google Maps Street View Static API

**Database:** Cloud Firestore

**User Authentication:** Firebase Authentication

## Developers
* Jason Lihuang (jasonlihuang@utexas.edu)
* Leon Cai (leoncai197@gmail.com)
