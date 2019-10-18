package com.github.draylar.locate.api;

import net.minecraft.world.gen.feature.StructureFeature;

import java.util.ArrayList;
import java.util.List;

public class LocateRegistry {

    private static ArrayList<String> locatableFeatures = new ArrayList<>();

    public static void registerLocatableFeature(StructureFeature feature) {
        locatableFeatures.add(feature.getName());
    }

    public static List<String> getLocatableFeatures() {
        return locatableFeatures;
    }

    private LocateRegistry() {
        // NO-OP
    }
}
