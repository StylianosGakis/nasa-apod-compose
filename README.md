# nasa-apod-compose
A simple Compose application accessing the Nasa APOD API

To run the application, an API key must be generated from [here](https://api.nasa.gov/) or the key `DEMO_KEY` can be used as explained in the NASA API documentation.

Then create a file named 'keys.properties.txt' in the root folder with the following content:

```
base_url = "https://api.nasa.gov/planetary/apod/"
api_key = "${your_api_key_here}"
```
