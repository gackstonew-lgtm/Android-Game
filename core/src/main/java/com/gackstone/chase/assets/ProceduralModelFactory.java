package com.gackstone.chase.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Procedural 3D model generator producing high-fidelity stylized 3D meshes
 * for player cars, enemy police units, traffic vehicles, environment props,
 * tracks, and cockpit interiors.
 *
 * All generated models follow libGDX 3D conventions with proper normals,
 * specular materials, and optimized vertex memory footprints.
 */
public class ProceduralModelFactory implements Disposable {

    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Array<Model> builtModels = new Array<>();

    private static final long VERTEX_ATTRIBUTES =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

    public ProceduralModelFactory() {
    }

    /**
     * Creates a sleek aerodynamic sports supercar (Phantom GT).
     */
    public Model createPhantomGTSportsCar(Color primaryColor, Color secondaryColor, Color glowColor) {
        modelBuilder.begin();

        // 1. Lower chassis / body
        Material bodyMat = new Material(
                ColorAttribute.createDiffuse(primaryColor != null ? primaryColor : new Color(0.0f, 0.85f, 1.0f, 1.0f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder bodyBuilder = modelBuilder.part("chassis", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, bodyMat);
        // Main lower deck
        BoxShapeBuilder.build(bodyBuilder, 0, 0.28f, 0, 1.42f, 0.32f, 3.2f);
        // Front nose wedge
        BoxShapeBuilder.build(bodyBuilder, 0, 0.22f, 1.5f, 1.34f, 0.22f, 0.5f);
        // Rear diffuser
        BoxShapeBuilder.build(bodyBuilder, 0, 0.24f, -1.45f, 1.36f, 0.26f, 0.45f);

        // 2. Cabin & Windshield (Glass & Roof)
        Material glassMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.08f, 0.12f, 0.18f, 0.95f)),
                ColorAttribute.createSpecular(new Color(0.9f, 0.95f, 1.0f, 1.0f))
        );
        MeshPartBuilder glassBuilder = modelBuilder.part("cabin", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, glassMat);
        BoxShapeBuilder.build(glassBuilder, 0, 0.54f, -0.1f, 1.12f, 0.36f, 1.5f);

        // 3. Trim / Spoiler / Aerodynamics (Secondary color)
        Material trimMat = new Material(
                ColorAttribute.createDiffuse(secondaryColor != null ? secondaryColor : new Color(0.12f, 0.14f, 0.18f, 1.0f)),
                ColorAttribute.createSpecular(Color.LIGHT_GRAY)
        );
        MeshPartBuilder trimBuilder = modelBuilder.part("trim", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, trimMat);
        // Rear spoiler wing & mounts
        BoxShapeBuilder.build(trimBuilder, 0, 0.82f, -1.42f, 1.48f, 0.06f, 0.32f);
        BoxShapeBuilder.build(trimBuilder, -0.5f, 0.62f, -1.42f, 0.08f, 0.36f, 0.12f);
        BoxShapeBuilder.build(trimBuilder, 0.5f, 0.62f, -1.42f, 0.08f, 0.36f, 0.12f);
        // Side skirts
        BoxShapeBuilder.build(trimBuilder, -0.72f, 0.16f, 0, 0.08f, 0.14f, 2.6f);
        BoxShapeBuilder.build(trimBuilder, 0.72f, 0.16f, 0, 0.08f, 0.14f, 2.6f);

        // 4. Wheels & Rims
        Material wheelMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.05f, 0.05f, 0.07f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.74f, 0.28f, 0.95f, 1.0f, 0.3f, 0.22f);

        // 5. Headlights & Taillights
        Material lightMat = new Material(
                ColorAttribute.createDiffuse(glowColor != null ? glowColor : Color.CYAN),
                ColorAttribute.createEmissive(glowColor != null ? glowColor : Color.CYAN)
        );
        MeshPartBuilder lightBuilder = modelBuilder.part("headlights", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, lightMat);
        BoxShapeBuilder.build(lightBuilder, -0.52f, 0.32f, 1.72f, 0.28f, 0.12f, 0.06f);
        BoxShapeBuilder.build(lightBuilder, 0.52f, 0.32f, 1.72f, 0.28f, 0.12f, 0.06f);

