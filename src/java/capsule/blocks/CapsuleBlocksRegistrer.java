package capsule.blocks;

import capsule.items.CapsuleItem;
import capsule.items.CapsuleItemsRegistrer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CapsuleBlocksRegistrer {
	
	public static Block blockCapsuleReference;
	
	public static void createBlocks(String modid) {
		GameRegistry.registerBlock(blockCapsuleReference = new BlockCapsuleMarker("capsulemarker", Material.ground), "capsulemarker");
		GameRegistry.registerTileEntity(TileEntityCapture.class, "capsulemarker_te");
    }
	
	public static void registerRenderers(String modid) {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		mesher.register(Item.getItemFromBlock(blockCapsuleReference), 0, new ModelResourceLocation(modid+":capsulemarker", "inventory"));
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCapture.class, new CaptureTESR());
	}
	
	public static void registerRecipes() {
		ItemStack capsulemarker = new ItemStack(CapsuleBlocksRegistrer.blockCapsuleReference, 1, CapsuleItem.STATE_EMPTY);
		
		// base recipes
		GameRegistry.addRecipe(capsulemarker, new Object[] {"###", "#C#", "###", '#', Items.iron_ingot, 'C', CapsuleItemsRegistrer.capsule});
		
	}
}