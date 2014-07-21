A basic Flickr consumer. Layouts adjust for portrait/landscape as well as for tablets.

An IntentService is used along with [FlickrjApi4Android](https://github.com/yuyang226/FlickrjApi4Android) to access the Flickr API and parse the json into POJOs. It then caches the data in SQLite though a ContentProvider. A CursorLoader is used to populate the GridView from the ContentProvider, and [Picasso](https://github.com/square/picasso) downloaded, cached, and injected the images from their urls. Any networking failures are sent via intent to our BroadcastReciever to be displayed in a Toast.
