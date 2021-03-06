package com.sxtanna.database.tests;

import com.sxtanna.database.Kuery;
import com.sxtanna.database.ext.Kext;
import com.sxtanna.database.ext.Krs;
import com.sxtanna.database.ext.PrimaryKey;
import com.sxtanna.database.task.builder.Create;
import com.sxtanna.database.task.builder.Delete;
import com.sxtanna.database.task.builder.Insert;
import com.sxtanna.database.task.builder.Select;
import com.sxtanna.database.type.base.SqlObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public final class JKueryTestOrm extends DatabaseTest<Kuery> {

	private final Create createUser = Create.from(User.class);
	private final Insert<User> insertUser = Insert.into(User.class).onDupeUpdate();

	private final Select<User> selectUser = Select.from(User.class);
	private final Select<User> selectSNames = Select.from(User.class).startsWith("name", "S");
	private final Select<User> selectAscendNames = Select.from(User.class).ascend("name");
	private final Delete<User> deleteSNames = Delete.from(User.class).startsWith("name", "S");


	@NotNull
	@Override
	public Kuery create() {
		// To load from a config file
		// Kuery.get(new File({path}));

		// To load using a preloaded config use like the one above
		return Kuery.get(JKueryTest.CONFIG);
	}

	@Override
	public void runTest() {

		database.addCreator(Kext.create(User.class, rs -> new User(Krs.getString(rs, "name"))));

		database.execute(task -> {

			task.execute(createUser);

			final User emilie = new User("Emiliee"), ranald = new User("Sxtanna");

			task.execute(insertUser, emilie);
			task.execute(insertUser, ranald);

			final int[] users = {0};

			task.execute(selectUser, user -> {
				users[0]++;
				System.out.println("Found user " + user.getName());
			});

			assertEquals(users[0], 2, "Should have found 2 users, Sxtanna and Emiliee");

			task.execute(selectSNames, user -> {

				System.out.println("Found user " + user.getName());
				assertEquals("Sxtanna", user.getName(), "Name should have been Sxtanna");

			});

			final List<String> names = new ArrayList<>();

			task.execute(selectAscendNames, user -> {

				System.out.println("Found user " + user.getName());
				names.add(user.getName());

			});

			assertIterableEquals(names, Arrays.asList("Emiliee", "Sxtanna"), "Results were in wrong order");

			task.execute(deleteSNames);
			task.execute(deleteSNames, emilie);
		});
	}


	private static final class User implements SqlObject {

		@PrimaryKey
		private final String name;

		User(String name) {
			this.name = name;
		}


		String getName() {
			return name;
		}

	}

}
