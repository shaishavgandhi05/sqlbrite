/*
 * Copyright (C) 2017 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sqlbrite3

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test


class SqlBriteTableDetectorTest {

    companion object {
        private val BRITE_DATABASE_STUB = TestFiles.java(
                """
      package com.squareup.sqlbrite3;

      public final class BriteDatabase {


        public void createQuery(String table, String sql, Object... args) {
        }

      }
      """.trimIndent()
        )
    }

    @Test fun usingValidTableName() {
        lint().files(
                BRITE_DATABASE_STUB,
                java("""
                    package test.pkg;

                    import com.squareup.sqlbrite3.BriteDatabase;

                    public class Test {
                        private static final String TABLE = "actual_table";
                        private static final String QUERY = "SELECT * from " + TABLE;

                        public void test() {
                            BriteDatabase db = new BriteDatabase();
                            db.createQuery(TABLE, QUERY);
                        }

                    }
                """.trimIndent()))
                .issues(SqlBriteTableDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test fun usingValidTableNameWithArgs() {
        lint().files(
                BRITE_DATABASE_STUB,
                java("""
                    package test.pkg;

                    import com.squareup.sqlbrite3.BriteDatabase;

                    public class Test {
                        private static final String TABLE = "actual_table";
                        private static final String QUERY = "SELECT * from " + TABLE + " WHERE id = ?";

                        public void test() {
                            BriteDatabase db = new BriteDatabase();
                            db.createQuery(TABLE, QUERY, "id");
                        }

                    }
                """.trimIndent()))
                .issues(SqlBriteTableDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test fun usingInvalidTableName() {
        lint().files(
                BRITE_DATABASE_STUB,
                java("""
                    package test.pkg;

                    import com.squareup.sqlbrite3.BriteDatabase;

                    public class Test {
                        private static final String TABLE = "actual_table";
                        private static final String QUERY = "SELECT * from other_table";

                        public void test() {
                            BriteDatabase db = new BriteDatabase();
                            db.createQuery(TABLE, QUERY);
                        }

                    }
                """.trimIndent()))
                .issues(SqlBriteTableDetector.ISSUE)
                .run()
                .expect("src/test/pkg/Test.java:11: " +
                        "Error: Invalid table name in query statement. Query statement, " +
                        "'SELECT * from other_table' should contain table name :  actual_table [SqlBriteTableName]\n" +
                        "        db.createQuery(TABLE, QUERY);\n" +
                        "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                        "1 errors, 0 warnings\n")
    }
}