package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.mechanics.dust.DustLayers.Side.BOTTOM
import chylex.hee.game.mechanics.dust.DustLayers.Side.TOP
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getTile
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.isTopSolid
import chylex.hee.system.util.setAir
import chylex.hee.system.util.size
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer.TRANSLUCENT
import net.minecraft.util.Hand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.IBlockReader
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
	/* UPDATE
	override fun neighborChanged(state: BlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (!pos.down().isTopSolid(world)){
			dropBlockAsItem(world, pos, state, 0)
			pos.setAir(world)
		}
	}*/
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: EntityLivingBase?, stack: ItemStack){
		val list = stack.heeTagOrNull?.getListOfCompounds(LAYERS_TAG)
		
		if (list != null){
			pos.getTile<TileEntityJarODust>(world)?.layers?.deserializeNBT(list)
		}
	}
	
	// Drops
	
	private fun getDrop(world: IBlockReader, pos: BlockPos): ItemStack?{
		return pos.getTile<TileEntityJarODust>(world)?.let { tile -> ItemStack(this).also { setLayersInStack(it, tile.layers) } }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack>{
		val drop = context.get(LootParameters.POSITION)?.let { getDrop(context.world, it) }
		
		return if (drop != null)
			mutableListOf(drop)
		else
			mutableListOf()
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack{
		return getDrop(world, pos) ?: ItemStack(this)
	}
	/* UPDATE
	override fun removedByPlayer(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean{
		if (willHarvest){
			return true // skip super call before drops
		}
		
		return super.removedByPlayer(state, world, pos, player, willHarvest)
	}*/
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack){
		super.harvestBlock(world, player, pos, state, tile, stack)
		pos.setAir(world)
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
				lines.add(TextComponentTranslation("tile.hee.jar_o_dust.tooltip.entry", dustAmount, TextComponentTranslation(dustType.item.translationKey)))
			}
		}
	}
	
	override fun getRenderLayer() = TRANSLUCENT
}
