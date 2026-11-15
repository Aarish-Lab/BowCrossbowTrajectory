# BowCrossbowTrajectory (Minecraft 26.1.2, Fabric Loader 0.20.1, Java 25)

## Build
1. Install JDK 25 and Gradle 9.4+ (any recent Gradle), then in this folder run `gradle wrapper` once (creates gradlew).
2. `./gradlew build` -> `build/libs/BowCrossbowTrajectory.jar`
3. Put the jar in `mods/` together with Fabric API (26.1.2 build) and Mod Menu 18.x.
Physics unit test (no Minecraft needed): `javac -d out src/main/java/com/bowcrossbow/trajectory/{CollisionProbe,TrajectoryCalculator,MultishotHandler}.java test/PhysicsTest.java && java -cp out PhysicsTest`

## NOT yet compiled against Minecraft
Written against the 26.1 Mojang-named API from documentation; if the compiler complains, the likeliest lines are:
- TrajectoryRenderer: `RenderTypes.lines()` package, `VertexConsumer.setLineWidth`, `LevelRenderEvents.AFTER_ENTITIES`, `Camera.position()`, poseStack being camera-relative
- CrossbowHandler: `ChargedProjectiles.isEmpty()/contains()`, `ItemStack.getEnchantments().keySet()`
- KeybindManager: `KeyMapping.Category.register`, `Identifier.fromNamespaceAndPath`, `mc.gui.getChat().addMessage`
- CollisionDetector: `Level.getMinY()`, `Level.getEntities(...)`
- ConfigScreen: widget constructors (`StringWidget`, `EditBox`, `AbstractSliderButton`)
