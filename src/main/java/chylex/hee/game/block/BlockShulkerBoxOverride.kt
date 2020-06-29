package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityShulkerBoxCustom
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.MagicValues
import chylex.hee.system.migration.vanilla.BlockShulkerBox
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.migration.vanilla.TileEntityShulkerBox
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nbt
import net.minecraft.block.BlockState
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.DyeColor
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.ShulkerBoxTileEntity.AnimationStatus.CLOSED
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.storage.loot.LootContext.Builder
import net.minecraft.world.storage.loot.LootParameters
import java.util.stream.IntStream

class BlockShulkerBoxOverride(properties: Properties, color: DyeColor?) : BlockShulkerBox(color, properties){
	companion object{
		val ALL_BLOCKS
			get() = listOf(null, *DyeColor.values()).map { BlockShulkerBox.getBlockByColor(it) as BlockShulkerBox }
	}
	
	enum class BoxSize(val slots: Int, val translationKey: String){ // TODO add some that distinguishes them in the model/texture
		SMALL (1 * 9, "block.hee.small_shulker_box"),
		MEDIUM(2 * 9, "block.hee.medium_shulker_box"),
		LARGE (3 * 9, "block.hee.large_shulker_box");
		
		val slotIndices: IntArray = IntStream.range(0, slots).toArray()
	}
	
	override fun getTranslationKey(): String{
		return "block.hee.shulker_box"
	}
	
	override fun createNewTileEntity(world: IBlockReader): TileEntity{
		return TileEntityShulkerBoxCustom()
	}
	
	override fun getDrops(state: BlockState, builder: Builder): MutableList<ItemStack>{
		val tile = builder.get(LootParameters.BLOCK_ENTITY)
		
		if (tile !is TileEntityShulkerBox){
			return mutableListOf()
		}
		
		return mutableListOf(getColoredItemStack(color).apply {
			nbt.put(MagicValues.TILE_ENTITY_TAG, tile.write(TagCompound()))
		})
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): ActionResultType{
		if (world.isRemote || player.isSpectator){
			return SUCCESS
		}
		
		pos.getTile<TileEntityShulkerBox>(world)?.let {
			val facing = state.get(FACING)
			
			val isNotBlocked = when(it.animationStatus){
				CLOSED -> VoxelShapes
					.fullCube()
					.boundingBox
					.expand(0.5 * facing.xOffset, 0.5 * facing.yOffset, 0.5 * facing.zOffset)
					.contract(facing.xOffset.toDouble(), facing.yOffset.toDouble(), facing.zOffset.toDouble())
					.offset(pos.offset(facing))
					.let(world::hasNoCollisions)
				
				else -> true
			}
			
			if (isNotBlocked){
				if (it is TileEntityShulkerBoxCustom){
					ModContainers.open(player, it, it.boxSize.slots)
				}
				else{
					ModContainers.open(player, object : INamedContainerProvider by it {
						override fun getDisplayName() = TextComponentTranslation(BoxSize.LARGE.translationKey)
					}, BoxSize.LARGE.slots)
				}
				
				player.addStat(Stats.OPEN_SHULKER_BOX)
			}
			
			return SUCCESS
		}
		
		return PASS
	}
}
