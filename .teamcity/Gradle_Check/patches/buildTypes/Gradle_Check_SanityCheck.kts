package Gradle_Check.patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2017_2.*
import jetbrains.buildServer.configs.kotlin.v2017_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with uuid = 'Gradle_Check_SanityCheck' (id = 'Gradle_Check_SanityCheck')
accordingly and delete the patch script.
*/
changeBuildType("Gradle_Check_SanityCheck") {
    params {
        add {
            param("system.java9Home", "%linux.java8.oracle.64bit%")
        }
    }
}
