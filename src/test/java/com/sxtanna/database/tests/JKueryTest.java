package com.sxtanna.database.tests;

import com.sxtanna.database.Kuery;
import com.sxtanna.database.config.KueryConfig;
import com.sxtanna.database.ext.Kext;
import com.sxtanna.database.ext.Krs;
import com.sxtanna.database.struct.base.Duo;
import com.sxtanna.database.task.builder.Create;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sxtanna.database.ext.Kext.sorts;
import static com.sxtanna.database.ext.Kext.targets;
import static com.sxtanna.database.ext.Krs.getString;
import static com.sxtanna.database.ext.Krs.whileNext;
import static com.sxtanna.database.struct.base.Duo.co;
import static com.sxtanna.database.struct.base.Duo.valueColumns;
import static com.sxtanna.database.struct.obj.Duplicate.Update;
import static com.sxtanna.database.struct.obj.Sort.Order;
import static com.sxtanna.database.struct.obj.SqlType.VarChar;
import static com.sxtanna.database.struct.obj.Target.Where;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

// For those of us that "enjoy" verbosity and deprecation
public final class JKueryTest extends DatabaseTest<Kuery> {

	// Configs can be loaded out of the scope of a database
	protected static final KueryConfig CONFIG = Kuery.getConfig(new File("../SqlConfig.json"));


	@NotNull
	@Override
	public Kuery create() {
		return Kuery.get(CONFIG);
	}

	@Override
	public void runTest() {

		// None ORM with plain queries
		database.execute(task -> {

			task.execute("CREATE TABLE IF NOT EXISTS User(name VARCHAR(255) PRIMARY KEY NOT NULL)");

			task.execute("INSERT INTO User (name) VALUES (?) ON DUPLICATE KEY UPDATE name=VALUES(name)", "Emiliee");
			task.execute("INSERT INTO User (name) VALUES (?) ON DUPLICATE KEY UPDATE name=VALUES(name)", "Sxtanna");

			final int[] users = {0};

			task.query("SELECT * FROM User", rs -> {

				try {
					while (rs.next()) {
						users[0]++;
						System.out.println("Found user " + rs.getString("name"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			});

			assertEquals(users[0], 2, "Should have found 2 users, Sxtanna and Emiliee");

			// Yes, I hate the Java array syntax too...
			task.query("SELECT * FROM User WHERE name LIKE ?", new String[] {"S%"}, rs -> {

				try {
					while (rs.next()) {
						final String name = rs.getString("name");
						assertEquals("Sxtanna", name, "Name should have been Sxtanna");

						System.out.println("Found user " + name);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});

			task.execute("DELETE FROM User WHERE name='Emiliee'");
			task.execute("DELETE FROM User WHERE name LIKE 'S%'");
		});

		// Non ORM with helper methods
		database.execute(task -> {

			task.execute(Create.table("User").co("name", new VarChar(255, true)));

			task.insert("User", valueColumns(co("name", "Emiliee")), new Update("name"));
			task.insert("User", valueColumns(co("name", "Sxtanna")), new Update("name"));


			final int[] users = {0};

			// Select all users
			task.select("User", resultSet -> whileNext(resultSet, rs -> {
				users[0]++;
				System.out.println("Found user " + getString(rs, "name"));
			}));

			assertEquals(users[0], 2, "Should have found 2 users, Sxtanna and Emiliee");


			// Select all users who name's start with S
			task.select("User", targets(Where.starts("name", "S")), resultSet -> whileNext(resultSet, rs -> {

				final String name = getString(rs, "name");
				assertEquals("Sxtanna", name, "Name should have been Sxtanna");

				System.out.println("Found user " + name);

			}));


			final List<String> names = new ArrayList<>();

			// Select all users and order by name ascending
			task.select("User", sorts(Order.ascend("name")), resultSet -> whileNext(resultSet, rs -> {

				final String name = getString(rs, "name");
				System.out.println("Found user " + name);

				names.add(name);

			}));

			assertIterableEquals(names, Arrays.asList("Emiliee", "Sxtanna"));

            task.delete("User", Where.starts("name", "S"));
            task.delete("User", Where.equals("name", "Emiliee"));
		});


		// Version without static imports
		database.execute(task -> {

			task.execute(Create.table("User").co("name", new VarChar(255, true)));

			task.insert("User", Duo.valueColumns(Duo.co("name", "Emiliee")), new Update("name"));
			task.insert("User", Duo.valueColumns(Duo.co("name", "Sxtanna")), new Update("name"));


			final int[] users = {0};

			task.select("User", resultSet -> Krs.whileNext(resultSet, rs -> {
				users[0]++;

				System.out.println("Found user " + Krs.getString(rs, "name"));
			}));

			assertEquals(users[0], 2, "Should have found 2 users, Sxtanna and Emiliee");

			task.select("User", Kext.targets(Where.starts("name", "S")), resultSet -> Krs.whileNext(resultSet, rs -> {

				final String name = Krs.getString(rs, "name");

				assertEquals("Sxtanna", name, "Name should have been Sxtanna");
				System.out.println("Found user " + name);

			}));

			final List<String> names = new ArrayList<>();

			task.select("User", Kext.sorts(Order.ascend("name")), resultSet -> whileNext(resultSet, rs -> {

				final String name = getString(rs, "name");
				System.out.println("Found user " + name);

				names.add(name);

			}));

			assertIterableEquals(names, Arrays.asList("Emiliee", "Sxtanna"), "Results were in wrong order");

            task.delete("User", Where.starts("name", "S"));
            task.delete("User", Where.equals("name", "Emiliee"));
		});
	}

}
