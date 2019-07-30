package chylex.hee.game.item
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.infusion.Infusion.SAFETY
import chylex.hee.game.item.infusion.Infusion.STABILITY
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.displayString
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import chylex.hee.system.util.Pos
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.pow

class ItemEnergyReceptacle : ItemAbstractInfusable(){
	private companion object{
		private const val CLUSTER_SNAPSHOT_TAG = "Cluster"
		private const val UPDATE_TIME_TAG = "UpdateTime"
		private const val RENDER_COLOR_TAG = "RenderColor"
		
		private const val INITIAL_LEVEL_TAG = "OrigLevel"
		private const val INITIAL_DIMENSION_TAG = "OrigDim"
		private const val INITIAL_TERRITORY_TAG = "OrigTerritory"
		
		private const val ENERGY_LOSS_TICK_RATE = 10L
		private const val ITEM_COOLDOWN = 16
		
		private fun calculateNewEnergyLevel(snapshot: ClusterSnapshot, elapsedTicks: Long, infusions: InfusionList): IEnergyQuantity{
			// TODO make sure Table Pedestals keep updating the item, or at least perform an update just before the infusion
			val power = if (infusions.has(STABILITY)) 0.0003F else 0.001F
			
			val decreasePerCycle = Floating(snapshot.energyCapacity.floating.value.pow(power) - 1F)
			val elapsedCycles = elapsedTicks / ENERGY_LOSS_TICK_RATE
			
			return snapshot.energyLevel - (decreasePerCycle * elapsedCycles.toFloat())
		}
		
		private fun hasMovedTooFar(nbt: NBTTagCompound, currentWorld: World, currentPos: BlockPos): Boolean{
			val provider = currentWorld.provider
			
			if (provider.dimension != nbt.getInteger(INITIAL_DIMENSION_TAG)){
				return true
			}
			
			if (provider is WorldProviderEndCustom && TerritoryInstance.fromPos(currentPos) != nbt.getIntegerOrNull(INITIAL_TERRITORY_TAG)?.let(TerritoryInstance.Companion::fromHash)){
				return true
			}
			
			return false
		}
		
		private fun shouldLoseHealth(cluster: TileEntityEnergyCluster, nbt: NBTTagCompound, infusions: InfusionList): Boolean{
			if (infusions.has(SAFETY)){
				return false
			}
			
			if (hasMovedTooFar(nbt, cluster.world, cluster.pos)){
				return true
			}
			
			val totalEnergyLost = Internal(nbt.getInteger(INITIAL_LEVEL_TAG)) - cluster.energyLevel
			
			if (totalEnergyLost > maxOf(Floating(1F), cluster.energyBaseCapacity * 0.2F)){
				return true
			}
			
			return false
		}
	}
	
	init{
		maxStackSize = 1
		
		addPropertyOverride(Resource.Custom("has_cluster")){
			stack, _, _ -> if (stack.heeTagOrNull.hasKey(CLUSTER_SNAPSHOT_TAG)) 1F else 0F
		}
		
		// TODO tweak animation
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		val stack = player.getHeldItem(hand)
		
		with(stack.heeTag){
			if (hasKey(CLUSTER_SNAPSHOT_TAG)){
				val finalPos = BlockEditor.place(ModBlocks.ENERGY_CLUSTER, player, hand, stack, pos, facing, hitX, hitY, hitZ)
				
				if (world.isRemote){
					return SUCCESS
				}
				
				if (finalPos != null){
					finalPos.getTile<TileEntityEnergyCluster>(world)?.let {
						it.loadClusterSnapshot(ClusterSnapshot(getCompoundTag(CLUSTER_SNAPSHOT_TAG)), inactive = false)
						
						if (shouldLoseHealth(it, this, InfusionTag.getList(stack))){
							it.deteriorateHealth()
						}
					}
					
					removeTag(CLUSTER_SNAPSHOT_TAG)
					removeTag(UPDATE_TIME_TAG)
					removeTag(RENDER_COLOR_TAG)
					removeTag(INITIAL_LEVEL_TAG)
					removeTag(INITIAL_DIMENSION_TAG)
					
					player.cooldownTracker.setCooldown(this@ItemEnergyReceptacle, ITEM_COOLDOWN)
					return SUCCESS
				}
			}
			else if (BlockEditor.canEdit(pos, player, stack)){
				if (world.isRemote){
					return SUCCESS
				}
				
				val cluster = pos.getTile<TileEntityEnergyCluster>(world)
				
				if (cluster != null && cluster.tryDisturb()){
					val provider = world.provider
					
					setTag(CLUSTER_SNAPSHOT_TAG, cluster.getClusterSnapshot().tag)
					
					setLong(UPDATE_TIME_TAG, world.totalWorldTime)
					setInteger(RENDER_COLOR_TAG, cluster.color.primary(75F, 80F).toInt())
					
					setInteger(INITIAL_LEVEL_TAG, cluster.energyLevel.internal.value)
					setInteger(INITIAL_DIMENSION_TAG, provider.dimension)
					
					if (provider is WorldProviderEndCustom){
						TerritoryInstance.fromPos(pos)?.let { setInteger(INITIAL_TERRITORY_TAG, it.hash) }
					}
					
					cluster.breakWithoutExplosion = true
					pos.breakBlock(world, false)
					
					player.cooldownTracker.setCooldown(this@ItemEnergyReceptacle, ITEM_COOLDOWN)
					return SUCCESS
				}
			}
		}
		
		return FAIL
	}
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote){
			return
		}
		
		with(stack.heeTagOrNull ?: return){
			if (!hasKey(CLUSTER_SNAPSHOT_TAG)){
				return
			}
			
			val currentTime = world.totalWorldTime
			val ticksElapsed = currentTime - getLong(UPDATE_TIME_TAG)
			
			if (ticksElapsed < ENERGY_LOSS_TICK_RATE){
				return
			}
			
			val snapshot = ClusterSnapshot(getCompoundTag(CLUSTER_SNAPSHOT_TAG))
			val newLevel = calculateNewEnergyLevel(snapshot, ticksElapsed, InfusionTag.getList(stack))
			
			setTag(CLUSTER_SNAPSHOT_TAG, snapshot.clone(energyLevel = newLevel).tag)
			setLong(UPDATE_TIME_TAG, currentTime)
			
			if (hasMovedTooFar(this, world, Pos(entity))){ // force health deterioration
				setInteger(INITIAL_DIMENSION_TAG, Int.MIN_VALUE)
				removeTag(INITIAL_TERRITORY_TAG)
			}
		}
	}
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		if (world != null){
			val tag = stack.heeTagOrNull
			
			if (!tag.hasKey(CLUSTER_SNAPSHOT_TAG)){
				lines.add(I18n.format("item.hee.energy_receptacle.tooltip.empty"))
			}
			else{
				val snapshot = ClusterSnapshot(tag.getCompoundTag(CLUSTER_SNAPSHOT_TAG))
				val level = calculateNewEnergyLevel(snapshot, world.totalWorldTime - tag.getLong(UPDATE_TIME_TAG), InfusionTag.getList(stack))
				
				lines.add(I18n.format("item.hee.energy_receptacle.tooltip.holding", level.displayString))
			}
		}
		
		super.addInformation(stack, world, lines, flags)
	}
	
	@SideOnly(Side.CLIENT)
	object Color : IItemColor{
		private const val NONE = -1
		private val WHITE = RGB(255, 255, 255).toInt()
		
		override fun colorMultiplier(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> stack.heeTagOrNull?.getInteger(RENDER_COLOR_TAG) ?: WHITE
			else -> NONE
		}
	}
}
