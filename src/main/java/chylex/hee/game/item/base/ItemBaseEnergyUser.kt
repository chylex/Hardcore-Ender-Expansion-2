package chylex.hee.game.item.base
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.system.util.Pos
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getTile
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHand.OFF_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.commons.lang3.math.Fraction
import kotlin.math.max
import kotlin.math.pow

abstract class ItemBaseEnergyUser : Item(){
	init{
		maxStackSize = 1
	}
	
	protected abstract val energyCapacity: Units
	protected abstract val energyPerUse: Fraction
	
	// Energy storage
	
	private companion object{
		const val ENERGY_LEVEL_TAG = "EnergyLevel"
		
		const val CLUSTER_POS_TAG = "ClusterPos"
		const val CLUSTER_TICK_OFFSET_TAG = "ClusterTick"
		
		private fun removeClusterTags(nbt: NBTTagCompound){
			nbt.removeTag(CLUSTER_POS_TAG)
			nbt.removeTag(CLUSTER_TICK_OFFSET_TAG)
		}
	}
	
	private val internalEnergyCapacity
		get() = energyCapacity.value * energyPerUse.denominator
	
	private fun getEnergyLevel(stack: ItemStack): Short{
		return stack.heeTagOrNull?.getShort(ENERGY_LEVEL_TAG) ?: 0
	}
	
	private fun offsetEnergyLevel(stack: ItemStack, byAmount: Int): Boolean{
		with(stack.heeTag){
			val prevLevel = getShort(ENERGY_LEVEL_TAG)
			val newLevel = (prevLevel + byAmount).coerceIn(0, internalEnergyCapacity).toShort()
			
			setShort(ENERGY_LEVEL_TAG, newLevel)
			return prevLevel != newLevel
		}
	}
	
	fun chargeEnergyUnit(stack: ItemStack): Boolean = offsetEnergyLevel(stack, energyPerUse.denominator)
	
	fun useEnergyUnit(stack: ItemStack): Boolean = offsetEnergyLevel(stack, -energyPerUse.numerator)
	
	fun setChargePercentage(stack: ItemStack, percentage: Float){
		stack.heeTag.setShort(ENERGY_LEVEL_TAG, (internalEnergyCapacity * percentage).floorToInt().coerceIn(0, internalEnergyCapacity).toShort())
	}
	
	// Energy handling
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		val tile = pos.getTile<TileEntityEnergyCluster>(world)
		val stack = player.getHeldItem(hand)
		
		if (tile == null || !player.canPlayerEdit(pos, facing, stack)){
			return FAIL
		}
		else if (world.isRemote){
			return SUCCESS
		}
		
		with(stack.heeTag){
			if (hasKey(CLUSTER_POS_TAG)){
				removeClusterTags(this)
			}
			else if (pos.distanceTo(player) <= player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).attributeValue){
				setLong(CLUSTER_POS_TAG, pos.toLong())
				setByte(CLUSTER_TICK_OFFSET_TAG, (4L - (world.totalWorldTime % 4L)).toByte())
			}
		}
		
		return SUCCESS
	}
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote || entity !is EntityPlayer){
			return
		}
		
		with(stack.heeTagOrNull ?: return){
			if (hasKey(CLUSTER_POS_TAG) && (world.totalWorldTime + getByte(CLUSTER_TICK_OFFSET_TAG)) % 4L == 0L){
				val pos = Pos(getLong(CLUSTER_POS_TAG))
				val tile = pos.getTile<TileEntityEnergyCluster>(world)
				
				if ((isSelected || entity.getHeldItem(OFF_HAND) == stack) &&
					getShort(ENERGY_LEVEL_TAG) < internalEnergyCapacity &&
					pos.distanceTo(entity) <= entity.getEntityAttribute(EntityPlayer.REACH_DISTANCE).attributeValue &&
					tile != null &&
					tile.drainEnergy(Units(1))
				){
					chargeEnergyUnit(stack)
					// TODO particles
				}
				else{
					removeClusterTags(this)
				}
			}
		}
	}
	
	// Client visuals
	
	override fun showDurabilityBar(stack: ItemStack): Boolean = true
	
	override fun getDurabilityForDisplay(stack: ItemStack): Double = 1.0 - (getEnergyLevel(stack).toDouble() / internalEnergyCapacity)
	
	override fun getRGBDurabilityForDisplay(stack: ItemStack): Int{
		val level = 1F - getDurabilityForDisplay(stack).pow(2.0).toFloat()
		return MathHelper.hsvToRGB(max(0F, 0.62F + 0.15F * level), 0.45F + 0.15F * level, 0.8F + 0.2F * level)
	}
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		
		if (flags.isAdvanced){
			lines.add(I18n.translateToLocalFormatted("item.tooltip.hee.energy.level", getEnergyLevel(stack), internalEnergyCapacity))
		}
		
		lines.add(I18n.translateToLocalFormatted("item.tooltip.hee.energy.uses", (getEnergyLevel(stack).toDouble() / energyPerUse.numerator).ceilToInt()))
	}
}
