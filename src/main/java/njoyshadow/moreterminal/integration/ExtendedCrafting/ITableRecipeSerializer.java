package njoyshadow.moreterminal.integration.ExtendedCrafting;

import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;

import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public interface ITableRecipeSerializer<T extends ITableRecipe> extends net.minecraftforge.registries.IForgeRegistryEntry<IRecipeSerializer<?>> {
    ShapedTableRecipe.Serializer CRAFTING_SHAPED = register("extended_crafting_shaped", new ShapedTableRecipe.Serializer());
    ShapedTableRecipe.Serializer CRAFTING_SHAPELESS = register("extended_crafting_shapeless", new ShapedTableRecipe.Serializer());

    T read(ResourceLocation recipeId, JsonObject json);

    @javax.annotation.Nullable
    T read(ResourceLocation recipeId, PacketBuffer buffer);

    static <S extends IRecipeSerializer<T>, T extends ITableRecipe> S register(String key, S recipeSerializer) {
        return Registry.register(Registry.RECIPE_SERIALIZER, key, recipeSerializer);


    }

}
