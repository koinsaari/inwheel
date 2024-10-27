# The application

This is a modern Android application designed to provide detailed information about the
accessibility of various places. The goal of this app is to help users, especially those in
wheelchair to easily find accessible locations and services. Please note that this is still in early
development, so not all features are fully stable or functional.

## Features

- Search for accessible locations (fetched from Overpass API)
- Detailed information about entrance, parking, restrooms, and more
- Map-based interface using Google Maps API

## How to run

To run this application locally you'll need your own Google Maps API key.

### Steps to run locally

1. Clone the repository.
2. Go to Google Cloud Console, create a project and enable **Google Maps API** to it.
3. Create secrets.properties file in the root of the project
4. Add your generated API key to the file:

    ```bash
    MAPS_API_KEY={your_api_key_}
    ```

5. Run the app in Android Studio.

If you'd like to test the app without setting up the API key, you can join as a tester via Firebase.
Please reach out to me at aaro.koinsaari@proton.me for more information.

## Contributing

Contributions to this project are welcome! If you're interested in improving the app, fixing bugs,
or adding new features, feel free to open a pull request or an issue. See the project board
at (https://github.com/users/AaroKoinsaari/projects/1).

## Contact

For any questions or support, please contact me at aaro.koinsaari@proton.me.
