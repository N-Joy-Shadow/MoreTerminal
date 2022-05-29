package njoyshadow.moreterminal.utils;

import appeng.api.definitions.*;
import appeng.bootstrap.FeatureFactory;
import appeng.core.api.definitions.ApiParts;
import appeng.core.features.ItemDefinition;
import appeng.core.features.registries.PartModels;
import njoyshadow.moreterminal.api.utlis.IMTDefinitions;

import njoyshadow.moreterminal.item.part.MTParts;

public class MTDefinitions implements IMTDefinitions {
    private final MTParts parts;
    private final FeatureFactory registry = new FeatureFactory();


    public MTDefinitions(final PartModels partModels){
        this.parts = new MTParts(this.registry , partModels);
    }
    public FeatureFactory getRegistry() {
        return registry;
    }

    @Override
    public MTParts parts() {
        return this.parts;
    }
}
