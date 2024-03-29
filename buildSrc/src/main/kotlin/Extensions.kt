@file:Suppress("NOTHING_TO_INLINE")

package heartmusic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import java.util.Properties
import kotlin.math.pow

val Project.minSdk: Int
  get() = intProperty("minSdk")

val Project.targetSdk: Int
  get() = intProperty("targetSdk")

val Project.compileSdk: Int
  get() = intProperty("compileSdk")

// Remember to update `kotlinDslPluginOptions.jvmTarget` in buildSrc/build.gradle.kts.
val Project.jvmTargetVersion: JavaVersion
  get() = JavaVersion.VERSION_17

val Project.groupId: String
  get() = stringProperty("GROUP")

val Project.versionName: String
  get() = stringProperty("VERSION_NAME")

val Project.versionCode: Int
  get() = versionName
    .takeWhile { it.isDigit() || it == '.' }
    .split('.')
    .map { it.toInt() }
    .reversed()
    .sumByIndexed { index, unit ->
      // 1.2.3 -> 102030
      (unit * 10.0.pow(2 * index + 1)).toInt()
    }

val Project.kotestDebug: Boolean
  get() = booleanLocalProperty("kotest.debug")

private fun Project.intProperty(name: String): Int {
  return (property(name) as String).toInt()
}

private fun Project.stringProperty(name: String): String {
  return property(name) as String
}

private fun Project.booleanLocalProperty(name: String): Boolean {
  return localProperties[name]?.toString()?.toBoolean() ?: false
}

private var localProperties: Properties? = null

private val Project.localProperties: Properties get() {
  return heartmusic.localProperties ?: Properties().apply {
    rootProject.file("local.properties").inputStream().use(::load)
    heartmusic.localProperties = this
  }
}

private inline fun <T> List<T>.sumByIndexed(selector: (Int, T) -> Int): Int {
  var index = 0
  var sum = 0
  for (element in this) {
    sum += selector(index++, element)
  }
  return sum
}

inline infix fun <T> Property<T>.by(value: T) = set(value)

inline infix fun <T> Property<T>.by(provider: Provider<T>) = set(provider)

inline infix fun <T> SetProperty<T>.by(value: Set<T>) = set(value)
