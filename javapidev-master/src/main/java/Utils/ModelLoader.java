package Utils;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.io.File;
import java.util.Random;

/**
 * Utility class for loading and handling 3D models
 */
public class ModelLoader {
    
    private static final Random random = new Random();
    
    /**
     * Attempts to load a 3D model from a GLB file
     * Note: This is a placeholder that will need to be implemented with a proper GLB loader
     * 
     * @param modelPath Path to the GLB model file
     * @return A Group containing the model representation (currently a placeholder)
     */
    public static Group loadGlbModel(String modelPath) {
        // TODO: Implement actual GLB loading with a third-party library
        
        // For now, check if the file exists
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            System.err.println("Model file not found: " + modelPath);
            return createPlaceholderModel();
        }
        
        System.out.println("Loading GLB model from: " + modelPath);
        
        // For the initial implementation, return a placeholder model
        // This will be replaced with actual GLB loading later
        return createEnergyCity();
    }
    
    /**
     * Creates a placeholder 3D model for testing purposes
     * 
     * @return A Group containing a placeholder 3D model
     */
    public static Group createPlaceholderModel() {
        Group modelGroup = new Group();
        
        // Create a simple box as placeholder
        Box box = new Box(200, 200, 200);
        
        // Create material with checkered pattern
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.LIGHTBLUE);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(32);
        box.setMaterial(material);
        
        // Add text indicating this is a placeholder
        modelGroup.getChildren().add(box);
        
        return modelGroup;
    }
    
    /**
     * Creates a representation of a green energy city
     * This is used as a placeholder for the actual GLB model
     * 
     * @return A Group containing the city model
     */
    public static Group createEnergyCity() {
        Group cityGroup = new Group();
        
        // Create ground
        Box ground = new Box(800, 20, 800);
        PhongMaterial groundMaterial = new PhongMaterial(Color.web("#4CAF50").darker());
        ground.setMaterial(groundMaterial);
        ground.setTranslateY(200);
        
        // Add ground to city
        cityGroup.getChildren().add(ground);
        
        // Add buildings
        addBuildings(cityGroup);
        
        // Add wind turbines
        addWindTurbines(cityGroup);
        
        // Add solar panels
        addSolarPanels(cityGroup);
        
        // Add central energy hub
        addEnergyHub(cityGroup);
        
        return cityGroup;
    }
    
    private static void addBuildings(Group cityGroup) {
        // Create grid of buildings
        for (int x = -350; x <= 350; x += 80) {
            for (int z = -350; z <= 350; z += 80) {
                // Skip center area for energy hub
                if (Math.abs(x) < 150 && Math.abs(z) < 150) {
                    continue;
                }
                
                // Skip some positions randomly
                if (random.nextDouble() < 0.3) {
                    continue;
                }
                
                // Create building with random height
                double height = 50 + random.nextDouble() * 200;
                Box building = new Box(40, height, 40);
                
                // Set material with green glass appearance
                PhongMaterial material = new PhongMaterial();
                
                // Randomly choose between green and blue tints for buildings
                if (random.nextBoolean()) {
                    // Green glass buildings
                    material.setDiffuseColor(Color.rgb(100 + random.nextInt(50), 
                                                      200 + random.nextInt(55),
                                                      100 + random.nextInt(50), 0.8));
                } else {
                    // Blue glass buildings
                    material.setDiffuseColor(Color.rgb(100 + random.nextInt(30),
                                                      180 + random.nextInt(50),
                                                      200 + random.nextInt(55), 0.8));
                }
                
                material.setSpecularColor(Color.WHITE);
                material.setSpecularPower(64);
                building.setMaterial(material);
                
                // Position building
                building.setTranslateX(x);
                building.setTranslateY(200 - height/2);
                building.setTranslateZ(z);
                
                cityGroup.getChildren().add(building);
            }
        }
    }
    
    private static void addWindTurbines(Group cityGroup) {
        // Add wind turbines around the perimeter
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            double x = Math.sin(angle) * 400;
            double z = Math.cos(angle) * 400;
            
            Group turbine = createWindTurbine();
            turbine.setTranslateX(x);
            turbine.setTranslateY(0);
            turbine.setTranslateZ(z);
            
            // Rotate to face center
            turbine.getTransforms().add(new Rotate(Math.toDegrees(angle), Rotate.Y_AXIS));
            
            cityGroup.getChildren().add(turbine);
        }
    }
    
    private static Group createWindTurbine() {
        Group turbineGroup = new Group();
        
        // Create tower
        Cylinder tower = new Cylinder(5, 300);
        PhongMaterial towerMaterial = new PhongMaterial(Color.LIGHTGRAY);
        tower.setMaterial(towerMaterial);
        tower.setTranslateY(50);
        
        // Create blades hub
        Sphere hub = new Sphere(10);
        PhongMaterial hubMaterial = new PhongMaterial(Color.WHITE);
        hub.setMaterial(hubMaterial);
        hub.setTranslateY(-100);
        
        // Create blades
        for (int i = 0; i < 3; i++) {
            Box blade = new Box(100, 5, 20);
            PhongMaterial bladeMaterial = new PhongMaterial(Color.WHITE);
            blade.setMaterial(bladeMaterial);
            blade.setTranslateY(-100);
            
            // Rotate blade to position
            blade.getTransforms().add(new Rotate(i * 120, Rotate.Z_AXIS));
            
            turbineGroup.getChildren().add(blade);
        }
        
        turbineGroup.getChildren().addAll(tower, hub);
        return turbineGroup;
    }
    
    private static void addSolarPanels(Group cityGroup) {
        // Create some solar panel arrays
        for (int i = 0; i < 4; i++) {
            Group solarArray = createSolarPanelArray();
            
            // Position arrays in different locations
            switch (i) {
                case 0:
                    solarArray.setTranslateX(-300);
                    solarArray.setTranslateZ(0);
                    break;
                case 1:
                    solarArray.setTranslateX(300);
                    solarArray.setTranslateZ(0);
                    break;
                case 2:
                    solarArray.setTranslateX(0);
                    solarArray.setTranslateZ(-300);
                    break;
                case 3:
                    solarArray.setTranslateX(0);
                    solarArray.setTranslateZ(300);
                    break;
            }
            
            cityGroup.getChildren().add(solarArray);
        }
    }
    
    private static Group createSolarPanelArray() {
        Group arrayGroup = new Group();
        
        // Create a grid of panels
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Box panel = new Box(20, 2, 30);
                PhongMaterial panelMaterial = new PhongMaterial(Color.DARKBLUE);
                panelMaterial.setSpecularColor(Color.LIGHTBLUE);
                panelMaterial.setSpecularPower(50);
                panel.setMaterial(panelMaterial);
                
                // Position panel in grid
                panel.setTranslateX((col - 2) * 25);
                panel.setTranslateY(180);
                panel.setTranslateZ((row - 2) * 35);
                
                // Tilt panel toward the sun
                panel.getTransforms().add(new Rotate(30, Rotate.X_AXIS));
                
                arrayGroup.getChildren().add(panel);
            }
        }
        
        return arrayGroup;
    }
    
    private static void addEnergyHub(Group cityGroup) {
        // Create central energy hub
        Group hubGroup = new Group();
        
        // Base cylinder
        Cylinder base = new Cylinder(100, 50);
        PhongMaterial baseMaterial = new PhongMaterial(Color.web("#0288D1"));
        base.setMaterial(baseMaterial);
        base.setTranslateY(175);
        
        // Middle section
        Cylinder middle = new Cylinder(70, 100);
        PhongMaterial middleMaterial = new PhongMaterial(Color.web("#03A9F4"));
        middle.setMaterial(middleMaterial);
        middle.setTranslateY(100);
        
        // Top dome
        Sphere dome = new Sphere(80);
        PhongMaterial domeMaterial = new PhongMaterial(Color.web("#4FC3F7"));
        domeMaterial.setSpecularColor(Color.WHITE);
        domeMaterial.setSpecularPower(80);
        dome.setMaterial(domeMaterial);
        dome.setTranslateY(30);
        
        // Energy beam
        Cylinder beam = new Cylinder(5, 100);
        PhongMaterial beamMaterial = new PhongMaterial(Color.web("#76FF03"));
        beam.setMaterial(beamMaterial);
        beam.setTranslateY(-20);
        
        // Energy sphere at top
        Sphere energySphere = new Sphere(20);
        PhongMaterial sphereMaterial = new PhongMaterial(Color.web("#EEFF41"));
        energySphere.setMaterial(sphereMaterial);
        energySphere.setTranslateY(-80);
        
        hubGroup.getChildren().addAll(base, middle, dome, beam, energySphere);
        cityGroup.getChildren().add(hubGroup);
    }
} 