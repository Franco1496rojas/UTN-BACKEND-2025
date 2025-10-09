This project has been prepared to target Java 21 (LTS).

What changed
- The `pom.xml` property `maven.compiler.release` was updated from 17 to 21.
- `maven-compiler-plugin` is configured to use `<release>${maven.compiler.release}</release>` so Maven compiles against Java 21.

How to install JDK 21 on Windows
1. Download Eclipse Temurin (Adoptium) or other JDK 21 distribution from:
   - https://adoptium.net (Temurin)
   - https://jdk.java.net/21/
2. Install the JDK and note the installation path, for example: `C:\Program Files\Eclipse Adoptium\jdk-21.0.2+1`.
3. Open PowerShell as Administrator and set JAVA_HOME and update PATH (temporary for current session):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.2+1'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
javac -version
```

To set it permanently, use System Properties or setx:
```powershell
setx JAVA_HOME 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.2+1' -m
# You may need to reopen terminals for changes to apply.
```

How to build and test the project
1. From project root, run:

```powershell
mvn -v
mvn -DskipTests=false clean test
```

Notes and troubleshooting
- If Maven fails to find a compatible JDK, ensure `java -version` shows a 21.x output in the same terminal where you run `mvn`.
- Some third-party libraries might need upgrades to run on Java 21; open an issue if tests or compilation fail and include the error output.

If you'd like, I can attempt to install JDK 21 on your machine automatically (requires elevated privileges and access to installer), or run the Maven build here if you allow me to run commands in your environment.