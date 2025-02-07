# GeneticConflictSeeker

## Install the provided lib

Run the validation phase:

![validate.png](./lib/validate.png)

After this step, install the library:

![install.png](./lib/install.png)

Once the compile phase does execute, the lib ca-cdr-1.0.1-alpha-22.jar is correctly installed in your local repository, 
just as any other artifact that may have been retrieved from Maven central itself.

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