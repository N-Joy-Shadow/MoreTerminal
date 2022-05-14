package njoyshadow.moreterminal.utils;

import appeng.api.definitions.IDefinitions;
import appeng.api.features.IRegistryContainer;

public interface IMTApi {


    IMTDefinitions definitions();

    /**
     * @return Registry Container for the numerous registries in AE2.
     */
    IRegistryContainer registries();
}
