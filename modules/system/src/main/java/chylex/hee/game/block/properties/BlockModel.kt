package chylex.hee.game.block.properties

import chylex.hee.game.Resource
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.system.isVanilla
import chylex.hee.system.path
import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation

sealed class BlockModel : IBlockStateModel {
	override val blockState
		get() = BlockStatePreset.Simple
	
	override val blockModel
		get() = this
	
	override val itemModel
		get() = BlockItemModel(ItemModel.AsBlock)
	
	class SimpleBlockModel internal constructor(val parent: ResourceLocation, val textureName: String, val textureLocation: ResourceLocation?) : BlockModel()
	
	companion object {
		val Cube = Cube()
		val Cross = Cross()
		val Leaves = SimpleBlockModel(Resource.Vanilla("block/leaves"), "all", null)
		
		fun Cube(texture: ResourceLocation? = null) = SimpleBlockModel(Resource.Vanilla("block/cube_all"), "all", texture)
		fun Cross(texture: ResourceLocation? = null) = SimpleBlockModel(Resource.Vanilla("block/cross"), "cross", texture)
		fun PottedPlant(texture: ResourceLocation? = null) = SimpleBlockModel(Resource.Vanilla("block/flower_pot_cross"), "plant", texture)
	}
	
	class CubeBottomTop(val side: ResourceLocation? = null, val bottom: ResourceLocation? = null, val top: ResourceLocation? = null) : BlockModel()
	class PortalFrame(val frameBlock: Block, val topSuffix: String) : BlockModel()
	class ParticleOnly(val particle: ResourceLocation) : BlockModel()
	
	class Parent(val name: String, val parent: ResourceLocation) : BlockModel()
	class FromParent(val parent: ResourceLocation) : BlockModel() {
		constructor(block: Block) : this(Resource("block/" + block.path, block.isVanilla))
	}
	
	class Suffixed(val suffix: String, val wrapped: BlockModel = Cube): BlockModel()
	class WithTextures(val baseModel: BlockModel, val textures: Map<String, ResourceLocation>) : BlockModel()
	class NoAmbientOcclusion(val baseModel: BlockModel) : BlockModel()
	
	class Multi(vararg val models: BlockModel) : BlockModel() {
		constructor(models: List<BlockModel>) : this(*models.toTypedArray())
	}
	
	object CubeColumn : BlockModel()
	object Table : BlockModel()
	object Fluid : BlockModel()
	object Manual : BlockModel()
}
