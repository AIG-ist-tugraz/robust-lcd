# GeneticConflictSeeker

## Build jar files

Activate the Maven profile **package** to build the jar files.

![package.jpg](./lib/package.jpg)

After the build is complete, you can find the jar files in the **target** folder.

![jar.jpg](./lib/jar.jpg)

## gc_seeker-jar-with-dependencies.jar

This app is designed for feature models, e.g., DELL.splx.

```shell
java -jar gc_seeker-jar-with-dependencies.jar -cfg ./conf/gc_seeker.cfg
```

## gc_seeker_apm-jar-with-dependencies.jar

This app is designed for APM KB.

```shell
java -jar gc_seeker_apm-jar-with-dependencies.jar -cfg ./conf/gc_seeker_apm.cfg
```

## gc_seeker_camera-jar-with-dependencies.jar

This app is designed for CameraKB.

```shell
java -jar gc_seeker_camera-jar-with-dependencies.jar -cfg ./conf/gc_seeker_camera.cfg
```