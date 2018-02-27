# mongodb-denormalizer

Takes MongoDB collections modelled after relational databases and creates
more NoSQL-friendly relationships with embedded documents and/or references.

Useful after migrating tabular data (CSV, SQL etc.) to a MongoDB database
and wanting to take advantage of a flexible schema and embedded documents
to boost read performance.

For more background on MongoDB data model design, see the
[Mongo Docs](https://docs.mongodb.com/manual/core/data-model-design/).

## Use cases

### You have:
one-to-many relationships between collections, modelled after a relational database

```JSON
authors: [	{"_id" : 1, "name" : "de Cervantes"},
		{"_id" : 2, "name": "Tolkien"}],
books: [	{"_id" : "A", "name" : "Don Quixote", "author_id" : 1},
		{"_id" : "B", "name" : "The Lord of The Rings",  "author_id" : 2},
		{"_id" : "C", "name" : "The Hobbit",  "author_id" : 2},]
```
		
### You instead need:
**(A) a data model using document references**

```JSON
authors: [	{"_id" : 1, "name" : "de Cervantes", "books" : ["A"]},
		{"_id" : 2, "name": "Tolkien", "books" : ["B", "C"]}]
```

**(B) a data model using embedded documents**

```JSON
authors: [	{"_id" : 1, "name" : "de Cervantes", "books" : [
			{"_id" : "A", "name" : "Don Quixote"}]},
		{"_id" : 2, "name": "Tolkien", "books" : [
			{"_id" : "B", "name" : "The Lord of The Rings"},
			{"_id" : "C", "name" : "The Hobbit"}]}]
```

### Or, you have:
many-to-many relationships modelled using a link table, or link collection

```JSON
products: [{"_id" : 1}, {"_id" : 2}, {"_id" : 3}],
releases: [{"_id" : 7}, {"_id" : 8}, {"_id" : 9}],
productsreleases: [	{"_id" : 20, "p_id" : 1, "r_id" : 7, "type" : "ALPHA"},
			{"_id" : 21, "p_id" : 1, "r_id" : 8, "type" : "BETA"},
			{"_id" : 22, "p_id" : 2, "r_id" : 8, "type" : "ALPHA"},
			{"_id" : 23, "p_id" : 3, "r_id" : 9, "type" : "FINAL"}]
```
		
### You instead need:
**(C) only two collections, referencing each other through document reference arrays**

```JSON
products: [	{"_id" : 1, "releases" : [7,8]},
		{"_id" : 2, "releases" : [8]},
		{"_id" : 3, "releases" : [9]}],
releases: [	{"_id" : 7, "products" : [1]},
		{"_id" : 8, "products" : [1,2]},
		{"_id" : 9, "products" : [3]}]
```

**(C') ...optionally embedding a field from the link table / collection to one or both**

```JSON
products: [	{"_id" : 1, "releases" : [	{"_id" : 7, "type" : "ALPHA"},
						{"_id" : 8, "type" : "BETA"}]},
		{"_id" : 2, "releases" : [	{"_id" : 8, "type" : "ALPHA"}]},
		{"_id" : 3, "releases" : [	{"_id" : 9, "type" : "FINAL"}]}],
releases: [	{"_id" : 7, "products" : [1]},
		{"_id" : 8, "products" : [1,2]},
		{"_id" : 9, "products" : [3]}]
```
					
## Usage

### As a dependency in a Java project

1. Import the JAR located in the `target` folder (depends on `mongodb-java-driver`,
tested on `3.6.2`)

2. Create a model for your desired change:

**Case (A) – a data model using document references**

```Java
DBModel model = new DBModel().add(new CollModel("books")
	.join(new JoinModel("authors")
		.on("author_id", "_id")
		.reference("_id")
		.as("books")));
```

**Case (B) – a data model using embedded documents**
		
```Java
DBModel model = new DBModel().add(new CollModel("books")
	.join(new JoinModel("authors")
		.on("author_id", "_id")
		.reference("_id")
		.as("books")
		.embed("name")));	// chain as many .embeds as you like
```
					
**Case (C) – a many-to-many model using document references**
		
```Java
DBModel model = new DBModel().add(new CollModel("productsreleases")
	.join(new JoinModel("products")
		.on("p_id", "_id")
		.reference("r_id") // reference other foreign key here
		.as("releases")
		.embed("type")) // optionally embed fields
	.join(new JoinModel("releases")
		.on("r_id", "_id")
		.reference("p_id") // vice versa
		.as("products")));
```
					
3. Pass the model into a Denormalizer and hit it:
		
```Java
// Connect to your DB using the dependent mongodb-java-driver
MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
MongoDatabase db = client.getDatabase("mydatabase");

DenormalizerFactory.getDenormalizer(db).denormalize(model);

client.close(); //remember to close!
```
		
Detailed descriptions of all methods in the API are in the [JavaDoc](https://mgackowski.github.io/mongodb-denormalizer/).

### As a standalone command line application

1. Download the JAR located in the `target` folder

2. Prepare a model of the desired changes in JSON format (analogous to the use
cases described above). The model should have the following structure:

```JSON
{
	"database": "my-db-name",
	"collections" : [
		{
			"name" : "source-collection-name",
			"joins" : [
				{
					"collection:" : "target-collection-name",
					"onSource" : "foreign_id",
					"onTarget" : "_id",
					"as" : "newArrayName",
					"reference" : "_id",
					"embed" : ["optionalField", "optionalField", "etc..."]
				}
			]
		}
	]
}
```

3. Run the app: `java -jar mongodb-denormalizer-xx.xx.xx.jar 'my-database-host:port' 'path-to-model.json`

The utility has been tested, still it is strongly recommended that you back up your database.
