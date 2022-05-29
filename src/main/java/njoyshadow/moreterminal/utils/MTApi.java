package njoyshadow.moreterminal.utils;

import appeng.api.client.IClientHelper;
import appeng.api.definitions.IDefinitions;
import appeng.api.features.IRegistryContainer;
import appeng.core.ApiDefinitions;
import appeng.core.api.ApiClientHelper;
import appeng.core.api.ApiPart;
import appeng.core.features.registries.PartModels;
import appeng.core.features.registries.RegistryContainer;
import njoyshadow.moreterminal.api.utlis.IMTApi;

public class MTApi implements IMTApi {

    public static final MTApi INSTANCE = new MTApi();


    private final IRegistryContainer registryContainer;
    private final MTDefinitions definitions;

    private final ApiPart partHelper;

    public static IMTApi instance() {
        return INSTANCE;
    }
    private MTApi(){
        this.registryContainer = new RegistryContainer();
        this.partHelper = new ApiPart();
        this.definitions = new MTDefinitions((PartModels) this.registryContainer.partModels());
    }


    public PartModels getPartModels() {
        return (PartModels) this.registryContainer.partModels();
    }


    @Override
    public MTDefinitions definitions() {
        return this.definitions;
    }

    @Override
    public IRegistryContainer registries() {
        return this.registryContainer;
    }
}
