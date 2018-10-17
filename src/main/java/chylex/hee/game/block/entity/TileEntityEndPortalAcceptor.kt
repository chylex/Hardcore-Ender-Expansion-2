package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.CHARGING
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.FINISHED
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.IDLE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ChargeState.WAITING
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ForegroundRenderState.ANIMATING
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ForegroundRenderState.INVISIBLE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor.ForegroundRenderState.VISIBLE
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.render.util.LerpedFloat
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setEnum
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import kotlin.math.max
import kotlin.math.min

class TileEntityEndPortalAcceptor : TileEntityBase(), ITickable{
	private companion object{
		private const val NO_REFRESH = Int.MAX_VALUE
		
		private const val WAITING_REFRESH_RATE = 20
		private const val CHARGING_REFRESH_RATE = 2
		
		private val ENERGY_REQUIRED = Units(100)
		private val ENERGY_PER_UPDATE = Units(1)
		
		private val ANIMATION_PROGRESS_PER_UPDATE = 1F / (CHARGING_REFRESH_RATE.toFloat() * ENERGY_REQUIRED.units.value / ENERGY_PER_UPDATE.units.value)
	}
	
	// Client animation
	
	private enum class ForegroundRenderState{
		INVISIBLE,
		ANIMATING,
		VISIBLE
	}
	
	private var foregroundRenderState = INVISIBLE
	val foregroundRenderProgress = LerpedFloat(0F)
	
	private fun updateAnimation(){
		if (foregroundRenderState == INVISIBLE){
			foregroundRenderProgress.update(max(0F, foregroundRenderProgress - ANIMATION_PROGRESS_PER_UPDATE * 8F))
		}
		else if (foregroundRenderState == ANIMATING){
			foregroundRenderProgress.update(min(1F, foregroundRenderProgress + ANIMATION_PROGRESS_PER_UPDATE))
		}
	}
	
	// Charge handling
	
	private enum class ChargeState(val refreshRate: Int){
		IDLE(NO_REFRESH),
		WAITING(WAITING_REFRESH_RATE),
		CHARGING(CHARGING_REFRESH_RATE),
		FINISHED(NO_REFRESH)
	}
	
	val isCharged
		get() = chargedEnergy >= ENERGY_REQUIRED
	
	private var chargeState = IDLE
	private var ticksToRefresh = NO_REFRESH
	private var isRefreshing = false
	
	private var chargedEnergy: IEnergyQuantity = Units(0)
	
	fun refreshClusterState(){
		if (world.isRemote || isRefreshing){
			return
		}
		
		isRefreshing = true // required to ignore a neighborChanged call when breaking the Cluster
		
		val newChargeState: ChargeState
		val cluster = pos.up().getTile<TileEntityEnergyCluster>(world)
		
		if (cluster == null){
			newChargeState = when(chargeState){
				FINISHED -> FINISHED
				IDLE     -> IDLE
				WAITING  -> IDLE
				
				CHARGING -> {
					// TODO leak energy
					chargedEnergy = Units(0)
					IDLE
				}
			}
		}
		else{
			newChargeState = when(chargeState){
				FINISHED -> FINISHED
				IDLE     -> WAITING
				
				WAITING  ->
					if (cluster.energyLevel >= ENERGY_REQUIRED)
						CHARGING
					else
						WAITING
				
				CHARGING ->
					if (cluster.drainEnergy(ENERGY_PER_UPDATE)){
						chargedEnergy += ENERGY_PER_UPDATE
						
						if (chargedEnergy >= ENERGY_REQUIRED){
							cluster.breakWithoutExplosion = true
							cluster.pos.breakBlock(world, false)
							FINISHED
						}
						else{
							markDirty()
							CHARGING
						}
					}
					else{
						cluster.breakWithoutExplosion = true
						cluster.pos.breakBlock(world, false)
						CHARGING
					}
			}
		}
		
		if (chargeState != newChargeState){
			chargeState = newChargeState
			notifyUpdate(FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER)
			markDirty()
		}
		
		isRefreshing = false
		ticksToRefresh = chargeState.refreshRate
	}
	
	// Overrides
	
	override fun firstTick(){
		refreshClusterState()
	}
	
	override fun update(){
		if (world.isRemote){
			updateAnimation()
		}
		else if (ticksToRefresh != NO_REFRESH && --ticksToRefresh <= 0){
			refreshClusterState()
		}
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			setInteger("EnergyCharge", chargedEnergy.units.value)
		}
		else if (context == NETWORK){
			setEnum("RenderState", when(chargeState){
				IDLE, WAITING -> INVISIBLE
				CHARGING      -> ANIMATING
				FINISHED      -> VISIBLE
			})
			
			if (chargeState == CHARGING){
				setFloat("RenderProgress", chargedEnergy.units.value.toFloat() / ENERGY_REQUIRED.units.value)
			}
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			chargedEnergy = Units(getInteger("EnergyCharge"))
			
			if (chargedEnergy >= ENERGY_REQUIRED){
				chargeState = FINISHED
			}
			else if (chargedEnergy > Units(0)){
				chargeState = CHARGING
			}
		}
		else if (context == NETWORK){
			foregroundRenderState = getEnum<ForegroundRenderState>("RenderState") ?: INVISIBLE
			
			foregroundRenderProgress.updateImmediately(when(foregroundRenderState){
				INVISIBLE -> 0F
				ANIMATING -> getFloat("RenderProgress")
				VISIBLE   -> 1F
			})
		}
	}
}
