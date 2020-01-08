package chylex.hee.game.item
import chylex.hee.client.render.util.NO_TINT
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
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import kotlin.math.pow

class ItemEnergyReceptacle(properties: Properties) : ItemAbstractInfusable(properties){
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
		
		private fun hasMovedTooFar(nbt: TagCompound, currentWorld: World, currentPos: BlockPos): Boolean{
			val dimension = currentWorld.dimension
			
			if (dimension.type.id != nbt.getInt(INITIAL_DIMENSION_TAG)){
				return true
			}
			
			if (dimension is WorldProviderEndCustom && TerritoryInstance.fromPos(currentPos) != nbt.getIntegerOrNull(INITIAL_TERRITORY_TAG)?.let(TerritoryInstance.Companion::fromHash)){
				return true
			}
			
			return false
		}
		
		private fun shouldLoseHealth(cluster: TileEntityEnergyCluster, nbt: TagCompound, infusions: InfusionList): Boolean{
			if (infusions.has(SAFETY)){
				return false
			}
			
			if (hasMovedTooFar(nbt, cluster.world!!, cluster.pos)){
				return true
			}
			
			val totalEnergyLost = Internal(nbt.getInt(INITIAL_LEVEL_TAG)) - cluster.energyLevel
			
			if (totalEnergyLost > maxOf(Floating(1F), cluster.energyBaseCapacity * 0.2F)){
				return true
			}
			
			return false
		}
	}
	
	init{
		addPropertyOverride(Resource.Custom("has_cluster")){
			stack, _, _ -> if (stack.heeTagOrNull.hasKey(CLUSTER_SNAPSHOT_TAG)) 1F else 0F
		}
		
		// TODO tweak animation
	}
	
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val stack = player.getHeldItem(context.hand)
		
		stack.heeTag.use {
			if (hasKey(CLUSTER_SNAPSHOT_TAG)){
				val finalPos = BlockEditor.place(ModBlocks.ENERGY_CLUSTER, player, stack, context)
				
				if (world.isRemote){
					return SUCCESS
				}
				
				if (finalPos != null){
					finalPos.getTile<TileEntityEnergyCluster>(world)?.let {
						it.loadClusterSnapshot(ClusterSnapshot(getCompound(CLUSTER_SNAPSHOT_TAG)), inactive = false)
						
						if (shouldLoseHealth(it, this, InfusionTag.getList(stack))){
							it.deteriorateHealth()
						}
					}
					
					remove(CLUSTER_SNAPSHOT_TAG)
					remove(UPDATE_TIME_TAG)
					remove(RENDER_COLOR_TAG)
					remove(INITIAL_LEVEL_TAG)
					remove(INITIAL_DIMENSION_TAG)
					
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
					val dimension = world.dimension
					
					put(CLUSTER_SNAPSHOT_TAG, cluster.getClusterSnapshot().tag)
					
					putLong(UPDATE_TIME_TAG, world.totalTime)
					putInt(RENDER_COLOR_TAG, cluster.color.primary(75F, 80F).i)
					
					putInt(INITIAL_LEVEL_TAG, cluster.energyLevel.internal.value)
					putInt(INITIAL_DIMENSION_TAG, dimension.type.id)
					
					if (dimension is WorldProviderEndCustom){
						TerritoryInstance.fromPos(pos)?.let { putInt(INITIAL_TERRITORY_TAG, it.hash) }
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
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote){
			return
		}
		
		with(stack.heeTagOrNull ?: return){
			if (!hasKey(CLUSTER_SNAPSHOT_TAG)){
				return
			}
			
			val currentTime = world.totalTime
			val ticksElapsed = currentTime - getLong(UPDATE_TIME_TAG)
			
			if (ticksElapsed < ENERGY_LOSS_TICK_RATE){
				return
			}
			
			val snapshot = ClusterSnapshot(getCompound(CLUSTER_SNAPSHOT_TAG))
			val newLevel = calculateNewEnergyLevel(snapshot, ticksElapsed, InfusionTag.getList(stack))
			
			put(CLUSTER_SNAPSHOT_TAG, snapshot.clone(energyLevel = newLevel).tag)
			putLong(UPDATE_TIME_TAG, currentTime)
			
			if (hasMovedTooFar(this, world, Pos(entity))){ // force health deterioration
				putInt(INITIAL_DIMENSION_TAG, Int.MIN_VALUE)
				remove(INITIAL_TERRITORY_TAG)
			}
		}
	}
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		if (world != null){
			val tag = stack.heeTagOrNull
			
			if (!tag.hasKey(CLUSTER_SNAPSHOT_TAG)){
				lines.add(TextComponentTranslation("item.hee.energy_receptacle.tooltip.empty"))
			}
			else{
				val snapshot = ClusterSnapshot(tag.getCompound(CLUSTER_SNAPSHOT_TAG))
				val level = calculateNewEnergyLevel(snapshot, world.totalTime - tag.getLong(UPDATE_TIME_TAG), InfusionTag.getList(stack))
				
				lines.add(TextComponentTranslation("item.hee.energy_receptacle.tooltip.holding", level.displayString))
			}
		}
		
		super.addInformation(stack, world, lines, flags)
	}
	
	@Sided(Side.CLIENT)
	object Color : IItemColor{
		private val WHITE = RGB(255u).i
		
		override fun getColor(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> stack.heeTagOrNull?.getInt(RENDER_COLOR_TAG) ?: WHITE
			else -> NO_TINT
		}
	}
}
