package chylex.hee.game.block.properties

import chylex.hee.game.Resource.location
import chylex.hee.game.item.properties.ItemModel
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.ResourceLocation

object BlockStateModels {
	val Manual = BlockStateModel(
		BlockStatePreset.None,
		BlockModel.Manual
	)
	
	fun Cube(modelBlock: Block) = BlockStateModel(
		BlockStatePreset.SimpleFrom(modelBlock),
		BlockModel.Manual,
		ItemModel.FromParent(modelBlock)
	)
	
	val Pillar = BlockStateModel(
		BlockStatePreset.Pillar,
		BlockModel.Manual,
		ItemModel.AsBlock
	)
	
	fun Pillar(modelBlock: Block) = BlockStateModel(
		BlockStatePreset.PillarFrom(modelBlock),
		BlockModel.Manual,
		ItemModel.FromParent(modelBlock)
	)
	
	val Cross = BlockStateModel(
		BlockStatePreset.Simple,
		BlockModel.Cross,
		ItemModel.Simple
	)
	
	val Log = BlockStateModel(
		BlockStatePreset.Log,
		BlockModel.Manual,
		ItemModel.AsBlock
	)
	
	val Fluid = BlockStateModel(
		BlockStatePreset.Simple,
		BlockModel.Fluid
	)
	
	fun Stairs(fullBlock: Block, side: ResourceLocation? = null) = BlockStateModel(
		BlockStatePreset.Stairs(fullBlock, side),
		BlockModel.Manual,
		ItemModel.AsBlock
	)
	
	fun Slab(fullBlock: Block, side: ResourceLocation? = null) = BlockStateModel(
		BlockStatePreset.Slab(fullBlock, side),
		BlockModel.Manual,
		ItemModel.AsBlock
	)
	
	fun Wall(fullBlock: Block) = BlockStateModel(
		BlockStatePreset.Wall(fullBlock),
		BlockModel.Manual,
		ItemModel.Wall
	)
	
	fun Chest(particleTexture: ResourceLocation) = BlockStateModel(
		BlockStatePreset.Simple,
		BlockModel.ParticleOnly(particleTexture),
		ItemModel.FromParent(Blocks.CHEST.asItem().location)
	)
	
	fun Cauldron(fluidTexture: ResourceLocation) = BlockStateModel(
		BlockStatePreset.Cauldron(fluidTexture),
		BlockModel.Manual
	)
	
	fun PottedPlant(plantBlock: Block) = BlockStateModel(
		BlockStatePreset.Simple,
		BlockModel.PottedPlant(plantBlock.location)
	)
	
	fun ItemOnly(itemModel: ItemModel, asItem: Boolean = false) = BlockStateModel(
		BlockStatePreset.None,
		BlockModel.Manual,
		BlockItemModel(itemModel, asItem)
	)
}
