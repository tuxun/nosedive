# multifacette
![Java CI with Gradle](https://github.com/tuxun/nosedive/workflows/Java%20CI%20with%20Gradle/badge.svg)
![Android CI](https://github.com/tuxun/nosedive/workflows/Android%20CI/badge.svg)

Little Android Slideshow application for an art show.

At the first start, if wifi is detected, the app download images in Android cache directory from an external web server.

This external server should have the script /serverside/index.php, caching the near images list in filelist.json and returning it. 
