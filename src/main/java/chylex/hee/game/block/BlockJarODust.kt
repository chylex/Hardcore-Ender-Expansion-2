package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockDropsComponent
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.components.IBlockExplodedComponent
import chylex.hee.game.block.components.IBlockPlacementComponent
import chylex.hee.game.block.components.IBlockShapeComponent
import chylex.hee.game.block.components.IPlayerUseBlockComponent
import chylex.hee.game.block.components.ISetBlockStateFromNeighbor
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.TRANSLUCENT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.util.size
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.mechanics.dust.DustLayers.Side.BOTTOM
import chylex.hee.game.mechanics.dust.DustLayers.Side.TOP
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.isTopSolid
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModSounds
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.math.center
import chylex.hee.util.nbt.getListOfCompounds
import chylex.hee.util.nbt.putList
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext.Builder
import net.minecraft.loot.LootParameters
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

object BlockJarODust : HeeBlockBuilder() {
	init {
		localization = LocalizationStrategy.ReplaceWords("O", "o'")
		localizationExtra["block.hee.jar_o_dust.tooltip.entry"] = "§7%sx %s"
		
		model = BlockStateModel(BlockStatePreset.Simple, BlockModel.Manual)
		renderLayer = TRANSLUCENT
		
		material = Materials.JAR_O_DUST
		color = MaterialColor.ORANGE_TERRACOTTA
		sound = SoundType.METAL
		
		drop = BlockDrop.Manual
		hardness = BlockHardness(hardness = 0.4F, resistance = 0F)
		
		components.tooltip = object : ITooltipComponent {
			override fun add(lines: MutableList<ITextComponent>, stack: ItemStack, advanced: Boolean, world: IBlockReader?) {
				val contents = getLayersFromStack(stack)?.contents ?: return
				val entries = contents
					.groupingBy { it.first }
					.fold(0) { acc, entry -> acc + entry.second }
					.entries
					.sortedWith(compareBy({ -it.value }, { it.key.key }))
				
				for ((dustType, dustAmount) in entries) {
					lines.add(TranslationTextComponent("block.hee.jar_o_dust.tooltip.entry", dustAmount, TranslationTextComponent(dustType.item.translationKey)))
				}
			}
		}
		
		components.shape = IBlockShapeComponent.of(VoxelShapes.create(0.1875, 0.0, 0.1875, 0.8125, 0.84375, 0.8125))
		
		components.drops = object : IBlockDropsComponent {
			override fun getDrops(state: BlockState, context: Builder): MutableList<ItemStack> {
				val drop = (context.get(LootParameters.BLOCK_ENTITY) as? TileEntityJarODust)?.let(::getDrop)
				
				return if (drop == null)
					mutableListOf()
				else
					mutableListOf(drop)
			}
			
			override fun getPickBlock(state: BlockState, world: IBlockReader, pos: BlockPos): ItemStack? {
				return pos.getTile<TileEntityJarODust>(world)?.let(::getDrop)
			}
		}
		
		components.entity = IBlockEntityComponent(::TileEntityJarODust)
		
		components.placement = object : IBlockPlacementComponent {
			override fun isPositionValid(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean {
				return pos.down().isTopSolid(world)
			}
			
			override fun onPlacedBy(state: BlockState, world: World, pos: BlockPos, placer: LivingEntity?, stack: ItemStack) {
				val list = stack.heeTagOrNull?.getListOfCompounds(TileEntityJarODust.LAYERS_TAG)
				if (list != null) {
					pos.getTile<TileEntityJarODust>(world)?.layers?.deserializeNBT(list)
				}
			}
		}
		
		components.setStateFromNeighbor = ISetBlockStateFromNeighbor { state, world, pos, neighborFacing, _ ->
			if (neighborFacing == DOWN && !state.isValidPosition(world, pos))
				Blocks.AIR.defaultState
			else
				state
		}
		
		components.playerUse = IPlayerUseBlockComponent { _, world, pos, player, hand ->
			val heldItem = player.getHeldItem(hand)
			
			val result = if (heldItem.isEmpty)
				tryExtractDust(world, pos, player, hand)
			else
				tryInsertDust(world, pos, player, heldItem)
			
			if (result) SUCCESS else PASS
		}
		
		components.onExploded = object : IBlockExplodedComponent {
			override fun canDrop(explosion: Explosion): Boolean {
				return false
			}
			
			override fun onExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion) {
				pos.getTile<TileEntityJarODust>(world)?.let {
					val layers = it.layers
					
					repeat(layers.contents.size) {
						Block.spawnAsEntity(world, pos, layers.removeDust(BOTTOM))
					}
				}
				
				ModSounds.BLOCK_JAR_O_DUST_SHATTER.playServer(world, pos.center, SoundCategory.BLOCKS)
			}
		}
	}
	
	// ItemStack
	
	fun getLayersFromStack(stack: ItemStack): DustLayers? {
		return if (stack.item === ModBlocks.JAR_O_DUST.asItem())
			stack.heeTagOrNull?.getListOfCompounds(TileEntityJarODust.LAYERS_TAG)?.let { list -> DustLayers(TileEntityJarODust.DUST_CAPACITY).apply { deserializeNBT(list) } }
		else
			null
	}
	
	fun setLayersInStack(stack: ItemStack, layers: DustLayers) {
		if (stack.item === ModBlocks.JAR_O_DUST.asItem()) {
			stack.heeTag.putList(TileEntityJarODust.LAYERS_TAG, layers.serializeNBT())
		}
	}
	
	// Drops
	
	private fun getDrop(tile: TileEntityJarODust): ItemStack {
		return ItemStack(ModBlocks.JAR_O_DUST).also { setLayersInStack(it, tile.layers) }
	}
	
	// Interaction
	
	private fun tryExtractDust(world: World, pos: BlockPos, player: PlayerEntity, hand: Hand): Boolean {
		if (world.isRemote) {
			return true
		}
		
		val removed = pos.getTile<TileEntityJarODust>(world)?.layers?.removeDust(if (player.isSneaking) BOTTOM else TOP)
		if (removed != null) {
			player.setHeldItem(hand, removed)
		}
		
		return true
	}
	
	private fun tryInsertDust(world: World, pos: BlockPos, player: PlayerEntity, stack: ItemStack): Boolean {
		val dustType = DustType.fromStack(stack) ?: return false
		
		if (world.isRemote) {
			return true
		}
		
		val tile = pos.getTile<TileEntityJarODust>(world) ?: return true
		val added = tile.layers.addDust(dustType, stack.size)
		
		if (!player.isCreative) {
			stack.size -= added
		}
		
		return true
	}
}
