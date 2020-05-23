package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.mechanics.dust.DustLayers.Side.BOTTOM
import chylex.hee.game.mechanics.dust.DustLayers.Side.TOP
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.center
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getTile
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.isTopSolid
import chylex.hee.system.util.playServer
import chylex.hee.system.util.size
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer.TRANSLUCENT
import net.minecraft.util.Direction
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters

class BlockJarODust(builder: BlockBuilder) : BlockSimpleShaped(builder, AABB){
	companion object{
		val AABB = AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 0.84375, 0.8125)
		
		const val LAYERS_TAG = "Layers"
	}
	
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityJarODust()
	}
	
	// ItemStack serialization
	
	fun getLayersFromStack(stack: ItemStack): DustLayers?{
		return if (stack.item === this.asItem())
			stack.heeTagOrNull?.getListOfCompounds(LAYERS_TAG)?.let { list -> DustLayers(TileEntityJarODust.DUST_CAPACITY).apply { deserializeNBT(list) } }
		else
			null
	}
	
	fun setLayersInStack(stack: ItemStack, layers: DustLayers){
		if (stack.item === this.asItem()){
			stack.heeTag.putList(LAYERS_TAG, layers.serializeNBT())
		}
	}
	
	// Placement
	
	override fun isValidPosition(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean{
		return super.isValidPosition(state, world, pos) && pos.down().isTopSolid(world)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		return if (facing == DOWN && !isValidPosition(state, world, pos))
			Blocks.AIR.defaultState
		else
			super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: EntityLivingBase?, stack: ItemStack){
		val list = stack.heeTagOrNull?.getListOfCompounds(LAYERS_TAG)
		
		if (list != null){
			pos.getTile<TileEntityJarODust>(world)?.layers?.deserializeNBT(list)
		}
	}
	
	// Drops
	
	private fun getDrop(tile: TileEntityJarODust): ItemStack?{
		return ItemStack(this).also { setLayersInStack(it, tile.layers) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>{
		val drop = (context.get(LootParameters.BLOCK_ENTITY) as? TileEntityJarODust)?.let(::getDrop)
		
		return if (drop != null)
			mutableListOf(drop)
		else
			mutableListOf()
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer): ItemStack{
		return pos.getTile<TileEntityJarODust>(world)?.let(::getDrop) ?: ItemStack(this)
	}
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean{
		return false
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion){
		pos.getTile<TileEntityJarODust>(world)?.let {
			val layers = it.layers
			
			repeat(layers.contents.size){
				spawnAsEntity(world, pos, layers.removeDust(BOTTOM))
			}
		}
		
		Sounds.BLOCK_GLASS_BREAK.playServer(world, pos.center, SoundCategory.BLOCKS)
		super.onBlockExploded(state, world, pos, explosion)
	}
	
	// Interaction
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): Boolean{
		val heldItem = player.getHeldItem(hand)
		
		return if (heldItem.isEmpty)
			tryExtractDust(world, pos, player, hand)
		else
			tryInsertDust(world, pos, player, heldItem)
	}
	
	private fun tryExtractDust(world: World, pos: BlockPos, player: EntityPlayer, hand: Hand): Boolean{
		if (world.isRemote){
			return true
		}
		
		val removed = pos.getTile<TileEntityJarODust>(world)?.layers?.removeDust(if (player.isSneaking) BOTTOM else TOP)
		
		if (removed != null){
			player.setHeldItem(hand, removed)
		}
		
		return true
	}
	
	private fun tryInsertDust(world: World, pos: BlockPos, player: EntityPlayer, stack: ItemStack): Boolean{
		val dustType = DustType.fromStack(stack) ?: return false
		
		if (world.isRemote){
			return true
		}
		
		val tile = pos.getTile<TileEntityJarODust>(world) ?: return true
		val added = tile.layers.addDust(dustType, stack.size)
		
		if (!player.isCreative){
			stack.size -= added
		}
		
		return true
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		val contents = getLayersFromStack(stack)?.contents
		
		if (contents != null){
			val entries = contents
				.groupingBy { it.first }
				.fold(0){ acc, entry -> acc + entry.second }
				.entries
				.sortedWith(compareBy({ -it.value }, { it.key.key }))
			
			for((dustType, dustAmount) in entries){
				lines.add(TextComponentTranslation("block.hee.jar_o_dust.tooltip.entry", dustAmount, TextComponentTranslation(dustType.item.translationKey)))
			}
		}
	}
	
	override fun getRenderLayer() = TRANSLUCENT
}