        Material tailMat = new Material(
                ColorAttribute.createDiffuse(Color.RED),
                ColorAttribute.createEmissive(Color.RED)
        );
        MeshPartBuilder tailBuilder = modelBuilder.part("taillights", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, tailMat);
        BoxShapeBuilder.build(tailBuilder, 0, 0.36f, -1.64f, 1.22f, 0.08f, 0.06f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a muscular, aggressive supercharged V8 muscle car (Apex Muscle).
     */
    public Model createApexMuscleCar(Color primaryColor, Color secondaryColor, Color glowColor) {
        modelBuilder.begin();

        // 1. Broad, aggressive body
        Material bodyMat = new Material(
                ColorAttribute.createDiffuse(primaryColor != null ? primaryColor : new Color(1.0f, 0.45f, 0.0f, 1.0f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder bodyBuilder = modelBuilder.part("chassis", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, bodyMat);
        BoxShapeBuilder.build(bodyBuilder, 0, 0.32f, 0, 1.54f, 0.36f, 3.3f);
        // Muscular fender flares
        BoxShapeBuilder.build(bodyBuilder, -0.78f, 0.34f, 0.95f, 0.16f, 0.38f, 0.85f);
        BoxShapeBuilder.build(bodyBuilder, 0.78f, 0.34f, 0.95f, 0.16f, 0.38f, 0.85f);
        BoxShapeBuilder.build(bodyBuilder, -0.78f, 0.34f, -0.95f, 0.16f, 0.38f, 0.85f);
        BoxShapeBuilder.build(bodyBuilder, 0.78f, 0.34f, -0.95f, 0.16f, 0.38f, 0.85f);

        // 2. Cabin with fastback roof
        Material glassMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.1f, 0.12f, 0.15f, 0.95f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder glassBuilder = modelBuilder.part("cabin", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, glassMat);
        BoxShapeBuilder.build(glassBuilder, 0, 0.62f, -0.15f, 1.24f, 0.38f, 1.6f);

        // 3. Supercharger hood scoop & Matte black trim
        Material scoopMat = new Material(
                ColorAttribute.createDiffuse(secondaryColor != null ? secondaryColor : new Color(0.15f, 0.15f, 0.18f, 1.0f)),
                ColorAttribute.createSpecular(Color.LIGHT_GRAY)
        );
        MeshPartBuilder scoopBuilder = modelBuilder.part("scoop", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, scoopMat);
        BoxShapeBuilder.build(scoopBuilder, 0, 0.54f, 0.75f, 0.44f, 0.18f, 0.65f);
        // Front chin splitter
        BoxShapeBuilder.build(scoopBuilder, 0, 0.14f, 1.68f, 1.56f, 0.08f, 0.22f);

        // 4. Heavy wheels
        Material wheelMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.06f, 0.06f, 0.08f, 1.0f)),
                ColorAttribute.createSpecular(Color.GRAY)
        );
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.82f, 0.32f, 0.95f, 1.0f, 0.34f, 0.26f);

        // 5. Lights
        Material lightMat = new Material(
                ColorAttribute.createDiffuse(glowColor != null ? glowColor : Color.YELLOW),
                ColorAttribute.createEmissive(glowColor != null ? glowColor : Color.ORANGE)
        );
        MeshPartBuilder lightBuilder = modelBuilder.part("headlights", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, lightMat);
        // Quad round headlights
        BoxShapeBuilder.build(lightBuilder, -0.52f, 0.34f, 1.66f, 0.18f, 0.18f, 0.06f);
        BoxShapeBuilder.build(lightBuilder, -0.28f, 0.34f, 1.66f, 0.18f, 0.18f, 0.06f);
        BoxShapeBuilder.build(lightBuilder, 0.28f, 0.34f, 1.66f, 0.18f, 0.18f, 0.06f);
        BoxShapeBuilder.build(lightBuilder, 0.52f, 0.34f, 1.66f, 0.18f, 0.18f, 0.06f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a heavy armored tactical assault cruiser (Vanguard Titan).
     */
    public Model createVanguardArmoredCruiser(Color primaryColor, Color secondaryColor, Color glowColor) {
        modelBuilder.begin();

        // 1. Reinforced monolithic hull
        Material bodyMat = new Material(
                ColorAttribute.createDiffuse(primaryColor != null ? primaryColor : new Color(0.85f, 0.2f, 0.25f, 1.0f)),
                ColorAttribute.createSpecular(Color.GRAY)
        );
        MeshPartBuilder bodyBuilder = modelBuilder.part("chassis", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, bodyMat);
        BoxShapeBuilder.build(bodyBuilder, 0, 0.45f, 0, 1.68f, 0.55f, 3.4f);

        // 2. Armored slit cabin & turret roof
        Material armorMat = new Material(
                ColorAttribute.createDiffuse(secondaryColor != null ? secondaryColor : new Color(0.18f, 0.2f, 0.24f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder armorBuilder = modelBuilder.part("armor", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, armorMat);
        // Elevated cabin
        BoxShapeBuilder.build(armorBuilder, 0, 0.85f, -0.2f, 1.48f, 0.45f, 1.8f);
        // Heavy steel push bumper
        BoxShapeBuilder.build(armorBuilder, 0, 0.36f, 1.78f, 1.76f, 0.42f, 0.25f);
        // Roof armor rack
        BoxShapeBuilder.build(armorBuilder, 0, 1.12f, -0.2f, 1.32f, 0.12f, 1.4f);

        // 3. Heavy oversized all-terrain wheels
        Material wheelMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.08f, 0.08f, 0.1f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.90f, 0.38f, 1.05f, 1.15f, 0.42f, 0.32f);

        // 4. Tactical floodlights
        Material lightMat = new Material(
                ColorAttribute.createDiffuse(glowColor != null ? glowColor : Color.RED),
                ColorAttribute.createEmissive(glowColor != null ? glowColor : Color.RED)
        );
        MeshPartBuilder lightBuilder = modelBuilder.part("headlights", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, lightMat);
        BoxShapeBuilder.build(lightBuilder, -0.62f, 0.55f, 1.72f, 0.32f, 0.16f, 0.08f);
        BoxShapeBuilder.build(lightBuilder, 0.62f, 0.55f, 1.72f, 0.32f, 0.16f, 0.08f);
        // Roof searchlight bar
        BoxShapeBuilder.build(lightBuilder, 0, 1.18f, 0.45f, 1.1f, 0.12f, 0.1f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a Highway Patrol Police Interceptor with roof LED lightbar and police livery.
     */
    public Model createPatrolCruiserEnemy() {
        modelBuilder.begin();

        // 1. White & Black pursuit chassis
        Material whiteMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.98f, 1.0f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder bodyBuilder = modelBuilder.part("chassis_white", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, whiteMat);
        BoxShapeBuilder.build(bodyBuilder, 0, 0.32f, 0, 1.5f, 0.34f, 3.2f);

        // Black doors & pushbar
        Material blackMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.08f, 0.09f, 0.12f, 1.0f)),
                ColorAttribute.createSpecular(Color.GRAY)
        );
        MeshPartBuilder blackBuilder = modelBuilder.part("chassis_black", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, blackMat);
        // Push bar
        BoxShapeBuilder.build(blackBuilder, 0, 0.32f, 1.68f, 1.25f, 0.35f, 0.18f);
        // Hood & Trunk black panels
        BoxShapeBuilder.build(blackBuilder, 0, 0.5f, 0.85f, 1.2f, 0.04f, 0.85f);
        BoxShapeBuilder.build(blackBuilder, 0, 0.5f, -1.05f, 1.2f, 0.04f, 0.75f);

        // 2. Cabin & Tinted Windows
        Material glassMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.12f, 0.16f, 0.22f, 0.95f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder glassBuilder = modelBuilder.part("cabin", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, glassMat);
        BoxShapeBuilder.build(glassBuilder, 0, 0.62f, -0.05f, 1.22f, 0.36f, 1.55f);

        // 3. Roof Police Siren Lightbar (Red, Blue, White)
        Material sirenRed = new Material(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createEmissive(Color.RED));
        Material sirenBlue = new Material(ColorAttribute.createDiffuse(Color.BLUE), ColorAttribute.createEmissive(Color.CYAN));
        Material sirenMount = new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY));

        MeshPartBuilder mountBuilder = modelBuilder.part("siren_mount", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, sirenMount);
        BoxShapeBuilder.build(mountBuilder, 0, 0.82f, -0.05f, 1.15f, 0.05f, 0.15f);

        MeshPartBuilder redBuilder = modelBuilder.part("siren_red", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, sirenRed);
        BoxShapeBuilder.build(redBuilder, -0.35f, 0.88f, -0.05f, 0.45f, 0.1f, 0.2f);

        MeshPartBuilder blueBuilder = modelBuilder.part("siren_blue", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, sirenBlue);
        BoxShapeBuilder.build(blueBuilder, 0.35f, 0.88f, -0.05f, 0.45f, 0.1f, 0.2f);

        // 4. Wheels
        Material wheelMat = new Material(ColorAttribute.createDiffuse(new Color(0.08f, 0.08f, 0.1f, 1.0f)));
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.78f, 0.3f, 0.95f, 1.0f, 0.32f, 0.24f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates an armored Tactical SWAT Interceptor SUV.
     */
    public Model createTacticalSUVEnemy() {
        modelBuilder.begin();

        // Heavy dark armor
        Material swatMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.12f, 0.14f, 0.18f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder bodyBuilder = modelBuilder.part("body", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, swatMat);
        // Main SUV Box
        BoxShapeBuilder.build(bodyBuilder, 0, 0.6f, 0, 1.72f, 0.75f, 3.6f);
        // Front Ramming Bumper
        BoxShapeBuilder.build(bodyBuilder, 0, 0.4f, 1.9f, 1.82f, 0.5f, 0.3f);
        // Side steps
        BoxShapeBuilder.build(bodyBuilder, -0.92f, 0.25f, 0, 0.14f, 0.08f, 2.8f);
        BoxShapeBuilder.build(bodyBuilder, 0.92f, 0.25f, 0, 0.14f, 0.08f, 2.8f);

        // Siren Lightbar
        Material sirenRed = new Material(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createEmissive(Color.RED));
        Material sirenBlue = new Material(ColorAttribute.createDiffuse(Color.BLUE), ColorAttribute.createEmissive(Color.CYAN));
        MeshPartBuilder redBuilder = modelBuilder.part("siren_red", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, sirenRed);
        BoxShapeBuilder.build(redBuilder, -0.4f, 1.05f, 0.3f, 0.5f, 0.12f, 0.22f);

        MeshPartBuilder blueBuilder = modelBuilder.part("siren_blue", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, sirenBlue);
        BoxShapeBuilder.build(blueBuilder, 0.4f, 1.05f, 0.3f, 0.5f, 0.12f, 0.22f);

        // Huge Off-road Wheels
        Material wheelMat = new Material(ColorAttribute.createDiffuse(new Color(0.06f, 0.06f, 0.08f, 1.0f)));
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.92f, 0.4f, 1.1f, 1.15f, 0.44f, 0.34f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a civilian traffic sedan.
     */
    public Model createTrafficSedan(Color color) {
        modelBuilder.begin();

        Material bodyMat = new Material(ColorAttribute.createDiffuse(color != null ? color : Color.LIGHT_GRAY));
        MeshPartBuilder body = modelBuilder.part("body", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, bodyMat);
        BoxShapeBuilder.build(body, 0, 0.3f, 0, 1.4f, 0.32f, 2.9f);

        Material glassMat = new Material(ColorAttribute.createDiffuse(new Color(0.15f, 0.18f, 0.22f, 1.0f)));
        MeshPartBuilder glass = modelBuilder.part("cabin", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, glassMat);
        BoxShapeBuilder.build(glass, 0, 0.56f, -0.1f, 1.16f, 0.32f, 1.4f);

        Material wheelMat = new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY));
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.74f, 0.26f, 0.85f, 0.9f, 0.28f, 0.2f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a civilian box delivery truck.
     */
    public Model createTrafficTruck(Color cabColor) {
        modelBuilder.begin();

        // Cab
        Material cabMat = new Material(ColorAttribute.createDiffuse(cabColor != null ? cabColor : Color.WHITE));
        MeshPartBuilder cab = modelBuilder.part("cab", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, cabMat);
        BoxShapeBuilder.build(cab, 0, 0.7f, 1.3f, 1.7f, 0.9f, 1.4f);

        // Cargo box
        Material cargoMat = new Material(ColorAttribute.createDiffuse(new Color(0.75f, 0.78f, 0.82f, 1.0f)));
        MeshPartBuilder box = modelBuilder.part("box", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, cargoMat);
        BoxShapeBuilder.build(box, 0, 1.1f, -0.6f, 1.85f, 1.5f, 3.2f);

        // Wheels
        Material wheelMat = new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY));
        MeshPartBuilder wheelBuilder = modelBuilder.part("wheels", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        addFourWheels(wheelBuilder, 0.92f, 0.35f, 1.2f, 1.2f, 0.38f, 0.3f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a detailed multi-lane road segment with asphalt, road markings, and curbs.
     */
    public Model createDetailedRoadSegment(float roadWidth, float segmentLength, Color asphaltColor, Color markingColor) {
        modelBuilder.begin();

        // 1. Asphalt Deck
        Material asphaltMat = new Material(
                ColorAttribute.createDiffuse(asphaltColor != null ? asphaltColor : new Color(0.16f, 0.18f, 0.22f, 1.0f)),
                ColorAttribute.createSpecular(new Color(0.12f, 0.12f, 0.12f, 1.0f))
        );
        MeshPartBuilder asphaltBuilder = modelBuilder.part("asphalt", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, asphaltMat);
        BoxShapeBuilder.build(asphaltBuilder, 0, 0.05f, 0, roadWidth, 0.1f, segmentLength);

        // 2. Road Markings (Dashed lane dividers & solid edge lines)
        Material markingMat = new Material(
                ColorAttribute.createDiffuse(markingColor != null ? markingColor : new Color(0.0f, 0.85f, 1.0f, 1.0f)),
                ColorAttribute.createEmissive(new Color(markingColor != null ? markingColor.r * 0.4f : 0.0f, markingColor != null ? markingColor.g * 0.4f : 0.3f, markingColor != null ? markingColor.b * 0.4f : 0.4f, 1.0f))
        );
        MeshPartBuilder markingBuilder = modelBuilder.part("markings", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, markingMat);

        // Outer solid lane borders
        float borderOffset = roadWidth * 0.5f - 0.6f;
        BoxShapeBuilder.build(markingBuilder, -borderOffset, 0.11f, 0, 0.18f, 0.02f, segmentLength);
        BoxShapeBuilder.build(markingBuilder, borderOffset, 0.11f, 0, 0.18f, 0.02f, segmentLength);

        // Dashed lane lines (e.g. at -2.5m and +2.5m)
        float[] laneDividers = {-roadWidth * 0.25f, roadWidth * 0.25f};
        float dashLength = 4.0f;
        float dashGap = 4.0f;
        float currentZ = -segmentLength * 0.5f + dashLength * 0.5f;

        while (currentZ < segmentLength * 0.5f) {
            for (float divX : laneDividers) {
                BoxShapeBuilder.build(markingBuilder, divX, 0.11f, currentZ, 0.16f, 0.02f, dashLength);
            }
            currentZ += dashLength + dashGap;
        }

        // 3. Concrete curbs
        Material curbMat = new Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.38f, 0.42f, 1.0f)));
        MeshPartBuilder curbBuilder = modelBuilder.part("curbs", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, curbMat);
        float curbOffset = roadWidth * 0.5f + 0.25f;
        BoxShapeBuilder.build(curbBuilder, -curbOffset, 0.18f, 0, 0.5f, 0.26f, segmentLength);
        BoxShapeBuilder.build(curbBuilder, curbOffset, 0.18f, 0, 0.5f, 0.26f, segmentLength);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a high-detail guard rail with posts and horizontal beams.
     */
    public Model createDetailedGuardRail(float length, Color railColor) {
        modelBuilder.begin();

        Material railMat = new Material(
                ColorAttribute.createDiffuse(railColor != null ? railColor : new Color(0.85f, 0.25f, 0.15f, 1.0f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder beamBuilder = modelBuilder.part("beam", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, railMat);
        // Double W-beam profile
        BoxShapeBuilder.build(beamBuilder, 0, 0.48f, 0, 0.14f, 0.38f, length);

        // Support posts spaced every 6 meters
        Material postMat = new Material(ColorAttribute.createDiffuse(new Color(0.28f, 0.3f, 0.35f, 1.0f)));
        MeshPartBuilder postBuilder = modelBuilder.part("posts", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, postMat);

        float spacing = 6.0f;
        float curZ = -length * 0.5f + spacing * 0.5f;
        while (curZ < length * 0.5f) {
            BoxShapeBuilder.build(postBuilder, 0, 0.25f, curZ, 0.16f, 0.55f, 0.16f);
            curZ += spacing;
        }

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a modern street lamp with an emissive lantern head.
     */
    public Model createStreetLight(Color emissiveColor) {
        modelBuilder.begin();

        Material poleMat = new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.28f, 0.32f, 1.0f)));
        MeshPartBuilder pole = modelBuilder.part("pole", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, poleMat);
        // Vertical mast
        BoxShapeBuilder.build(pole, 0, 3.5f, 0, 0.25f, 7.0f, 0.25f);
        // Overhanging arm
        BoxShapeBuilder.build(pole, 0.9f, 6.9f, 0, 1.8f, 0.2f, 0.2f);

        // Emissive light fixture
        Material lightMat = new Material(
                ColorAttribute.createDiffuse(emissiveColor != null ? emissiveColor : Color.CYAN),
                ColorAttribute.createEmissive(emissiveColor != null ? emissiveColor : Color.CYAN)
        );
        MeshPartBuilder lamp = modelBuilder.part("lamp", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, lightMat);
        BoxShapeBuilder.build(lamp, 1.7f, 6.75f, 0, 0.6f, 0.15f, 0.35f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a multi-story skyscraper / commercial building with lit window grids.
     */
    public Model createCitySkyscraper(float width, float height, float depth, Color facadeColor, Color windowGlow) {
        modelBuilder.begin();

        // 1. Concrete / Metallic Facade
        Material facadeMat = new Material(
                ColorAttribute.createDiffuse(facadeColor != null ? facadeColor : new Color(0.12f, 0.14f, 0.18f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder facade = modelBuilder.part("facade", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, facadeMat);
        BoxShapeBuilder.build(facade, 0, height * 0.5f, 0, width, height, depth);

        // 2. Glowing Neon Windows / Billboard Accent
        Material windowMat = new Material(
                ColorAttribute.createDiffuse(windowGlow != null ? windowGlow : Color.CYAN),
                ColorAttribute.createEmissive(windowGlow != null ? windowGlow : new Color(0.0f, 0.4f, 0.7f, 1.0f))
        );
        MeshPartBuilder windows = modelBuilder.part("windows", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, windowMat);

        // Create horizontal glowing bands and rooftop antenna
        float bandSpacing = 6.0f;
        float curY = 4.0f;
        while (curY < height - 2.0f) {
            BoxShapeBuilder.build(windows, 0, curY, depth * 0.5f + 0.05f, width * 0.85f, 1.2f, 0.08f);
            curY += bandSpacing;
        }

        // Rooftop communication spire
        BoxShapeBuilder.build(windows, 0, height + 4.0f, 0, 0.35f, 8.0f, 0.35f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a desert rock canyon mesa.
     */
    public Model createDesertMesa(float width, float height, float depth, Color rockColor) {
        modelBuilder.begin();

        Material rockMat = new Material(
                ColorAttribute.createDiffuse(rockColor != null ? rockColor : new Color(0.65f, 0.35f, 0.2f, 1.0f)),
                ColorAttribute.createSpecular(new Color(0.05f, 0.05f, 0.05f, 1.0f))
        );
        MeshPartBuilder mesa = modelBuilder.part("rock", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, rockMat);

        // Tiered steps simulating sandstone strata
        BoxShapeBuilder.build(mesa, 0, height * 0.25f, 0, width, height * 0.5f, depth);
        BoxShapeBuilder.build(mesa, 0, height * 0.65f, 0, width * 0.75f, height * 0.45f, depth * 0.75f);
        BoxShapeBuilder.build(mesa, 0, height * 0.92f, 0, width * 0.5f, height * 0.2f, depth * 0.5f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a desert saguaro cactus prop.
     */
    public Model createDesertCactus() {
        modelBuilder.begin();

        Material cactusMat = new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.45f, 0.22f, 1.0f)));
        MeshPartBuilder builder = modelBuilder.part("cactus", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, cactusMat);

        // Main trunk
        BoxShapeBuilder.build(builder, 0, 2.0f, 0, 0.5f, 4.0f, 0.5f);
        // Left arm
        BoxShapeBuilder.build(builder, -0.6f, 1.8f, 0, 0.8f, 0.4f, 0.4f);
        BoxShapeBuilder.build(builder, -0.9f, 2.6f, 0, 0.4f, 1.4f, 0.4f);
        // Right arm
        BoxShapeBuilder.build(builder, 0.6f, 2.2f, 0, 0.8f, 0.4f, 0.4f);
        BoxShapeBuilder.build(builder, 0.9f, 3.0f, 0, 0.4f, 1.4f, 0.4f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates industrial stacked cargo containers.
     */
    public Model createShippingContainer(Color containerColor) {
        modelBuilder.begin();

        Material mat = new Material(
                ColorAttribute.createDiffuse(containerColor != null ? containerColor : new Color(0.85f, 0.35f, 0.15f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder builder = modelBuilder.part("container", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, mat);
        // Standard ISO container proportions (W: 2.4m, H: 2.6m, L: 6.0m)
        BoxShapeBuilder.build(builder, 0, 1.3f, 0, 2.4f, 2.6f, 6.0f);

        // Corner posts & edge ribs
        Material cornerMat = new Material(ColorAttribute.createDiffuse(new Color(0.15f, 0.15f, 0.18f, 1.0f)));
        MeshPartBuilder ribBuilder = modelBuilder.part("ribs", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, cornerMat);
        BoxShapeBuilder.build(ribBuilder, -1.22f, 1.3f, 0, 0.08f, 2.65f, 6.05f);
        BoxShapeBuilder.build(ribBuilder, 1.22f, 1.3f, 0, 0.08f, 2.65f, 6.05f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates an interactive first-person cockpit dashboard and steering wheel.
     */
    public Model createCockpitDashboard() {
        modelBuilder.begin();

        // 1. Dashboard body
        Material dashMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.1f, 0.11f, 0.14f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder dash = modelBuilder.part("dashboard", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, dashMat);
        // Main console
        BoxShapeBuilder.build(dash, 0, -0.2f, 0.6f, 1.3f, 0.35f, 0.7f);
        // Instrument cluster hood
        BoxShapeBuilder.build(dash, -0.32f, 0.04f, 0.62f, 0.48f, 0.18f, 0.45f);

        // 2. Glowing Instrument Dials (Speedometer / Tachometer / Boost gauge)
        Material gaugeMat = new Material(
                ColorAttribute.createDiffuse(Color.CYAN),
                ColorAttribute.createEmissive(Color.CYAN)
        );
        MeshPartBuilder gauges = modelBuilder.part("gauges", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, gaugeMat);
        BoxShapeBuilder.build(gauges, -0.42f, 0.0f, 0.38f, 0.16f, 0.16f, 0.02f);
        BoxShapeBuilder.build(gauges, -0.22f, 0.0f, 0.38f, 0.16f, 0.16f, 0.02f);

        // Center Infotainment Navigation Screen
        Material navMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.0f, 0.8f, 0.5f, 1.0f)),
                ColorAttribute.createEmissive(new Color(0.0f, 0.4f, 0.25f, 1.0f))
        );
        MeshPartBuilder nav = modelBuilder.part("nav_screen", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, navMat);
        BoxShapeBuilder.build(nav, 0.15f, -0.05f, 0.45f, 0.35f, 0.22f, 0.02f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates an animated steering wheel for first-person cockpit view.
     */
    public Model createSteeringWheel() {
        modelBuilder.begin();

        Material wheelMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.18f, 0.18f, 0.22f, 1.0f)),
                ColorAttribute.createSpecular(Color.LIGHT_GRAY)
        );
        MeshPartBuilder wheel = modelBuilder.part("steering_wheel", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, wheelMat);
        // Outer rim (stylized polygonal ring)
        BoxShapeBuilder.build(wheel, 0, 0.15f, 0, 0.42f, 0.04f, 0.04f);
        BoxShapeBuilder.build(wheel, 0, -0.15f, 0, 0.42f, 0.04f, 0.04f);
        BoxShapeBuilder.build(wheel, -0.2f, 0, 0, 0.04f, 0.32f, 0.04f);
        BoxShapeBuilder.build(wheel, 0.2f, 0, 0, 0.04f, 0.32f, 0.04f);
        // Center hub and spokes
        BoxShapeBuilder.build(wheel, 0, 0, 0, 0.12f, 0.12f, 0.06f);
        BoxShapeBuilder.build(wheel, 0, 0, 0, 0.38f, 0.03f, 0.03f);
        BoxShapeBuilder.build(wheel, 0, -0.07f, 0, 0.03f, 0.15f, 0.03f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a traffic cone obstacle.
     */
    public Model createTrafficCone() {
        modelBuilder.begin();

        Material coneMat = new Material(
                ColorAttribute.createDiffuse(new Color(1.0f, 0.4f, 0.0f, 1.0f)),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder cone = modelBuilder.part("cone", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, coneMat);
        // Base plate
        BoxShapeBuilder.build(cone, 0, 0.03f, 0, 0.5f, 0.06f, 0.5f);
        // Tiered conical body
        BoxShapeBuilder.build(cone, 0, 0.25f, 0, 0.36f, 0.4f, 0.36f);
        BoxShapeBuilder.build(cone, 0, 0.55f, 0, 0.22f, 0.3f, 0.22f);
        BoxShapeBuilder.build(cone, 0, 0.75f, 0, 0.12f, 0.2f, 0.12f);

        // Reflective white band
        Material whiteMat = new Material(
                ColorAttribute.createDiffuse(Color.WHITE),
                ColorAttribute.createSpecular(Color.WHITE)
        );
        MeshPartBuilder white = modelBuilder.part("white_band", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, whiteMat);
        BoxShapeBuilder.build(white, 0, 0.42f, 0, 0.3f, 0.12f, 0.3f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    /**
     * Creates a concrete highway barrier obstacle.
     */
    public Model createConcreteBarrier() {
        modelBuilder.begin();

        Material concreteMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.7f, 0.72f, 0.75f, 1.0f)),
                ColorAttribute.createSpecular(Color.DARK_GRAY)
        );
        MeshPartBuilder barrier = modelBuilder.part("barrier", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, concreteMat);
        // Jersey barrier profile (wide base, tapered top)
        BoxShapeBuilder.build(barrier, 0, 0.2f, 0, 1.4f, 0.4f, 1.8f);
        BoxShapeBuilder.build(barrier, 0, 0.55f, 0, 0.8f, 0.45f, 1.8f);

        // Warning reflector stripes
        Material reflectMat = new Material(
                ColorAttribute.createDiffuse(Color.YELLOW),
                ColorAttribute.createEmissive(new Color(0.6f, 0.5f, 0.0f, 1.0f))
        );
        MeshPartBuilder reflect = modelBuilder.part("reflectors", GL20.GL_TRIANGLES, VERTEX_ATTRIBUTES, reflectMat);
        BoxShapeBuilder.build(reflect, -0.42f, 0.5f, 0, 0.04f, 0.15f, 0.8f);
        BoxShapeBuilder.build(reflect, 0.42f, 0.5f, 0, 0.04f, 0.15f, 0.8f);

        Model model = modelBuilder.end();
        builtModels.add(model);
        return model;
    }

    private void addFourWheels(MeshPartBuilder builder, float halfTrack, float radius, float frontZ, float rearZ, float diameter, float width) {
        // Front Left
        BoxShapeBuilder.build(builder, -halfTrack, radius, frontZ, width, diameter, diameter);
        // Front Right
        BoxShapeBuilder.build(builder, halfTrack, radius, frontZ, width, diameter, diameter);
        // Rear Left
        BoxShapeBuilder.build(builder, -halfTrack, radius, -rearZ, width, diameter, diameter);
        // Rear Right
        BoxShapeBuilder.build(builder, halfTrack, radius, -rearZ, width, diameter, diameter);
    }

    @Override
    public void dispose() {
        for (Model model : builtModels) {
            model.dispose();
        }
        builtModels.clear();
    }
}
