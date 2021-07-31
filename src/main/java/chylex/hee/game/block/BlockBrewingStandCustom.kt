package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource.location
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockItemModel
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.IBlockStateModel
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.setBlock
import chylex.hee.init.ModContainers
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.BrewingStandBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.BrewingStandTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

open class BlockBrewingStandCustom(builder: BlockBuilder) : BrewingStandBlock(builder.p), IHeeBlock {
	override val model: IBlockStateModel
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.WithTextures(BlockModel.FromParent(Blocks.BREWING_STAND), mapOf(
				"particle" to Blocks.BREWING_STAND.location,
				"base" to Blocks.BREWING_STAND.location("_base"),
				"stand" to this.location,
			)),
			BlockItemModel(ItemModel.Simple, asItem = true)
		)
	
	final override val renderLayer
		get() = CUTOUT
	
	override val drop: BlockDrop
		get() = BlockDrop.NamedTile
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityBrewingStandCustom()
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		if (world.isRemote) {
			return SUCCESS
		}
		
		val tile = pos.getTile<BrewingStandTileEntity>(world)
		
		if (tile is TileEntityBrewingStandCustom) {
			ModContainers.open(player, tile, pos)
		}
		else {
			// POLISH maybe make the tile entity upgrade smoother but this is fine lol
			pos.breakBlock(world, false)
			pos.setBlock(world, this)
		}
		
		return SUCCESS
	}
	
	class Override(builder: BlockBuilder) : BlockBrewingStandCustom(builder) {
		override val localization
			get() = LocalizationStrategy.None
		
		override val model
			get() = BlockStateModels.Manual
		
		override val drop
			get() = BlockDrop.Manual
	}
}
