/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/script")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ScriptCodegenTestGenerated extends AbstractScriptCodegenTest {
    @TestMetadata("adder.kts")
    public void testAdder() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/adder.kts");
        doTest(fileName);
    }

    public void testAllFilesPresentInScript() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/codegen/script"), Pattern.compile("^(.+)\\.kts$"), true);
    }

    @TestMetadata("classLiteralInsideFunction.kts")
    public void testClassLiteralInsideFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/classLiteralInsideFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("empty.kts")
    public void testEmpty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/empty.kts");
        doTest(fileName);
    }

    @TestMetadata("helloWorld.kts")
    public void testHelloWorld() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/helloWorld.kts");
        doTest(fileName);
    }

    @TestMetadata("inline.kts")
    public void testInline() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/inline.kts");
        doTest(fileName);
    }

    @TestMetadata("localDelegatedProperty.kts")
    public void testLocalDelegatedProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/localDelegatedProperty.kts");
        doTest(fileName);
    }

    @TestMetadata("localFunction.kts")
    public void testLocalFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/localFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("outerCapture.kts")
    public void testOuterCapture() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/outerCapture.kts");
        doTest(fileName);
    }

    @TestMetadata("parameter.kts")
    public void testParameter() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameter.kts");
        doTest(fileName);
    }

    @TestMetadata("parameterArray.kts")
    public void testParameterArray() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameterArray.kts");
        doTest(fileName);
    }

    @TestMetadata("parameterClosure.kts")
    public void testParameterClosure() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameterClosure.kts");
        doTest(fileName);
    }

    @TestMetadata("parameterLong.kts")
    public void testParameterLong() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/parameterLong.kts");
        doTest(fileName);
    }

    @TestMetadata("secondLevelFunction.kts")
    public void testSecondLevelFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/secondLevelFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("secondLevelFunctionClosure.kts")
    public void testSecondLevelFunctionClosure() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/secondLevelFunctionClosure.kts");
        doTest(fileName);
    }

    @TestMetadata("secondLevelVal.kts")
    public void testSecondLevelVal() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/secondLevelVal.kts");
        doTest(fileName);
    }

    @TestMetadata("simpleClass.kts")
    public void testSimpleClass() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/simpleClass.kts");
        doTest(fileName);
    }

    @TestMetadata("string.kts")
    public void testString() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/string.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelFunction.kts")
    public void testTopLevelFunction() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelFunction.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelFunctionClosure.kts")
    public void testTopLevelFunctionClosure() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelFunctionClosure.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelLocalDelegatedProperty.kts")
    public void testTopLevelLocalDelegatedProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelLocalDelegatedProperty.kts");
        doTest(fileName);
    }

    @TestMetadata("topLevelProperty.kts")
    public void testTopLevelProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/script/topLevelProperty.kts");
        doTest(fileName);
    }
}
