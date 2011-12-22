/*
 * Copyright 2011 the original author or authors.
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

package org.gradle.launcher.daemon.server

import org.gradle.BuildResult
import org.gradle.GradleLauncher
import org.gradle.configuration.GradleLauncherMetaData
import org.gradle.initialization.GradleLauncherAction
import org.gradle.launcher.daemon.client.DaemonClient
import org.gradle.launcher.daemon.client.EmbeddedDaemonClientServices
import org.gradle.launcher.exec.DefaultBuildActionParameters
import org.gradle.util.TemporaryFolder
import org.junit.Rule
import spock.lang.Specification

/**
 * by Szczepan Faber, created at: 12/21/11
 */
class DaemonServerExceptionHandlingTest extends Specification {

    @Rule def temp = new TemporaryFolder()
    def daemonClient = new EmbeddedDaemonClientServices().get(DaemonClient)
    def parameters = new DefaultBuildActionParameters(new GradleLauncherMetaData(), 0, [:], [:], temp.dir)

    static class UsesUnknownClass implements GradleLauncherAction, Serializable {
        Object someState
        Object getResult() { null }
        BuildResult run(GradleLauncher launcher) { null }
    }

    def "behaves when the daemon cannot receive the first command"() {
        given:
        def clz = new GroovyClassLoader().parseClass("class Foo implements Serializable {}")
        def unloadableClass = clz.newInstance()
        //the action contains some state that cannot be deserialized on the daemon side
        //this a real-world scenario, the tooling api can ask the daemon to build model
        //that does not exist with given daemon version
        def action = new UsesUnknownClass(someState: unloadableClass)

        when:
        daemonClient.execute(action, parameters)

        then:
        def ex = thrown(Exception)
        ex.message.contains("Unable to receive command from connection")
    }
}
