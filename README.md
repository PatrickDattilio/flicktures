A basic Flickr consumer. Layouts adjust for portrait/landscape as well as for tablets.

An IntentService was used along with [OkHttp](https://github.com/square/okhttp) to download the JSON string. It was then parsed using GSON and inserted into a ContentProvider. A CursorLoader was used to populate the ListView from the ContentProvider, and [Picasso](https://github.com/square/picasso) downloaded, cached, and injected the images from their urls. Any networking failures are sent via intent to our BroadcastReciever to be handled.
