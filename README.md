# pvsstudio-maven-plugin
Maven plugin for add comments to java files to use free version of PVS-Studio

```java
<build>
    <plugins>
        <plugin>
            <groupId>com.zvpdev.maven.plugins</groupId>
            <artifactId>pvsstudio-maven-plugin</artifactId>
            <version>1.0</version>
            <executions>
                <execution>
                    <id>pvsstudio-free-comments</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>add-comment</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <commentType>PRIVATE</commentType>
            </configuration>
        </plugin>
    </plugins>
</build>
```
