# Kuery
MySQL Kotlin wrapper based on HikariCP

## How to get it!
```xml
<dependency>
    <groupId>com.sxtanna.database</groupId>
    <artifactId>Kuery</artifactId>
    <version>LATEST</version>
</dependency>
```

## How it works!

### 1-0. To create a new instance of [Kuery](src/main/kotlin/com/sxtanna/database/Kuery.kt), you would follow this syntax

#### From Kotlin
``` kotlin
val kuery = Kuery[file: File]
```

#### From Java
``` java
final Kuery kuery = Kuery.get(file: File);
```

### 1-1. To initialize and shutdown a database use these two methods
``` kotlin
kuery.enable()
```
and
``` kotlin
kuery.disable()
```


### 2-0. After you have an instance, to get a resource

#### From Kotlin
``` kotlin
val resource = kuery.resource()
```

#### From Java
``` java
final Connection resource = kuery.resource();
```
*[Database#resource](https://github.com/KDatabases/Core/blob/master/src/main/kotlin/com.sxtanna/database/base/Database.kt#L96) will throw an IllegalStateException if it's unable to create a resource a/o the database isn't enabled*


### 2-1. Or you could utilize the Database's ability to automatically manage the connection with the *invoke* methods

#### From Kotlin
``` kotlin
kuery {                                                                  
	createTable("Table", "UUID" co Char(36, true), "Name" co VarChar(36))
}                                                                                                                                           
```

#### From Java
``` java
kuery.execute(task -> task.createTable("Users", Column.tableColumns(Column.col("UUID", new SqlType.Char(36, true)), Column.col("Name", new SqlType.VarChar(16)))));
```
*Yes, I know the Java version is ugly AF, but this can be slightly fixed with static imports*
``` java
kuery.execute(task -> task.createTable("Users", tableColumns(col("UUID", new Char(36, true)), col("Name", new VarChar(16)))));
```
