package njoyshadow.moreterminal.item.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

public class FindFluid extends FluidBlockWrapper {

    public FindFluid(IFluidBlock fluidBlock, Level world, BlockPos blockPos) {
        super(fluidBlock, world, blockPos);

    }

    public static void asdasd(){
         var asd = Fluid.FLUID_STATE_REGISTRY;
         asd.forEach((x) -> {
             System.out.println("FLUID : " + x.getType().getRegistryName());
         });
    }
}
