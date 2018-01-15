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

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator.evaluateString
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import java.util.*

private const val CREATE_QUERY_METHOD_NAME = "createQuery"
private const val BRITE_DATABASE = "com.squareup.sqlbrite3.BriteDatabase"

class SqlBriteTableDetector : Detector(), Detector.UastScanner {

    companion object {
        val ISSUE = Issue.create("SqlBriteTableName",
                "One or more of the tables provided is not in the query statement",
                "All the tables passed in the first parameter must exist in the query statement",
                Category.MESSAGES,
                8,
                Severity.ERROR,
                Implementation(SqlBriteTableDetector::class.java, EnumSet.of(JAVA_FILE, TEST_SOURCES))
                )
    }

    override fun getApplicableMethodNames() = listOf(CREATE_QUERY_METHOD_NAME)

    override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator

        if (evaluator.isMemberInClass(method, BRITE_DATABASE)) {
            // Skip the signature createQuery(DatabaseQuery query)
            if (node.valueArgumentCount < 2) return

            val arguments = node.valueArguments

            val tableName = evaluateString(context, arguments[0], true)
            val query = evaluateString(context, arguments[1], true)


            // Skipping list of tables for now
            if (tableName != null && query != null) {
                if (!query.toString().contains(tableName.toString(), true)) {
                    context.report(ISSUE, node, context.getLocation(node), "Invalid table name " +
                            "in query statement. Query statement, '$query' should contain table name : " +
                            " $tableName")
                }
            }
        }
    }

}