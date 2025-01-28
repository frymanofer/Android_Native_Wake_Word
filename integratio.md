## Installation
Wake Words and KeyWords detection by DaVoice.io
contact us at info@Davoice.io

1. Download the AAR file from the [releases page](https://github.com/yourusername/MyLibrary-Binary/releases).
2. Copy the `MyLibrary-release.aar` file to your project's `libs` directory.

3. Add the following to your app directory `build.gradle` file:

```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
    }

// Or vi jitpack.io Gradle:
Add it in your root build.gradle at the end of repositories:
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency
	dependencies {
	        implementation 'com.github.frymanofer:KeywordsDetectionAndroidLibrary:Tag'
	}
// Or vi jitpack.io Maven:
Step 1:
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
Step 2. Add the dependency
	<dependency>
	    <groupId>com.github.frymanofer</groupId>
	    <artifactId>KeywordsDetectionAndroidLibrary</artifactId>
	    <version>Tag</version>
	</dependency>
// Or vi jitpack.io sbt:
Add it in your build.sbt at the end of resolvers:
    resolvers += "jitpack" at "https://jitpack.io"
Step 2. Add the dependency
	libraryDependencies += "com.github.frymanofer" % "KeywordsDetectionAndroidLibrary" % "Tag"	
// Or vi jitpack.io leiningen:
Add it in your project.clj at the end of repositories:
    :repositories [["jitpack" "https://jitpack.io"]]
Step 2. Add the dependency
	:dependencies [[com.github.frymanofer/KeywordsDetectionAndroidLibrary "Tag"]]	





## Usage

```java
import com.davoice.keywordsdetection.keywordslibrary.KeyWordsDetection;

