/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.testing

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class TestInputAnnotationFailures extends AbstractIntegrationSpec {
    def "using @Input annotation on #fieldType file fields with upToDate check fails with helpful error message"() {
        given:
        buildFile << """
            import groovy.transform.CompileStatic

            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            @CacheableTask
            @CompileStatic
            abstract class MyTask extends DefaultTask {
                @Input
                $fieldInitialization

                @TaskAction
                void action() {
                    logger.warn("Input file: {}", $fieldRead)
                }
            }

            tasks.register('myTask', MyTask)
        """

        expect:
        fails 'myTask'
        result.assertHasErrorOutput("A problem was found with the configuration of task ':myTask' (type 'MyTask').")
        result.assertHasErrorOutput("- Type 'MyTask' property 'myField' has @Input annotation used on property of type '$fieldType'.")
        result.assertHasErrorOutput("Reason: A property of type '$fieldType' annotated with @Input cannot determine how to interpret the file.")
        result.assertHasErrorOutput("Possible solutions:")
        result.assertHasErrorOutput("1. Annotate with @InputFile for regular files.")
        result.assertHasErrorOutput("2. Annotate with @InputFiles for collections of files.")
        result.assertHasErrorOutput(". If you want to track the path, return File.absolutePath as a String and keep @Input.")

        where:
        fieldType       | fieldInitialization                                                               | fieldRead
        'File'          | "File myField = project.layout.projectDirectory.file('myFile.txt').getAsFile()"   | 'myField.absolutePath'
        'RegularFile'   | "RegularFile myField = project.layout.projectDirectory.file('myFile.txt')"        | 'myField.getAsFile().absolutePath'
    }

    def "using @Input annotation on #fieldType directory fields with upToDate check fails with helpful error message"() {
        given:
        buildFile << """
            import groovy.transform.CompileStatic

            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            @CacheableTask
            @CompileStatic
            abstract class MyTask extends DefaultTask {
                @Input
                $fieldInitialization

                @TaskAction
                void action() {
                    logger.warn("Input file: {}", $fieldRead)
                }
            }

            tasks.register('myTask', MyTask)
        """

        expect:
        fails 'myTask'
        result.assertHasErrorOutput("A problem was found with the configuration of task ':myTask' (type 'MyTask').")
        result.assertHasErrorOutput("- Type 'MyTask' property 'myField' has @Input annotation used on property of type '$fieldType'.")
        result.assertHasErrorOutput("Reason: A property of type '$fieldType' annotated with @Input cannot determine how to interpret the file.")
        result.assertHasErrorOutput("Possible solution: Annotate with @InputDirectory for directories.")

        where:
        fieldType     | fieldInitialization                                                         | fieldRead
       'Directory'    | "Directory myField = project.layout.projectDirectory.dir('myDir')"          | 'myField.getAsFile().absolutePath'
    }

    def "using @Input annotation on #propertyType file properties fails with helpful error message"() {
        given:
        buildFile << """
            import groovy.transform.CompileStatic

            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            @CacheableTask
            @CompileStatic
            abstract class MyTask extends DefaultTask {
                @Inject
                public abstract ObjectFactory getObjectFactory()

                @Input
                $propertyInitialization

                @TaskAction
                void action() {
                    logger.warn("Input file: {}", $propertyRead)
                }
            }

            tasks.register('myTask', MyTask)
        """

        expect:
        fails 'myTask'
        result.assertHasErrorOutput("A problem was found with the configuration of task ':myTask' (type 'MyTask').")
        result.assertHasErrorOutput("- Type 'MyTask' property 'myProp' has @Input annotation used on property of type '$propertyType'.")
        result.assertHasErrorOutput("Reason: A property of type '$propertyType' annotated with @Input cannot determine how to interpret the file.")
        result.assertHasErrorOutput("Possible solutions:")
        result.assertHasErrorOutput("1. Annotate with @InputFile for regular files.")
        result.assertHasErrorOutput("2. Annotate with @InputFiles for collections of files.")
        result.assertHasErrorOutput(". If you want to track the path, return File.absolutePath as a String and keep @Input.")

        where:
        propertyType            | propertyInitialization                                                                | propertyRead
        "File"                  | "final File myProp = project.layout.projectDirectory.file('myFile.txt').getAsFile()"  | 'myProp.absolutePath'
        "RegularFileProperty"   | "abstract RegularFileProperty getMyProp()"                                            | "myProp.getAsFile().get().absolutePath"
    }

    def "using @Input annotation on #propertyType file properties succeeds"() {
        given:
        buildFile << """
            import groovy.transform.CompileStatic

            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            @CacheableTask
            @CompileStatic
            abstract class MyTask extends DefaultTask {
                @Inject
                public abstract ObjectFactory getObjectFactory()

                @Input
                $propertyInitialization

                @TaskAction
                void action() {
                    logger.warn("Input file: {}", $propertyRead)
                }
            }

            tasks.register('myTask', MyTask) {
                $propertyAssignment
            }
        """

        expect:
        succeeds 'myTask'

        where:
        propertyType            | propertyInitialization                                                        | propertyAssignment                                                        | propertyRead
        "Property<File>"        | "final Property<File> myProp = getObjectFactory().property(File.class)"       | "myProp.set(project.layout.projectDirectory.file('myFile').getAsFile())"  | 'myProp.get().absolutePath'
    }

    def "using @Input annotation on #propertyType directory properties fails with helpful error message"() {
        given:
        buildFile << """
            import groovy.transform.CompileStatic

            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            @CacheableTask
            @CompileStatic
            abstract class MyTask extends DefaultTask {
                @Input
                $propertyInitialization

                @TaskAction
                void action() {
                    logger.warn("Input file: {}", $propertyRead)
                }
            }

            tasks.register('myTask', MyTask)
        """

        expect:
        fails 'myTask'
        result.assertHasErrorOutput("A problem was found with the configuration of task ':myTask' (type 'MyTask').")
        result.assertHasErrorOutput("- Type 'MyTask' property 'myProp' has @Input annotation used on property of type '$propertyType'.")
        result.assertHasErrorOutput("Reason: A property of type '$propertyType' annotated with @Input cannot determine how to interpret the file.")
        result.assertHasErrorOutput("Possible solution: Annotate with @InputDirectory for directories.")

        where:
        propertyType            | propertyInitialization                        | propertyRead
        "DirectoryProperty"     | "abstract DirectoryProperty getMyProp()"      | "myProp.getAsFile().get().absolutePath"
    }
}
