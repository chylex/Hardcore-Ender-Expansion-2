package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.mechanics.dust.DustLayers.Side.BOTTOM
import chylex.hee.game.mechanics.dust.DustLayers.Side.TOP
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.setAir
import chylex.hee.system.util.size
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.EnumPushReaction.DESTROY
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer.TRANSLUCENT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class BlockJarODust(builder: BlockSimple.Builder) : BlockSimpleShaped(builder, AABB), ITileEntityProvider{
	companion object{
		val AABB = AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 0.84375, 0.8125)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityJarODust()
	}
	
	// ItemStack serialization
	
	fun getLayersFromStack(stack: ItemStack): DustLayers?{
		return if (stack.item === Item.getItemFromBlock(this))
			stack.heeTagOrNull?.getListOfCompounds("Layers")?.let { list -> DustLayers(TileEntityJarODust.DUST_CAPACITY).apply { deserializeNBT(list) } }
		else
			null
	}
	
	fun setLayersInStack(stack: ItemStack, layers: DustLayers){
		if (stack.item === Item.getItemFromBlock(this)){
			stack.heeTag.setList("Layers", layers.serializeNBT())
		}
	}
	
	// Placement
	
	override fun canPlaceBlockAt(world: World, pos: BlockPos): Boolean{
		return super.canPlaceBlockAt(world, pos) && pos.down().let { it.getState(world).isSideSolid(world, it, UP) }
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (!pos.down().let { it.getState(world).isSideSolid(world, it, UP) }){
			dropBlockAsItem(world, pos, state, 0)
			pos.setAir(world)
		}
	}
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack){
		val list = stack.heeTagOrNull?.getListOfCompounds("Layers")
		
		if (list != null){
			pos.getTile<TileEntityJarODust>(world)?.layers?.deserializeNBT(list)
		}
	}
	
	// Drops
	
	private fun getDrop(world: IBlockAccess, pos: BlockPos): ItemStack?{
		return pos.getTile<TileEntityJarODust>(world)?.let { tile -> ItemStack(this).also { setLayersInStack(it, tile.layers) } }
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		getDrop(world, pos)?.let(drops::add)
	}
	
	override fun getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack{
		return getDrop(world, pos) ?: ItemStack(this)
	}
	
	override fun removedByPlayer(state: IBlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean{
		if (willHarvest){
			return true // skip super call before drops
		}
		
		return super.removedByPlayer(state, world, pos, player, willHarvest)
	}
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, tile: TileEntity?, stack: ItemStack){
		super.harvestBlock(world, player, pos, state, tile, stack)
		pos.setAir(world)
	}
	
	// Interaction
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		val heldItem = player.getHeldItem(hand)
		
		return if (heldItem.isEmpty)
			tryExtractDust(world, pos, player, hand)
		else
			tryInsertDust(world, pos, player, heldItem)
	}
	
	private fun tryExtractDust(world: World, pos: BlockPos, player: EntityPlayer, hand: EnumHand): Boolean{
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
	
	override fun getPushReaction(state: IBlockState) = DESTROY
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		val contents = getLayersFromStack(stack)?.contents
		
		if (contents != null){
			val entries = contents
				.groupingBy { it.first }
				.fold(0){ acc, entry -> acc + entry.second }
				.entries
				.sortedWith(compareBy({ -it.value }, { it.key.key }))
			
			for((dustType, dustAmount) in entries){
				lines.add(I18n.format("tile.hee.jar_o_dust.tooltip.entry", dustAmount, I18n.format("${dustType.item.translationKey}.name")))
			}
		}
	}
	
	override fun getRenderLayer() = TRANSLUCENT
}
